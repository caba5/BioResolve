package bioresolve;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the <i>nil</i> component in a sequence.
 * @author caba
 */
public class NilContextComponent extends ContextComponent {
    @Override
    public List<Entity> getEntitiesSequence() { // A nil component shouldn't return any entity (since it contains only nil)
        return new ArrayList<>();
    }

    @Override
    public Set<Entity> getEntitiesSet() {
        return new HashSet<>(){{ add(new Entity("nil")); }};
    }
    
    @Override
    public String toString() {
        return "nil";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = result * 37 + "nil".hashCode();

        return result;
    }
}
