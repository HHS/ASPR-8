package plugins.materials.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.SimpleReportId;
import plugins.stochastics.StochasticsDataManager;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = StageReport.class)
public final class AT_StageReport {

	private static enum Action {
		CREATED("Create"),

		DESTROYED("Destroy"),

		OFFERED("Offer"),

		TRANSFERRED("Transfer");

		private final String displayName;

		private Action(final String displayName) {
			this.displayName = displayName;
		}
	}

	private ReportItem getReportItem(ActorContext agentContext, StageId stageId, MaterialsProducerId materialsProducerId, boolean isOffered, Action action) {

		return ReportItem	.builder()//
							.setReportId(REPORT_ID)//
							.setReportHeader(REPORT_HEADER)//
							.addValue(agentContext.getTime())//
							.addValue(stageId)//
							.addValue(materialsProducerId)//
							.addValue(action.displayName)//
							.addValue(isOffered)//
							.build();//
	}

	public void testConstructor() {

	}
	
	@Test
	@UnitTestMethod(name = "init", args = {ActorContext.class})
	public void testInit() {
		/*
		 * Create containers for the expected and actual report items that will
		 * be generated by various stage related events.
		 * 
		 */
		Set<ReportItem> expectedReportItems = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// Generate 500 stage-based actions and record the expected report items
		RandomGenerator rg = RandomGeneratorProvider.getRandomGenerator(8635270533185454765L);

		double actionTime = 0;

		// create a list of 500 producer ids at random
		List<TestMaterialsProducerId> producerIds = new ArrayList<>();
		for (int i = 0; i < 500; i++) {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(rg);
			producerIds.add(testMaterialsProducerId);
		}

		// for each producer id, execute and action and increment time for the
		// plan
		for (TestMaterialsProducerId testMaterialsProducerId : producerIds) {

			/*
			 * Have the producer execute one of the four actions: stage
			 * creation, stage destruction, stage offer status change and stage
			 * transfer
			 * 
			 */
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
				List<StageId> offeredStages = materialsDataManager.getOfferedStages(testMaterialsProducerId);

				// select one of the four possible actions at random based on
				// the availability of the action

				// should we create a stage? -- we can always create a new
				// stage, so assume that is what we will do
				Action action = Action.CREATED;
				int candidateActionCount = 1;

				// should we destroy a non-offered stage?
				if (stages.size() > offeredStages.size()) {
					candidateActionCount++;
					if (randomGenerator.nextDouble() < 1.0 / candidateActionCount) {
						action = Action.DESTROYED;
					}
				}

				// should we transfer an offered stage?
				if (offeredStages.size() > 0) {
					candidateActionCount++;
					if (randomGenerator.nextDouble() < 1.0 / candidateActionCount) {
						action = Action.TRANSFERRED;
					}
				}

				// should we change the offer state of a stage?
				if (stages.size() > 0) {
					candidateActionCount++;
					if (randomGenerator.nextDouble() < 1.0 / candidateActionCount) {
						action = Action.OFFERED;
					}
				}
				
				StageId stageId;
				boolean stageOffered;
				switch (action) {
				case CREATED:
					stageId = materialsDataManager.addStage(testMaterialsProducerId);
					stageOffered = materialsDataManager.isStageOffered(stageId);
					expectedReportItems.add(getReportItem(c, stageId, testMaterialsProducerId, stageOffered, action));
					break;
				case DESTROYED:
					stages.removeAll(offeredStages);
					stageId = stages.get(randomGenerator.nextInt(stages.size()));
					stageOffered = materialsDataManager.isStageOffered(stageId);
					materialsDataManager.removeStage(stageId, false);					
					expectedReportItems.add(getReportItem(c, stageId, testMaterialsProducerId, stageOffered, action));
					break;
				case OFFERED:
					stageId = stages.get(randomGenerator.nextInt(stages.size()));
					stageOffered = materialsDataManager.isStageOffered(stageId);
					materialsDataManager.setStageOfferState(stageId, !stageOffered);
					expectedReportItems.add(getReportItem(c, stageId, testMaterialsProducerId, !stageOffered, action));
					break;
				case TRANSFERRED:
					stageId = offeredStages.get(randomGenerator.nextInt(offeredStages.size()));
					stageOffered = materialsDataManager.isStageOffered(stageId);
					List<MaterialsProducerId> candidateProducerIds = new ArrayList<>(materialsDataManager.getMaterialsProducerIds());
					candidateProducerIds.remove(testMaterialsProducerId);
					MaterialsProducerId materialsProducerId = candidateProducerIds.get(randomGenerator.nextInt(candidateProducerIds.size()));
					materialsDataManager.transferOfferedStage(stageId, materialsProducerId);
					expectedReportItems.add(getReportItem(c, stageId, materialsProducerId, true, action));
					expectedReportItems.add(getReportItem(c, stageId, materialsProducerId, false, Action.OFFERED));
					break;
				default:
					throw new RuntimeException("unhandled action type");
				}

			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		Set<ReportItem> actualReportItems = MaterialsActionSupport.testConsumers(542686524159732447L, testPlugin, new StageReport(REPORT_ID)::init);

		assertEquals(expectedReportItems, actualReportItems);
	}

	private static final ReportId REPORT_ID = new SimpleReportId("report");

	private static final ReportHeader REPORT_HEADER = getReportHeader();

	private static ReportHeader getReportHeader() {
		return ReportHeader	.builder()//
							.add("time")//
							.add("stage")//
							.add("materials_producer")//
							.add("action")//
							.add("offered")//
							.build();

	}

}
