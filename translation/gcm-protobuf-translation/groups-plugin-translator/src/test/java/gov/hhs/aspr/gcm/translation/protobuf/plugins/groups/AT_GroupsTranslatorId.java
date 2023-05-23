package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestField;

public class AT_GroupsTranslatorId {

    @Test
    @UnitTestField(target = GroupsTranslatorId.class, name = "TRANSLATOR_ID")
    public void testTranslatorId() {
        assertNotNull(GroupsTranslatorId.TRANSLATOR_ID);
    }
}
