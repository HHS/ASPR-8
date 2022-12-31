package plugins.people.testsupport;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestMethod;

public class AT_PeopleActionSupport {

	@Test
	@UnitTestMethod(target = PeopleActionSupport.class, name = "testConsumer", args = { Consumer.class }, tags = UnitTag.INCOMPLETE)
	public void testTestConsumer() {

	}

	@Test
	@UnitTestMethod(target = PeopleActionSupport.class, name = "testConsumers", args = { Plugin.class }, tags = UnitTag.INCOMPLETE)
	public void testTestConsumers() {

	}

}