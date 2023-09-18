package reactionsystem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExampleDefault {
    public static void main(String[] args) {
        String reactionsString = "([a,b],[c],[b])";

        Reaction.checkReactionStringConformity(reactionsString);

        Set<Entity> S = Entity.extrapolateEntitiesFromReactionsString(reactionsString);

        Set<Reaction> A = Reaction.parseReactions(reactionsString);

        ReactionSystem RS = new ReactionSystem(S, A);

        String environment = "";
        String context = "{a, b}.{a}.{a, c}.nil + {a, b}.{a}.{a}.nil";

        List<Context> parGamma = Context.parseParallel(context);
        Environment env = new Environment(environment);

        List<InteractiveProcess> pi = InteractiveProcess.createParallelProcesses(env, parGamma);

        ManagersCoordinator.setRS(RS);
        ManagersCoordinator coordinator = ManagersCoordinator.getInstance();

        coordinator.spawnManager(pi);

        coordinator.getLastManager().bindManagerToProcesses();

        coordinator.compute();
    }
}
