package tools.codestats;

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
