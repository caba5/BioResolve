package reactionsystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author caba
 */
public class InteractiveProcess {
    private final Environment environment;
    private Context contextSequence; // C set
    private int contextSequenceIndex; // Index i of C
    
    private final List<Set<Entity>> resultSequence; // D set
    private int resultSequenceIndex; // Index i of D
    
    private final List<Set<Entity>> stateSequence; // W set
    private int stateSequenceIndex; // Index i of W

    public boolean hasEnded;

    private int managerId;
    public boolean dirty;

    private String initiallySubstitutedFrom;
    private String stemsFrom;
    private final Context initialContext;

    public static List<InteractiveProcess> createParallelProcesses(Environment env, List<Context> parallelContexts) {
        List<InteractiveProcess> parallelProcesses = new ArrayList<>(parallelContexts.size());

        for (Context ctx : parallelContexts) {
            InteractiveProcess p = new InteractiveProcess(-1, env, ctx);
            parallelProcesses.add(p);
        }

        return parallelProcesses;
    }
    
    public InteractiveProcess(int managerId, Environment env, Context contextSequence) { // TODO: check contexts first marking which are used in order to avoid throwing if env contains some unused var
        this.environment = env;
        this.contextSequence = contextSequence;
        this.initialContext = contextSequence;
        
        this.resultSequence = new ArrayList<>(contextSequence.getContext().size() + 1);
        this.resultSequence.add(new HashSet<>()); // D0 is empty
        
        this.stateSequence = new ArrayList<>();

        this.managerId = managerId;
        this.initiallySubstitutedFrom = "";
    }

    // This constructor is used when creating a (quasi-deep) clone of a process
    private InteractiveProcess(
            int managerId,
            Environment env,
            Context contextSequence,
            int contextSequenceIndex,
            List<Set<Entity>> stateSequence,
            int stateSequenceIndex,
            List<Set<Entity>> resultSequence,
            int resultSequenceIndex,
            String initiallySubstitutedFrom,
            String stemsFrom,
            Context initialContext
    ) {
        this.managerId = managerId;
        this.environment = env;
        this.contextSequence = contextSequence;
        this.contextSequenceIndex = contextSequenceIndex;
        this.stateSequence = stateSequence;
        this.stateSequenceIndex = stateSequenceIndex;
        this.resultSequence = resultSequence;
        this.resultSequenceIndex = resultSequenceIndex;
        this.initiallySubstitutedFrom = initiallySubstitutedFrom;
        this.stemsFrom = stemsFrom;
        this.initialContext = initialContext;
    }

    public void pushResult(Set<Entity> parallelResult) {
        stateSequence.add(parallelResult); // This is Wi = Ci U Di
        ++stateSequenceIndex;

        resultSequence.add(parallelResult); // This is Di+1 = Ci U Di
        ++resultSequenceIndex;

        dirty = false;
    }

