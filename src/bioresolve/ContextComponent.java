package bioresolve;

import java.util.List;
import java.util.Set;

/**
 * The abstract class defining a generic context component.
 * @author caba
 */
public abstract class ContextComponent {
    /**
     * This method shall return the sequence of entities present in the deriving component.
     * @return The list of entities.
     */
    public abstract List<Entity> getEntitiesSequence();

    /**
     * This method shall return the set of entities present in the deriving component.
     * @return The set of entities.
     */
    public abstract Set<Entity> getEntitiesSet();

    @Override
    public abstract String toString();
}
