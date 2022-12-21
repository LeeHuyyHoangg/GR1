package localsearch.applications.idpcndu_network.model;

import localsearch.applications.idpcndu_network.constraint.IdpcnduConstraint;
import localsearch.applications.idpcndu_network.exceptions.InvalidOptionException;
import localsearch.applications.idpcndu_network.exceptions.NotExistException;
import localsearch.applications.idpcndu_network.dataManager.ClusterManager;
import localsearch.applications.idpcndu_network.dataManager.PathManager;
import localsearch.model.ConstraintSystem;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private final LocalSearchManager localSearchManager = new LocalSearchManager();
    private final ConstraintSystem constraintSystem = new ConstraintSystem(localSearchManager);
    private IdpcnduConstraint idpcnduConstraint;

    private List<Integer> path = new ArrayList<>();
    /**
     * clusterHistory, acceptableViolation, weightSum will be auto-updated corresponding to directionUsed
     */
    private final VarIntLS[] clusterHistory;
    private int weightSum = 0;

    public Route(int startNode, int maxNumberOfEdge, int numberOfCluster) {
        this.path.add(startNode);
        clusterHistory = new VarIntLS[maxNumberOfEdge];
        for (int i = 0; i < maxNumberOfEdge; i++) {
            clusterHistory[i] = new VarIntLS(localSearchManager, -maxNumberOfEdge, numberOfCluster);
            clusterHistory[i].setValue(-i);
        }
        idpcnduConstraint = new IdpcnduConstraint(clusterHistory);
        constraintSystem.post(idpcnduConstraint);
        localSearchManager.close();
    }

    public void add(int node) {
        int currentNode = getCurrentNode();
        if (!PathManager.getInstance().existPath(currentNode, node)) {
            throw new NotExistException("There's no node going from " + currentNode + " to " + node);
        } else {
            int oldCurrentCluster = getCurrentCluster();
            weightSum += PathManager.getInstance().getWeight(currentNode, node);
            path.add(node);

            int newCurrentCluster = ClusterManager.getInstance().getCluster(node);
            clusterHistory[path.size() - 1].setValuePropagate(newCurrentCluster);
        }
    }

    public int removeLastNode() {
        if (path.size() > 1) {
            int oldCurrentCluster = getCurrentCluster();
            int lastNode = getCurrentNode();
            path.remove(path.size() - 1);
            weightSum -= PathManager.getInstance().getWeight(getCurrentNode(), lastNode);

            clusterHistory[path.size()].setValuePropagate(-path.size());
            return lastNode;
        }
        else if (path.size() == 1){
            path.remove(0);
            weightSum -= 0;

            clusterHistory[path.size()].setValuePropagate(-path.size());
            return 0;
        }
        else {
            return -1;
        }
    }

    //region For remove violation Level 2
    public void replaceSubRoute(Route subRoute, int from, int to) {
        if (from < 0 || from > path.size()) {
            throw new InvalidOptionException("Invalid from index");
        }
        if (to < 0 || to > path.size()) {
            throw new InvalidOptionException("Invalid to index");
        }
        if (from >= to) {
            throw new InvalidOptionException("From index must be smaller than to index");
        }

        if (path.get(from) != subRoute.getStartNode()) {
            throw new InvalidOptionException("From nodes don't match ");
        }

        if (path.get(to) != subRoute.getCurrentNode()) {
            throw new InvalidOptionException("To nodes don't match ");
        }

        List<Integer> headRoute = path.subList(0, from);
        List<Integer> tailRoute = path.subList(to + 1, path.size());

        for (int i = 1; i < path.size(); i++) {
            clusterHistory[i].setValuePropagate(-i);
        }
        weightSum = 0;
        path = new ArrayList<>();
        path.addAll(headRoute);
        path.addAll(subRoute.getNodeList());
        path.addAll(tailRoute);

        for (int i = 1; i < path.size(); i++) {
            int nodeCluster = ClusterManager.getInstance().getCluster(path.get(i));
            clusterHistory[i].setValuePropagate(nodeCluster);
            weightSum += PathManager.getInstance().getWeight(path.get(i - 1), path.get(i));
        }
    }
    //endregion

    //region Getters
    public boolean isEmpty() {
        return path.size() == 1;
    }

    public int getStartNode() {
        return path.get(0);
    }

    public int getCurrentNode() {
        return path.get(path.size() - 1);
    }

    public int getNodeAt(int index) {
        if (index > path.size() - 1) {
            return -1;
        }
        return path.get(index);
    }

    public int getIndexOfNode(int node) {
        return path.indexOf(node);
    }

    public List<Integer> getNodeList() {
        return path;
    }

    public int getNumberOfNode() {
        return path.size();
    }

    public int getCurrentCluster() {
        return ClusterManager.getInstance().getCluster(getCurrentNode());
    }

    public List<Integer> getClusterList() {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            if (!result.contains(clusterHistory[i].getValue())) {
                result.add(clusterHistory[i].getValue());
            }
        }
        return result;
    }

    public List<Integer> getClusterListTillIndex(int index) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < index + 1; i++) {
            if (!result.contains(clusterHistory[i].getValue())) {
                result.add(clusterHistory[i].getValue());
            }
        }
        return result;
    }

    public int getWeightSum() {
        return weightSum;
    }

    public int getSubRouteWeight(int from, int to) {
        if (from < 0 || from > path.size()) {
            throw new InvalidOptionException("Invalid from index");
        }
        if (to < 0 || to > path.size()) {
            throw new InvalidOptionException("Invalid to index");
        }
        if (from > to) {
            throw new InvalidOptionException("From index must be smaller than to index");
        }

        int result = 0;
        for (int i = from; i < to; i++) {
            result += PathManager.getInstance().getWeight(path.get(i), path.get(i + 1));
        }
        return result;
    }

    //region For checking Violation
    public boolean causeViolationAtIndex(int index) {
        if (index == 0 || index >= path.size()) {
            return false;
        } else if (index < 0) {
            throw new InvalidOptionException("Replace index can't be negative");
        } else {
            int oldCluster = clusterHistory[index].getValue();
            return idpcnduConstraint.valueCauseViolation(oldCluster);
        }
    }

    public int getRouteViolation() {
        return constraintSystem.violations();
    }
    //endregion
    //endregion

    //region Printers
    public void printRoute() {
        if (path.size() > 1) {
            for (int i = 1; i < path.size(); i++) {
                System.out.println("From " + path.get(i - 1) +
                        " to " + path.get(i) +
                        ", weight " + PathManager.getInstance().getWeight(path.get(i - 1), path.get(i)) +
                        ", cluster " + clusterHistory[i].getValue());
            }
            System.out.println("Total weight : " + weightSum + ", violation: " + getRouteViolation());
        } else {
            System.out.println("Empty route");
        }
    }

    public void printRouteSimple() {
        if (path.size() > 1) {
            for (int node : path) {
                System.out.print(node + " -> ");
            }
            System.out.println();
        } else {
            System.out.println("Empty route");
        }
    }

    public String getRouteSimple(){
        StringBuilder stringBuilder = new StringBuilder();
        if (path.size() > 1) {
            for (int node : path) {
                stringBuilder.append(node + " -> ");
            }
        } else {
            stringBuilder.append("Empty route");
        }
        return stringBuilder.toString();
    }
    //endregion
}
