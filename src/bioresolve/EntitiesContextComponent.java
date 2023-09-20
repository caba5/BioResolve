package bioresolve;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author caba
 */
public class EntitiesContextComponent extends ContextComponent {
    private final List<Entity> entities;
    
    public EntitiesContextComponent(final List<Entity> entities) {
        this.entities = entities;
    }
    
    public List<Entity> getEntities() {
        return entities;
    }

    @Override
    public List<Entity> getEntitiesSequence() {
        return entities;
    }

    @Override
    public Set<Entity> getEntitiesSet() {
        return new HashSet<>(entities);
    }
    
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("{");

        for (int i = 0; i < entities.size(); ++i)
            s.append(entities.get(i).toString()).append(i < entities.size() - 1 ? "," : "");

        s.append("}");

        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;

        EntitiesContextComponent other = (EntitiesContextComponent) o;
        return entities.equals(other.entities);
    }

    @Override
    public int hashCode() {
        int result = 17;

        for (Entity e : entities)
            result = result * 37 + e.hashCode();

        return result;
    }
}
