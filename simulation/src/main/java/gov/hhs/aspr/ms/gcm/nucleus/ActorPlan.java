package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class ActorPlan extends Plan {
    ActorId actorId;
    protected final Consumer<ActorContext> consumer;

    public ActorPlan(double time, Consumer<ActorContext> consumer) {
        super(time, Planner.ACTOR);
        this.consumer = consumer;
    }

    public ActorPlan(double time, boolean active, Consumer<ActorContext> consumer) {
        super(time, active, Planner.ACTOR);
        this.consumer = consumer;
    }

    protected void execute(ActorContext context) {
        this.consumer.accept(context);
    }

    void setActorId(ActorId actorId) {
        this.actorId = actorId;
    }

    void validate(double simTime) {
        if (consumer == null) {
            throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
        }

        if (time < simTime) {
			throw new ContractException(NucleusError.PAST_PLANNING_TIME);
		}
    }
}
