package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class DataManagerPlan extends Plan {
    DataManagerId dataManagerId;
    final Planner planner = Planner.DATA_MANAGER;
    final Consumer<DataManagerContext> consumer;

    protected DataManagerPlan(double time, Consumer<DataManagerContext> consumer, String key) {
        super(time, key);
        if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
        this.consumer = consumer;
    }

    protected DataManagerPlan(double time, boolean active, Consumer<DataManagerContext> consumer, String key) {
        super(time, active, key);
        if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
        this.consumer = consumer;
    }

    void execute(DataManagerContext context) {
        this.consumer.accept(context);
    }

    void setDataManagerId(DataManagerId dataManagerId) {
        this.dataManagerId = dataManagerId;
    }
}
