package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

public class ActorPlan extends Plan {
    ActorId actorId;
    protected final Consumer<ActorContext> consumer;

    public ActorPlan(double time, boolean active, long arrivalId, Consumer<ActorContext> consumer) {
        super(time, active, arrivalId, Planner.ACTOR);
        this.consumer = consumer;
    }

    public ActorPlan(double time, Consumer<ActorContext> consumer) {
        super(time, true, -1, Planner.ACTOR);
        this.consumer = consumer;
    }

    public ActorPlan(double time, boolean active, Consumer<ActorContext> consumer) {
        super(time, active, -1, Planner.ACTOR);
        this.consumer = consumer;
    }

    protected void execute(ActorContext context) {
        this.consumer.accept(context);
    }

    void setActorId(ActorId actorId) {
        this.actorId = actorId;
    }
}
