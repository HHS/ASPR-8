package gov.hhs.aspr.ms.taskit.core.testsupport.testobject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;

public class AT_TestAppChildObject {

    @Test
    @UnitTestConstructor(target = TestAppChildObject.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestAppChildObject());
    }
}
