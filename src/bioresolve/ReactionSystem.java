package bioresolve;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the reaction system, i.e. the set of entities together with the set of reactions.
 * @author caba
 */
public class ReactionSystem {
    private final Set<Entity> entities;
    private final Set<Reaction> reactions;

    /**
     * @param entities The set of entities.
     * @param reactions The set of reactions.
     * @throws IllegalArgumentException If any of the reaction entities does not belong to the entity set.
     */
    public ReactionSystem(final Set<Entity> entities, final Set<Reaction> reactions) throws IllegalArgumentException {
        this.entities = entities;
        
        for (final Reaction reaction : reactions) {
            checkEntitySetBelonging(reaction.getReactants());
            checkEntitySetBelonging(reaction.getInhibitors());
            checkEntitySetBelonging(reaction.getProducts());
        }
        this.reactions = reactions;
    }

    /**
     * Checks whether a set of entities belongs to the reaction system's entity set.
     * @param entitySet The entity set to be checked.
     * @throws IllegalArgumentException If an entity does not belong the reaction system's set.
     */
    private void checkEntitySetBelonging(final Set<Entity> entitySet) throws IllegalArgumentException {
        for (final Entity e : entitySet) {
            if (!this.entities.contains(e))
                throw new IllegalArgumentException("The provided set of reactions contains some entities which do not belong to the provided entities set");
        }
    }
    
    public Set<Entity> computeResults(final Set<Entity> wSet) {
        final Set<Entity> res = new HashSet<>();
        
        for (final Reaction reaction : reactions)
            res.addAll(reaction.computeResult(wSet));
        
        return res;
    }
    
    public Set<Entity> getEntities() {
        return this.entities;
    }
}
