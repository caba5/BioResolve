package reactionsystem;

import java.util.*;

import static java.util.Collections.addAll;

public class ProcessManager {
    private final ReactionSystem rs;
    private List<InteractiveProcess> parallelProcesses;

    /*
     Since the manager needs to be uniquely identified by its parallelProcesses field but the field itself is subject
     to change due to choices between contexts (+), 'managerCode' stores the manager's original identity, allowing it
     to be cached by the coordinator.
    */
    private final int managerCode;

    private final Map<State, Set<Entity>> cachedResults;

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
        this.cachedResults = new HashMap<>();
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

    public class NodePair {
        private final Set<Entity> from;
        private final Set<Entity> to;
        private final Set<Entity> arc;

        private NodePair(Set<Entity> from, Set<Entity> to, Set<Entity> arc) {
            this.from = from;
            this.to = to;
            this.arc = arc;
        }

        public Set<Entity> getFrom() {
            return from;
        }

        public Set<Entity> getTo() {
            return to;
        }

        public Set<Entity> getArc() {
            return arc;
        };
    }

    public class State {
        private final List<Integer> contextSequenceIndices;
        private final List<Context> contextSequences;
        private final Set<Entity> result;

        private State(List<Integer> contextSequenceIndices, List<Context> contextSequences, Set<Entity> result) {
            this.contextSequenceIndices = contextSequenceIndices;
            this.contextSequences = contextSequences;
            this.result = result; // result won't be null as this constructor's call is made after its explicit checking
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (this.getClass() != o.getClass()) return false;

            State s = (State) o;
            return contextSequences.equals(s.contextSequences)
                    && contextSequenceIndices.equals(s.contextSequenceIndices)
                    && result.equals(s.result);
        }

        @Override
        public int hashCode() {
            int result = 17;

            for (int idx : contextSequenceIndices)
                result = result * 37 + idx;

            for (Context ctx : contextSequences)
                result = result * 37 + ctx.hashCode();

            result = result * 37 + this.result.hashCode();

            return result;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder("{ Indices: [");

            for (int i = 0; i < contextSequenceIndices.size(); ++i)
                s.append(contextSequenceIndices.get(i)).append(i < contextSequenceIndices.size() - 1 ? ", " : "");

            s.append("], Contexts: [");

            for (int i = 0; i < contextSequences.size(); ++i)
                s.append(contextSequences.get(i)).append(i < contextSequences.size() - 1 ? ", " : "");

            s.append("] }");

            return s.toString();
        }
    }

    private boolean compute() {
        Set<Entity> mergedWSet = new HashSet<>();

        int endedProcessesNumber = 0; // Keep track of the number of processes which have reached 'nil'

        for (InteractiveProcess p : parallelProcesses) {
            Set<Entity> processResult = p.advanceStateSequence();

            if (p.hasEnded) ++endedProcessesNumber;
            else if (processResult != null) mergedWSet.addAll(processResult);
        }

        if (endedProcessesNumber == parallelProcesses.size()) // Return if all the processes have reached their last point
            return false;

        Set<Entity> cumulativeResult = rs.computeResults(mergedWSet); // cumulativeResult = Wi

        if (cumulativeResult.isEmpty()) return false;

        if (BioResolve.DEBUG) System.out.println(getResultString(cumulativeResult)); // TODO: pass the below results instead of recomputing

        Set<Entity> from = new HashSet<>(); // Di
        Set<Entity> arc = new HashSet<>(); // Ci U Di

        for (int i = 0; i < parallelProcesses.size(); ++i) {
            from.addAll(parallelProcesses.get(i).getCurrentResult());
            arc.addAll(parallelProcesses.get(i).getLastContext());
            parallelProcesses.get(i).pushResult(cumulativeResult);
        }

        arc.addAll(from);

        processGraph.add(new NodePair(from, cumulativeResult, arc));

        // Get each process' 'i'
        List<Integer> contextSequenceIndices = parallelProcesses.stream().map(InteractiveProcess::getContextSequenceIndex).toList();
        // Get each process' 'C'
        List<Context> contextSequences = parallelProcesses.stream().map(InteractiveProcess::getContextSequence).toList();

        State currentProcessesState = new State(contextSequenceIndices, contextSequences, cumulativeResult); // TODO: would it be correct if caching just the result? Think about getting to the same Wi but having different Ci and Di (probably not the same)

        if (ManagersCoordinator.getInstance().getCachedManagers().contains(currentProcessesState)) { // !!!!!!!!!!!!!!TODO: for some reason it doesn't compute the small iOP to itself nor from initial to small iOP
            if (BioResolve.DEBUG) System.out.println("[Warning] All results have already been computed. Stopping.");
            return false;
        }

        ManagersCoordinator.getInstance().getCachedManagers().add(currentProcessesState); // TODO: useless to be a map: the key already contains the value

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

    public Map<State, Set<Entity>> getCachedResults() {
        return cachedResults;
    }

    public int getManagerCode() {
        return managerCode;
    }

    public List<NodePair> getProcessGraph() {
        return processGraph;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Process manager containing: ");

        for (int i = 0; i < parallelProcesses.size(); ++i)
            s.append(parallelProcesses.get(i)).append(i < parallelProcesses.size() - 1 ? ", " : "");

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
