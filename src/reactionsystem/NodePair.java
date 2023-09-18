package reactionsystem;


import java.util.Set;

public class NodePair {
    private final Set<Entity> from;
    private final Set<Entity> to;
    private final Set<Entity> arc;
    private final String fromContext;
    private final String toContext;

    public NodePair(Set<Entity> from, String fromContext, Set<Entity> to, String toContext, Set<Entity> arc) {
        this.from = from;
        this.to = to;
        this.arc = arc;
        this.fromContext = fromContext;
        this.toContext = toContext;
    }

    public Set<Entity> getFrom() {
        return from;
    }

    public Set<Entity> getTo() {
        return to;
    }

    public Set<Entity> getArc() {
        return arc;
    };

    public String getFromContext() {
        return fromContext;
    }

    public String getToContext() {
        return toContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;

        NodePair np = (NodePair) o;
        return from.equals(np.from)
                && to.equals(np.to)
                && arc.equals(np.arc);
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = result * 37 + from.hashCode();
        result = result * 37 + to.hashCode();
        result = result * 37 + arc.hashCode();
        result = result * 37 + fromContext.hashCode();
        result = result * 37 + toContext.hashCode();

        return result;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{");

        int i = from.size() - 1;
        for (Entity e : from)
            s.append(e).append(i-- > 0 ? "," : "");

        s.append("{").append(fromContext).append(" ---- {");

        i = arc.size() - 1;
        for (Entity e : arc)
            s.append(e).append(i-- > 0 ? "," : "");

        s.append("}").append(" ---> {");

        i = to.size() - 1;
        for (Entity e : to)
            s.append(e).append(i-- > 0 ? "," : "");

        s.append("}").append(toContext);

        return s.toString();
    }
}