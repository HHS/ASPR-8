package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

public class ActorPlan extends Plan {
    ActorId actorId;
    protected Consumer<ActorContext> consumer;

    public ActorPlan(double time, Consumer<ActorContext> consumer) {
        super(time);
        this.consumer = consumer;
    }

    public ActorPlan(double time, boolean active, Consumer<ActorContext> consumer) {
        super(time, active);
        this.consumer = consumer;
    }

    public final Planner getPlanner() {
        return Planner.ACTOR;
    }

    public final Consumer<ActorContext> getConsumer() {
        return this.consumer;
    }

    public void execute(ActorContext context) {
        this.consumer.accept(context);
    }

    public Consumer<ActorContext> getCallbackConsumer() {
        return this.consumer;
    }

    void setActorId(ActorId actorId) {
        this.actorId = actorId;
    }

}
