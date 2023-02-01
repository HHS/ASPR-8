package plugins.resources.testsupport;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestMethod;

public class AT_ResourcesActionSupport {

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "testConsumer", args = { int.class, long.class, Consumer.class }, tags = UnitTag.INCOMPLETE)
	public void testTestConsumer() {
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "testConsumers", args = { int.class, long.class, Plugin.class }, tags = UnitTag.INCOMPLETE)
	public void testTestConsumers() {
	}
}