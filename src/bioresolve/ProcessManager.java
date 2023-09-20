package bioresolve;

import java.util.*;

public class ProcessManager {
    private final ReactionSystem rs;
    private final List<InteractiveProcess> parallelProcesses;

    private final List<NodePair> processGraph;

    private final int managerId;

    public ProcessManager(
            final int managerId,
            final ReactionSystem rs,
            final List<InteractiveProcess> parallelProcesses,
            final boolean check
    ) throws IllegalArgumentException {
        if (parallelProcesses == null || parallelProcesses.isEmpty())
            throw new IllegalArgumentException("The provided list of processes is empty.");

        // All the processes share the same environment, hence taking the first is enough
        final Environment env = parallelProcesses.get(0).getEnvironment();
        final Collection<Context> declaredContexts = env.getEnv().values();

        // Sanity checks
        if (check) {
            for (final Context ctx : declaredContexts)
                checkEntitiesBelongToRS(rs, ctx); // Checks the environment
            for (final InteractiveProcess p : parallelProcesses)
                checkEntitiesBelongToRS(rs, p.getContextSequence()); // Checks each process' context sequence
        }

        this.rs = rs;
        this.parallelProcesses = parallelProcesses;
        this.managerId = managerId;
        this.processGraph = new ArrayList<>();
    }

    private void checkEntitiesBelongToRS(final ReactionSystem rs, final Context context) throws IllegalArgumentException {
        final Set<Entity> entities = rs.getEntities();

        final List<ContextComponent> relativeContext = context.getContext();
        for (ContextComponent comp : relativeContext) {
            final List<Entity> entitySequence = comp.getEntitiesSequence();

            for (final Entity e : entitySequence)
                if (!entities.contains(e))
                    throw new IllegalArgumentException("The context contains some entity which does not belong to the provided reaction system.");
        }
    }

    public void run() {
        while (compute()) {}
    }

    private boolean compute() {
        final Set<Entity> mergedWSet = new HashSet<>();

        int endedProcessesNumber = 0; // Keep track of the number of processes which have reached 'nil'

        StringBuilder fromContext = new StringBuilder();

        for (final InteractiveProcess p : parallelProcesses) {
            fromContext.append(" | ").append(p.getRemainingContextAsString());

            final Set<Entity> processResult = p.advanceStateSequence();

            if (p.hasEnded) ++endedProcessesNumber;
            else if (processResult != null) mergedWSet.addAll(processResult);
        }
        if (endedProcessesNumber == parallelProcesses.size()) // Return if all the processes have reached their last point
            return false;

        final Set<Entity> cumulativeResult = rs.computeResults(mergedWSet); // cumulativeResult = Wi

        if (BioResolve.DEBUG) System.out.println(getResultString(cumulativeResult));

        final Set<Entity> from = new HashSet<>(); // Di
        final Set<Entity> arc = new HashSet<>(); // Ci U Di
        final StringBuilder toContext = new StringBuilder();

        for (final InteractiveProcess parallelProcess : parallelProcesses) {
            from.addAll(parallelProcess.getCurrentResult());
            arc.addAll(parallelProcess.getLastContext());

            parallelProcess.pushResult(cumulativeResult);

            toContext.append(" | ").append(parallelProcess.getRemainingContextAsString());
        }

        arc.addAll(from);

        if (parallelProcesses.get(0).getResultSequence().size() == 2) { // This corresponds to being the initial node // TODO: parallel?
            from.add(new Entity("-"));

            fromContext = new StringBuilder();
            for (final InteractiveProcess p : parallelProcesses)
                // Since there are as many parallel processes as there are variables in the environment, it is ok to use the same index
                fromContext.append(" | ").append(p.getInitialContext());
        } else if (parallelProcesses.get(0).getContextSequenceIndex() == 1 && !parallelProcesses.get(0).getStemsFrom().isEmpty()) { // TODO: parallel?
            fromContext = new StringBuilder();
            for (final InteractiveProcess p : parallelProcesses)
                fromContext.append(" | ").append(p.getStemsFrom());
        }

        final NodePair node = new NodePair(from, fromContext.toString(), cumulativeResult, toContext.toString(), arc);

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
        final StringBuilder res = new StringBuilder("(Ci = {");

        int i = 0;
        final int len = parallelProcesses.size() - 1;

        for (final InteractiveProcess p : parallelProcesses) {
            final String ctx = p.getLastContextAsString();
            res.append(ctx);
            if (i++ < len) res.append(",");
        }

        res.append("} U Di = {");

        final Set<Entity> cumulativeDSet = new HashSet<>();

        for (final InteractiveProcess p : parallelProcesses)
            cumulativeDSet.addAll(p.getCurrentResult());

        res.append(Entity.stringifyEntitiesCollection(cumulativeDSet));

        res.append("}) = {");

        final Set<Entity> fromEntities = cumulativeDSet;

        for (final InteractiveProcess p : parallelProcesses)
            fromEntities.addAll(p.getLastContext());

        res.append(Entity.stringifyEntitiesCollection(fromEntities));

        res.append("} ---> {");

        res.append(Entity.stringifyEntitiesCollection(cumulativeResult));

        res.append("}");

        return res.toString();
    }

    public void bindManagerToProcesses() {
        for (final InteractiveProcess p : parallelProcesses)
            p.setManagerId(managerId);
    }

    public List<InteractiveProcess> getParallelProcesses() {
        return parallelProcesses;
    }

    public List<NodePair> getProcessGraph() {
        return processGraph;
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("Process manager with state: ");

        final Set<Entity> cumRes = new HashSet<>();
        final Set<Entity> cumCtx = new HashSet<>();

        for (final InteractiveProcess p : parallelProcesses) {
            cumRes.addAll(p.getCurrentResult());
            cumCtx.addAll(p.getLastContext());
        }

        s.append("{").append(Entity.stringifyEntitiesCollection(cumRes)).append("}");

        s.append(", and current merged context: ");

        s.append("{").append(Entity.stringifyEntitiesCollection(cumCtx)).append("}");

        return s.toString();
    }
}
