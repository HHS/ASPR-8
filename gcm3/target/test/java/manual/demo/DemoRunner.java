
package manual.demo;

public class DemoRunner {

//	private final Path tractsPath;
//	private final Path populationPath;
//
//	private DemoRunner(Path tractsPath, Path populationPath) {
//		this.tractsPath = tractsPath;
//		this.populationPath = populationPath;
//	}
//
//	private void defineProperties(ExperimentBuilder experimentBuilder) {
//
//		for (RegionProperty regionProperty : RegionProperty.values()) {
//			experimentBuilder.defineRegionProperty(regionProperty, regionProperty.getPropertyDefinition());
//		}
//
//		for (PersonProperty personProperty : PersonProperty.values()) {
//			experimentBuilder.definePersonProperty(personProperty, personProperty.getPropertyDefinition());
//		}
//
//		for (GroupProperty groupProperty : GroupProperty.values()) {
//			experimentBuilder.defineGroupProperty(groupProperty.getGroupType(), GroupProperty.SHARED_IMMUNITY, groupProperty.getPropertyDefinition());
//		}
//
//		for (GlobalProperty globalProperty : GlobalProperty.values()) {
//			experimentBuilder.defineGlobalProperty(globalProperty, globalProperty.getPropertyDefinition());
//		}
//
//		for (CompartmentProperty compartmentProperty : CompartmentProperty.values()) {
//			experimentBuilder.defineCompartmentProperty(compartmentProperty.getCompartment(), compartmentProperty, compartmentProperty.getPropertyDefinition());
//		}
//	}
//
//	private void addIdentifiers(ExperimentBuilder experimentBuilder) {
//
//	
//		
//		experimentBuilder.addCompartmentId(Compartment.SUSCEPTIBLE, () -> new SusceptibleCompartment()::init);
//		experimentBuilder.addCompartmentId(Compartment.EXPOSED, () -> new ExposedCompartment()::init);
//		experimentBuilder.addCompartmentId(Compartment.INFECTED, () -> new InfectedCompartment()::init);
//		experimentBuilder.addCompartmentId(Compartment.DEAD, () -> new DeadCompartment()::init);
//		experimentBuilder.addGlobalComponentId(GlobalComponent.POPULATION_LOADER, () -> new PopulationLoader()::init);
//		experimentBuilder.addGlobalComponentId(GlobalComponent.IMMUNIZER, () -> new Immunizer()::init);
//
//		experimentBuilder.addMaterialsProducerId(MaterialsProducers.PRODUCER_ALPHA, () -> new ProducerAlpha()::init);
//		experimentBuilder.addMaterialsProducerId(MaterialsProducers.PRODUCER_BETA, () -> new ProducerBeta()::init);
//		experimentBuilder.addMaterialsProducerId(MaterialsProducers.PRODUCER_GAMMA, () -> new ProducerGamma()::init);
//
//		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
//			experimentBuilder.addRandomNumberGeneratorId(testRandomGeneratorId);
//		}
//
//		for (Resource resource : Resource.values()) {
//			experimentBuilder.addResource(resource);
//			// experimentBuilder.setResourceTimeTracking(resource,
//			// TimeTrackingPolicy.TRACK_TIME);
//		}
//
//		for (Material material : Material.values()) {
//			experimentBuilder.addMaterial(material);
//		}
//
//		for (GroupType groupType : GroupType.values()) {
//			experimentBuilder.addGroupTypeId(groupType);
//		}
//
//	}
//
//	private void addPropertyValues(ExperimentBuilder experimentBuilder) {
//
//		Builder builder = PopulationDescription.builder();
//		try {
//			Files.readAllLines(populationPath).stream().skip(1).forEach(line -> {
//				String[] strings = line.split(",", -1);
//				int age = Integer.parseInt(strings[0]);
//				String homeId = strings[1];
//				String schoolId = strings[2];
//				String workPlaceId = strings[3];
//				PopulationElement populationElement = new PopulationElement(age, homeId, schoolId, workPlaceId);
//				builder.addPopulationElement(populationElement);
//			});
//		} catch (IOException e) {
//			throw new RuntimeException();
//		}
//		PopulationDescription populationDescription = builder.build();
//		experimentBuilder.addGlobalPropertyValue(GlobalProperty.POPULATION_DESCRIPTION, populationDescription);
//
//		experimentBuilder.addCompartmentPropertyValue(Compartment.INFECTED, CompartmentProperty.WEIGHT_THRESHOLD, 5d);
//
//		// experimentBuilder.addGlobalPropertyValue(GlobalProperty.ALPHA, 3.7);
//		// experimentBuilder.addGlobalPropertyValue(GlobalProperty.ALPHA, 4.2);
//		// experimentBuilder.addGlobalPropertyValue(GlobalProperty.ALPHA, 5.1);
//
//	}
//
//	private List<String> addRegions(ExperimentBuilder experimentBuilder) throws IOException {
//
//		List<String> result = new ArrayList<>();
//		Set<String> regionStrings = Files.readAllLines(populationPath).stream().skip(1).map(line -> {
//			String[] strings = line.split(",", -1);
//			String homeId = strings[1];
//			return homeId.substring(0, 11);
//		}).collect(Collectors.toCollection(LinkedHashSet::new));
//
//		Files.readAllLines(tractsPath).stream().skip(1).filter(line -> {
//			String[] strings = line.split(",");
//			String name = strings[0];
//			return regionStrings.contains(name);
//		}).forEach(line -> {
//			String[] strings = line.split(",");
//			String name = strings[0];
//			result.add(name);
//			double lon = Double.parseDouble(strings[1]);
//			double lat = Double.parseDouble(strings[2]);
//			TractId tractId = new TractId(name);
//			experimentBuilder.addRegionId(tractId, () -> new StandardRegion()::init);
//			experimentBuilder.addRegionPropertyValue(tractId, RegionProperty.LAT, lat);
//			experimentBuilder.addRegionPropertyValue(tractId, RegionProperty.LON, lon);
//			experimentBuilder.addRegionResourceLevel(tractId, Resource.RESOURCE_1, 1000);
//			experimentBuilder.addRegionResourceLevel(tractId, Resource.RESOURCE_2, 10000);
//			experimentBuilder.addRegionResourceLevel(tractId, Resource.RESOURCE_3, 1000);
//		});
//		return result;
//
//	}
//
//	private static class TriggerComponentId implements GlobalComponentId {
//		private final String name;
//
//		public TriggerComponentId(String name) {
//			this.name = name;
//		}
//
//		@Override
//		public String toString() {
//			return name;
//		}
//	}
//
//	private void workWithTriggers(ExperimentBuilder experimentBuilder, List<String> regionNames) {
//
//		TriggerContainer.Builder builder = TriggerContainer.builder();
//
//		for (String regionName : regionNames) {
//
//			TractId tractId = new TractId(regionName);
//			TriggerComponentId triggerComponentId = new TriggerComponentId(regionName);
//			RegionResourceTrigger regionResourceTrigger = new RegionResourceTrigger(tractId, Resource.RESOURCE_1, 0.1);
//			builder.addTrigger(triggerComponentId, regionResourceTrigger);
//			experimentBuilder.addGlobalComponentId(triggerComponentId, () -> new RegionResourceTriggerComponent()::init);
//		}
//
//		experimentBuilder.defineGlobalProperty(TriggerContainer.TRIGGER_CONTAINER, TriggerContainer.getTriggerContainerPropertyDefinition());
//		experimentBuilder.addGlobalPropertyValue(TriggerContainer.TRIGGER_CONTAINER, builder.build());
//	}
//
//	private void execute() throws IOException {
//		// select an output directory
//		Path outputdirectory = Paths.get("c:\\temp\\gcm");
//		// Files.createTempDirectory("gcm");
//
//		// build the experiment
//		ExperimentBuilder experimentBuilder = new ExperimentBuilder();
//
//		experimentBuilder.setBaseScenarioId(100);
//
//		NIOReportItemHandler.Builder nioReportItemHandlerBuilder = NIOReportItemHandler.builder();
//		
//		
//		ReportId reportId = new SimpleReportId(RegionPropertyReport.class);
//		experimentBuilder.addReportId(reportId,()->new RegionPropertyReport(RegionProperty.LAT,RegionProperty.LON)::init);
//		nioReportItemHandlerBuilder.addReport(reportId, outputdirectory.resolve("region property report.xls"));
//
//		
//		reportId = new SimpleReportId(CompartmentPopulationReport.class);
//		experimentBuilder.addReportId(reportId,()->new CompartmentPopulationReport(ReportPeriod.DAILY)::init);
//		nioReportItemHandlerBuilder.addReport(reportId, outputdirectory.resolve("compartment population report.xls"));
//		
//		
//
//		defineProperties(experimentBuilder);
//		addIdentifiers(experimentBuilder);
//		addPropertyValues(experimentBuilder);
//		List<String> regionNames = addRegions(experimentBuilder);
//
//		workWithTriggers(experimentBuilder, regionNames);
//		Experiment experiment = experimentBuilder.build();
//		
//		nioReportItemHandlerBuilder.setRegularExperiment(experiment);
//		NIOReportItemHandler nioReportItemHandler = nioReportItemHandlerBuilder.build();
//		
//		// prepare the experiment executor
//		ExperimentExecutor experimentExecutor = new ExperimentExecutor();
//
//		// base settings
//		experimentExecutor.setExperiment(experiment);
//		experimentExecutor.addOutputItemHandler(nioReportItemHandler);
//		experimentExecutor.setSeed(345345L);
//		experimentExecutor.setThreadCount(0);
//		experimentExecutor.setReplicationCount(1);
//
//		// add console output
//		experimentExecutor.setProduceSimulationStatusOutput(true);
//		// experimentExecutor.setLogItemHandler(localLogItemHandler);
//
//		// add profile data reports
//		// experimentExecutor.setProfileReport(outputdirectory.resolve("profile
//		// report.xls"));
//
//		// experimentExecutor.setPlanningQueueReport(outputdirectory.resolve("planning
//		// queue report.xls"), 300);
//
//		// experimentExecutor.setMemoryReport(outputdirectory.resolve("memory
//		// report.xls"), 10);
//
//		// add some standard reports
//
//		// experimentExecutor.addExperimentColumnReport(outputdirectory.resolve("experiment
//		// column report.xls"));
//
//		// experimentExecutor.addCompartmentTransferReport(outputdirectory.resolve("compartment
//		// report.xls"),ReportPeriod.DAILY);
//
//		// experimentExecutor.addGroupPopulationReport(outputdirectory.resolve("group
//		// population report.xls"),ReportPeriod.DAILY);
//
//		// experimentExecutor.addStageReport(outputdirectory.resolve("stage
//		// report.xls"));
//
//		// experimentExecutor.addBatchStatusReport(outputdirectory.resolve("batch
//		// status report.xls"));
//		
//
//		
//		// add a custom report
//
//		// experimentExecutor.addCustomReport(outputdirectory.resolve("person
//		// life report.xls"), PersonLifeReport.class, new PersonId(1234));
//
//		// turn on experiment progress halt and resume
//
//		// experimentExecutor.setExperimentProgressLog(outputdirectory.resolve("experiment
//		// progress log.xls"));
//
//		// execute the experiment
//
//		// experimentExecutor.addGlobalPropertyReport(outputdirectory.resolve("global
//		// property report.xls"),GlobalProperty.POPULATION_PATH);
//
//		// experimentExecutor.setProfileReport(outputdirectory.resolve("profile
//		// report.xls"));
//		experimentExecutor.execute();
//	}
//
//	public static void main(String[] args) throws IOException {
//		Path tractsPath = Paths.get(args[0]);
//		Path populationPath = Paths.get(args[1]);
//		// Path tractsPath =
//		// Paths.get("C:\\hhs-io\\hhs-core-flu\\input\\population\\tracts\\tract-ids.csv");
//		// Path populationPath =
//		// Paths.get("C:\\hhs-io\\hhs-core-flu\\input\\population\\dc.csv");
//
//		new DemoRunner(tractsPath, populationPath).execute();
//	}
}
