package reactionsystem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class ManagersCoordinator {
    private static ReactionSystem rs = null;

    private static ManagersCoordinator instance = null;

    private final List<ProcessManager> managers;

    private int managerId;

    private final Set<NodePair> cachedManagers;

    private ManagersCoordinator() {
        this.managers = new ArrayList<>();
        this.managerId = 0;
        this.cachedManagers = new HashSet<>();
    }

    public void spawnManager(final List<InteractiveProcess> processes) {
        final ProcessManager newManager = new ProcessManager(managerId, rs, processes, false);

        managers.add(newManager);
        ++managerId;

        if (BioResolve.DEBUG) System.out.println("[Info] Spawned a new process manager with id " + (managerId - 1));

    }

    public void cloneManagerSubstitutingProcess(
            final int sourceManagerId,
            final InteractiveProcess toDiscard,
            final InteractiveProcess toAdd
    ) throws IllegalArgumentException {
        if (sourceManagerId >= managerId ||  sourceManagerId < 0)
            throw new IllegalArgumentException("No such manager.");
        if (toAdd == null)
            throw new IllegalArgumentException("Cannot push a null InteractiveProcess");

        final ProcessManager sourceManager = managers.get(sourceManagerId);
        final List<InteractiveProcess> sourceProcesses = sourceManager.getParallelProcesses();

        final List<InteractiveProcess> filteredProcesses = new ArrayList<>(sourceProcesses.size());
        for (final InteractiveProcess p : sourceProcesses)
            if (!p.equals(toDiscard)) filteredProcesses.add(p.clone(managerId));

        filteredProcesses.add(toAdd);

        spawnManager(filteredProcesses);
    }

    public Duration compute() {
        final String sep = " ------------------------------------------- ";
        int i = 0;

        Instant begin = Instant.now();
        while (i < managers.size()) {
            if (BioResolve.DEBUG) System.out.println(sep + "Running manager " + i + sep);
            managers.get(i).run();
            if (BioResolve.DEBUG) System.out.println(sep + "Ending manager " + i + sep);

            ++i;
        }
        Instant end = Instant.now();
        if (BioResolve.DEBUG) System.out.println("All managers finished their jobs.");

        generateDOTGraph();

        return Duration.between(begin, end);
    }

    public void generateDOTGraph() {
        final String GRAPHFILENAME = "result.dot";

        final StringBuilder graph = new StringBuilder("digraph G { node [shape=box] edge [arrowhead=vee] ");

        final Set<String> nodes = new HashSet<>();
        final Set<String> arcs = new HashSet<>();

        for (final ProcessManager p : managers) {
            final List<NodePair> pGraph = p.getProcessGraph();
            for (final NodePair node : pGraph) {
                final String from = "\"" + Entity.stringifyEntitiesCollection(node.from()) + node.fromContext() + "\"";
                final String to = "\"" + Entity.stringifyEntitiesCollection(node.to()) + node.toContext() + "\"";
                final String arc = from + " -> " + to + " [label = \"" + Entity.stringifyEntitiesCollection(node.arc()) + "\"]";

                nodes.add(from);
                nodes.add(to);
                arcs.add(arc);
            }
        }

        for (final String node : nodes)
            graph.append(node).append(";\t");

        for (final String arc : arcs)
            graph.append(arc).append(";\t");

        graph.append("}");

        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(GRAPHFILENAME));
            writer.write(graph.toString());
            writer.close();
        } catch (IOException e) {
            System.err.println("Could not write the .dot graph to file. " + e);
        }
    }

    public ProcessManager getLastManager() {
        if (managerId > 0)
            return managers.get(managerId - 1);

        return null;
    }

    public int getNewManagerId() {
        return managerId;   // managerId tracks the Id of the next manager that will be created
    }

    public Set<NodePair> getCachedManagers() {
        return cachedManagers;
    }

    public static void setRS(final ReactionSystem rs) {
        ManagersCoordinator.rs = rs;
    }

    public void resetCoordinator() {
        managers.clear();
        cachedManagers.clear();
        managerId = 0;
    }

    public static ManagersCoordinator getInstance() throws IllegalStateException {
        if (ManagersCoordinator.instance != null)
            return ManagersCoordinator.instance;
        else if (ManagersCoordinator.rs != null) {
            ManagersCoordinator.instance = new ManagersCoordinator();
            return ManagersCoordinator.instance;
        } else
            throw new IllegalStateException("No reaction system has been assigned prior to building the coordinator's instance.");
    }
}
