package reactionsystem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author caba
 */
public class Context {
    private final List<ContextComponent> context;
    
    public Context(List<ContextComponent> context) {
        this.context = context;
    }
    
    public List<ContextComponent> getContext() {
        return context;
    }

    public static List<Context> parseParallel(String ctx) {
        List<Context> res = new ArrayList<>();

        String trimmedCtx = ctx.trim();

        // A parallel context cannot contain these symbols (since they belong to context components different from the Id ones).
        if (ctx.contains("nil") || ctx.contains("+") || ctx.contains("{") || ctx.contains("<")) {
            res.add(parseContext(ctx));
            return res;
        }

        // This checks if the provided context is a composition of
        // constant context variables defined inside the environment
        String parCtxRegex = "(?!\\w+\\s*),(?=\\s*\\w+)";

        String[] parallelContexts = trimmedCtx.split(parCtxRegex);

        for (String s : parallelContexts)
            res.add(parseContext(s));

        return res;
    }
    
    public static Context parseContext(String ctx) {
        List<ContextComponent> out;
        String trimmedCtx = ctx.trim();
        
        if (trimmedCtx.contains("+")) {
            String[] choices = trimmedCtx.split("\\s*\\Q+\\E\\s*"); // Each choice represents a ContextComponent
            List<Context> parsedChoices = new ArrayList<>();
            
            for (String choice : choices)
                parsedChoices.add(parseSingle(choice));
            
            out = new ArrayList<>();
            out.add(new ChoiceContextComponent(parsedChoices));
            return new Context(out);
        }
            
        return parseSingle(trimmedCtx);
    }
    
    private static Context parseSingle(String ctx) throws IllegalArgumentException {
        String[] unitComponents = ctx.split("\\Q.\\E");

        if (unitComponents.length == 0)
            throw new IllegalArgumentException("Found empty string instead of a context.");
        
        List<ContextComponent> resultSequence = new ArrayList<>();
        
        try {
            // The split string can contain: nil, {vars}, CtxId, <N,CtxId>.
            for (String unitComponent : unitComponents) {
                String sanitizedComponent;
                if (unitComponent.charAt(0) == '(')
                    sanitizedComponent = unitComponent.substring(1);
                else if (unitComponent.charAt(unitComponent.length() - 1) == ')')
                    sanitizedComponent = unitComponent.substring(0, unitComponent.length() - 1);
                else
                    sanitizedComponent = unitComponent;

                if (sanitizedComponent.equals("nil")) {
                    resultSequence.add(new NilContextComponent());
                    break;
                } else if (sanitizedComponent.charAt(0) == '{') {
                    List<Entity> entities = new ArrayList<Entity>();
                    String[] vars = sanitizedComponent.substring(1, sanitizedComponent.indexOf('}')).split(",");

                    for (String var : vars)
                        if (!var.isEmpty()) entities.add(new Entity(var.trim()));

                    resultSequence.add(new EntitiesContextComponent(entities));
                } else if (sanitizedComponent.charAt(0) == '<') {
                    String[] vars = sanitizedComponent.substring(1, sanitizedComponent.indexOf('>')).split(",");
                    resultSequence.add(new RepeatedContextComponent(Integer.parseInt(vars[0]), new IdContextComponent(vars[1].trim())));
                } else
                    resultSequence.add(new IdContextComponent(sanitizedComponent));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Badly formatted string. " + e);
        }
        
        return new Context(resultSequence);
    }
    
    /**
     * This method is used to insert the context to which an IdContextComponent in a given position refers to OR
     * to insert a repeated sequence translated by a RepeatedContextComponent
    **/
    public Context getSubstitutedContext(int position, Context ctx) throws IllegalArgumentException {
        if (!(context.get(position) instanceof IdContextComponent) && 
            !(context.get(position) instanceof RepeatedContextComponent))
            throw new IllegalArgumentException("The provided context position does not refer neither" +
                    " to a substitutable variable nor to a repeated context");

        List<ContextComponent> newContext = new ArrayList<>();
        List<ContextComponent> toSubstitute = ctx.getContext();

        if (BioResolve.DEBUG) {
            System.out.print("[Info] Substituting " + context.get(position) + " with ");
            int i = 0;
            final int len = toSubstitute.size() - 1;
            for (ContextComponent cc : toSubstitute)
                System.out.print(cc + (i++ < len ? "." : ""));
            System.out.println();
        }
        
//        int i = 0;
//        for (; i < position; ++i)
//            newContext.add(context.get(i));

        newContext.addAll(toSubstitute);

//        ++i;
        for (int i = position + 1; i < context.size(); ++i)
            newContext.add(context.get(i));
        
//        context = newContext;
        return new Context(newContext);
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        
        for (int i = 0; i < context.size(); ++i) {
            s.append(context.get(i));
            if (i < context.size() - 1)
                s.append(".");
        }
        
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;

        Context s = (Context) o;
        return context.equals(s.context);
    }

    @Override
    public int hashCode() {
        int result = 17;

        for (ContextComponent ctxComp : context)
            result = result * 37 + ctxComp.hashCode();

        return result;
    }
}
