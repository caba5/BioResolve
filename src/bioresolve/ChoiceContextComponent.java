package bioresolve;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author caba
 */
public class ChoiceContextComponent extends ContextComponent {
    private final List<Context> choices;
    
    public ChoiceContextComponent(final List<Context> choices) {
        this.choices = choices;
    }
    
    public List<Context> getChoices() {
        return choices;
    }

    @Override
    public List<Entity> getEntitiesSequence() {
        final List<Entity> seq = new ArrayList<>();
        
        for (final Context choice : choices) {
            final List<ContextComponent> ctx = choice.getContext();
            for (final ContextComponent comp : ctx) {
                final List<Entity> entitiesSeq = comp.getEntitiesSequence();
                seq.addAll(entitiesSeq);
            }
        }
        
        return seq;
    }

    @Override
    public Set<Entity> getEntitiesSet() {
        final Set<Entity> s = new HashSet<>();

        for (final Context choice : choices) {
            for (final ContextComponent comp : choice.getContext())
                s.addAll(comp.getEntitiesSet());
        }

        return s;
    }
    
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();

        for (int i = 0; i < choices.size(); ++i) {
            final List<ContextComponent> ctx = choices.get(i).getContext();
            for (int j = 0; j < ctx.size(); ++j)
                s.append(ctx.get(j).toString()).append(j < ctx.size() - 1 ? "." : "");
            s.append(i < choices.size() - 1 ? " + " : "");
        }

        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;

        ChoiceContextComponent other = (ChoiceContextComponent) o;
        return choices.equals(other.choices);
    }

    @Override
    public int hashCode() {
        int result = 17;

        for (Context c : choices)
            result = result * 37 + c.hashCode();

        return result;
    }
}
