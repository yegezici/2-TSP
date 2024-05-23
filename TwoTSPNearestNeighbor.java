import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TwoTSPNearestNeighbor {

    public static void main(String[] args) {
        String inputFile = "example-input-3.txt"; // Input file path
        try {
            int[][] distanceMatrix = readInput(inputFile);
            int startCity1 = 0; // Starting city index for salesman 1
            int startCity2 = 1; // Starting city index for salesman 2

            int[][] paths = nearestNeighborForTwo(distanceMatrix, startCity1, startCity2);
            int[] path1 = paths[0];
            int[] path2 = paths[1];

            int cost1 = calculateCost(path1, distanceMatrix);
            int cost2 = calculateCost(path2, distanceMatrix);

            System.out.println("Path 1: " + Arrays.toString(path1));
            System.err.println("-----------------------------------------------------------");
            System.out.println("Path 2: " + Arrays.toString(path2));
            System.out.println("Total distance for salesman 1: " + cost1);
            System.out.println("Total distance for salesman 2: " + cost2);
            System.out.println("Total distance for both salesmen: " + (cost1 + cost2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[][] readInput(String inputFile) throws IOException {
        List<int[]> cities = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\s+");
            int id = Integer.parseInt(parts[0]);
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            cities.add(new int[]{x, y});
        }
        reader.close();

        int numCities = cities.size();
        int[][] distanceMatrix = new int[numCities][numCities];
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                int[] city1 = cities.get(i);
                int[] city2 = cities.get(j);
                distanceMatrix[i][j] = calculateDistance(city1[0], city1[1], city2[0], city2[1]);
            }
        }
        return distanceMatrix;
    }

    public static int calculateDistance(int x1, int y1, int x2, int y2) {
        return (int) Math.round(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    public static int[][] nearestNeighborForTwo(int[][] graph, int start1, int start2) {
        int numCities = graph.length;
        boolean[] visited = new boolean[numCities];
        int[] path1 = new int[numCities + 1];
        int[] path2 = new int[numCities + 1];
        int pathIndex1 = 0, pathIndex2 = 0;

        int currentCity1 = start1;
        int currentCity2 = start2;
        
        visited[start1] = true;
        visited[start2] = true;
        
        path1[pathIndex1++] = start1;
        path2[pathIndex2++] = start2;

        while (pathIndex1 + pathIndex2 < numCities) {
            int nearestNeighbor1 = -1;
            int nearestNeighbor2 = -1;
            int nearestDistance1 = Integer.MAX_VALUE;
            int nearestDistance2 = Integer.MAX_VALUE;

            for (int city = 0; city < numCities; city++) {
                if (!visited[city] && graph[currentCity1][city] < nearestDistance1) {
                    nearestNeighbor1 = city;
                    nearestDistance1 = graph[currentCity1][city];
                }
                if (!visited[city] && graph[currentCity2][city] < nearestDistance2) {
                    nearestNeighbor2 = city;
                    nearestDistance2 = graph[currentCity2][city];
                }
            }

            if (nearestNeighbor1 != -1 && (nearestNeighbor2 == -1 || nearestDistance1 <= nearestDistance2)) {
                path1[pathIndex1++] = nearestNeighbor1;
                currentCity1 = nearestNeighbor1;
                visited[nearestNeighbor1] = true;
            }
            
            if (nearestNeighbor2 != -1 && (nearestNeighbor1 == -1 || nearestDistance2 < nearestDistance1)) {
                path2[pathIndex2++] = nearestNeighbor2;
                currentCity2 = nearestNeighbor2;
                visited[nearestNeighbor2] = true;
            }
        }

        path1[pathIndex1++] = start1;
        path2[pathIndex2++] = start2;

        return new int[][]{Arrays.copyOf(path1, pathIndex1), Arrays.copyOf(path2, pathIndex2)};
    }

    public static int calculateCost(int[] path, int[][] graph) {
        int cost = 0;
        for (int i = 0; i < path.length - 1; i++) {
            cost += graph[path[i]][path[i + 1]];
        }
        return cost;
    }
}
