package bioresolve;

import java.util.List;
import java.util.Set;

/**
 *
 * @author caba
 */
public abstract class ContextComponent {
    public abstract List<Entity> getEntitiesSequence();

    public abstract Set<Entity> getEntitiesSet();

    @Override
    public abstract String toString();
}
