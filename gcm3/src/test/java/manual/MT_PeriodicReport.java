package manual;

import java.util.ArrayList;
import java.util.List;

import nucleus.ActorContext;
import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.Event;
import nucleus.Plugin;
import nucleus.SimplePluginId;
import nucleus.Simulation;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportId;

public class MT_PeriodicReport {
	
	private static int masterId;
	
	private MT_PeriodicReport() {

	}
	
	private static class TestDataManager extends DataManager{
		@Override
		public void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			for (int i = 0; i < 30; i++) {				
				double planTime = (double) i / 8;
				if(i%8==0) {
					planTime = (double) (i / 8);
				}
				dataManagerContext.addPlan(this::executePlan, planTime);
			}
		}
		public void executePlan(DataManagerContext dataManagerContext) {
			dataManagerContext.releaseEvent(new TestEvent(masterId++,"DM", dataManagerContext.getTime()));
		}
		
	}

	private static class TestEvent implements Event {
		private final int id;
		private final String source;
		private final double eventTime;

		public TestEvent(int id, String source, double eventTime) {
			this.id = id;
			this.source = source;
			this.eventTime = eventTime;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TestEvent [id=");
			builder.append(id);
			builder.append(", source=");
			builder.append(source);
			builder.append("]");
			return builder.toString();
		}

	}

	private static class TestPeriodicReport extends PeriodicReport {

		public TestPeriodicReport(ReportId reportId, ReportPeriod reportPeriod) {
			super(reportId, reportPeriod);

		}

		private List<TestEvent> testEvents = new ArrayList<>();

		@Override
		protected void flush(ActorContext actorContext) {
			System.out.println("flushing at time " + actorContext.getTime());

			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			this.addTimeFieldHeaders(reportHeaderBuilder);
			reportHeaderBuilder.add("id");
			reportHeaderBuilder.add("source");
			reportHeaderBuilder.add("event time");

			ReportHeader reportHeader = reportHeaderBuilder.build();

			ReportItem.Builder reportItemBuilder = ReportItem.builder();

			for (TestEvent testEvent : testEvents) {
				reportItemBuilder.setReportHeader(reportHeader);//
				fillTimeFields(reportItemBuilder);//
				reportItemBuilder.addValue(testEvent.id);//
				reportItemBuilder.addValue(testEvent.source);//
				reportItemBuilder.addValue(testEvent.eventTime);//
				reportItemBuilder.setReportId(getReportId());//
				ReportItem reportItem = reportItemBuilder.build();//
				System.out.println(reportItem);//
			}
			testEvents.clear();
			System.out.println();
		}

		@Override
		public void init(ActorContext actorContext) {
			super.init(actorContext);			
			//actorContext.subscribe(TestEvent.class, this::handleTestEvent);
			actorContext.subscribe(TestEvent.class, getFlushingConsumer(this::handleTestEvent));
		}

		private void handleTestEvent(ActorContext actorContext, TestEvent testEvent) {
			testEvents.add(testEvent);
		}
	}

	private static class BroadCaster {
		

		public void init(ActorContext actorContext) {
			for (int i = 0; i < 30; i++) {
				double planTime = (double) i / 10;
				if(i%10==0) {
					planTime = (double)( i / 10);
				}
				actorContext.addPlan(this::executePlan, planTime);
			}
		}

		public void executePlan(ActorContext actorContext) {
			actorContext.releaseEvent(new TestEvent(masterId++,"BroadCaster", actorContext.getTime()));
		}
	}

	public static void main(String[] args) {

		Plugin plugin = Plugin.builder().setPluginId(new SimplePluginId("plugin")).setInitializer((c) -> {
			c.addActor(new TestPeriodicReport(new SimpleReportId("report"), ReportPeriod.DAILY)::init);
			c.addActor(new BroadCaster()::init);
			c.addDataManager(new TestDataManager());
		}).build();

		Simulation	.builder()//
					.addPlugin(plugin)//
					.build()//
					.execute();//

	}

}
