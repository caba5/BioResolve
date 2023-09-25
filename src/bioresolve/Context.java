package bioresolve;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a context of the program. The instances are created initially by parsing the context string,
 * identifying possible parallel contexts (i.e. multiple instances of this class).<br>
 * Each context stores a list of context components, which are identified during the initial parsing phase.
 * @author caba
 */
public class Context {
    private final List<ContextComponent> context;

    /**
     * @param context A list of context components, usually provided by the context parser.
     */
    public Context(final List<ContextComponent> context) {
        this.context = context;
    }

    /**
     * Parses a string into multiple contexts if it represents a parallel composition.
     * @param ctx The string representing the context.
     * @return A new Context instance
     */
    public static List<Context> parseParallel(final String ctx) {
        final List<Context> res = new ArrayList<>();

        final String trimmedCtx = ctx.trim();

        // A parallel context cannot contain these symbols (since they belong to context components different from the Id ones).
        if (ctx.contains("nil") || ctx.contains("+") || ctx.contains("{") || ctx.contains("<")) {
            res.add(parseContext(ctx));
            return res;
        }

        // This checks if the provided context is a composition of
        // constant context variables defined inside the environment
        final String parCtxRegex = "(?!\\w+\\s*),(?=\\s*\\w+)";

        final String[] parallelContexts = trimmedCtx.split(parCtxRegex);

        for (final String s : parallelContexts)
            res.add(parseContext(s));

        return res;
    }

    /**
     * Parses a string representing a single context, that is a single unit of a parallel composition (if present).
     * @param ctx The string representing a parallel composition's unit.
     * @return
     */
    public static Context parseContext(final String ctx) {
        final List<ContextComponent> out;
        final String trimmedCtx = ctx.trim();
        
        if (trimmedCtx.contains("+")) {
            final String[] choices = trimmedCtx.split("\\s*\\Q+\\E\\s*"); // Each choice represents a ContextComponent
            final List<Context> parsedChoices = new ArrayList<>();
            
            for (final String choice : choices)
                parsedChoices.add(parseSingle(choice));
            
            out = new ArrayList<>();
            out.add(new ChoiceContextComponent(parsedChoices));
            return new Context(out);
        }
            
        return parseSingle(trimmedCtx);
    }

    /**
     * Parses the components of the single context's string, creating a new context out of them. Used by the
     * {@link #parseContext(String) parseContext(ctx)} to process the different parts of a possible choice context
     * component.
     * @param ctx The string representing the left or right part of a choice (i.e. <i>+</i>).
     * @return A new context instance.
     * @throws IllegalArgumentException
     */
    private static Context parseSingle(final String ctx) throws IllegalArgumentException {
        final String[] unitComponents = ctx.split("\\Q.\\E");

        if (unitComponents.length == 0)
            throw new IllegalArgumentException("Found empty string instead of a context.");
        
        final List<ContextComponent> resultSequence = new ArrayList<>();
        
        try {
            // The split string can contain: nil, {vars}, CtxId, <N,CtxId>.
            for (final String unitComponent : unitComponents) {
                final String sanitizedComponent;
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
                    final List<Entity> entities = new ArrayList<>();
                    final String[] vars = sanitizedComponent.substring(1, sanitizedComponent.indexOf('}')).split(",");

                    for (final String var : vars)
                        if (!var.isEmpty()) entities.add(new Entity(var.trim()));

                    resultSequence.add(new EntitiesContextComponent(entities));
                } else if (sanitizedComponent.charAt(0) == '<') {
                    final String[] vars = sanitizedComponent.substring(1, sanitizedComponent.indexOf('>')).split(",");
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
     * to insert a repeated sequence translated by a RepeatedContextComponent.
     * @param position The position of the context to substitute.
     * @param ctx The context that will be inserted into the current's at the provided position.
     * @return A new context instance containing the substitution.
     * @throws IllegalArgumentException
    **/
    public Context getSubstitutedContext(final int position, final Context ctx) throws IllegalArgumentException {
        if (!(context.get(position) instanceof IdContextComponent) && 
            !(context.get(position) instanceof RepeatedContextComponent))
            throw new IllegalArgumentException("The provided context position does not refer neither" +
                    " to a substitutable variable nor to a repeated context");

        final List<ContextComponent> toSubstitute = ctx.getContext();

        if (BioResolve.OUT) {
            System.out.print("[Info] Substituting " + context.get(position) + " with ");
            int i = 0;
            final int len = toSubstitute.size() - 1;
            for (ContextComponent cc : toSubstitute)
                System.out.print(cc + (i++ < len ? "." : ""));
            System.out.println();
        }

        final List<ContextComponent> newContext = new ArrayList<>(toSubstitute);

        for (int i = position + 1; i < context.size(); ++i)
            newContext.add(context.get(i));

        return new Context(newContext);
    }

    public List<ContextComponent> getContext() {
        return context;
    }
    
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        
        for (int i = 0; i < context.size(); ++i)
            s.append(context.get(i)).append(i < context.size() - 1 ? "." : "");
        
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
