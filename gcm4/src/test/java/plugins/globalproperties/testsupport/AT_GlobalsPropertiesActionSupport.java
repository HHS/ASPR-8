package plugins.globalproperties.testsupport;

import nucleus.Plugin;
import org.junit.jupiter.api.Test;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = GlobalsPropertiesActionSupport.class)

public class AT_GlobalsPropertiesActionSupport {

    @Test
    @UnitTestMethod(name = "testConsumer", args = {Consumer.class}, tags = {UnitTag.EMPTY})
    void testTestConsumer() {
    }

    @Test
    @UnitTestMethod(name = "testConsumers", args = {Plugin.class}, tags = {UnitTag.EMPTY})
    void testTestConsumers() {
    }
}