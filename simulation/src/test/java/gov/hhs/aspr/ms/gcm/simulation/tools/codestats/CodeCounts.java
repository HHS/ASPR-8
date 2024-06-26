package gov.hhs.aspr.ms.gcm.simulation.tools.codestats;

import gov.hhs.aspr.ms.util.meta.codecount.CodeCountReport;

public class CodeCounts {
	public static void main(String[] args) {
		CodeCountReport.Builder codeCountReportBuilder = CodeCountReport.builder();
		for(String arg : args) {
			codeCountReportBuilder.addDirectory(arg);
		}
		CodeCountReport codeCountReport = codeCountReportBuilder.build();
		System.out.println(codeCountReport.getDetailsReport());
	}
}
