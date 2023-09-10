package reactionsystem;

import java.util.List;
import java.util.Set;

public class ExampleLactose {
    public static void main(String[] args) {
//        Entity eLac = new Entity("lac");
//        Entity eLacI = new Entity("lacI");
//        Entity eI = new Entity("i");
//        Entity eCya = new Entity("cya");
//        Entity eCAMP = new Entity("cAMP");
//        Entity eCrp = new Entity("crp");
//        Entity eCAP = new Entity("cAP");
//        Entity eLactose = new Entity("lactose");
//        Entity eGlucose = new Entity("glucose");
//        Entity eVoid = new Entity("void");
//        Entity eIOP = new Entity("iOP");
//        Entity eCampCap = new Entity("cAMPCAMP");
//        Entity eZ = new Entity("z");
//        Entity eY = new Entity("y");
//        Entity eA = new Entity("a");
//
//        Set<Entity> S = Entity.createEntitySet(eLac, eLacI, eI, eCya, eCAMP, eCrp, eCAP, eLactose, eGlucose, eVoid, eIOP, eCampCap, eZ, eY, eA);
//
//        Reaction r1 = new Reaction(Entity.createEntitySet(eLac), Entity.createEntitySet(eVoid), Entity.createEntitySet(eLac));
//        Reaction r2 = new Reaction(Entity.createEntitySet(eLacI), Entity.createEntitySet(eVoid), Entity.createEntitySet(eLacI));
//        Reaction r3 = new Reaction(Entity.createEntitySet(eLacI), Entity.createEntitySet(eVoid), Entity.createEntitySet(eI));
//        Reaction r4 = new Reaction(Entity.createEntitySet(eI), Entity.createEntitySet(eLactose), Entity.createEntitySet(eIOP));
//        Reaction r5 = new Reaction(Entity.createEntitySet(eCya), Entity.createEntitySet(eVoid), Entity.createEntitySet(eCya));
//        Reaction r6 = new Reaction(Entity.createEntitySet(eCya), Entity.createEntitySet(eVoid), Entity.createEntitySet(eCAMP));
//        Reaction r7 = new Reaction(Entity.createEntitySet(eCrp), Entity.createEntitySet(eVoid), Entity.createEntitySet(eCrp));
//        Reaction r8 = new Reaction(Entity.createEntitySet(eCrp), Entity.createEntitySet(eVoid), Entity.createEntitySet(eCAP));
//        Reaction r9 = new Reaction(Entity.createEntitySet(eCAMP, eCAP), Entity.createEntitySet(eGlucose), Entity.createEntitySet(eCampCap));
//        Reaction r10 = new Reaction(Entity.createEntitySet(eLac, eCampCap), Entity.createEntitySet(eIOP), Entity.createEntitySet(eZ, eY, eA));

        String reactionsString = "([lac],[void],[lac]), " +
                "([lacI],[void],[lacI]), " +
                "([lacI],[void],[i]), " +
                "([i],[lactose],[iOP]), " +
                "([cya],[void],[cya]), " +
                "([cya],[void],[cAMP]), " +
                "([crp],[void],[crp]), " +
                "([crp],[void],[cAP]), " +
                "([cAMP,cAP],[glucose],[cAMPCAP]), " +
                "([lac,cAMPCAP],[iOP],[z,y,a])";

        Reaction.checkReactionStringConformity(reactionsString);

        Set<Entity> S = Entity.extrapolateEntitiesFromReactionsString(reactionsString);

        Set<Reaction> A = Reaction.parseReactions(reactionsString);

        ReactionSystem RS = new ReactionSystem(S, A);

        String environment = "x = {lac,lacI,i,cya,cAMP,crp,cAP}.x , y = ({lactose}.y + {glucose}.y)";
        String context = "x, y";

        List<Context> parGamma = Context.parseParallel(context);
        Environment env = new Environment(environment);

        List<InteractiveProcess> pi = InteractiveProcess.createParallelProcesses(env, parGamma);

        ManagersCoordinator.setRS(RS);
        ManagersCoordinator coordinator = ManagersCoordinator.getInstance();

        coordinator.spawnManager(pi);

        coordinator.getLastManager().bindManagerToProcesses();

        coordinator.compute();

//        ProcessManager pm = new ProcessManager(RS, pi);

//        pm.run();
    }
}
