package plugins.stochastics.testsupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.wrappers.MutableBoolean;

@UnitTest(target = StochasticsActionSupport.class)
public class AT_StochasticsActionSupport {

	@Test
	@UnitTestMethod(name = "testConsumer", args = { long.class, Consumer.class })
	public void testTestConsumer() {
		MutableBoolean actorExecuted = new MutableBoolean();
		Consumer<ActorContext> consumer = (c) -> actorExecuted.setValue(true);
		StochasticsActionSupport.testConsumer(45235233432345378L, consumer);
		assertTrue(actorExecuted.getValue());
	}

	@Test
	@UnitTestMethod(name = "testConsumers", args = {long.class, Plugin.class })
	public void testTestConsumers() {
		MutableBoolean actorExecuted = new MutableBoolean();

		TestPluginData.Builder builder = TestPluginData.builder();
		builder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> actorExecuted.setValue(true)));
		TestPluginData testPluginData = builder.build();
		Plugin plugin = TestPlugin.getTestPlugin(testPluginData);
		StochasticsActionSupport.testConsumers(45235233432345378L, plugin);

		assertTrue(actorExecuted.getValue());
	}

}
