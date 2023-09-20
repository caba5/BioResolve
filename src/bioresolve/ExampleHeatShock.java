package bioresolve;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class ExampleHeatShock {
    public static void main(String[] args) {
//        Entity eHsf = new Entity("hsf");
//        Entity eHsf3 = new Entity("hsf3");
//        Entity eVoid = new Entity("void");
//        Entity eProt = new Entity("prot");
//        Entity eHse = new Entity("hse");
//        Entity eNostress = new Entity("nostress");
//        Entity eHspHsf = new Entity("hspHsf");
//        Entity eStress = new Entity("stress");
//        Entity eHsp = new Entity("hsp");
//        Entity eHsf3Hse = new Entity("hsf3Hse");
//        Entity eMfp = new Entity("mfp");
//        Entity eHspMfp = new Entity("hspMfp");
//
//        Set<Entity> S = Entity.createEntitySet(eHsf, eHsf3, eVoid, eProt, eHse, eNostress, eHspHsf, eStress, eHsp, eHsf3Hse, eMfp, eHspMfp);

//        ArrayList<Reaction> reactions = new ArrayList<>();

//        reactions.add(new Reaction(Entity.createEntitySet(eHsf), Entity.createEntitySet(eHsp), Entity.createEntitySet(eHsf3)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHsf, eHsp, eMfp), Entity.createEntitySet(eVoid), Entity.createEntitySet(eHsf3)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHsf3), Entity.createEntitySet(eHse, eHsp), Entity.createEntitySet(eHsf)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHsf3, eHsp, eMfp), Entity.createEntitySet(eHse), Entity.createEntitySet(eHsf)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHsf3, eHse), Entity.createEntitySet(eHspHsf), Entity.createEntitySet(eHsf3Hse)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHsf3, eHse, eHsp, eMfp), Entity.createEntitySet(eVoid), Entity.createEntitySet(eHsf3Hse)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHse), Entity.createEntitySet(eHsf3), Entity.createEntitySet(eHse)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHse, eHsf3, eHsp), Entity.createEntitySet(eMfp), Entity.createEntitySet(eHse)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHsf3Hse), Entity.createEntitySet(eHsp), Entity.createEntitySet(eHsf3Hse, eHsp)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHsf3Hse, eHsp, eMfp), Entity.createEntitySet(eVoid), Entity.createEntitySet(eHsf3Hse, eHsp)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHsp, eHsf), Entity.createEntitySet(eMfp), Entity.createEntitySet(eHspHsf)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHspHsf, eStress), Entity.createEntitySet(eNostress), Entity.createEntitySet(eHsp, eHsf)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHspHsf, eNostress), Entity.createEntitySet(eStress), Entity.createEntitySet(eHspHsf)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHsp, eHsf3), Entity.createEntitySet(eMfp), Entity.createEntitySet(eHspHsf)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHsp, eHsf3Hse), Entity.createEntitySet(eMfp), Entity.createEntitySet(eHspHsf, eHse)));
//        reactions.add(new Reaction(Entity.createEntitySet(eProt, eStress), Entity.createEntitySet(eNostress), Entity.createEntitySet(eProt, eMfp)));
//        reactions.add(new Reaction(Entity.createEntitySet(eProt, eNostress), Entity.createEntitySet(eStress), Entity.createEntitySet(eProt)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHsp, eMfp), Entity.createEntitySet(eVoid), Entity.createEntitySet(eHspMfp)));
//        reactions.add(new Reaction(Entity.createEntitySet(eMfp), Entity.createEntitySet(eHsp), Entity.createEntitySet(eMfp)));
//        reactions.add(new Reaction(Entity.createEntitySet(eHspMfp), Entity.createEntitySet(eVoid), Entity.createEntitySet(eHsp, eProt)));
//
//        Set<Reaction> A = Reaction.createReactionSet(reactions);

        String reactionsString = "([hsf],[hsp],[hsf3]), " +
                "([hsf,hsp,mfp],[void],[hsf3]), " +
                "([hsf3],[hse,hsp],[hsf]), " +
                "([hsf3,hsp,mfp],[hse],[hsf]), " +
                "([hsf3,hse],[hsp],[hsf3Hse]), " +
                "([hsf3,hse,hsp,mfp],[void],[hsf3Hse]), " +
                "([hse],[hsf3],[hse]), " +
                "([hse,hsf3,hsp],[mfp],[hse]), " +
                "([hsf3Hse],[hsp],[hsf3Hse,hsp]), " +
                "([hsf3Hse,hsp,mfp],[void],[hsf3Hse,hsp]), " +
                "([hsp,hsf],[mfp],[hspHsf]), " +
                "([hspHsf,stress],[nostress],[hsp,hsf]), " +
                "([hspHsf,nostress],[stress],[hspHsf]), " +
                "([hsp,hsf3],[mfp],[hspHsf]), " +
                "([hsp,hsf3Hse],[mfp],[hspHsf,hse]), " +
                "([prot,stress],[nostress],[prot,mfp]), " +
                "([prot,nostress],[stress],[prot]), " +
                "([hsp,mfp],[void],[hspMfp]), " +
                "([mfp],[hsp],[mfp]), " +
                "([hspMfp],[void],[hsp,prot])";

        Reaction.checkReactionStringConformity(reactionsString);

        Set<Entity> S = Entity.extrapolateEntitiesFromReactionsString(reactionsString);

        Set<Reaction> A = Reaction.parseReactions(reactionsString);

        ReactionSystem RS = new ReactionSystem(S, A);

        String environment = "exp1={hsf,prot,hse,nostress}.{nostress}.{nostress}.{nostress}.{nostress}.{nostress}.nil," +
                "exp2={hse,prot,hspHsf,stress}.{stress}.{stress}.{stress}.{stress}.nil," +
                "exp3={hsp,prot,hsf3Hse,mfp,hspMfp,nostress}.{nostress}.{nostress}.{nostress}.{nostress}.nil";
        String context = "(exp1 + exp2)";

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
