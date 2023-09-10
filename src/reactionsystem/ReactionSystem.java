package reactionsystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author caba
 */
public class ReactionSystem {
    private final Set<Entity> entities;
    private final Set<Reaction> reactions;
    
    public ReactionSystem(Set<Entity> entities, Set<Reaction> reactions) throws IllegalArgumentException {        
        this.entities = entities;
        
        for (Reaction reaction : reactions) {
            checkEntitySetBelonging(reaction.getReactants());
            checkEntitySetBelonging(reaction.getInhibitors());
            checkEntitySetBelonging(reaction.getProducts());
        }
        this.reactions = reactions;
    }
    
    private void checkEntitySetBelonging(Set<Entity> entitySet) throws IllegalArgumentException {
        for (Entity e : entitySet) {
            if (!this.entities.contains(e))
                throw new IllegalArgumentException("The provided set of reactions contains some entities which do not belong to the provided entities set");
        }
    }
    
    public Set<Entity> computeResults(Set<Entity> wSet) { // TODO: check that wSet's entities belong to entities of the reaction system
        Set<Entity> res = new HashSet<>();
        
        for (Reaction reaction : reactions)
            res.addAll(reaction.computeResult(wSet));
        
        return res;
    }
    
    public Set<Entity> getEntities() {
        return this.entities;
    }
    
    public Set<Reaction> getReactions() {
        return this.reactions;
    }
}
