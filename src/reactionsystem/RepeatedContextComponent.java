package reactionsystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author caba
 */
public class RepeatedContextComponent extends ContextComponent {
    private final Context sequence;
    
    public RepeatedContextComponent(final int n, final ContextComponent c) {
        List<ContextComponent> sequence = new ArrayList<>(n);
        for (int i = 0; i < n; ++i)
            sequence.add(c);
        
        this.sequence = new Context(sequence);
    }
    
    public Context getRepeatedSequence() {
        return sequence;
    }

    @Override
    public List<Entity> getEntitiesSequence() {
        final List<Entity> seq = new ArrayList<>();
        
        final List<ContextComponent> ctx = sequence.getContext();
        for (final ContextComponent comp : ctx) {
            final List<Entity> entitiesSeq = comp.getEntitiesSequence();
            seq.addAll(entitiesSeq);
        }
        
        return seq;
    }

    @Override
    public Set<Entity> getEntitiesSet() {
        final Set<Entity> s = new HashSet<>();

        for (final ContextComponent comp : sequence.getContext())
            s.addAll(comp.getEntitiesSet());

        return s;
    }
    
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        
        final List<ContextComponent> ctx = sequence.getContext();
        for (int i = 0; i < ctx.size(); ++i)
            s.append(ctx.get(i)).append(i < ctx.size() - 1 ? "." : "");
        
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;

        RepeatedContextComponent other = (RepeatedContextComponent) o;
        return sequence.equals(other.sequence);
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = result * 37 + sequence.hashCode();

        return result;
    }
}
