package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class ReportPlan extends Plan {
    ReportId reportId;
    final Planner planner = Planner.REPORT;
    final Consumer<ReportContext> consumer;

    protected ReportPlan(double time, Consumer<ReportContext> consumer, String key) {
        super(time, key);
        if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
        this.consumer = consumer;
    }

    protected ReportPlan(double time, boolean active, Consumer<ReportContext> consumer, String key) {
        super(time, active, key);
        if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
        this.consumer = consumer;
    }

    void execute(ReportContext context) {
        this.consumer.accept(context);
    }

    void setReportId(ReportId reportId) {
        this.reportId = reportId;
    }
}
