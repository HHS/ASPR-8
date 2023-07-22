package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestField;

public class AT_PartitionsTranslatorId {
    
    @Test
    @UnitTestField(target = PartitionsTranslatorId.class, name = "TRANSLATOR_ID")
    public void testTranslatorId() {
        assertNotNull(PartitionsTranslatorId.TRANSLATOR_ID);
    }
}
