package localsearch.applications.idpcndu_network;

import localsearch.applications.idpcndu_network.dataManager.ClusterManager;
import localsearch.applications.idpcndu_network.dataManager.PathManager;
import localsearch.applications.idpcndu_network.model.Route;
import localsearch.applications.idpcndu_network.util.Timer;
import localsearch.applications.idpcndu_network.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IDPCNDU {
    private int fromNode;
    private int toNode;

    public IDPCNDU(String inputFile){
        input(inputFile);
    }
    private void input(String filePath) {
        ClusterManager.clear();
        PathManager.clear();
        try {
            List<String> stringList = Utils.fileToStringArray(filePath);
            //scan line 0 for numberOfNode and numberOfCLuster
            List<Integer> integerList = Utils.stringToIntList(stringList.get(0));
            ClusterManager.getInstance().numberOfCluster = integerList.get(1);

            //scan line 1 for fromNode and toNode
            integerList = Utils.stringToIntList(stringList.get(1));
            fromNode = integerList.get(0);
            toNode = integerList.get(1);

            //scan next numberOfCLuster lines for nodes and their clusters
            for (int i = 2; i < ClusterManager.getInstance().numberOfCluster + 2; i++) {
                List<Integer> intList = Utils.stringToIntList(stringList.get(i));
                for (Integer integer : intList) {
                    ClusterManager.getInstance().addNode(integer, i - 2);
                }
            }

            //the rest are edges
            for (int i = ClusterManager.getInstance().numberOfCluster + 2; i < stringList.size(); i++) {
                List<Integer> intList = Utils.stringToIntList(stringList.get(i));
                PathManager.getInstance().addDirection(intList.get(0), intList.get(1), intList.get(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Route findSimplePath(int maxNumberOfEdge, int randomSeed) {

        Route result = new Route(fromNode, maxNumberOfEdge, ClusterManager.getInstance().numberOfCluster);

        List<Integer> currentNodeForbiddenNode = new ArrayList<>();
        currentNodeForbiddenNode.add(fromNode);
        while (true) {
//            result.printRouteSimple();
            List<Integer> possibleNodes = PathManager.getInstance().getPossibleDirectionFromWithoutNodes(result.getCurrentNode(), currentNodeForbiddenNode);
            if (possibleNodes == null || possibleNodes.size() == 0) {
                /*
                if currentNode can't go anywhere -> lastSelectingEdge is a mistake
                -> should undo select the lastSelectEdge
                */
                int lastNode = result.removeLastNode();
                if (lastNode == 0) {
                    //can't go back, node 0 can't go anywhere
                    return null;
                }
                currentNodeForbiddenNode.add(lastNode);
            } else {
                //check if any Edge can go to the destination
                for (Integer node : possibleNodes) {
                    if (node == toNode) {
                        //if yes, end process
                        result.add(node);
                        return result;
                    }
                }

                //if no, select 1 random edge to continue
                int selectedNode = Utils.getRandomListItem(possibleNodes, randomSeed);

                result.add(selectedNode);
                currentNodeForbiddenNode.add(selectedNode);
            }
        }

    }

    private static Route findPathWithoutCluster(int fromNode, List<Integer> possibleTo, int maxNumberOfEdge, List<Integer> forbiddenClusters) {

        Route result = new Route(fromNode, maxNumberOfEdge, ClusterManager.getInstance().numberOfCluster);

        List<Integer> forbiddenNodeList = new ArrayList<>();
        forbiddenNodeList.add(fromNode);
        while (true) {
            List<Integer> possibleNodes = PathManager.getInstance().getPossibleDirectionFromWithoutNodesAndClusters(result.getCurrentNode(), forbiddenNodeList, forbiddenClusters);

            if (possibleNodes == null || possibleNodes.size() == 0) {
                /*
                if currentNode can't go anywhere -> lastSelectingEdge is a mistake
                -> should undo select the lastSelectEdge
                */
                int lastNode = result.removeLastNode();
                if (lastNode == 0) {
                    //can't go back, node 0 can't go anywhere
                    return null;
                }
                forbiddenNodeList.add(lastNode);
            } else {
                //check if any Edge can go to the destination
                for (Integer node : possibleNodes) {
                    if (possibleTo.contains(node)) {
                        //if yes, end process
                        result.add(node);
                        return result;
                    }
                }

                //if no, select 1 edge with the smallest weight to continue
                int currentNode = result.getCurrentNode();

                int selectedNode = possibleNodes.get(possibleNodes.size() - 1);
                int selectedPathWeight = PathManager.getInstance().getWeight(currentNode, selectedNode);
                for (int i : possibleNodes) {
                    if (selectedPathWeight > PathManager.getInstance().getWeight(currentNode, i)) {
                        selectedNode = i;
                        selectedPathWeight = PathManager.getInstance().getWeight(currentNode, selectedNode);
                    }
                }
                result.add(selectedNode);
                forbiddenNodeList.add(selectedNode);
            }
        }
    }

    private static Route findOptimalWeightPath(Route route, int fromIndex, int maxNumberOfEdge) {
        List<Integer> possibleTo = route.getNodeList().subList(fromIndex + 1, route.getNodeList().size());

        Route result = new Route(route.getNodeAt(fromIndex), maxNumberOfEdge, ClusterManager.getInstance().numberOfCluster);

        Stack<List<Integer>> forbiddenNodeListStack = new Stack<>();

        List<Integer> usedNode = route.getNodeList().subList(0, fromIndex +1);
        List<Integer> forbiddenNodeList = new ArrayList<>(usedNode);
        forbiddenNodeListStack.push(new ArrayList<>());
        while (true) {
            List<Integer> possibleNodes = PathManager.getInstance().getPossibleDirectionFromWithoutNodes(result.getCurrentNode(), forbiddenNodeList);
            if (possibleNodes != null) {
                //check if any can go to destination
                for (Integer node : possibleNodes.toArray(new Integer[0])) {
                    if (possibleTo.contains(node)) {
                        //if yes, check weight
                        result.add(node);
                        if (result.getWeightSum() >= route.getSubRouteWeight(fromIndex, route.getIndexOfNode(node))) {
                            result.removeLastNode();
                            possibleNodes.remove(node);
                        } else {
                            return result;
                        }
                    }
                }
            }
            if (possibleNodes == null || possibleNodes.size() == 0) {
                /*
                if currentNode can't go anywhere -> lastSelectingEdge is a mistake
                -> should undo select the lastSelectEdge
                */
                int lastNode = result.removeLastNode();
                if (lastNode == 0) {
                    //can't go back, node 0 can't go anywhere
                    return null;
                }
                forbiddenNodeList = forbiddenNodeListStack.pop();
                forbiddenNodeList.add(lastNode);
            } else {

                // select 1 edge with the smallest weight to continue
                int currentNode = result.getCurrentNode();

                int selectedNode = possibleNodes.get(possibleNodes.size() - 1);
                int selectedPathWeight = PathManager.getInstance().getWeight(currentNode, selectedNode);
                for (int i : possibleNodes) {
                    if (selectedPathWeight > PathManager.getInstance().getWeight(currentNode, i)) {
                        selectedNode = i;
                        selectedPathWeight = PathManager.getInstance().getWeight(currentNode, selectedNode);
                    }
                }
                result.add(selectedNode);
                if (result.getWeightSum() >= route.getSubRouteWeight(fromIndex, route.getNumberOfNode() - 1)) {
                    result.removeLastNode();
                    int lastNode = result.removeLastNode();
                    if (lastNode == 0) {
                        //can't go back, node 0 can't go anywhere
                        return null;
                    }
                    forbiddenNodeList = forbiddenNodeListStack.pop();
                    forbiddenNodeList.add(lastNode);
                } else {
                    forbiddenNodeList.add(selectedNode);
                    forbiddenNodeListStack.push(forbiddenNodeList);
                    forbiddenNodeList = new ArrayList<>(result.getNodeList());
                    forbiddenNodeList.addAll(usedNode);
                }


            }
        }
    }

    public Route removeViolations(Route route) {
        if (route.getRouteViolation() == 0) {
            return route;
        }

        for (int i = 1; i < route.getNumberOfNode() - 1; i++) {
            if (route.causeViolationAtIndex(i)) {
                List<Integer> nextNodes = route.getNodeList().subList(i + 1, route.getNodeList().size());
                List<Integer> usedCluster = route.getClusterListTillIndex(i);

                for (int j = i - 1; j > 0; j--) {
                    Route subRoute;
                    int maxNumberOfEdge = 50;
                    while(true) {
                        try {
                            subRoute = findPathWithoutCluster(route.getNodeAt(j), nextNodes, maxNumberOfEdge, usedCluster);
                            break;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            maxNumberOfEdge += 50;
                        }
                    }
                    if (subRoute != null && !subRoute.isEmpty()) {
                        route.replaceSubRoute(subRoute, j, route.getIndexOfNode(subRoute.getCurrentNode()));
                        i = j+1;
                        break;
                    } else {
                        usedCluster.remove(Integer.valueOf(ClusterManager.getInstance().getCluster(route.getNodeAt(j))));
                    }

                }
            }
        }
        return route;
    }

    public boolean reduceWeight(Route route) {
        boolean didSomething = false;
        for (int i = 0; i < route.getNumberOfNode() - 1; i++) {
            Route subRoute;
            try {
                subRoute = findOptimalWeightPath(route, i, 50);
            } catch (ArrayIndexOutOfBoundsException e) {
                subRoute = findOptimalWeightPath(route, i, 100);
            }
            if (subRoute != null && !subRoute.isEmpty()) {
                route.replaceSubRoute(subRoute, i, route.getIndexOfNode(subRoute.getCurrentNode()));
                didSomething = true;
            }
        }

        return didSomething;
    }

    public static void main(String[] args) throws IOException {
        File inputFolder = new File("data\\Data IDPCDU Nodes\\");
        for (String fileName : inputFolder.list()) {
            String inputFile = "data\\Data IDPCDU Nodes\\" + fileName;
            String outputFile = "output\\" + fileName;
            File outputFileLocation = new File(outputFile);
            if(outputFileLocation.exists()){
                continue;
            }
            int numberOfRun = 30;
            List<Integer> resultWeight = new ArrayList<>();
            List<Integer> resultViolation = new ArrayList<>();
            List<Long> resultRuntime = new ArrayList<>();

            for (int runCounter = 0; runCounter < numberOfRun; runCounter++) {
                Utils.appendLineToFile("Runtime:" + runCounter, outputFile);
                Timer timer = new Timer();
                IDPCNDU idpcndu = new IDPCNDU(inputFile);

//            System.out.println("Input takes :" + timer.getTimeInterval() + " milliseconds");

                Route route;
                int maxNumberOfEdge = 100;
                while (true) {
                    try {
                        route = idpcndu.findSimplePath(maxNumberOfEdge, runCounter);
                        break;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        maxNumberOfEdge += 100;
                    }
                }
//            System.out.println("Calculate straight forward route takes :" + timer.getTimeInterval() + " milliseconds");
//            route.printRoute();
                Utils.appendLineToFile("Begin route: " + route.getRouteSimple(), outputFile);
                Utils.appendLineToFile("Violation: " + route.getRouteViolation(), outputFile);
                Utils.appendLineToFile("Weight: " + route.getWeightSum(), outputFile);

                for (int i = 0; i < 100; i++) {
                    if (!idpcndu.reduceWeight(route)) {
                        break;
                    }
//                System.out.println("Reduce weight round " + i);
//                route.printRouteSimple();
//                System.out.println("Current Weight " + route.getWeightSum());
                }
//            System.out.println("Reduce weight takes:" + timer.getTimeInterval() + " milliseconds");
//            route.printRoute();

                idpcndu.removeViolations(route);
//            System.out.println("Reduce violation takes:" + timer.getTimeInterval() + " milliseconds");
//            route1.printRoute();

                long totalTime = timer.getTotalTime();
                resultRuntime.add(totalTime);
                resultWeight.add(route.getWeightSum());
                resultViolation.add(route.getRouteViolation());

                Utils.appendLineToFile("Result route: " + route.getRouteSimple(), outputFile);
                Utils.appendLineToFile("Violation: " + route.getRouteViolation(), outputFile);
                Utils.appendLineToFile("Weight: " + route.getWeightSum(), outputFile);
                Utils.appendLineToFile("Total Time: " + totalTime, outputFile);
                Utils.appendLineToFile("", outputFile);
                System.out.println("Total time: " + timer.getTotalTime() + " milliseconds");
            }

            Utils.appendLineToFile("Violation average: " + Utils.getAverageInt(resultViolation), outputFile);
            Utils.appendLineToFile("Violation StandardDeviation: " + Utils.getStandardDeviationInt(resultViolation), outputFile);
            Utils.appendLineToFile("Best violation: " + Collections.min(resultViolation), outputFile);
            Utils.appendLineToFile("Worst violation: " + Collections.max(resultViolation), outputFile);

            Utils.appendLineToFile("Weight average: " + Utils.getAverageInt(resultWeight), outputFile);
            Utils.appendLineToFile("Weight StandardDeviation: " + Utils.getStandardDeviationInt(resultWeight), outputFile);
            Utils.appendLineToFile("Best weight: " + Collections.min(resultWeight), outputFile);
            Utils.appendLineToFile("Worst weight: " + Collections.max(resultWeight), outputFile);

            Utils.appendLineToFile("Runtime average: " + Utils.getAverageLong(resultRuntime), outputFile);
            Utils.appendLineToFile("Runtime StandardDeviation: " + Utils.getStandardDeviationLong(resultRuntime), outputFile);
            Utils.appendLineToFile("Best runtime: " + Collections.min(resultRuntime), outputFile);
            Utils.appendLineToFile("Worst runtime: " + Collections.max(resultRuntime), outputFile);

            System.out.println("Processing done on file :" + fileName);
        }
    }
}
