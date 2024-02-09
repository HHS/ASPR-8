package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class ActorPlan extends Plan {
    ActorId actorId;
    final Planner planner = Planner.ACTOR;
    final Consumer<ActorContext> consumer;

    public ActorPlan(double time, Consumer<ActorContext> consumer, String key) {
        super(time, key);
        if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
        this.consumer = consumer;
    }

    public ActorPlan(double time, boolean active, Consumer<ActorContext> consumer, String key) {
        super(time, active, key);
        if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
        this.consumer = consumer;
    }

    void execute(ActorContext context) {
        this.consumer.accept(context);
    }

    void setActorId(ActorId actorId) {
        this.actorId = actorId;
    }
}
