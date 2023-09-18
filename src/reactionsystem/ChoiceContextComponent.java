package reactionsystem;

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
    
    public ChoiceContextComponent(List<Context> choices) {
        this.choices = choices;
    }
    
    public List<Context> getChoices() {
        return choices;
    }

    @Override
    public List<Entity> getEntitiesSequence() {
        List<Entity> seq = new ArrayList<>();
        
        for (Context choice : choices) {
            List<ContextComponent> ctx = choice.getContext();
            for (ContextComponent comp : ctx) {
                List<Entity> entitiesSeq = comp.getEntitiesSequence();
                seq.addAll(entitiesSeq);
            }
        }
        
        return seq;
    }

    @Override
    public Set<Entity> getEntitiesSet() {
        Set<Entity> s = new HashSet<>();

        for (Context choice : choices) {
            for (ContextComponent comp : choice.getContext())
                s.addAll(comp.getEntitiesSet());
        }

        return s;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < choices.size(); ++i) {
            List<ContextComponent> ctx = choices.get(i).getContext();
            for (int j = 0; j < ctx.size(); ++j) {
                s.append(ctx.get(j).toString());
                if (j < ctx.size() - 1)
                    s.append(".");
            }
            if (i < choices.size() - 1)
                s.append(" + ");
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
