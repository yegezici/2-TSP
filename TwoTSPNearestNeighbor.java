import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TwoTSPNearestNeighbor {

    public static void main(String[] args) {
        String inputFile = "input.txt"; // Input file path
        try {
            int[][] distanceMatrix = readInput(inputFile);
            int startCity = 0; // Starting city index
            int[] path = nearestNeighbor(distanceMatrix, startCity);
            int cost = calculateCost(path, distanceMatrix);

            System.out.println("Path: " + Arrays.toString(path));
            System.out.println("Cost: " + cost);
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

    public static int[] nearestNeighbor(int[][] graph, int start) {
        int numCities = graph.length;
        boolean[] visited = new boolean[numCities];
        int[] path = new int[2 * numCities];
        int currentCity = start;
        int pathIndex = 0;

        visited[start] = true;
        path[pathIndex++] = start;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < numCities / 2; j++) {
                int nearestNeighbor = -1;
                int nearestDistance = Integer.MAX_VALUE;

                for (int city = 0; city < numCities; city++) {
                    if (!visited[city] && graph[currentCity][city] < nearestDistance) {
                        nearestNeighbor = city;
                        nearestDistance = graph[currentCity][city];
                    }
                }

                path[pathIndex++] = nearestNeighbor;
                currentCity = nearestNeighbor;
                visited[nearestNeighbor] = true;
            }
        }

        path[pathIndex++] = start;

        return path;
    }

    public static int calculateCost(int[] path, int[][] graph) {
        int cost = 0;
        for (int i = 0; i < path.length - 1; i++) {
            cost += graph[path[i]][path[i + 1]];
        }
        return cost;
    }
}
