import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class TwoSalesmenTSP {

    private final double[][] graph;
    private final int V;

    public TwoSalesmenTSP(double[][] graph) {
        this.graph = graph;
        this.V = graph.length;
    }

    public List<int[]> findTSPPaths() {
        ChristofidesAlgorithm christofides = new ChristofidesAlgorithm(graph);
        List<Integer> tspPath = christofides.findTSPPath();

        // Split the TSP path into two subtours
        List<int[]> paths = splitPath(tspPath);

        // Optimize the paths using 2-opt algorithm
        paths.set(0, optimizePath(paths.get(0)));
        paths.set(1, optimizePath(paths.get(1)));

        return paths;
    }

    private List<int[]> splitPath(List<Integer> tspPath) {
        int halfSize = tspPath.size() / 2;
        int[] path1 = new int[halfSize];
        int[] path2 = new int[tspPath.size() - halfSize];

        path1[0] = tspPath.get(0);  // Ensure starting city (0) is visited by salesman 1
        int idx1 = 1, idx2 = 0;

        for (int i = 1; i < tspPath.size(); i++) {
            if (i % 2 == 0 && idx1 < halfSize) {
                path1[idx1++] = tspPath.get(i);
            } else {
                path2[idx2++] = tspPath.get(i);
            }
        }

        // Remove city 0 from path2 if it is there
        path2 = Arrays.stream(path2).filter(city -> city != 0).toArray();

        return Arrays.asList(path1, path2);
    }

    private double calculatePathDistance(int[] path) {
        double totalDistance = 0.0;
        for (int i = 0; i < path.length - 1; i++) {
            totalDistance += graph[path[i]][path[i + 1]];
        }
        totalDistance += graph[path[path.length - 1]][path[0]]; // Returning to the start point
        return totalDistance;
    }

    private int[] optimizePath(int[] path) {
        boolean improvement = true;
        while (improvement) {
            improvement = false;
            for (int i = 1; i < path.length - 2; i++) {
                for (int j = i + 1; j < path.length - 1; j++) {
                    if (j - i == 1) continue;  // Skip adjacent nodes
                    double delta = -graph[path[i]][path[i + 1]] - graph[path[j]][path[j + 1]]
                            + graph[path[i]][path[j]] + graph[path[i + 1]][path[j + 1]];
                    if (delta < 0) {
                        reverseSegment(path, i + 1, j);
                        improvement = true;
                    }
                }
            }
        }
        return path;
    }

    private void reverseSegment(int[] path, int start, int end) {
        while (start < end) {
            int temp = path[start];
            path[start] = path[end];
            path[end] = temp;
            start++;
            end--;
        }
    }

    private static class ChristofidesAlgorithm {

        private final double[][] graph;
        private final int V;

        public ChristofidesAlgorithm(double[][] graph) {
            this.graph = graph;
            this.V = graph.length;
        }

        public List<Integer> findTSPPath() {
            List<Edge> mstEdges = findMST();
            Set<Integer> oddDegreeVertices = findOddDegreeVertices(mstEdges);
            List<Edge> minWeightMatching = findMinWeightMatching(oddDegreeVertices);
            List<Edge> eulerianCircuitEdges = new ArrayList<>(mstEdges);
            eulerianCircuitEdges.addAll(minWeightMatching);
            List<Integer> eulerianCircuit = findEulerianCircuit(eulerianCircuitEdges);
            return createHamiltonianCircuit(eulerianCircuit);
        }

        private List<Edge> findMST() {
            boolean[] inMST = new boolean[V];
            double[] key = new double[V];
            int[] parent = new int[V];
            PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.weight));

            Arrays.fill(key, Double.MAX_VALUE);
            key[0] = 0;
            pq.add(new Edge(-1, 0, 0));  // Start with the first node

            List<Edge> mstEdges = new ArrayList<>();
            while (!pq.isEmpty()) {
                Edge edge = pq.poll();
                int u = edge.to;
                if (inMST[u]) continue;
                inMST[u] = true;
                if (edge.from != -1) mstEdges.add(edge);

                for (int v = 0; v < V; v++) {
                    if (graph[u][v] != 0 && !inMST[v] && graph[u][v] < key[v]) {
                        parent[v] = u;
                        key[v] = graph[u][v];
                        pq.add(new Edge(u, v, graph[u][v]));
                    }
                }
            }
            return mstEdges;
        }

        private Set<Integer> findOddDegreeVertices(List<Edge> mstEdges) {
            int[] degree = new int[V];
            for (Edge edge : mstEdges) {
                degree[edge.from]++;
                degree[edge.to]++;
            }

            Set<Integer> oddDegreeVertices = new HashSet<>();
            for (int i = 0; i < V; i++) {
                if (degree[i] % 2 != 0) {
                    oddDegreeVertices.add(i);
                }
            }
            return oddDegreeVertices;
        }

        private List<Edge> findMinWeightMatching(Set<Integer> oddDegreeVertices) {
            List<Integer> vertices = new ArrayList<>(oddDegreeVertices);
            List<Edge> matching = new ArrayList<>();

            while (!vertices.isEmpty()) {
                int v = vertices.get(0);
                int closest = -1;
                double minWeight = Double.MAX_VALUE;

                for (int u : vertices) {
                    if (v != u && graph[v][u] < minWeight) {
                        minWeight = graph[v][u];
                        closest = u;
                    }
                }

                matching.add(new Edge(v, closest, graph[v][closest]));
                vertices.remove(Integer.valueOf(v));
                vertices.remove(Integer.valueOf(closest));
            }

            return matching;
        }

        private List<Integer> findEulerianCircuit(List<Edge> eulerianCircuitEdges) {
            Map<Integer, List<Integer>> adj = new HashMap<>();
            for (Edge edge : eulerianCircuitEdges) {
                adj.computeIfAbsent(edge.from, k -> new ArrayList<>()).add(edge.to);
                adj.computeIfAbsent(edge.to, k -> new ArrayList<>()).add(edge.from);
            }

            List<Integer> circuit = new ArrayList<>();
            Stack<Integer> currPath = new Stack<>();
            currPath.push(0);
            int currVertex = 0;

            while (!currPath.isEmpty()) {
                if (adj.get(currVertex) != null && !adj.get(currVertex).isEmpty()) {
                    currPath.push(currVertex);
                    int nextVertex = adj.get(currVertex).remove(0);
                    adj.get(nextVertex).remove(Integer.valueOf(currVertex));
                    currVertex = nextVertex;
                } else {
                    circuit.add(currVertex);
                    currVertex = currPath.pop();
                }
            }

            return circuit;
        }

        private List<Integer> createHamiltonianCircuit(List<Integer> eulerianCircuit) {
            List<Integer> hamiltonianCircuit = new ArrayList<>();
            Set<Integer> visited = new HashSet<>();
            for (int vertex : eulerianCircuit) {
                if (visited.add(vertex)) {
                    hamiltonianCircuit.add(vertex);
                }
            }
            hamiltonianCircuit.add(hamiltonianCircuit.get(0)); // Return to the starting point
            return hamiltonianCircuit;
        }

        private static class Edge {
            int from;
            int to;
            double weight;

            Edge(int from, int to, double weight) {
                this.from = from;
                this.to = to;
                this.weight = weight;
            }
        }
    }

    public static void main(String[] args) {
       
        Scanner scan = new Scanner(System.in);
        String filename = "example-input-3.txt";
        List<City> cities = new ArrayList<>();

        try (Scanner fileScanner = new Scanner(new File(filename))) {
            while (fileScanner.hasNextLine()) {
                String[] parts = fileScanner.nextLine().split(" ");
                int id = Integer.parseInt(parts[0]);
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                cities.add(new City(id, x, y));
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
            return;
        }

        

        int V = cities.size();
        double[][] graph = new double[V][V];

        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                if (i != j) {
                    graph[i][j] = euclideanDistance(cities.get(i), cities.get(j));
                } else {
                    graph[i][j] = 0;
                }
            }
        }

        TwoSalesmenTSP tsp = new TwoSalesmenTSP(graph);
        List<int[]> paths = tsp.findTSPPaths();

        double distance1 = tsp.calculatePathDistance(paths.get(0));
        double distance2 = tsp.calculatePathDistance(paths.get(1));

        //System.out.println("Salesman 1 Path: " + Arrays.toString(paths.get(0)));
        System.out.println("Salesman 1 Distance: " + distance1);

        //System.out.println("Salesman 2 Path: " + Arrays.toString(paths.get(1)));
        System.out.println("Salesman 2 Distance: " + distance2);
        System.out.println("TOTAL DISTANCE: " + (distance1 + distance2));
    
    
    }


    private static double euclideanDistance(City city1, City city2) {
        return Math.sqrt(Math.pow(city1.x - city2.x, 2) + Math.pow(city1.y - city2.y, 2));
    }

    private static class City {
        int id;
        double x;
        double y;

        City(int id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }
}
