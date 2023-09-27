package bioresolve;

import java.util.*;

/**
 * Represents a reaction of the system.
 * @author caba
 */
public class Reaction {
    private final Set<Entity> reactants;
    private final Set<Entity> inhibitors;
    private final Set<Entity> products;

    /**
     * @param reactants The set of reactants.
     * @param inhibitors The set of inhibitors.
     * @param products The set of products.
     * @throws IllegalArgumentException
     */
    public Reaction(Set<Entity> reactants, Set<Entity> inhibitors, Set<Entity> products) throws IllegalArgumentException {
        if (reactants.isEmpty() || inhibitors.isEmpty() || products.isEmpty())
            throw new IllegalArgumentException("The provided arguments have to be non-empty sets of entities");

        this.reactants = reactants;
        this.inhibitors = inhibitors;
        this.products = products;
    }

    /**
     * Given a set, it computes the reaction applied to its entities.
     * @param wSet The set on which to compute the reaction.
     * @return The products of the reaction if all the reactants and no inhibitors are present, an empty result otherwise.
     */
    public Set<Entity> computeResult(final Set<Entity> wSet) {
        for (final Entity reactant : reactants)
            if (!wSet.contains(reactant))
                return Collections.emptySet();

        for (final Entity inhibitor : inhibitors)
            if (wSet.contains(inhibitor))
                return Collections.emptySet();

        if (BioResolve.OUT) printReaction();

        return products;
    }

    /**
     * Checks that the reaction string is correct.<br>
     * This has to be called before both entity extrapolation and reactions parsing
     * @param reactions The reactions string.
     * @throws IllegalArgumentException If the reaction is wrongly specified.
     */
    public static void checkReactionStringConformity(final String reactions) throws IllegalArgumentException {
        final String[] reacs = reactions.trim().split("(?!\\)\\s*),\\s*(?=\\()");

        final String reactionForm = "\\(\\[(\\s*\\w+\\s*(,\\s*\\w+\\s*)*)?\\]\\s*," +
                "\\s*\\[(\\s*\\w+\\s*(,\\s*\\w+\\s*)*)?\\]\\s*," +
                "\\s*\\[(\\s*\\w+\\s*(,\\s*\\w+\\s*)*)?\\]\\s*\\)";

        for (final String reaction : reacs)
            if (!reaction.matches(reactionForm))
                throw new IllegalArgumentException("The reaction " + reaction + " does not respect the form ([a,b], [c,d], [e,f]).");
    }

    /**
     * Parses the string containing all the reactions, creating a reaction for each.
     * @param reactions The reactions string.
     * @return The set of reactions which have been created.
     */
    public static Set<Reaction> parseReactions(final String reactions) {
        final Set<Reaction> res = new HashSet<>();

        final String[] reacs = reactions.trim().split("(?!\\)\\s*),\\s*(?=\\()");

        for (final String reaction : reacs) {
            final String[] reactionComponents = reaction.substring(1, reaction.length() - 1).split("(?!])\\s*,\\s*(?=\\[)");

            final String[] reactants = reactionComponents[0].substring(1, reactionComponents[0].length() - 1).split(",");
            final String[] inhibitors = reactionComponents[1].substring(1, reactionComponents[1].length() - 1).split(",");
            final String[] products = reactionComponents[2].substring(1, reactionComponents[2].length() - 1).split(",");

            final Set<Entity> reactantsSet = Entity.createSetFromStringsArr(reactants);
            final Set<Entity> inhibitorsSet = Entity.createSetFromStringsArr(inhibitors);
            final Set<Entity> productsSet = Entity.createSetFromStringsArr(products);

            final Reaction parsedReaction = new Reaction(reactantsSet, inhibitorsSet, productsSet);

            res.add(parsedReaction);
        }

        return res;
    }

    /**
     * Prints the activated reaction.
     */
    private void printReaction() {
        System.out.print("Activated reaction: [");
        int i = 0;
        int len = reactants.size() - 1;
        for (final Entity reac : reactants)
            System.out.print(reac.symbol() + (i++ < len ? "," : ""));

        System.out.print("], [");

        i = 0;
        len = inhibitors.size() - 1;
        for (final Entity inhib : inhibitors)
            System.out.print(inhib.symbol() + (i++ < len ? "," : ""));

        System.out.print("], [");

        i = 0;
        len = products.size() - 1;
        for (final Entity prod : products)
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
