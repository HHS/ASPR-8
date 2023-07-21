package tools;

import util.meta.unittestcoverage.reports.MissingTestsReport;

public class MissingTestsReportRunner {
    public static void main(String[] args) {
        String[] args2 = new String[3];
        args2[0] = args[0];
        args2[1] = args[1];
        // args2[2] = "global";

		MissingTestsReport.run(args2);
	}
}
