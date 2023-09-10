package reactionsystem;

import java.util.*;

public class ProcessManager {
    private final ReactionSystem rs;
    private List<InteractiveProcess> parallelProcesses;

    private final Map<State, Set<Entity>> cachedResults;

    private final int managerId;

    public ProcessManager(int managerId, ReactionSystem rs, List<InteractiveProcess> parallelProcesses) throws IllegalArgumentException {
        if (parallelProcesses != null && parallelProcesses.isEmpty())
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
    }

    public ProcessManager(int managerId, ReactionSystem rs) {
        this.rs = rs;
        this.cachedResults = new HashMap<>();
        this.managerId = managerId;
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

        Set<Entity> cumulativeResult = cachedResults.get(new State(contextSequenceIndices, contextSequences));

//        State temp = new State(contextSequenceIndices, contextSequences);
//        for (Map.Entry<State, Set<Entity>> entry2 : cachedResults.entrySet()) {
//            System.out.println(temp.equals(entry2.getKey()) ? temp + " == " + entry2.getKey() : temp + " != " + entry2.getKey());
//            if (temp.equals(entry2.getKey())) {
//                System.out.println("\t\t\tInside");
//                Set<Entity> cccc = cachedResults.get(temp);
//                System.out.println("\t\t\t" + cccc);
//            }
//        }

        if (cumulativeResult != null) {
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

        cumulativeResult = rs.computeResults(mergedWSet);

        if (cumulativeResult.isEmpty())
            return false;

        if (BioResolve.DEBUG)
            System.out.println(getResultString(cumulativeResult));

        for (InteractiveProcess p : parallelProcesses)
            p.pushResult(cumulativeResult);

//        cacheResult(cumulativeResult);
        cachedResults.put(new State(contextSequenceIndices, contextSequences), cumulativeResult);

        return true;
    }

    private void cacheResult(Set<Entity> cumulativeResult) {
        List<Integer> contextSequenceIndices = parallelProcesses.stream().map(InteractiveProcess::getContextSequenceIndex).toList();
        List<Context> contextSequences = parallelProcesses.stream().map(InteractiveProcess::getContextSequence).toList();

        cachedResults.put(new State(contextSequenceIndices, contextSequences), cumulativeResult);
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

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Process manager containing: ");

        for (int i = 0; i < parallelProcesses.size(); ++i)
            s.append(parallelProcesses.get(i)).append(i < parallelProcesses.size() - 1 ? ", " : "");

        return s.toString();
    }
}
