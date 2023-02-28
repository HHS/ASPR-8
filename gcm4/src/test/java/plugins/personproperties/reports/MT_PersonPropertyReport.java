package plugins.personproperties.reports;

import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;

public class MT_PersonPropertyReport {

    public static void main (String[] args) {
        new MT_PersonPropertyReport().test1();
    }

    private void test1() {

        PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
        builder.setReportPeriod(ReportPeriod.DAILY);
        builder.setReportLabel(new SimpleReportLabel(1000));
        builder.setDefaultInclusion(true);

    }
}
