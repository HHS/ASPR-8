package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestField;

public class AT_GlobalPropertiesTranslatorId {

    @Test
    @UnitTestField(target = GlobalPropertiesTranslatorId.class, name = "TRANSLATOR_ID")
    public void testTranslatorId() {
        assertNotNull(GlobalPropertiesTranslatorId.TRANSLATOR_ID);
    }
}
