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
    
    public RepeatedContextComponent(int n, ContextComponent c) {
        List<ContextComponent> sequence = new ArrayList<ContextComponent>(n);
        for (int i = 0; i < n; ++i)
            sequence.add(c);
        
        this.sequence = new Context(sequence);
    }
    
    public Context getRepeatedSequence() {
        return sequence;
    }

    @Override
    public List<Entity> getEntitiesSequence() {
        List<Entity> seq = new ArrayList<>();
        
        List<ContextComponent> ctx = sequence.getContext();
        for (ContextComponent comp : ctx) {
            List<Entity> entitiesSeq = comp.getEntitiesSequence();
            seq.addAll(entitiesSeq);
        }
        
        return seq;
    }

    @Override
    public Set<Entity> getEntitiesSet() {
        Set<Entity> s = new HashSet<>();

        for (ContextComponent comp : sequence.getContext())
            s.addAll(comp.getEntitiesSet());

        return s;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        
        List<ContextComponent> ctx = sequence.getContext();
        for (int i = 0; i < ctx.size(); ++i) {
            s.append(ctx.get(i).toString());
            if (i < ctx.size() - 1)
                s.append(".");
        }
        
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
