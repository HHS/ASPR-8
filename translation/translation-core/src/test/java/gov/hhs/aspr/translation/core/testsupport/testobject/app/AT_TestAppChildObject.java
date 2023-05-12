package gov.hhs.aspr.translation.core.testsupport.testobject.app;

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
