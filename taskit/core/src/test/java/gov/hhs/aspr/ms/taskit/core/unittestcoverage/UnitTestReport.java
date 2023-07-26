package gov.hhs.aspr.ms.taskit.core.unittestcoverage;

public final class UnitTestReport {

	public static void main(final String[] args) {

		System.out.println("Missing Tests Report:");
		util.meta.unittestcoverage.reports.MissingTestsReport.run(args);
		System.out.print("\n\n\n");

		System.out.println("MetaInfo Report:");
		util.meta.unittestcoverage.reports.MetaInfoReport.run(args);
		System.out.print("\n\n\n");

		System.out.println("Incomplete Tests Report:");
		util.meta.unittestcoverage.reports.IncompleteClassReport.run(args);
		System.out.print("\n\n\n");

		System.out.println("Status Report:");
		util.meta.unittestcoverage.reports.StatusReport.run(args);
		System.out.print("\n\n\n");
	}

}
