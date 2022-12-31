package plugins.materials.testsupport;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestMethod;

public class AT_MaterialsActionSupport {

	@Test
	@UnitTestMethod(target = MaterialsActionSupport.class, name = "testConsumer", args = { long.class, Consumer.class }, tags = { UnitTag.INCOMPLETE })
	public void testTestConsumer() {
		// TBD
	}

	@Test
	@UnitTestMethod(target = MaterialsActionSupport.class, name = "testConsumers", args = { long.class, Plugin.class }, tags = { UnitTag.INCOMPLETE })
	public void testTestConsumers2() {
		// TBD
	}

	@Test
	@UnitTestMethod(target = MaterialsActionSupport.class, name = "testConsumers", args = { long.class, Plugin.class, Consumer.class }, tags = { UnitTag.INCOMPLETE })
	public void testTestConsumers() {
		// TBD
	}
}
