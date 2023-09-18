package reactionsystem;

import java.util.*;

public class ProcessManager {
    private final ReactionSystem rs;
    private List<InteractiveProcess> parallelProcesses;

    /*
     Since the manager needs to be uniquely identified by its parallelProcesses field but the field itself is subject
     to change due to choices between contexts (+), 'managerCode' stores the manager's original identity, allowing it
     to be cached by the coordinator.
    */
    private final int managerCode;

    private List<NodePair> processGraph;

    private final int managerId;

    public ProcessManager(int managerId, ReactionSystem rs, List<InteractiveProcess> parallelProcesses) throws IllegalArgumentException {
        if (parallelProcesses == null || parallelProcesses.isEmpty())
            throw new IllegalArgumentException("The provided list of processes is empty.");

        // All the processes share the same environment, hence taking the first is enough
        Environment env = parallelProcesses.get(0).getEnvironment();
        Collection<Context> declaredContexts = env.getEnv().values();

        // Sanity checks
        for (Context ctx : declaredContexts)
            checkEntitiesBelongToRS(rs, ctx); // Checks the environment
        for (InteractiveProcess p : parallelProcesses)
            checkEntitiesBelongToRS(rs, p.getContextSequence()); // Checks each process' context sequence

        this.rs = rs;
        this.parallelProcesses = parallelProcesses;
        this.managerId = managerId;
        this.managerCode = this.hashCode();                                                                 // TODO: ugly
        this.processGraph = new ArrayList<>();
    }

    private void checkEntitiesBelongToRS(ReactionSystem rs, Context context) throws IllegalArgumentException {
        Set<Entity> entities = rs.getEntities();
//        System.out.println("RS entities");
//        for (Entity e : entities)
//            System.out.println(e.symbol());

        List<ContextComponent> relativeContext = context.getContext();
        for (ContextComponent comp : relativeContext) {
            List<Entity> entitySequence = comp.getEntitiesSequence();
//            System.out.println("Sequence entities");
//            for (Entity e : entitySequence)
//                System.out.println(e.symbol());

            for (Entity e : entitySequence)
                if (!entities.contains(e))
                    throw new IllegalArgumentException("The context contains some entity which does not belong to the provided reaction system.");
        }
    }

    public void run() {
        while (compute()) {}
    }

    private boolean compute() {
        Set<Entity> mergedWSet = new HashSet<>();

        int endedProcessesNumber = 0; // Keep track of the number of processes which have reached 'nil'

        StringBuilder fromContext = new StringBuilder();

        for (InteractiveProcess p : parallelProcesses) {
            fromContext.append(" | ").append(p.getRemainingContextAsString());
            Set<Entity> processResult = p.advanceStateSequence();

            if (p.hasEnded) ++endedProcessesNumber;
            else if (processResult != null) mergedWSet.addAll(processResult);
        }
        if (endedProcessesNumber == parallelProcesses.size()) // Return if all the processes have reached their last point
            return false;

        Set<Entity> cumulativeResult = rs.computeResults(mergedWSet); // cumulativeResult = Wi

        if (BioResolve.DEBUG) System.out.println(getResultString(cumulativeResult)); // TODO: pass the below results instead of recomputing

        Set<Entity> from = new HashSet<>(); // Di
        Set<Entity> arc = new HashSet<>(); // Ci U Di
        StringBuilder toContext = new StringBuilder();

        for (InteractiveProcess parallelProcess : parallelProcesses) {
            from.addAll(parallelProcess.getCurrentResult());
            arc.addAll(parallelProcess.getLastContext());
            parallelProcess.pushResult(cumulativeResult);
            toContext.append(" | ").append(parallelProcess.getRemainingContextAsString());
        }

        arc.addAll(from);

        if (parallelProcesses.get(0).getResultSequence().size() == 2) { // This corresponds to being the initial node // TODO: parallel?
            from.add(new Entity("-"));

            // Get the environment shared by all the processes
            List<String> environmentVars = new ArrayList<>(parallelProcesses.get(0).getEnvironment().getEnv().keySet());
            fromContext = new StringBuilder();
            for (InteractiveProcess p : parallelProcesses)
                // Since there are as many parallel processes as there are variables in the environment, it is ok to use the same index
                fromContext.append(" | ").append(p.getInitialContext());
        } else if (parallelProcesses.get(0).getContextSequenceIndex() == 1 && !parallelProcesses.get(0).getStemsFrom().isEmpty()) { // TODO: parallel?
            fromContext = new StringBuilder();
            for (InteractiveProcess p : parallelProcesses)
                fromContext.append(" | ").append(p.getStemsFrom());
        }

        NodePair node = new NodePair(from, fromContext.toString(), cumulativeResult, toContext.toString(), arc);

        System.out.println("\t\t\t\tThis manager's node: " + node);

        processGraph.add(node);

        if (cumulativeResult.isEmpty()) return false;

        if (ManagersCoordinator.getInstance().getCachedManagers().contains(node)) {
            if (BioResolve.DEBUG) System.out.println("[Warning] All results have already been computed. Stopping.");
            return false;
        }

        ManagersCoordinator.getInstance().getCachedManagers().add(node);

        return true;
    }

