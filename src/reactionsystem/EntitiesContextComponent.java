package reactionsystem;

import java.util.List;

/**
 *
 * @author caba
 */
public class EntitiesContextComponent extends ContextComponent {
    private final List<Entity> entities;
    
    public EntitiesContextComponent(List<Entity> entities) {
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
    public String toString() {
        StringBuilder s = new StringBuilder("{");

        for (int i = 0; i < entities.size(); ++i) {
            s.append(entities.get(i).toString());
            if (i < entities.size() - 1)
                s.append(",");
        }

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
