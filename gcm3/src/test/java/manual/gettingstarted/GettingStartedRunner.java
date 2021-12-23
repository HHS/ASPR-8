
package manual.gettingstarted;

import java.nio.file.Paths;

import nucleus.ReportId;
import nucleus.SimpleReportId;
import plugins.gcm.experiment.Experiment;
import plugins.gcm.experiment.ExperimentBuilder;
import plugins.gcm.experiment.ExperimentExecutor;
import plugins.gcm.experiment.output.NIOReportItemHandler;
import plugins.gcm.reports.CompartmentTransferReport;
import plugins.gcm.reports.PersonResourceReport;
import plugins.gcm.reports.ResourceReport;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.reports.support.ReportPeriod;

public class GettingStartedRunner {

	public static void main(String[] args) {
		// Create the experiment builder
		ExperimentBuilder experimentBuilder = new ExperimentBuilder();

		// Define person property - immune
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Boolean.class)//
																	.setDefaultValue(false)//
																	.build();//

		experimentBuilder.definePersonProperty(PersonProperty.IMMUNE, propertyDefinition);

		// Define person property - sick
		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Boolean.class)//
												.setDefaultValue(false)//
												.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();//
		experimentBuilder.definePersonProperty(PersonProperty.SICK, propertyDefinition);

		// Add a single region
		experimentBuilder.addRegionId(Region.REGION_1, () -> new SimpleRegion()::init);

		/*
		 * Add the exposed, treatment, recovered and terminal compartments. Note
		 * that we identify the compartment with an ID and then pass only a
		 * class reference, not an instance of the compartment class. More on
		 * that in later lessons.
		 */

		experimentBuilder.addCompartmentId(Compartment.TREATMENT, () -> new TreatmentCompartment()::init);
		experimentBuilder.addCompartmentId(Compartment.EXPOSED, () -> new ExposedCompartment()::init);
		experimentBuilder.addCompartmentId(Compartment.TERMINAL, () -> new TerminalCompartment()::init);
		experimentBuilder.addCompartmentId(Compartment.RECOVERED, () -> new RecoveredCompartment()::init);

		// Add a resource to the model and give REGION_1 300 doses of medication
		experimentBuilder.addResource(Resource.MEDICATION);
		experimentBuilder.addRegionResourceLevel(Region.REGION_1, Resource.MEDICATION, 300);

		// experimentBuilder.addMaterial(Material.THIMEROSAL);
		// experimentBuilder.addBatch(batchId, materialId, amount,
		// materialsProducerId);

		// Add a few people
		for (int i = 0; i < 100; i++) {
			PersonId personId = new PersonId(i);
			experimentBuilder.addPerson(personId, Region.REGION_1, Compartment.EXPOSED);
		}
		
		
		NIOReportItemHandler.Builder nioReportItemHandlerBuilder = NIOReportItemHandler.builder();
		
		// Add a report
		ReportId reportId = new SimpleReportId(CompartmentTransferReport.class);
		experimentBuilder.addReportId(reportId,()->new CompartmentTransferReport(ReportPeriod.DAILY)::init);
		nioReportItemHandlerBuilder.addReport(reportId, Paths.get("c:\\temp\\gcm\\compartment report.txt"));
		
		reportId = new SimpleReportId(PersonResourceReport.class);
		experimentBuilder.addReportId(reportId,()->new PersonResourceReport(ReportPeriod.DAILY,false,false,Resource.MEDICATION)::init);
		nioReportItemHandlerBuilder.addReport(reportId, Paths.get("c:\\temp\\gcm\\person resource report.txt"));
		
		
		reportId = new SimpleReportId(ResourceReport.class);
		experimentBuilder.addReportId(reportId,()->new ResourceReport(ReportPeriod.DAILY,Resource.MEDICATION)::init);
		nioReportItemHandlerBuilder.addReport(reportId, Paths.get("c:\\temp\\gcm\\resource report.txt"));

		
		// Build the experiment
		Experiment experiment = experimentBuilder.build();
		nioReportItemHandlerBuilder.setRegularExperiment(experiment);
		NIOReportItemHandler nioReportItemHandler = nioReportItemHandlerBuilder.build();
		// Prepare the experiment executor
		ExperimentExecutor experimentExecutor = new ExperimentExecutor();
		experimentExecutor.setExperiment(experiment);
		experimentExecutor.addOutputItemHandler(nioReportItemHandler);


		// Execute the experiment
		experimentExecutor.execute();
	}
}
