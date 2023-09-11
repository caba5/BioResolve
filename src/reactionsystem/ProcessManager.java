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

    private final Map<State, Set<Entity>> cachedResults;

    private List<List<SequencePair>> processGraph;

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
        this.managerCode = this.hashCode(); // TODO: ugly
        this.processGraph = new ArrayList<>(parallelProcesses.size());
        for (int i = 0; i < parallelProcesses.size(); ++i) this.processGraph.set(i, new ArrayList<>());
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

    public class SequencePair {
        private final Set<Entity> result;
        private final Set<Entity> products;

        private SequencePair(Set<Entity> result, Set<Entity> products) {
            this.result = result;
            this.products = products;
        }
    }

    private class State {
        private final List<Integer> contextSequenceIndices;
        private final List<Context> contextSequences;

        private State(List<Integer> contextSequenceIndices, List<Context> contextSequences) {
            this.contextSequenceIndices = contextSequenceIndices;
            this.contextSequences = contextSequences;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (this.getClass() != o.getClass()) return false;

            State s = (State) o;
            return contextSequences.equals(s.contextSequences)
                    && contextSequenceIndices.equals(s.contextSequenceIndices);
        }

        @Override
        public int hashCode() {
            int result = 17;

            for (int idx : contextSequenceIndices)
                result = result * 37 + idx;

            for (Context ctx : contextSequences)
                result = result * 37 + ctx.hashCode();

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
        List<Integer> contextSequenceIndices = parallelProcesses.stream().map(InteractiveProcess::getContextSequenceIndex).toList();
        List<Context> contextSequences = parallelProcesses.stream().map(InteractiveProcess::getContextSequence).toList();

        if (cachedResults.containsKey(new State(contextSequenceIndices, contextSequences))) {
            if (BioResolve.DEBUG)
                System.out.println("[Warning] All results have already been computed. Stopping.");
            return false;
        }

        Set<Entity> mergedWSet = new HashSet<>();

        int endedProcessesNumber = 0;

        for (InteractiveProcess p : parallelProcesses) {
            Set<Entity> processResult = p.advanceStateSequence();

            if (p.hasEnded)
                ++endedProcessesNumber;
            else if (processResult != null)
                mergedWSet.addAll(processResult);
        }

        if (endedProcessesNumber == parallelProcesses.size())
            return false;

        Set<Entity> cumulativeResult = rs.computeResults(mergedWSet);

        if (cumulativeResult.isEmpty())
            return false;

        if (BioResolve.DEBUG)
            System.out.println(getResultString(cumulativeResult));

        for (int i = 0; i < parallelProcesses.size(); ++i) {
            parallelProcesses.get(i).pushResult(cumulativeResult);
            processGraph.get(i).add(new SequencePair(parallelProcesses.get(i).getCurrentResult(), cumulativeResult));
        }

        cachedResults.put(new State(contextSequenceIndices, contextSequences), cumulativeResult);

        return true;
    }

    private String getResultString(Set<Entity> cumulativeResult) {
        StringBuilder res = new StringBuilder("Ci = {");

        int i = 0;
        final int len = parallelProcesses.size() - 1;

        for (InteractiveProcess p : parallelProcesses) {
            String ctx = p.getLastContextAsString();
            res.append(ctx);
            if (i++ < len) res.append(",");
        }

        res.append("}, Di = {");

        Set<Entity> cumulativeDSet = new HashSet<>();

        for (InteractiveProcess p : parallelProcesses)
            cumulativeDSet.addAll(p.getCurrentResult());

        res.append(Entity.stringifyEntitiesCollection(cumulativeDSet));

        res.append("}, Wi = {");

        res.append(Entity.stringifyEntitiesCollection(cumulativeResult));

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

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Process manager containing: ");

        for (int i = 0; i < parallelProcesses.size(); ++i)
            s.append(parallelProcesses.get(i)).append(i < parallelProcesses.size() - 1 ? ", " : "");

        return s.toString();
    }

    @Override
    public int hashCode() {
        int result = 17;

        for (InteractiveProcess p : parallelProcesses)
            result = result * 37 + p.hashCode();

        return result;
    }
}