    private String getResultString(Set<Entity> cumulativeResult) {
        StringBuilder res = new StringBuilder("(Ci = {");

        int i = 0;
        final int len = parallelProcesses.size() - 1;

        for (InteractiveProcess p : parallelProcesses) {
            String ctx = p.getLastContextAsString();
            res.append(ctx);
            if (i++ < len) res.append(",");
        }

        res.append("} U Di = {");

        Set<Entity> cumulativeDSet = new HashSet<>();

        for (InteractiveProcess p : parallelProcesses)
            cumulativeDSet.addAll(p.getCurrentResult());

        res.append(Entity.stringifyEntitiesCollection(cumulativeDSet));

        res.append("}) = {");

        Set<Entity> fromEntities = cumulativeDSet;

        for (InteractiveProcess p : parallelProcesses)
            fromEntities.addAll(p.getLastContext());

        res.append(Entity.stringifyEntitiesCollection(fromEntities));

        res.append("} ---> {"); // TODO: is this Wi by def?

        res.append(Entity.stringifyEntitiesCollection(cumulativeResult)); // TODO: might not be correct due to definition

        res.append("}");

        return res.toString();
    }

    public void bindManagerToProcesses() {
        for (InteractiveProcess p : parallelProcesses)
            p.setManagerId(managerId);
    }

    public List<InteractiveProcess> getParallelProcesses() {
        return parallelProcesses;
    }

    public void setParallelProcesses(List<InteractiveProcess> processes) throws IllegalArgumentException {
        if (processes.isEmpty())
            throw new IllegalArgumentException("The provided list of processes is empty.");

        parallelProcesses = processes;
    }

    public int getManagerCode() {
        return managerCode;
    }

    public List<NodePair> getProcessGraph() {
        return processGraph;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Process manager with state: ");

        Set<Entity> cumRes = new HashSet<>();
        Set<Entity> cumCtx = new HashSet<>();

        for (InteractiveProcess p : parallelProcesses) {
            cumRes.addAll(p.getCurrentResult());
            cumCtx.addAll(p.getLastContext());
        }

        s.append("{").append(Entity.stringifyEntitiesCollection(cumRes)).append("}");

        s.append(", and current merged context: ");

        s.append("{").append(Entity.stringifyEntitiesCollection(cumCtx)).append("}");

        return s.toString();
    }

    @Override
    public int hashCode() { // TODO: wrong, this hashes always the same since an InteractiveProcess will always hash the same
        int result = 17;

        for (InteractiveProcess p : parallelProcesses)
            result = result * 37 + p.hashCode();

        return result;
    }
}
