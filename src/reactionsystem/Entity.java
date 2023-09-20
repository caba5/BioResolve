package reactionsystem;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author caba
 */
public class Entity {
    private final String symbol;

    Entity(final String symbol) {
        this.symbol = symbol;
    }

    public static Set<Entity> createSetFromStringsArr(final String[] entities) {
        final Set<Entity> res = new HashSet<>();

        for (final String entity : entities)
            res.add(new Entity(entity));

        return res;
    }

    public static Set<Entity> extrapolateEntitiesFromReactionsString(final String reactions) throws IllegalArgumentException {
        final Set<Entity> res = new HashSet<>();

        final String entityPattern = "\\w+";

        final Pattern pattern = Pattern.compile(entityPattern);
        final Matcher matcher = pattern.matcher(reactions);

        while (matcher.find())
            res.add(new Entity(matcher.group(0)));

        return res;
    }

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
