package reactionsystem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author caba
 */
record Entity(String symbol) {

//    public boolean isNil() {
//        return symbol.equals("nil")
//                || symbol.equals("NIL")
//                || symbol.equals("null")
//                || symbol.equals("NULL");
//        // Possibly more...
//    }

    public static Set<Entity> createEntitySet(Entity... entities) {
        Set<Entity> res = new HashSet<>();

        Collections.addAll(res, entities);

        return res;
    }

    public static Set<Entity> createSetFromStringsArr(String[] entities) {
        Set<Entity> res = new HashSet<>();

        for (String entity : entities) {
            res.add(new Entity(entity));
        }

        return res;
    }

    public static Set<Entity> extrapolateEntitiesFromReactionsString(String reactions) throws IllegalArgumentException {
        Set<Entity> res = new HashSet<>();

        final String entityPattern = "\\w+";

        final Pattern pattern = Pattern.compile(entityPattern);
        final Matcher matcher = pattern.matcher(reactions);

        while (matcher.find()) {
            res.add(new Entity(matcher.group(0)));
        }

        return res;
    }

    public static void printEntitiesCollection(Collection<Entity> entityCollection) { // TODO: remove and substitute usages with below
        for (Entity e : entityCollection)
            System.out.print(e.symbol());
    }

    public static String stringifyEntitiesCollection(Collection<Entity> entityCollection) {
        StringBuilder res = new StringBuilder();

        final int len = entityCollection.size() - 1;
        int i = 0;

        for (Entity e : entityCollection) {
            res.append(e);
            if (i++ < len) res.append(",");
        }

        return res.toString();
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
    public String toString() {
        return symbol;
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = result * 37 + symbol.hashCode();

        return result;
    }
}
