package reactionsystem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author caba
 */
public class NilContextComponent extends ContextComponent {
    @Override
    public List<Entity> getEntitiesSequence() { // A nil component shouldn't return any entity (since it contains only nil)
//        List<Entity> seq = new ArrayList<Entity>();
//        seq.add(new Entity("nil"));
//        return seq;
        return new ArrayList<>();
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
