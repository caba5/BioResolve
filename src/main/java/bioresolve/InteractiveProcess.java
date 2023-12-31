package bioresolve;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is in charge of advancing the computation process.<br>
 * It performs the substitution of variables, the assignment of choices (spawning other {@link ProcessManager managers)},
 * and the computation of the current set, that is the union of the current component's context and the result of the
 * previous computations performed by its manager.
 * @author caba
 */
public class InteractiveProcess {
    private final Environment environment;
    private Context contextSequence; // C set
    private int contextSequenceIndex; // Index i of C
    
    private final List<Set<Entity>> resultSequence; // D set
    private int resultSequenceIndex; // Index i of D

    public boolean hasEnded;

    private int managerId;
    public boolean dirty;

    private String initiallySubstitutedFrom;
    private String stemsFrom;
    private final Context initialContext;

    /**
     * A utility function which instantiates as many instances of this class as the number of parallel contexts.
     * @param env The environment common to each process.
     * @param parallelContexts The list of parallel contexts.
     * @return A list of process instances.
     */
    public static List<InteractiveProcess> createParallelProcesses(final Environment env, final List<Context> parallelContexts) {
        List<InteractiveProcess> parallelProcesses = new ArrayList<>(parallelContexts.size());

        for (Context ctx : parallelContexts) {
            InteractiveProcess p = new InteractiveProcess(-1, env, ctx);
            parallelProcesses.add(p);
        }

        return parallelProcesses;
    }

    /**
     * @param managerId The unique id of the manager responsible for the computation of the results for this process and
     *                  its parallel counterparts.
     * @param env The environment common to each process.
     * @param contextSequence The context on which the process works on.
     */
    public InteractiveProcess(final int managerId, final Environment env, final Context contextSequence) {
        this.environment = env;
        this.contextSequence = contextSequence;
        this.initialContext = contextSequence;
        
        this.resultSequence = new ArrayList<>(contextSequence.getContext().size() + 1);
        this.resultSequence.add(new HashSet<>()); // D0 is empty

        this.managerId = managerId;
        this.initiallySubstitutedFrom = "";
    }

    /**
     * This constructor is used when creating a (quasi-deep) clone of a process.
    */
    private InteractiveProcess(
            final int managerId,
            final Environment env,
            final Context contextSequence,
            final int contextSequenceIndex,
            final List<Set<Entity>> resultSequence,
            final int resultSequenceIndex,
            final String initiallySubstitutedFrom,
            final String stemsFrom,
            final Context initialContext
    ) {
        this.managerId = managerId;
        this.environment = env;
        this.contextSequence = contextSequence;
        this.contextSequenceIndex = contextSequenceIndex;
        this.resultSequence = resultSequence;
        this.resultSequenceIndex = resultSequenceIndex;
        this.initiallySubstitutedFrom = initiallySubstitutedFrom;
        this.stemsFrom = stemsFrom;
        this.initialContext = initialContext;
    }

    /**
     * Pushes the merged results of the reaction computations of this process together with its parallel counterparts,
     * if present.
     * @param parallelResult
     */
    public void pushResult(final Set<Entity> parallelResult) {
        resultSequence.add(parallelResult); // This is Di+1 = Ci U Di
        ++resultSequenceIndex;

        dirty = false;
    }

