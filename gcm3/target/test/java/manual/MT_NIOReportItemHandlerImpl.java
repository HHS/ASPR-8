package manual;

/**
 * A manual test that shows that the NIOReportItemHandler writes headers
 * properly under multiple threads depositing report items.
 * 
 * @author Shawn Hatch
 *
 */
public final class MT_NIOReportItemHandlerImpl {

//	private MT_NIOReportItemHandlerImpl() {
//	}
//
//	public static void main(String[] args) {
//
//		Path path = Paths.get(args[0]);
//
//		NIOReportItemHandler.Builder nioReportItemHandlerBuilder = NIOReportItemHandler.builder();
//		ReportId reportId = new SimpleReportId(CompartmentPopulationReport.class);
//		nioReportItemHandlerBuilder.addReport(reportId,path);
//		NIOReportItemHandler nioReportItemHandler = nioReportItemHandlerBuilder.build();
//
//		ReportHeader reportHeader = ReportHeader.builder().add("Alpha").add("Beta").build();
//
//		int jobCount = 10;
//		JobCompletionCounter jobCompletionCounter = new JobCompletionCounter(jobCount, nioReportItemHandler);
//
//		for (int i = 0; i < jobCount; i++) {
//			new Thread(new Runner(reportHeader, i, nioReportItemHandler, jobCompletionCounter)).start();
//		}
//
//	}
//
//	private static class JobCompletionCounter {
//		
//		private final NIOReportItemHandler outputItemHandler;
//
//		public JobCompletionCounter(int jobs, NIOReportItemHandler outputItemHandler) {
//			this.jobs = jobs;
//			this.outputItemHandler = outputItemHandler;
//		}
//
//		private int jobs = 0;
//
//		public synchronized void decrementJobs() {
//			jobs--;
//			if (jobs == 0) {
//				outputItemHandler.closeExperiment();
//			}
//		}
//
//	}
//
//	private static class Runner implements Runnable {
//		private final ReportHeader reportHeader;
//		private final Integer index;
//		private final NIOReportItemHandler nioReportItemHandler;
//		private final JobCompletionCounter jobCompletionCounter;
//
//		public Runner(ReportHeader reportHeader, Integer index, NIOReportItemHandler nioReportItemHandler, JobCompletionCounter jobCompletionCounter) {
//			this.reportHeader = reportHeader;
//			this.index = index;
//			this.nioReportItemHandler = nioReportItemHandler;
//			this.jobCompletionCounter = jobCompletionCounter;
//		}
//
//		@Override
//		public void run() {
//			for (int j = 0; j < 30; j++) {
//				final ReportItem.Builder reportItemBuilder = ReportItem.builder();
//				reportItemBuilder.setReportHeader(reportHeader);
//				reportItemBuilder.setReportId(new ReportId() {});
//				reportItemBuilder.addValue(index);
//				reportItemBuilder.addValue(j);
//
//				ReportItem reportItem = reportItemBuilder.build();
//
//				nioReportItemHandler.handle(new ScenarioId(552), new ReplicationId(342), reportItem);
//			}
//			jobCompletionCounter.decrementJobs();
//		}
//
//	}
}
