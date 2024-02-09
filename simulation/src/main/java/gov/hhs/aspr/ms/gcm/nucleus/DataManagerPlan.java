package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

public class DataManagerPlan extends Plan {
    DataManagerId dataManagerId;
    private Consumer<DataManagerContext> consumer;

    protected DataManagerPlan(double time, Consumer<DataManagerContext> consumer) {
        super(time);
        this.consumer = consumer;
    }

    protected DataManagerPlan(double time, boolean active, Consumer<DataManagerContext> consumer) {
        super(time, active);
        this.consumer = consumer;
    }

    public final Planner getPlanner() {
        return Planner.DATA_MANAGER;
    }

    public final Consumer<DataManagerContext> getConsumer() {
        return this.consumer;
    }

    void execute(DataManagerContext context) {
        this.consumer.accept(context);
    }

    Consumer<DataManagerContext> getCallbackConsumer() {
        return this.consumer;
    }

    void setDataManagerId(DataManagerId dataManagerId) {
        this.dataManagerId = dataManagerId;
    }

}
