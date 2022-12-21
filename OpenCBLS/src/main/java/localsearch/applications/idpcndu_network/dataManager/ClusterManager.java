package localsearch.applications.idpcndu_network.dataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterManager {
    private static ClusterManager instance;

    public static ClusterManager getInstance() {
        if (instance == null) {
            instance = new ClusterManager();
        }

        return instance;
    }

    public static void clear(){
        instance = null;
    }

    private final Map<Integer, Integer> nodeToCluster = new HashMap<>();
    public int numberOfCluster = 0;

    public void addNode(int id, int cluster) {
        if (cluster >= numberOfCluster) {
            return;
        }
        nodeToCluster.put(id, cluster);
    }

    public int getCluster(int id) {
        return nodeToCluster.getOrDefault(id, -1);
    }

    public List<Integer> nodeListToClusterList(List<Integer> nodeList) {
        List<Integer> result = new ArrayList<>();
        for (int node : nodeList) {
            int cluster = getCluster(node);
            if (!result.contains(cluster)) {
                result.add(cluster);
            }
        }

        return result;
    }
}
