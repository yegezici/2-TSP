import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TwoTSPNearestNeighbor {

    public static void main(String[] args) {
       
        String inputFile = "test-input-4.txt"; // Input file path
        String outputFile = "test-output-4.txt"; // Output file path
        try {
            List<int[]> cities = readInput(inputFile);
            int startCity1 = 0; // Starting city index for salesman 1
            int startCity2 = 1; // Starting city index for salesman 2

            int[][] paths = nearestNeighborForTwo(cities, startCity1, startCity2);
            int[] path1 = paths[0];
            int[] path2 = paths[1];

            path1 = optimizeWith2Opt(path1, cities);
            path2 = optimizeWith2Opt(path2, cities);

            int cost1 = calculateCost(path1, cities);
            int cost2 = calculateCost(path2, cities);
            int totalCost = cost1 + cost2;

            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(totalCost + "\n");
                writer.write( cost1 + " " + (path1.length - 1) + "\n");
                for(int i = 0 ; i < path1.length - 1 ; i++ )
                 writer.write(path1[i] + "\n");
                writer.write("\n");
                writer.write(cost2 + " " + (path2.length-1) + "\n");
                for(int i = 0 ; i < path2.length - 1; i++ )
                 writer.write(path2[i] + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<int[]> readInput(String inputFile) throws IOException {
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

        return cities;
    }

    public static int[][] nearestNeighborForTwo(List<int[]> cities, int start1, int start2) {
        int numCities = cities.size();
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
                if (!visited[city]) {
                    int distance1 = calculateDistance(cities.get(currentCity1), cities.get(city));
                    int distance2 = calculateDistance(cities.get(currentCity2), cities.get(city));

                    if (distance1 < nearestDistance1) {
                        nearestNeighbor1 = city;
                        nearestDistance1 = distance1;
                    }

                    if (distance2 < nearestDistance2) {
                        nearestNeighbor2 = city;
                        nearestDistance2 = distance2;
                    }
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

    public static int[] optimizeWith2Opt(int[] path, List<int[]> cities) {
        int n = path.length;
        boolean improved = true;

        while (improved) {
            improved = false;

            for (int i = 1; i < n - 2; i++) {
                for (int j = i + 1; j < n - 1; j++) {
                    int delta = calculate2OptGain(path, i, j, cities);
                    if (delta < 0) {
                        path = apply2OptSwap(path, i, j);
                        improved = true;
                    }
                }
            }
        }

        return path;
    }

    public static int calculate2OptGain(int[] path, int i, int j, List<int[]> cities) {
        int n = path.length;
        int[] a = cities.get(path[i - 1]);
        int[] b = cities.get(path[i]);
        int[] c = cities.get(path[j]);
        int[] d = cities.get(path[(j + 1) % n]);

        int currentCost = calculateDistance(a, b) + calculateDistance(c, d);
        int newCost = calculateDistance(a, c) + calculateDistance(b, d);

        return newCost - currentCost;
    }

    public static int[] apply2OptSwap(int[] path, int i, int j) {
        int[] newPath = new int[path.length];
        System.arraycopy(path, 0, newPath, 0, i);

        int dec = 0;
        for (int k = j; k >= i; k--) {
            newPath[i + dec] = path[k];
            dec++;
        }
        System.arraycopy(path, j + 1, newPath, j + 1, path.length - j - 1);
        return newPath;
    }

    public static int calculateCost(int[] path, List<int[]> cities) {
        int cost = 0;
        for (int i = 0; i < path.length - 1; i++) {
            cost += calculateDistance(cities.get(path[i]), cities.get(path[i + 1]));
        }
        return cost;
    }

    public static int calculateDistance(int[] city1, int[] city2) {
        int x1 = city1[0];
        int y1 = city1[1];
        int x2 = city2[0];
        int y2 = city2[1];
        return (int) Math.round(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }
}

       
