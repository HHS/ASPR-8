package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import tools.annotations.UnitTestMethod;
import util.wrappers.MutableBoolean;

public class AT_TestPluginFactory {
    private Consumer<ActorContext> factoryConsumer(MutableBoolean executed) {
		return (c) -> {
			executed.setValue(true);
		};
	}

    @Test
	@UnitTestMethod(target = TestPluginFactory.class, name = "factory", args = { Consumer.class })
	public void testFactory1() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation
				.executeSimulation(TestPluginFactory.factory(factoryConsumer(executed)).getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = TestPluginFactory.class, name = "factory", args = { TestPluginData.class })
	public void testFactory2() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(executed)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(TestPluginFactory.factory(testPluginData).getPlugins());
		assertTrue(executed.getValue());

	}
}
