
public class GNode {
    int x;
    int y;

    public GNode (Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public String printNode() {
        return "(" + x + ", " + y+")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GNode)) {
            return false;
        }

        GNode node = (GNode) o;

        return this.x == node.x && this.y == node.y;
    }

    public int hashCode()
    {
        int result = 4*x + 13;
        result += 5*y*y + 41;
        return result;
    }

}

