package bioresolve;

import java.util.Set;

/**
 * Represents a pair of nodes in the final graph. Besides being used for the creation of the DOT graph, a pair of nodes
 * is also used for caching a computed state.
 * @param from
 * @param fromContext
 * @param to
 * @param toContext
 * @param arc
 */
public record NodePair(Set<Entity> from, String fromContext, Set<Entity> to, String toContext, Set<Entity> arc) {
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
        final StringBuilder s = new StringBuilder("{");

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