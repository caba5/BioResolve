package reactionsystem;

import java.util.*;

public class ManagersCoordinator {
    private static ReactionSystem rs = null;

    private static ManagersCoordinator instance = null;

    private final List<ProcessManager> managers;

    private int managerId;

    private Set<Integer> cachedManagers;

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
            if (!cachedManagers.contains(managers.get(i).getManagerCode())) {
                String sep = " ------------------------------------------- ";
                if (BioResolve.DEBUG) System.out.println(sep + "Running " + managers.get(i) + sep);
                managers.get(i).run();
                cachedManagers.add(managers.get(i).getManagerCode());
                /*
                    TODO for the .dot:
                        - each process has to yield its sequence of results
                        - each manager has to yield the sequences of results + activated reactions (products) of its parallel processes (graph? maybe just a multidim list of tuples (result, products leading to result) for each process)
                        - since the same process is not repeated, the coordinator shouldn't (TO CHECK) have repetitions, thus the coordinator just has to compose the result of each manager
                */
                if (BioResolve.DEBUG) System.out.println(sep + "Ending " + managers.get(i) + sep);
            }

            ++i;
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
