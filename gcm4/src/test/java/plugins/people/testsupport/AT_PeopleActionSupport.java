package plugins.people.testsupport;

import nucleus.Plugin;
import org.junit.jupiter.api.Test;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

import java.util.function.Consumer;

@UnitTest(target = PeopleActionSupport.class)
public class AT_PeopleActionSupport {

    @Test
    @UnitTestMethod(name = "testConsumer", args = {Consumer.class}, tags = UnitTag.INCOMPLETE)
    public void testTestConsumer() {

    }

    @Test
    @UnitTestMethod(name = "testConsumers", args = {Plugin.class}, tags = UnitTag.INCOMPLETE)
    public void testTestConsumers() {

    }

}