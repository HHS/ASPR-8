package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestField;

public class AT_ResourcesTranslatorId {

    @Test
    @UnitTestField(target = ResourcesTranslatorId.class, name = "TRANSLATOR_ID")
    public void testTranslatorId() {
        assertNotNull(ResourcesTranslatorId.TRANSLATOR_ID);
    }
}
