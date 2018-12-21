import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ImageProcessor {
    private String FName;
    private int H; // # of Nodes
    private int W; // # of edges
    private ArrayList<ArrayList<Pixel>> M;
    private ArrayList<ArrayList<Integer>> I;


    public ImageProcessor(String FName) throws FileNotFoundException {
        this.FName = FName;
        createM();
        this.I = updateI(M);
    }

    private void createM() throws FileNotFoundException {
        File file = new File(FName);
        Scanner sc = new Scanner(file);

        // Get H number
        String Ntemp = sc.nextLine();
        H = Integer.valueOf(Ntemp.trim());

        // Get W number
        String Mtemp = sc.nextLine();
        W = Integer.valueOf(Mtemp.trim());

        M = new ArrayList<>();
        for (int i = 0; i < H; i ++) {
            M.add(i, new ArrayList<>());
        }

        int h = 0;
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine());
            for (int w = 0; w < W; w ++) {
                M.get(h).add(new Pixel(st.nextToken(), st.nextToken(), st.nextToken()));
            }
            h ++;
        }
    }

    // pre:
    // post: returns the 2-D matrix I as per its definition
    public ArrayList<ArrayList<Integer>> getImportance() {
        return this.I;
    }

    // pre:  W-k > 1
    // post: Compute the new image matrix after reducing the width by k
    //       Follow the method for reduction described above
    //       Write the result in a file named FName
    //       in the same format as the input image matrix
    public void writeReduced(int k, String FName) throws IOException {
        if (k > W-1) {
            throw new IllegalArgumentException();
        }

        ArrayList<ArrayList<Integer>> I = getImportance();

        ArrayList<ArrayList<Pixel>> newM = M;

        for (int i = 0; i < k; i ++) {
//            HashMap<GNode, HashSet<GEdge>> curGraph = genGraph(I);
//            ArrayList<Integer> minPath = curMinPath(curGraph);
            ArrayList<Integer> minPath = dpMinPath(I);

            // Update M
            newM = updateM(minPath, newM);
//            System.out.println("Finish M");
            // Update I
            if (newM.get(0).size() != 1) {
                I = updateI(newM);
            } else {
                break;
            }
//            System.out.println("Finish I");
        }

        // write out
        FileWriter fileWriter = new FileWriter(FName);
        fileWriter.write(H + "\n");
        fileWriter.write((W-k) + "\n");
        for (int i = 0; i < H; i ++) {
            ArrayList<Pixel> temp = newM.get(i);
            for (Pixel pixel : temp) {
                fileWriter.write(pixel.printPixel() + " ");
            }
            fileWriter.write("\n");
        }
        fileWriter.close();
    }

    private ArrayList<Integer> dpMinPath(ArrayList<ArrayList<Integer>> I) {
        HashMap<GNode, GNode> prev = new HashMap<>();
        int W = I.get(0).size();
        int[][] dp = new int[H][W];
        for (int j = 0; j < W; j++) {
            dp[0][j] = I.get(0).get(j);
            prev.put(new GNode(0,j), null);
        }

        for (int i = 1; i < H; i++) {
            for (int j = 0; j < W; j++) {
                // find the min path dist and the prev
                int x = i-1;
                int y = j-1;
                int lastMin = (y >= 0 ? dp[x][y] : Integer.MAX_VALUE);
                if (lastMin > dp[i-1][j]) {
                    y = j;
                    lastMin = dp[x][y];
                }
                if (j+1 < W && lastMin > dp[x][j+1]) {
                    y = j+1;
                    lastMin = dp[x][j+1];
                }
                dp[i][j] = lastMin + I.get(i).get(j);
                GNode child = new GNode(i, j);
                if (prev.containsKey(child)) {
                    GNode parent = prev.get(child);
                    if (dp[x][y] < dp[parent.x][parent.y]) {
                        prev.put(child, new GNode(x, y));
                    }
                } else {
                    prev.put(child, new GNode(x, y));
                }
            }
        }

        int lasty = 0;
        int min = dp[H-1][0];
        for (int j = 0; j < W; j++) {
            if (dp[H-1][j] < min) {
                min = dp[H-1][j];
                lasty = j;
            }
        }
        GNode lastNode = new GNode(H-1, lasty);

        Stack<GNode> stack = new Stack<>();
        GNode cur = lastNode;
        stack.push(lastNode);
        while (prev.get(cur) != null) {
            cur = prev.get(cur);
            stack.push(cur);
        }

        ArrayList<Integer> minPath = new ArrayList<>();
        while(!stack.isEmpty()) {
            GNode temp = stack.pop();
            minPath.add(temp.x);
            minPath.add(temp.y);
        }
//        System.out.println("Path is " + Arrays.toString(minPath.toArray(new Integer[minPath.size()])));
        return minPath;
    }

    private ArrayList<ArrayList<Integer>> updateI (ArrayList<ArrayList<Pixel>> Matrix) {
        ArrayList<ArrayList<Integer>> I = new ArrayList<>();
        int H = this.H;
        int W = Matrix.get(0).size();
        for (int h = 0; h < H; h ++) {
            I.add(new ArrayList<Integer>());
        }
        for (int i = 0; i < H; i ++) {
            for (int j = 0; j < W; j ++) {
                I.get(i).add(j, (int) Importance(Matrix, i, j));
            }
        }
        return I;
    }

    private ArrayList<ArrayList<Pixel>> updateM (ArrayList<Integer> minPath,
                                                               ArrayList<ArrayList<Pixel>> Matrix) {
        ArrayList<ArrayList<Pixel>> resultM = new ArrayList<>();
        int curH = this.H;
        int curW;

        for (int j = 0; j < minPath.size(); j = j + 2) {
            int x = minPath.get(j);
            int y = minPath.get(j+1);
            Matrix.get(x).remove(y);
        }

        curW = Matrix.get(0).size();
        for (int i = 0; i < curH; i ++) {
            ArrayList<Pixel> temp = new ArrayList<>();
            for (int j = 0; j < curW; j ++) {
                if (Matrix.get(i).get(j) != null) {
                    temp.add(Matrix.get(i).get(j));
                }
            }
            resultM.add(temp);
        }
        return resultM;
    }

    private double PDist (Pixel p, Pixel q) {
        double c1 = Math.pow((p.r - q.r), 2);
        double c2 = Math.pow((p.b - q.b), 2);
        double c3 = Math.pow((p.g - q.g), 2);
        return c1 + c2 + c3;
    }


    private double Importance(ArrayList<ArrayList<Pixel>> Matrix, int i, int j) {
        return XImportance(Matrix, i, j) + YImportance(Matrix, i, j);
    }

    private double XImportance(ArrayList<ArrayList<Pixel>> Matrix, int i, int j) {
        double xImportance;
        int W = Matrix.get(0).size();
        if (j == 0) {
            xImportance = PDist(Matrix.get(i).get(W-1), Matrix.get(i).get(j+1));
        } else if (j == (W-1)) {
            xImportance = PDist(Matrix.get(i).get(j-1), Matrix.get(i).get(0));
        } else {
            xImportance = PDist(Matrix.get(i).get(j-1), Matrix.get(i).get(j+1));
        }
        return xImportance;
    }

    private double YImportance(ArrayList<ArrayList<Pixel>> Matrix, int i, int j) {
        double yImportance;
        int H = this.H;
        if (H == 1) {
            throw new IllegalArgumentException("Not valid graph!");
        }

//        for (int k = 0; k < Matrix.size(); k ++) {
//            System.out.println(Arrays.toString(Matrix.get(i).toArray(new Pixel[Matrix.get(k).size()])));
//        }

        if (i == 0) {
//            System.out.println(Matrix.get(2).get(1) + " " +Matrix.get(1).get(1));
//            System.out.println(i + " "+j);
            yImportance = PDist(Matrix.get(H-1).get(j), Matrix.get(i+1).get(j));
        } else if (i == (H-1)) {
            yImportance = PDist(Matrix.get(i-1).get(j), Matrix.get(0).get(j));
        } else {
            yImportance = PDist(Matrix.get(i-1).get(j), Matrix.get(i+1).get(j));
        }
        return yImportance;
    }

    private class Pixel {
        int r;
        int b;
        int g;
        public Pixel(String r, String b, String g) {
            this.r = Integer.valueOf(r);
            this.b = Integer.valueOf(b);
            this.g = Integer.valueOf(g);
        }

        public String printPixel() {
            return r +" " + b + " " + g;
        }
    }
}
