package plugins.globalproperties.support;

import org.junit.jupiter.api.Test;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.support.GroupError;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target =GlobalPropertyInitialization.class)

public class AT_GlobalPropertyInitialization {

    @Test
    @UnitTestMethod(name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(GlobalPropertyInitialization.builder());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyInitialization.Builder.class, name = "build", args = {})
    public void testBuild() {
        // precondition test: if the property definition was not set
        SimpleGlobalPropertyId simpleGlobalPropertyId = new SimpleGlobalPropertyId(1);
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(true).build();
        ContractException propertyContractException = assertThrows(PropertyError.NULL_PROPERTY_DEFINITION, GlobalPropertyInitialization.builder()
                .setGlobalPropertyId(simpleGlobalPropertyId).setPropertyDefinition());
        // Problems here not being able to try and create a contract exception


        ContractException contractException = assertThrows(ContractException.class, () -> GlobalPropertyInitialization.builder().build());
        assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
    }

    @Test
    void getGlobalPropertyId() {

    }

    @Test
    void getPropertyDefinition() {
    }

    @Test
    void getValue() {
    }
}