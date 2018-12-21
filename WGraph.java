import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class WGraph {
    private String FName;
    private int N; // # of Nodes
    private int M; // # of edges
    private HashMap<GNode, HashSet<GEdge>> graph;


    public WGraph(String FName) throws FileNotFoundException {
        this.FName = FName;
        this.graph = new HashMap<>();
        createGraph();
    }

    public WGraph(HashMap<GNode, HashSet<GEdge>> curGraph) {
        this.FName = null;
        this.N = curGraph.size();
        this.graph = curGraph;
    }

    private void createGraph() throws FileNotFoundException {
        File file = new File(FName);
        Scanner sc = new Scanner(file);

        // Get Node number
        if (sc.hasNextLine()) {
            String Ntemp = sc.nextLine();
            N = Integer.valueOf(Ntemp.trim());
        } else {
            throw new IllegalArgumentException("Input format illegal!");
        }

        // Get Edge number
        if (sc.hasNextLine()) {
            String Mtemp = sc.nextLine();
            M = Integer.valueOf(Mtemp.trim());
        } else {
            throw new IllegalArgumentException("Input format illegal!");
        }

        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine());
            // first two
            Integer s1 = Integer.valueOf(st.nextToken());
            Integer s2 = Integer.valueOf(st.nextToken());
            GNode s = new GNode(s1, s2);
            // second two
            Integer t1 = Integer.valueOf(st.nextToken());
            Integer t2 = Integer.valueOf(st.nextToken());

            // weight
            Integer w = new Integer(st.nextToken());
            GNode t = new GNode(t1, t2);

            // add or update the node list
            if (!graph.containsKey(s)) {
                graph.put(s, new HashSet<>());
            }
            graph.get(s).add(new GEdge(t, w));

//            if (!graph.containsKey(t)) {
//                graph.put(t, new HashSet<>());
//            }
//            graph.get(t).add(new GEdge(s,Integer.MAX_VALUE));
        }
    }

    // The pre/post-conditions describes the structure of the
    // input/ouput. The semantics of these structures depend on
    // defintion of the corresponding method.
    // pre:  ux, uy, vx, vy are valid coordinates of vertices u and v
    //       in the graph
    // post: return arraylist contains even number of integers,
    //       for any even i,
    //       i-th and i+1-th integers in the array represent
    //       the x-coordinate and y-coordinate of the i/2-th vertex
    //       in the returned path (path is an ordered sequence of vertices)
    public ArrayList<Integer> V2V (int ux, int uy, int vx, int vy) {
        GNode s = new GNode(ux, uy);
        GNode t = new GNode(vx, vy);

        if (!(graph.containsKey(s) && graph.containsKey(t))) {
            return new ArrayList<>();
        }
        Wrap dijkstra = dijkstra(s);
        HashMap<GNode, GNode> prev = dijkstra.getPrev();
        return getPath(prev, s, t);
    }

    // pre:  ux, uy are valid coordinates of vertex u from the graph
    //       S represents a set of vertices.
    //       The S arraylist contains even number of intergers
    //       for any even i,
    //       i-th and i+1-th integers in the array represent
    //       the x-coordinate and y-coordinate of the i/2-th vertex
    //       in the set S.
    // post: same structure as the last method’s post.
    public ArrayList<Integer> V2S (int ux, int uy, ArrayList<Integer> S) {
        GNode s = new GNode(ux, uy);
        if (!graph.containsKey(s)) {
            return new ArrayList<>();
        }
        ArrayList<GNode> nodes = intToNode(S);
        Wrap dijkstra = dijkstra(s);
        WLMap weightAndPath = smallestWeightAndPath(nodes, s, dijkstra);

        return weightAndPath.getPath();
    }

    // pre:  S1 and S2 represent sets of vertices (see above for
    //       the representation of a set of vertices as arrayList)
    // post: same structure as the last method’s post.
    public ArrayList<Integer> S2S(ArrayList<Integer> S1, ArrayList<Integer> S2) {
        ArrayList<GNode> Snodes = intToNode(S1);
        ArrayList<GNode> Tnodes = intToNode(S2);
        PriorityQueue<WLMap> pathSet = new PriorityQueue<>();

        for (GNode s : Snodes) {
            Wrap dijkstra = dijkstra(s);
            WLMap map = smallestWeightAndPath(Tnodes, s, dijkstra);
            pathSet.add(map);
        }
        if (pathSet.isEmpty()) {
            return new ArrayList<>();
        }
        return pathSet.poll().getPath();
    }



    private Wrap dijkstra (GNode s) {
        HashMap<GNode, Integer> dist = new HashMap<>();
        HashMap<GNode, GNode> prev = new HashMap<>();

        for (GNode node : graph.keySet()) {
            prev.put(node, null);
        }

        // Priority Q
        PriorityQueue<GEdge> priorityQueue = new PriorityQueue<>();

        // Initialization
        for (GNode node : graph.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
        }

        // Set the first node
        dist.put(s, 0);
        // push to the pq
        for (GNode node : graph.keySet()) {
            priorityQueue.add(new GEdge(node, dist.get(node)));
        }

        while (!priorityQueue.isEmpty()) {
            GEdge uDist = priorityQueue.poll();
            GNode u = uDist.getNode();
            if (dist.get(u) == Integer.MAX_VALUE) {
                break; // No connected nodes found
            }
            HashSet<GEdge> uList = graph.get(u);
            for (GEdge vEdge : uList) {
                GNode vNode = vEdge.getNode();
                if (graph.containsKey(vNode)) {
                    int alt = dist.get(u) + vEdge.getVal();
                    if (alt < dist.get(vNode)) {
                        dist.put(vNode, alt);
                        prev.put(vNode, u);
                        decreasePrio(priorityQueue, vNode, alt);
                    }
                }
            }
        }
        Wrap wrap = new Wrap(dist, prev);
        return wrap;
    }

    private void decreasePrio(PriorityQueue<GEdge> priorityQueue, GNode node, int alt) {
        for (GEdge temp : priorityQueue) {
            if (temp.getNode().equals(node)) {
                priorityQueue.remove(temp);
                break;
            }
        }
        GEdge update = new GEdge(node, alt);
        priorityQueue.add(update);
    }

    private ArrayList<Integer> getPath (HashMap<GNode, GNode> prev, GNode s, GNode t) {
        Stack<GNode> stack = new Stack<>();
        GNode cur = t;
        stack.push(t);
        while (prev.get(cur) != null && !prev.get(cur).equals(s)) {
            cur = prev.get(cur);
            stack.push(cur);
        }
        stack.push(s);
        ArrayList<Integer> result = new ArrayList<>();
        while(!stack.isEmpty()) {
            GNode temp = stack.pop();
            result.add(temp.x);
            result.add(temp.y);
        }
        return result;
    }

    private ArrayList<GNode> intToNode (ArrayList<Integer> list) {
        if (list.size() % 2 != 0) {throw new IllegalArgumentException();}
        ArrayList<GNode> nodes = new ArrayList<>();
        for (int i = 0; i < list.size(); i=i+2) {
            GNode cur = new GNode(list.get(i), list.get(i+1));
            if (graph.containsKey(cur)) {
                nodes.add(cur);
            }
        }
        return nodes;
    }

    private WLMap smallestWeightAndPath (ArrayList<GNode> Tnodes, GNode s, Wrap dijkstra) {
        HashMap<GNode, GNode> prev = dijkstra.getPrev();
        HashMap<GNode, Integer> dist = dijkstra.getDist();

        // If the source is in the terminal set, return this node as path
        for (GNode node : Tnodes) {
            if (prev.get(node) == null) {
                ArrayList<Integer> single = new ArrayList<>();
                single.add(node.x);
                single.add(node.y);
                return new WLMap(0, single);
            }
        }

        int leastW = Integer.MAX_VALUE;
        GNode leastT = null;
        for (GNode node : Tnodes) {
            if (dist.get(node) < leastW) {
                leastW = dist.get(node);
                leastT = node;
            }
        }

        ArrayList<Integer> path = getPath(prev, s, leastT);
        WLMap weightAndPath = new WLMap(leastW, path);
        return weightAndPath;
    }

    protected class WLMap implements Comparable {
        Integer weight;
        ArrayList<Integer> path;
        public WLMap (int weight, ArrayList<Integer> path) {
            this.weight = weight;
            this.path = path;
        }

        public Integer getWeight () {
            return weight;
        }

        public ArrayList<Integer> getPath () {
            return path;
        }

        @Override
        public int compareTo(Object o) {
            WLMap map = null;
            if (o.getClass().equals(this.getClass())) {
                map = (WLMap) o;
            }

            if (this.weight > map.weight) {
                return 1;
            } else if (this.weight< map.weight) {
                return -1;
            }
            return 0;
        }
    }

    protected class Wrap {
        HashMap<GNode, Integer> dist;
        HashMap<GNode, GNode> prev;
        public Wrap (HashMap<GNode, Integer> dist, HashMap<GNode, GNode> prev) {
            this.dist = dist;
            this.prev = prev;
        }

        public HashMap<GNode, Integer> getDist () {
            return dist;
        }

        public HashMap<GNode, GNode> getPrev () {
            return prev;
        }
    }
}
