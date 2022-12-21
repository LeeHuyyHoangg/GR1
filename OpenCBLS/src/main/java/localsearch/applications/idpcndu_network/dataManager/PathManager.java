package localsearch.applications.idpcndu_network.dataManager;

import localsearch.applications.idpcndu_network.exceptions.NotExistException;

import java.util.*;

public class PathManager {
    private static PathManager instance;

    public static PathManager getInstance() {
        if (instance == null) {
            instance = new PathManager();
        }
        return instance;
    }

    public static void clear(){
        instance = null;
    }

    private final Map<Integer, Map<Integer,Integer>> fromToTo = new HashMap<>();

    public void addDirection(int from, int to, int weight) {
        if (!fromToTo.containsKey(from)) {
            fromToTo.put(from, new HashMap<>());
        }
        fromToTo.get(Integer.valueOf(from)).put(to,weight);
    }

    public List<Integer> getPossibleDirectionFrom(int from) {
        return fromToTo.containsKey(from)? new ArrayList<>(fromToTo.get(Integer.valueOf(from)).keySet()) : new ArrayList<>();
    }

    public List<Integer> getPossibleDirectionFromWithoutNodes(int from, List<Integer> forbiddenNodes) {
        List<Integer> result = fromToTo.containsKey(from)? new ArrayList<>(fromToTo.get(Integer.valueOf(from)).keySet()) : new ArrayList<>();
        result.removeIf(forbiddenNodes::contains);

        return result;
    }

    public List<Integer> getPossibleDirectionFromWithoutNodesAndClusters(int from, List<Integer> forbiddenNodes, List<Integer> forbiddenClusters) {
        List<Integer> result = fromToTo.containsKey(from)? new ArrayList<>(fromToTo.get(Integer.valueOf(from)).keySet()) : new ArrayList<>();
        result.removeIf(node -> forbiddenNodes.contains(node) || forbiddenClusters.contains(ClusterManager.getInstance().getCluster(node)));

        return result;
    }

//    public List<Integer> getPossibleDirectionFromWithoutClusters(int from, List<Integer> forbiddenClusters){
//        List<Integer> result = fromToTo.get(Integer.valueOf(from));
//        if(result == null){
//            return null;
//        } else {
//            result.removeIf(node -> forbiddenClusters.contains(ClusterManager.getInstance().getCluster(node)));
//
//            return result;
//        }
//    }


    public boolean existPath(int from, int to) {
        if (!fromToTo.containsKey(from)) {
            return false;
        }
        return fromToTo.get(Integer.valueOf(from)).containsKey(to);
    }

    public int getWeight(int from, int to) {
        if (!existPath(from, to)) {
            throw new NotExistException("The path from " + from + " to " + to + " does not exist");
        }
        return fromToTo.get(Integer.valueOf(from)).get(to);
    }
}
