package manual.microsim;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.AgentContext;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.ReportContext;
import nucleus.SimpleReportId;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.events.mutation.PersonCompartmentAssignmentEvent;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.components.ComponentPlugin;
import plugins.components.datacontainers.ComponentDataView;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.initialdata.StochasticsInitialData;

public class MicroSim {

	private MicroSim() {

	}

	private static enum Compartment implements CompartmentId {
		A, B,C
	}

	private static void compartmentAInitialization(AgentContext agentContext) {
		PersonDataView personDataView = agentContext.getDataView(PersonDataView.class).get();
		StochasticsDataView stochasticsDataView = agentContext.getDataView(StochasticsDataView.class).get();
		RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
		double transferTime = agentContext.getTime();
		for (int i = 0; i < 10000; i++) {
			agentContext.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().add(Compartment.A).build()));
			PersonId personId = personDataView.getLastIssuedPersonId().get();
			transferTime += randomGenerator.nextDouble();
			Compartment destination;
			if(randomGenerator.nextBoolean()) {
				destination = Compartment.B;
			}else {
				destination = Compartment.C;
			}

			agentContext.addPlan((c) -> {
				c.resolveEvent(new PersonCompartmentAssignmentEvent(personId, destination));
			}, transferTime);

		}
	}

	private static class PersonCompartmentTransferReport {
		public void init(ReportContext reportContext) {
			reportContext.subscribe(PersonCompartmentChangeObservationEvent.class, this::handlePersonCompartmentChangeObservationEvent);
		}

		private void handlePersonCompartmentChangeObservationEvent(ReportContext reportContext, PersonCompartmentChangeObservationEvent event) {
			ReportHeader reportHeader = ReportHeader.builder().add("Time").add("Person").add("Source").add("Destination").build();

			ReportItem reportItem = ReportItem.builder().setReportHeader(reportHeader).setReportId(new SimpleReportId(this.getClass())).addValue(reportContext.getTime()).addValue(event.getPersonId()).addValue(event.getPreviousCompartmentId()).addValue(event.getCurrentCompartmentId()).build();
			reportContext.releaseOutput(reportItem);
		}
	}

	private static void receivingCompartmentInitialization(AgentContext agentContext) {
		agentContext.subscribe(
				PersonCompartmentChangeObservationEvent.getEventLabelByArrivalCompartment(agentContext, agentContext.getDataView(ComponentDataView.class).get().getFocalComponentId()),
				MicroSim::compartmentBSeePersonArrival);
	}

	private static void compartmentBSeePersonArrival(AgentContext agentContext, PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		
	}
	
	private boolean headerWritten;

	private void handleOutput(BufferedWriter writer, Object output) {
		try {
			if (output instanceof ReportItem) {
				StringBuilder sb = new StringBuilder();
				ReportItem reportItem = (ReportItem) output;
				if(!headerWritten) {
					headerWritten = true;
					ReportHeader reportHeader = reportItem.getReportHeader();
					for(String headerString : reportHeader.getHeaderStrings()) {
						sb.append(headerString);
						sb.append("\t");
					}
					sb.append(System.getProperty("line.separator"));
				}

				for (int i = 0; i < reportItem.size(); i++) {
					sb.append(reportItem.getValue(i));
					sb.append("\t");
				}
				sb.append(System.getProperty("line.separator"));
				writer.write(sb.toString());
			} else {
				writer.write(output.toString());
				

			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	private void execute() throws IOException {

		// set up output file
		Path path = Paths.get("c:\\temp\\gcm\\output.txt");
		Files.deleteIfExists(path);
		CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
		OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encoder));

		Builder builder = Simulation.builder();

		// allowing people		
		builder.addPlugin(PeoplePlugin.PLUGIN_ID,new PeoplePlugin(PeopleInitialData.builder().build())::init);

		// getting random value generation		
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID,new StochasticsPlugin(StochasticsInitialData.builder().setSeed(1345245724553456L).build())::init);
		
		// add the component concept need for compartments to work correctly
		builder.addPlugin(ComponentPlugin.PLUGIN_ID,new ComponentPlugin()::init);

		// adding reports
		ReportsInitialData reportsInitialData = ReportsInitialData.builder().addReport(new SimpleReportId(PersonCompartmentTransferReport.class), () -> new PersonCompartmentTransferReport()::init).build();
		builder.addPlugin(ReportPlugin.PLUGIN_ID,new ReportPlugin(reportsInitialData)::init);

		builder.setOutputConsumer((obj) -> this.handleOutput(writer, obj));

		// adding the compartments
		CompartmentInitialData.Builder compartmentDataBuilder = CompartmentInitialData.builder();
		compartmentDataBuilder.setCompartmentInitialBehaviorSupplier(Compartment.A, () -> MicroSim::compartmentAInitialization);
		compartmentDataBuilder.setCompartmentInitialBehaviorSupplier(Compartment.B, () -> MicroSim::receivingCompartmentInitialization);
		compartmentDataBuilder.setCompartmentInitialBehaviorSupplier(Compartment.C, () -> MicroSim::receivingCompartmentInitialization);
		
		builder.addPlugin(CompartmentPlugin.PLUGIN_ID,new CompartmentPlugin(compartmentDataBuilder.build())::init);

		// building and executing the engine
		builder.build().execute();

		// close the output file
		writer.close();
	}

	public static void main(String[] args) throws IOException {
		new MicroSim().execute();
	}
}
