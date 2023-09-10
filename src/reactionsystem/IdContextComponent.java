package reactionsystem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author caba
 */
public class IdContextComponent extends ContextComponent {
    private final String contextId;
    
    public IdContextComponent(String id) {
        this.contextId = id;
    }
    
    public String getId() {
        return contextId;
    }

    @Override
    public List<Entity> getEntitiesSequence() { // An id component should not return any entity (since it contains just an id)
//        List<Entity> seq = new ArrayList<>();
//        seq.add(new Entity(contextId));
//        return seq;
        return new ArrayList<>();
    }
    
    @Override
    public String toString() {
        return contextId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;

        IdContextComponent other = (IdContextComponent) o;
        return contextId.equals(other.contextId);
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = result * 37 + (contextId == null ? 0 : contextId.hashCode());

        return result;
    }
}
