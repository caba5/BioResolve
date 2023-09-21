package bioresolve;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * This class defines a global unique managers coordinator, which is in charge of cloning them, executing them, and
 * generating the final DOT graph file.
 */
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

    /**
     * Creates a new process manager managing a list of parallel {@link InteractiveProcess processes}.
     * @param processes The list of parallel {@link InteractiveProcess processes}.
     */
    public void spawnManager(final List<InteractiveProcess> processes) {
        final ProcessManager newManager = new ProcessManager(managerId, rs, processes, false);

        managers.add(newManager);
        ++managerId;

        if (BioResolve.DEBUG) System.out.println("[Info] Spawned a new process manager with id " + (managerId - 1));

    }

    /**
     * Clones a manager with the specified id, substituting a given process which it manages with another new process.
     * @param sourceManagerId The unique id of the manager to clone.
     * @param toDiscard The process to discard.
     * @param toAdd The new process to attach.
     * @throws IllegalArgumentException When the manager's id is invalid or when the process to be added is null.
     */
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

    /**
     * Executes each one of the managers. Since processes can create new managers by cloning (when a choice component is
     * found), these are appended to the end of the managers list and their computation will eventually be reached.
     * @return The duration of the whole computation.
     */
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

    /**
     * Generates a DOT graph file in the current directory with a default name of (<i>result.dot</i>).
     */
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

    /**
     * Returns the id of the last manager in the managers list.
     * @return The id of the last manager.
     */
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

    /**
     * Sets the reaction system for the coordinator.
     * @param rs The reaction system.
     */
    public static void setRS(final ReactionSystem rs) {
        ManagersCoordinator.rs = rs;
    }

    /**
     * Resets the internal state of the coordinator. This allows to multiple executions through the GUI.
     */
    public void resetCoordinator() {
        managers.clear();
        cachedManagers.clear();
        managerId = 0;
    }

    /**
     * Gets the unique instance of the coordinator if present, creates one otherwise.
     * @return
     * @throws IllegalStateException
     */
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
