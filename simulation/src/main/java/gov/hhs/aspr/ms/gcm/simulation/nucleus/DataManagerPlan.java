package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.function.Consumer;

public class DataManagerPlan extends Plan {
    DataManagerId dataManagerId;
    final Consumer<DataManagerContext> consumer;

    public DataManagerPlan(double time, boolean active, long arrivalId, Consumer<DataManagerContext> consumer) {
        super(time, active, arrivalId, Planner.DATA_MANAGER);
        this.consumer = consumer;
    }

    public DataManagerPlan(double time, boolean active, Consumer<DataManagerContext> consumer) {
        super(time, active, -1L, Planner.DATA_MANAGER);
        this.consumer = consumer;
    }

    public DataManagerPlan(double time, Consumer<DataManagerContext> consumer) {
        super(time, true, -1L, Planner.DATA_MANAGER);
        this.consumer = consumer;
    }

    void execute(DataManagerContext context) {
        this.consumer.accept(context);
    }
}
