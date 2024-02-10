package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

public class ReportPlan extends Plan {
    ReportId reportId;
    final Consumer<ReportContext> consumer;

    public ReportPlan(double time, long arrivalId, Consumer<ReportContext> consumer) {
        super(time, false, arrivalId, Planner.REPORT);
        this.consumer = consumer;
    }

    public ReportPlan(double time, Consumer<ReportContext> consumer) {
        super(time, false, -1, Planner.REPORT);
        this.consumer = consumer;
    }

    void execute(ReportContext context) {
        this.consumer.accept(context);
    }

    void setReportId(ReportId reportId) {
        this.reportId = reportId;
    }
}
