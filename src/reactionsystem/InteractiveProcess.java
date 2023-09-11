package reactionsystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author caba
 */
public class InteractiveProcess {
    private final Environment environment;
    private Context contextSequence;
    private int contextSequenceIndex;
    
    private final List<Set<Entity>> resultSequence;
    private int resultSequenceIndex;
    
    private final List<Set<Entity>> stateSequence;
    private int stateSequenceIndex;

    public boolean hasEnded;

    private int managerId;

    public static List<InteractiveProcess> createParallelProcesses(Environment env, List<Context> parallelContexts) {
        List<InteractiveProcess> parallelProcesses = new ArrayList<>(parallelContexts.size());

        for (Context ctx : parallelContexts)
            parallelProcesses.add(new InteractiveProcess(-1, env, ctx));

        return parallelProcesses;
    }
    
    public InteractiveProcess(int managerId, Environment env, Context contextSequence) { // TODO: check contexts first marking which are used in order to avoid throwing if env contains some unused var
        this.environment = env;
        this.contextSequence = contextSequence;
        
        this.resultSequence = new ArrayList<>(contextSequence.getContext().size() + 1);
        this.resultSequence.add(new HashSet<>()); // D0 is empty
        
        this.stateSequence = new ArrayList<>();

        this.managerId = managerId;
    }

    public void pushResult(Set<Entity> parallelResult) {
        stateSequence.add(parallelResult); // This is Wi = Ci U Di
        ++stateSequenceIndex;

        resultSequence.add(parallelResult);
        ++resultSequenceIndex;
    }
    
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
                InteractiveProcess choiceProcess = new InteractiveProcess(coordinator.getNewManagerId(), environment, choices.get(i));
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
            Context refContext = environment.getEnv().get(idContextComponent.getId()); // This is checked in the constructor

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
        
        return wSet;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Context getContextSequence() {
        return contextSequence;
    }

    public Integer getContextSequenceIndex() { return contextSequenceIndex; }

    /*
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