    /**
     * Computes the process' next result, possibly substituting variables (if C<sub>i</sub> is an environment variable or
     * if it is a repeated context component) and creating new processes (if C<sub>i</sub> is a choice component).
     * @return The next result D<sub>i</sub>.
     */
    public Set<Entity> advanceStateSequence() {
        final List<ContextComponent> contextComponents = contextSequence.getContext();
        if (contextComponents.isEmpty() || contextSequenceIndex >= contextComponents.size())
            return null;

        final Set<Entity> wSet;
        
        final ContextComponent contextI = contextComponents.get(contextSequenceIndex);
        
        if (contextI instanceof ChoiceContextComponent choiceContextComponent) {
            final List<Context> choices = choiceContextComponent.getChoices();

            final ManagersCoordinator coordinator = ManagersCoordinator.getInstance();

            for (int i = 1; i < choices.size(); ++i) { // Creates a new manager containing the new process for each choice
                final InteractiveProcess choiceProcess = new InteractiveProcess(
                        coordinator.getNewManagerId(),
                        environment,
                        choices.get(i),
                        0,
                        new ArrayList<>(resultSequence),
                        resultSequenceIndex,
                        initiallySubstitutedFrom,
                        stemsFrom,
                        initialContext
                );
                coordinator.cloneManagerSubstitutingProcess(managerId, this, choiceProcess);
            }

            contextSequence = choices.get(0); // This process takes the first choice
            contextSequenceIndex = 0;
            
            return advanceStateSequence(); // Recursively destructure choices
        } else if (contextI instanceof IdContextComponent idContextComponent) {
            if (initiallySubstitutedFrom.isEmpty()) initiallySubstitutedFrom = idContextComponent.getId();
            final Context refContext = environment.getEnv().get(idContextComponent.getId()); // This is checked in the constructor

            stemsFrom = idContextComponent.getId();

            contextSequence = contextSequence.getSubstitutedContext(contextSequenceIndex, refContext);
            contextSequenceIndex = 0;
            
            return advanceStateSequence();
        } else if (contextI instanceof RepeatedContextComponent repeatedContextComponent) {
            final Context translatedContext = repeatedContextComponent.getRepeatedSequence();
            
            contextSequence.getSubstitutedContext(resultSequenceIndex, translatedContext);
            
            return advanceStateSequence();
        } else if (contextI instanceof EntitiesContextComponent entitiesContextComponent) {
            final List<Entity> contextEntities = entitiesContextComponent.getEntities();
            
            wSet = new HashSet<>() { { addAll(contextEntities); addAll(resultSequence.get(resultSequenceIndex)); } };

            ++contextSequenceIndex;
        } else { // NilContextComponent
            wSet = resultSequence.get(resultSequenceIndex);

            ++contextSequenceIndex;

            hasEnded = true;
        }

        dirty = true;
        return wSet;
    }

    /**
     * Performs a (quasi-)deep copy of the class as it is before starting the computation of the current result of the
     * sequence. This means that, in a situation in which a parallel composition of processes is present, if the instance
     * on which this method gets called has already advanced its state before all the parallel ones have done the same,
     * the copy will include its state preceding this advancement step.
     * @param callerManagerId The unique id of the manager which will be responsible for the new clone.
     * @return A new cloned process.
     */
    public InteractiveProcess clone(final int callerManagerId) {
        // Decrement the context sequence's index if it is dirty (it has advanced)
        final int contextSequenceIndex = dirty ? this.contextSequenceIndex - 1 : this.contextSequenceIndex;

        return new InteractiveProcess(
                callerManagerId,
                environment,
                contextSequence,
                contextSequenceIndex,
                new ArrayList<>(resultSequence),
                resultSequenceIndex,
                initiallySubstitutedFrom,
                stemsFrom,
                initialContext
        );
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Context getContextSequence() {
        return contextSequence;
    }

    public Integer getContextSequenceIndex() { return contextSequenceIndex; }

    /**
     * Returns the string of the last computed context.<br>
     * Mainly used for debugging purposes.
     * @return The string of the last computed context.
     */
    public String getLastContextAsString() {
        final ContextComponent contextI = contextSequence.getContext().get(contextSequenceIndex - 1);

        if (contextI instanceof EntitiesContextComponent entitiesContextComponent) {
            final List<Entity> contextEntities = entitiesContextComponent.getEntities();
            return Entity.stringifyEntitiesCollection(contextEntities);
        }

        return "";
    }

    /**
     * Returns the list of entities belonging to the second-last <b>seen</b> component of the context.
     * @return
     */
    public List<Entity> getLastContext() {
        final ContextComponent contextI = contextSequence.getContext().get(Math.max(contextSequenceIndex - 1, 0));

        // The only component having entities inside is EntitiesContextComponent
        if (contextI instanceof EntitiesContextComponent entitiesContextComponent)
            return entitiesContextComponent.getEntities();

        return new ArrayList<>();
    }

    public Context getInitialContext() {
        return initialContext;
    }

    /**
     * Computes the string representation of the context components which have not been considered yet.
     * @return The string representation of the remaining components.
     */
    public String getRemainingContextAsString() {
        final StringBuilder s = new StringBuilder();

        final List<ContextComponent> contextComponents = contextSequence.getContext();

        for (int i = contextSequenceIndex; i < contextComponents.size(); ++i)
            s.append(contextComponents.get(i))
                .append(i < contextComponents.size() - 1 ? "." : "");

        return s.toString();
    }

    public Set<Entity> getCurrentResult() {
        return resultSequence.get(resultSequenceIndex);
    }

    public List<Set<Entity>> getResultSequence() {
        return resultSequence;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public String getStemsFrom() {
        return stemsFrom;
    }

    @Override
    public String toString() {
        return contextSequence.toString();
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = result * 37 + contextSequence.hashCode();

        return result;
    }
}
