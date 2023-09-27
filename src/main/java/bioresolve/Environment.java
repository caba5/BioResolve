package bioresolve;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class describes the environment of the whole system.<br>
 * The environment is represented as a mapping between a string (the constant variable name specified in the environment)
 * and the context associated to it.
 * @author caba
 */
public class Environment {
    private final Map<String, Context> env;

    /**
     * @param env The string specifying the environment.
     */
    public Environment(final String env) {
        this.env = parseEnvironment(env);
    }

    /**
     * Parses the environment string, creating a new context for each definition and mapping it to its variable.
     * @param env The environment string.
     * @return The mapping between variables and contexts.
     */
    private Map<String, Context> parseEnvironment(final String env) {
        final Map<String, Context> e = new HashMap<>();

        /*
        A possibly more precise regex(?)
        \w+\s*=\s*((\{\w+\s*(,\s*\w+)*\})|(<\s*[1-9][0-9]+\s*,\s*\w+\s*>)|(\w+)|(\(.*\)))(\.((\{\w+\s*(,\s*\w+)*\})|(<\s*[1-9][0-9]+\s*,\s*\w+\s*>)|(\w+)|(\(.*\))))*\s*(,\w+\s*=\s*((\{\w+\s*(,\s*\w+)*\})|(<\s*[1-9][0-9]+\s*,\s*\w+\s*>)|(\w+)|(\(.*\)))(\.((\{\w+\s*(,\s*\w+)*\})|(<\s*[1-9][0-9]+\s*,\s*\w+\s*>)|(\w+)|(\(.*\))))*\s*)*
         */
        
        if (env.matches(".*\\s*,\\s*(?=\\w+\\s*=).*")) {
            final String[] assignments = env.split("\\s*,\\s*(?=\\w+\\s*=)");
            for (final String assignment : assignments)
                parseEnvironmentSingle(assignment, e);
        } else
            parseEnvironmentSingle(env, e);
        
        return e;
    }

    /**
     * Parses a single assignment in the environment (e.g. x = {a,b}.{c}.nil).
     * @param assignment The string representing the assignment.
     * @param map The map to populate.
     * @throws IllegalArgumentException
     */
    private void parseEnvironmentSingle(final String assignment, final Map<String, Context> map) throws IllegalArgumentException {
        if (assignment.isBlank())
            return;
        
        try {
            if (assignment.matches("^\\w+\\s*=\\s*.*")) {
                final int indexEqual = assignment.indexOf('=');
                final String varName = assignment.substring(0, indexEqual).trim();
                final String rest = assignment.substring(indexEqual + 1).trim();

                if (rest.contains(varName))
                    System.out.println("[Warning] The definition " + assignment + " appears to be recursive. This can lead to non-termination.");
                
                final Context comp;
                
                if (rest.charAt(0) == '(')
                    comp = Context.parseContext(rest.substring(1, rest.length() - 1));
                else
                    comp = Context.parseContext(rest);
                
                if (map.putIfAbsent(varName, comp) != null)
                    throw new IllegalArgumentException("Failed parsing the environment: the variable " + varName + " has been defined multiple times");
            } else
                throw new IllegalArgumentException("Failed parsing the environment since it has no variable definition");
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed parsing the environment. " + e);
        }
    }

    public Map<String, Context> getEnv() {
        return env;
    }
    
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        
        for (Map.Entry<String, Context> e : env.entrySet()) {
            final List<ContextComponent> comps = e.getValue().getContext();
            
            s.append(e.getKey()).append(": ");
            
            for (int i = 0; i < comps.size(); ++i) {
                s.append(comps.get(i));
                if (i < comps.size() - 1)
                    s.append(".");
            }
                
            s.append("\n");
        }
        
        return s.toString();
    }
}
