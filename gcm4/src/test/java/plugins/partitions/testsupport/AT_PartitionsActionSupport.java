package plugins.partitions.testsupport;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestMethod;

public class AT_PartitionsActionSupport {

	@Test
	@UnitTestMethod(target = PartitionsActionSupport.class, name = "testConsumer", args = { int.class, long.class, Consumer.class }, tags = { UnitTag.INCOMPLETE })
	public void testTestConsumer() {
		// TBD
	}

	@Test
	@UnitTestMethod(target = PartitionsActionSupport.class, name = "testConsumers", args = { int.class, long.class, Plugin.class }, tags = { UnitTag.INCOMPLETE })
	public void testTestConsumers() {
		// TBD
	}
}
