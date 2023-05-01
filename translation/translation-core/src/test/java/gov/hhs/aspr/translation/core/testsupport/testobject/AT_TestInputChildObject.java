package gov.hhs.aspr.translation.core.testsupport.testobject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;

public class AT_TestInputChildObject {
    @Test
    @UnitTestConstructor(target = TestInputChildObject.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestInputChildObject());
    }
}
