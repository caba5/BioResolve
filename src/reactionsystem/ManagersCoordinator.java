package reactionsystem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ManagersCoordinator {
    private final String GRAPHFILENAME = "result.dot";
    private static ReactionSystem rs = null;

    private static ManagersCoordinator instance = null;

    private final List<ProcessManager> managers;

    private int managerId;

    private Set<ProcessManager.State> cachedManagers;

    private ManagersCoordinator() {
        this.managers = new ArrayList<>();
        this.managerId = 0;
        this.cachedManagers = new HashSet<>();
    }

    public ProcessManager spawnManager(List<InteractiveProcess> processes) {
        ProcessManager newManager = new ProcessManager(managerId, rs, processes);

        managers.add(newManager);
        ++managerId;

        if (BioResolve.DEBUG) System.out.println("[Info] Spawned a new process manager");

        return newManager;
    }

    public void attachProcessesToManager(int managerId, List<InteractiveProcess> processes) throws IllegalArgumentException {
        if (managerId < 0 || managerId >= this.managerId)
            throw new IllegalArgumentException("No such manager.");

        ProcessManager selectedManager = managers.get(managerId);
        selectedManager.setParallelProcesses(processes);
    }

    public ProcessManager cloneManagerSubstitutingProcess(
            int sourceManagerId,
            InteractiveProcess toDiscard,
            InteractiveProcess toAdd
    ) throws IllegalArgumentException {
        if (sourceManagerId >= managerId ||  sourceManagerId < 0)
            throw new IllegalArgumentException("No such manager.");
        if (toAdd == null)
            throw new IllegalArgumentException("Cannot push a null InteractiveProcess");

        ProcessManager sourceManager = managers.get(sourceManagerId);
        List<InteractiveProcess> sourceProcesses = sourceManager.getParallelProcesses();

        List<InteractiveProcess> filteredProcesses = new ArrayList<>(sourceProcesses.size());
        for (InteractiveProcess p : sourceProcesses)
            if (!p.equals(toDiscard)) filteredProcesses.add(p);

        filteredProcesses.add(toAdd);

        ProcessManager destManager = spawnManager(filteredProcesses); // TODO: can create a new manager constructor which skips checks (which are useless at this point)
//        destManager.setParallelProcesses(filteredProcesses);

        return destManager;
    }

    public void compute() {
        int i = 0;
        while (i < managers.size()) {
                String sep = " ------------------------------------------- ";
                if (BioResolve.DEBUG) System.out.println(sep + "Running " + managers.get(i) + sep);
                managers.get(i).run();
                if (BioResolve.DEBUG) System.out.println(sep + "Ending " + managers.get(i) + sep);


            ++i;
        }
        if (BioResolve.DEBUG) System.out.println("All the managers finished their jobs.");
        generateDOTGraph();
    }

    public void generateDOTGraph() {
        StringBuilder graph = new StringBuilder("digraph G { node [shape=box] edge [arrowhead=vee] ");

        // TODO: manage links between nodes first: for each node, if from not already visited (hashset) add it as a visited node, same for to, then create arc label.

        Set<String> nodes = new HashSet<>();
        Set<String> arcs = new HashSet<>();

        for (ProcessManager p : managers) {
            List<ProcessManager.NodePair> pGraph = p.getProcessGraph();
            for (ProcessManager.NodePair node : pGraph) {
                String from = "\"" + Entity.stringifyEntitiesCollection(node.getFrom()) + "\"";
                String to = "\"" + Entity.stringifyEntitiesCollection(node.getTo()) + "\"";
                String arc = from + " -> " + to + " [label = \"" + Entity.stringifyEntitiesCollection(node.getArc()) + "\"]";

                nodes.add(from);
                nodes.add(to);
                arcs.add(arc);
            }
        }

        for (String node : nodes)
            graph.append(node).append(";\t");

        for (String arc : arcs)
            graph.append(arc).append(";\t");

        graph.append("}");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(GRAPHFILENAME));
            writer.write(graph.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println("Could not write the .dot graph to file. " + e);
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

    public Set<ProcessManager.State> getCachedManagers() {
        return cachedManagers;
    }

    public static void setRS(ReactionSystem rs) {
        ManagersCoordinator.rs = rs;
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
