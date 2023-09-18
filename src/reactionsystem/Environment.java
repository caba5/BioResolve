package reactionsystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author caba
 */
public class Environment {
    private final Map<String, Context> env;
    
    public Environment(String env) {
        this.env = parseEnvironment(env);
    }
    
    private Map<String, Context> parseEnvironment(String env) {
        Map<String, Context> e = new HashMap<String, Context>();

        /*
        A possibly more precise regex(?)
        \w+\s*=\s*((\{\w+\s*(,\s*\w+)*\})|(<\s*[1-9][0-9]+\s*,\s*\w+\s*>)|(\w+)|(\(.*\)))(\.((\{\w+\s*(,\s*\w+)*\})|(<\s*[1-9][0-9]+\s*,\s*\w+\s*>)|(\w+)|(\(.*\))))*\s*(,\w+\s*=\s*((\{\w+\s*(,\s*\w+)*\})|(<\s*[1-9][0-9]+\s*,\s*\w+\s*>)|(\w+)|(\(.*\)))(\.((\{\w+\s*(,\s*\w+)*\})|(<\s*[1-9][0-9]+\s*,\s*\w+\s*>)|(\w+)|(\(.*\))))*\s*)*
         */
        
        if (env.matches(".*\\s*,\\s*(?=\\w+\\s*=).*")) {
            String[] assignments = env.split("\\s*,\\s*(?=\\w+\\s*=)");
            for (String assignment : assignments)
                parseEnvironmentSingle(assignment, e);
        } else
            parseEnvironmentSingle(env, e);
        
        return e;
    }
    
    private void parseEnvironmentSingle(String assignment, Map<String, Context> map) throws IllegalArgumentException {
        if (assignment.isBlank())
            return;
        
        try {
            if (assignment.matches("^\\w+\\s*=\\s*.*")) {
                int indexEqual = assignment.indexOf('=');
                String varName = assignment.substring(0, indexEqual).trim();
                String rest = assignment.substring(indexEqual + 1).trim();

                if (rest.contains(varName))
                    System.out.println("[Warning] The definition " + assignment + " appears to be recursive. This can lead to non-termination.");
                
                Context comp;
                
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
        StringBuilder s = new StringBuilder();
        
        for (Map.Entry<String, Context> e : env.entrySet()) {
            List<ContextComponent> comps = e.getValue().getContext();
            
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
