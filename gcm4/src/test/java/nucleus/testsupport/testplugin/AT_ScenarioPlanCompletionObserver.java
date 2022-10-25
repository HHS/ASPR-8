package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.Simulation;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = ScenarioPlanCompletionObserver.class)
public class AT_ScenarioPlanCompletionObserver {

	@Test
	@UnitTestMethod(name = "allPlansExecuted", args = {})
	public void testAllPlansExecuted() {
		/*
		 * If there is no test plugin, then there will no plans to execute
		 */
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.build()//
					.execute();

		boolean allPlansExecuted = scenarioPlanCompletionObserver.allPlansExecuted();
		assertFalse(allPlansExecuted);

		/*
		 * With a test plugin, but no plans, there will still be no plans
		 * executed
		 */
		TestPluginData testPluginData = TestPluginData.builder().build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.build()//
					.execute();

		allPlansExecuted = scenarioPlanCompletionObserver.allPlansExecuted();
		assertFalse(allPlansExecuted);

		/*
		 * With a test plugin, but and at least one plan, all plans will execute
		 * if nothing halts the simulation
		 */
		testPluginData = TestPluginData//
										.builder()//
										.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
										}))//
										.build();
		testPlugin = TestPlugin.getTestPlugin(testPluginData);
		scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.build()//
					.execute();

		allPlansExecuted = scenarioPlanCompletionObserver.allPlansExecuted();
		assertTrue(allPlansExecuted);

		/*
		 * With a test plugin, if the simulation halts before all plans can
		 * execute then we expect a false return .
		 */
		testPluginData = TestPluginData//
										.builder()//
										.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
										}))//
										.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
											c.halt();
										}))//
										.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
										}))//
										.build();
		testPlugin = TestPlugin.getTestPlugin(testPluginData);
		scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.build()//
					.execute();

		allPlansExecuted = scenarioPlanCompletionObserver.allPlansExecuted();
		assertFalse(allPlansExecuted);

	}

	@Test
	@UnitTestMethod(name = "handleOutput", args = { Object.class }, tags= {UnitTag.LOCAL_PROXY})
	public void testHandleOutput() {
		//covered by testAllPlansExecuted()
	}

	@Test
	@UnitTestConstructor(args = {}, tags = { UnitTag.EMPTY })
	public void testConstructor() {
		// nothing to test
	}
}
