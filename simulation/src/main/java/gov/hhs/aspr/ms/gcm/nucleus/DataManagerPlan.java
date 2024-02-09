package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class DataManagerPlan extends Plan {
    DataManagerId dataManagerId;
    final Consumer<DataManagerContext> consumer;

    protected DataManagerPlan(double time, Consumer<DataManagerContext> consumer) {
        super(time, Planner.DATA_MANAGER);
        this.consumer = consumer;
    }

    protected DataManagerPlan(double time, boolean active, Consumer<DataManagerContext> consumer) {
        super(time, active, Planner.DATA_MANAGER);
        this.consumer = consumer;
    }

    void execute(DataManagerContext context) {
        this.consumer.accept(context);
    }

    void setDataManagerId(DataManagerId dataManagerId) {
        this.dataManagerId = dataManagerId;
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
