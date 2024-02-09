package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class ReportPlan extends Plan {
    ReportId reportId;
    final Consumer<ReportContext> consumer;

    protected ReportPlan(double time, Consumer<ReportContext> consumer) {
        super(time, Planner.REPORT);
        this.consumer = consumer;
    }

    protected ReportPlan(double time, boolean active, Consumer<ReportContext> consumer) {
        super(time, active, Planner.REPORT);
        this.consumer = consumer;
    }

    protected ReportPlan(double time, Consumer<ReportContext> consumer, Object key) {
        super(time, Planner.REPORT, key);
        this.consumer = consumer;
    }

    protected ReportPlan(double time, boolean active, Consumer<ReportContext> consumer, Object key) {
        super(time, active, Planner.REPORT, key);
        this.consumer = consumer;
    }

    void execute(ReportContext context) {
        this.consumer.accept(context);
    }

    void setReportId(ReportId reportId) {
        this.reportId = reportId;
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
