package plugins.people.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.PersonDataManager;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;

@UnitTest(target = PeopleLoader.class)

public final class AT_PeopleLoader {

	@Test
	@UnitTestMethod(name = "init", args = { ActorContext.class })
	public void testInit() {
		int personCount = 30;
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		
		PeoplePluginData.Builder peopleDataBuilder = PeoplePluginData.builder();
		BulkPersonConstructionData.Builder	bulkBuilder =	BulkPersonConstructionData.builder();
		for (int i = 0; i < personCount; i++) {
			expectedPersonIds.add(new PersonId(i));
			bulkBuilder.add(PersonContructionData.builder().build());
		}
		peopleDataBuilder.addBulkPersonContructionData(bulkBuilder.build());
		PeoplePluginData peoplePluginData = peopleDataBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		
		TestPluginData.Builder pluginDataBuilder = TestPluginData	.builder();//
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c)->{
			Optional<PersonDataManager> optional = c.getDataManager(PersonDataManager.class);
			assertTrue(optional.isPresent());
			PersonDataManager personDataManager = optional.get();
			assertEquals(expectedPersonIds, new LinkedHashSet<>(personDataManager.getPeople()));
		}));
		
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getPlugin(testPluginData);
		
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Simulation	.builder()//
					.addPlugin(peoplePlugin)//
					.addPlugin(testPlugin)//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.build()//
					.execute();//

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}

}