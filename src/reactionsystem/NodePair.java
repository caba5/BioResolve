package reactionsystem;


import java.util.Set;

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