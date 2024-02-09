package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

public class ReportPlan extends Plan {
    ReportId reportId;
    protected Consumer<ReportContext> consumer;

    protected ReportPlan(double time, Consumer<ReportContext> consumer) {
        super(time);
        this.consumer = consumer;
    }

    protected ReportPlan(double time, boolean active, Consumer<ReportContext> consumer) {
        super(time, active);
        this.consumer = consumer;
    }

    public final Planner getPlanner() {
        return Planner.REPORT;
    }

    public final Consumer<ReportContext> getConsumer() {
        return this.consumer;
    }

    void execute(ReportContext context) {
        this.consumer.accept(context);
    }

    Consumer<ReportContext> getCallbackConsumer() {
        return this.consumer;
    }

    void setReportId(ReportId reportId) {
        this.reportId = reportId;
    }
}
