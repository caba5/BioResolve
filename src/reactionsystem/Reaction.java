package reactionsystem;

import java.util.*;

/**
 *
 * @author caba
 */
public class Reaction {
    private final Set<Entity> reactants;
    private final Set<Entity> inhibitors;
    private final Set<Entity> products;
    
    public Reaction(Set<Entity> reactants, Set<Entity> inhibitors, Set<Entity> products) throws IllegalArgumentException {
        if (reactants.isEmpty() || inhibitors.isEmpty() || products.isEmpty())
            throw new IllegalArgumentException("The provided arguments have to be non-empty sets of entities");
        
        this.reactants = reactants;
        this.inhibitors = inhibitors;
        this.products = products;
    }
    
    public Set<Entity> computeResult(Set<Entity> wSet) {
        for (Entity reactant : reactants)
            if (!wSet.contains(reactant))
                return Collections.emptySet();
        
        for (Entity inhibitor : inhibitors)
            if (wSet.contains(inhibitor))
                return Collections.emptySet();

        if (BioResolve.DEBUG)
            printReaction();
        
        return products;        
    }

    public static Set<Reaction> createReactionSet(Reaction... reactions) {
        Set<Reaction> res = new HashSet<>();

        Collections.addAll(res, reactions);

        return res;
    }

    public static Set<Reaction> createReactionSet(List<Reaction> reactions) {
        return new HashSet<>(reactions);
    }

    // This has to be called before both entity extrapolation and reactions parsing
    public static void checkReactionStringConformity(String reactions) throws IllegalArgumentException {
        String[] reacs = reactions.trim().split("(?!\\)\\s*),\\s*(?=\\()");

        final String reactionForm = "\\(\\[(\\s*\\w+\\s*(,\\s*\\w+\\s*)*)?\\]\\s*," +
                "\\s*\\[(\\s*\\w+\\s*(,\\s*\\w+\\s*)*)?\\]\\s*," +
                "\\s*\\[(\\s*\\w+\\s*(,\\s*\\w+\\s*)*)?\\]\\s*\\)";

        for (String reaction : reacs)
            if (!reaction.matches(reactionForm))
                throw new IllegalArgumentException("The reaction " + reaction + " does not respect the form ([a,b], [c,d], [e,f]).");
    }

    public static Set<Reaction> parseReactions(String reactions) throws IllegalArgumentException {
        Set<Reaction> res = new HashSet<>();

        String[] reacs = reactions.trim().split("(?!\\)\\s*),\\s*(?=\\()");

        for (String reaction : reacs) {
            String[] reactionComponents = reaction.substring(1, reaction.length() - 1).split("(?!])\\s*,\\s*(?=\\[)");

            String[] reactants = reactionComponents[0].substring(1, reactionComponents[0].length() - 1).split(",");
            String[] inhibitors = reactionComponents[1].substring(1, reactionComponents[1].length() - 1).split(",");
            String[] products = reactionComponents[2].substring(1, reactionComponents[2].length() - 1).split(",");

            Set<Entity> reactantsSet = Entity.createSetFromStringsArr(reactants);
            Set<Entity> inhibitorsSet = Entity.createSetFromStringsArr(inhibitors);
            Set<Entity> productsSet = Entity.createSetFromStringsArr(products);

            Reaction parsedReaction = new Reaction(reactantsSet, inhibitorsSet, productsSet);

            res.add(parsedReaction);
        }

        return res;
    }

    private void printReaction() {
        System.out.print("Activated reaction: [");
        int i = 0;
        int len = reactants.size() - 1;
        for (Entity reac : reactants)
            System.out.print(reac.symbol() + (i++ < len ? "," : ""));

        System.out.print("], [");

        i = 0;
        len = inhibitors.size() - 1;
        for (Entity inhib : inhibitors)
            System.out.print(inhib.symbol() + (i++ < len ? "," : ""));

        System.out.print("], [");

        i = 0;
        len = products.size() - 1;
        for (Entity prod : products)
            System.out.print(prod.symbol() + (i++ < len ? "," : ""));

        System.out.println("].");
    }
    
    public Set<Entity> getReactants() {
        return this.reactants;
    }
    
    public Set<Entity> getInhibitors() {
        return this.inhibitors;
    }
    
    public Set<Entity> getProducts() {
        return this.products;
    }
}
