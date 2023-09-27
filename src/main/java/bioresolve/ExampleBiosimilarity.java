package bioresolve;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class ExampleBiosimilarity {
    public static void main(String[] args) {
        String reactionsString = "([a1],[b1],[c]), " +
                "([a2],[b2],[c]), " +
                "([b1],[a1],[d]), " +
                "([b2],[a2],[d]), " +
                "([c],[a1],[d])";

        Reaction.checkReactionStringConformity(reactionsString);

        Set<Entity> S = Entity.extrapolateEntitiesFromReactionsString(reactionsString);

        Set<Reaction> A = Reaction.parseReactions(reactionsString);

        ReactionSystem RS = new ReactionSystem(S, A);

        String environment = "x=({a1,b2}.{}.{a2,b1}.x + {a2,b1}.{}.{a1,b2}.x + {c}.nil)";
        String context = "x";

        List<Context> parGamma = Context.parseParallel(context);
        Environment env = new Environment(environment);

        List<InteractiveProcess> pi = InteractiveProcess.createParallelProcesses(env, parGamma);

        ManagersCoordinator.setRS(RS);
        ManagersCoordinator coordinator = ManagersCoordinator.getInstance();

        coordinator.spawnManager(pi);

        coordinator.getLastManager().bindManagerToProcesses();

        Duration totalTime = coordinator.compute();

        float t = (float) totalTime.toNanos() / 1000000000;
        System.out.println("Total time " + t + "s");
    }
}