    /**
     * Computes the process' next result, possibly substituting variables (if C<sub>i</sub> is an environment variable or
     * if it is a repeated context component) and creating new processes (if C<sub>i</sub> is a choice component).
     *
     * @return The next result D<sub>i</sub>.
     */
    public Set<Entity> advanceStateSequence() {
        List<ContextComponent> contextComponents = contextSequence.getContext();
        if (contextComponents.isEmpty() || contextSequenceIndex >= contextComponents.size())
            return null;

        Set<Entity> wSet;
        
        ContextComponent contextI = contextComponents.get(contextSequenceIndex);
        
        if (contextI instanceof ChoiceContextComponent choiceContextComponent) {
            List<Context> choices = choiceContextComponent.getChoices();

            ManagersCoordinator coordinator = ManagersCoordinator.getInstance();

            for (int i = 1; i < choices.size(); ++i) { // Creates a new manager containing the new process for each choice
                InteractiveProcess choiceProcess = new InteractiveProcess(coordinator.getNewManagerId(), environment, choices.get(i), 0, stateSequence, stateSequenceIndex, new ArrayList<>(resultSequence), resultSequenceIndex, initiallySubstitutedFrom, stemsFrom, initialContext);
                coordinator.cloneManagerSubstitutingProcess(managerId, this, choiceProcess); // TODO: split function into spawn(), substitute(), and addManager() (might merge the first two for efficiency reasons)
            }

//            Context chosenContext = choices.get(ThreadLocalRandom.current().nextInt(0, choices.size())); // TODO: add modular choice strategy
//
//            if (BioResolve.DEBUG) System.out.println("[Info] Context " + chosenContext + " has been chosen");
//
//            contextSequence = chosenContext; // The chosen context will become the new working context // TODO: change if the strategy changes

            contextSequence = choices.get(0); // This process takes the first choice
            contextSequenceIndex = 0;
            
            return advanceStateSequence(); // Recursively destructure choices
        } else if (contextI instanceof IdContextComponent idContextComponent) {
            if (initiallySubstitutedFrom.isEmpty()) initiallySubstitutedFrom = idContextComponent.getId();
            Context refContext = environment.getEnv().get(idContextComponent.getId()); // This is checked in the constructor

            stemsFrom = idContextComponent.getId();

            contextSequence = contextSequence.getSubstitutedContext(contextSequenceIndex, refContext);
            contextSequenceIndex = 0;
            
            return advanceStateSequence();
        } else if (contextI instanceof RepeatedContextComponent repeatedContextComponent) {
            Context translatedContext = repeatedContextComponent.getRepeatedSequence();
            
            contextSequence.getSubstitutedContext(stateSequenceIndex, translatedContext);
            
            return advanceStateSequence();
        } else if (contextI instanceof EntitiesContextComponent entitiesContextComponent) {
            List<Entity> contextEntities = entitiesContextComponent.getEntities();
            
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

    public InteractiveProcess clone(int callerManagerId) {
        if (!dirty) return new InteractiveProcess(
                callerManagerId,
                environment,
                contextSequence,
                contextSequenceIndex,
                stateSequence,
                stateSequenceIndex,
                new ArrayList<>(resultSequence),
                resultSequenceIndex,
                initiallySubstitutedFrom,
                stemsFrom,
                initialContext
        );

        return new InteractiveProcess(
                callerManagerId,
                environment,
                contextSequence,
                contextSequenceIndex - 1, // Since the process is dirty, it has advanced its index
                stateSequence,
                stateSequenceIndex,
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

    public String getInitiallySubstitutedFrom() { return initiallySubstitutedFrom; }

    /**
        Returns the string of the last computed context.
     */
    public String getLastContextAsString() {
        ContextComponent contextI = contextSequence.getContext().get(contextSequenceIndex - 1);

        if (contextI instanceof EntitiesContextComponent entitiesContextComponent) {
            List<Entity> contextEntities = entitiesContextComponent.getEntities();
            return Entity.stringifyEntitiesCollection(contextEntities);
        }

        return "";
    }

    public List<Entity> getLastContext() {
        ContextComponent contextI = contextSequence.getContext().get(Math.max(contextSequenceIndex - 1, 0));

        if (contextI instanceof  EntitiesContextComponent entitiesContextComponent)
            return entitiesContextComponent.getEntities();

        return new ArrayList<>();
    }

    public Context getInitialContext() {
        return initialContext;
    }

    public String getRemainingContextAsString() {
        StringBuilder s = new StringBuilder();

        List<ContextComponent> contextComponents = contextSequence.getContext();

        for (int i = contextSequenceIndex; i < contextComponents.size(); ++i)
            s.append(contextComponents.get(i))
                .append(i < contextComponents.size() - 1 ? "." : "");

        return s.toString();
    }

    public String getCurrentResultAsString() {
        Set<Entity> currentResult = resultSequence.get(resultSequenceIndex);

        return Entity.stringifyEntitiesCollection(currentResult);
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

    private void setContextSequenceIndex(int i) {
        contextSequenceIndex = i;
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
