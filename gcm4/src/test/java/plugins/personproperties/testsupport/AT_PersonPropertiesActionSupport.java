package plugins.personproperties.testsupport;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertiesActionSupport.class)
public class AT_PersonPropertiesActionSupport {
    @Test
    @UnitTestMethod(name = "testConsumer", args = { int.class, long.class, Consumer.class }, tags = {
            UnitTag.INCOMPLETE })
    public void testTestConsumer() {
        // TBD
    }

    @Test
    @UnitTestMethod(name = "testConsumers", args = { int.class, long.class, Plugin.class }, tags = {
            UnitTag.INCOMPLETE })
    public void testTestConsumers() {
        // TBD
    }
}