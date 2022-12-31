package plugins.globalproperties.testsupport;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestMethod;

public class AT_GlobalsPropertiesActionSupport {

	@Test
	@UnitTestMethod(target = GlobalsPropertiesActionSupport.class, name = "testConsumer", args = { Consumer.class }, tags = { UnitTag.INCOMPLETE })
	public void testTestConsumer() {
	}

	@Test
	@UnitTestMethod(target = GlobalsPropertiesActionSupport.class, name = "testConsumers", args = { Plugin.class }, tags = { UnitTag.INCOMPLETE })
	public void testTestConsumers() {
	}
}