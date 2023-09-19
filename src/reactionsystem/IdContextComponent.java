package reactionsystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author caba
 */
public class IdContextComponent extends ContextComponent {
    private final String contextId;
    
    public IdContextComponent(final String id) {
        this.contextId = id;
    }
    
    public String getId() {
        return contextId;
    }

    @Override
    public List<Entity> getEntitiesSequence() { // An id component should not return any entity (since it contains just an id)
        return new ArrayList<>();
    }

    @Override
    public Set<Entity> getEntitiesSet() {
        return new HashSet<>(){{ add(new Entity(contextId)); }}; // Hack: the contextId isn't a real Entity, but it works as such until inspected
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
