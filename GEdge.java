public class GEdge implements Comparable {
    private GNode node;
    private int val;

    public GEdge (GNode node, Integer w) {
        this.node = node;
        this.val = w;
    }

    public GNode getNode() {
        return node;
    }

    public int getVal() {
        return val;
    }
    @Override
    public int compareTo(Object o) {
        GEdge gEdge = null;
        if (o.getClass().equals(this.getClass())) {
            gEdge = (GEdge) o;
        }

        if (this.val > gEdge.val) {
            return 1;
        } else if (this.val < gEdge.val) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GEdge)) {
            return false;
        }

        GEdge edge = (GEdge) o;

        return this.node.equals(edge.node) && this.val == edge.val;
    }

    public int hashCode()
    {
        return node.hashCode();
    }
}


