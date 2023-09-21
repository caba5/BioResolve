package bioresolve;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a single entity of the system.
 * @author caba
 */
public class Entity {
    private final String symbol;

    /**
     * @param symbol A string representing the entity.
     */
    Entity(final String symbol) {
        this.symbol = symbol;
    }

    /**
     * Utility function used to create a set of entities from an array of strings.
     * @param entities The array of strings, each representing an entity.
     * @return A set of instances of this class.
     */
    public static Set<Entity> createSetFromStringsArr(final String[] entities) {
        final Set<Entity> res = new HashSet<>();

        for (final String entity : entities)
            res.add(new Entity(entity));

        return res;
    }

    /**
     * Utility function used to extract all the entities from a string representing a reaction.<br>
     * This method allows to avoid specifying every single entity before running the program.
     * @param reactions The string representing the reactions.
     * @return A set of instances of this class.
     * @throws IllegalArgumentException
     */
    public static Set<Entity> extrapolateEntitiesFromReactionsString(final String reactions) throws IllegalArgumentException {
        final Set<Entity> res = new HashSet<>();

        final String entityPattern = "\\w+";

        final Pattern pattern = Pattern.compile(entityPattern);
        final Matcher matcher = pattern.matcher(reactions);

        while (matcher.find())
            res.add(new Entity(matcher.group(0)));

        return res;
    }

    /**
     * Utility function used to create a string out of a collection of entities.<br>
     * It is mainly used for debugging purposes.
     * @param entityCollection A collection of entities.
     * @return The string representing the collection of entities.
     */
    public static String stringifyEntitiesCollection(final Collection<Entity> entityCollection) {
        final StringBuilder res = new StringBuilder();

        final int len = entityCollection.size() - 1;
        int i = 0;

        for (Entity e : entityCollection) {
            res.append(e);
            if (i++ < len) res.append(",");
        }

        return res.toString();
    }

    @Override
    public String toString() {
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;

        Entity e = (Entity) o;
        return symbol.equals(e.symbol);
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = result * 37 + symbol.hashCode();

        return result;
    }

    public String symbol() {
        return symbol;
    }

}
