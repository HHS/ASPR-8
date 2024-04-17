package gov.hhs.aspr.ms.gcm.pipeline.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_PipelineError {
    
    @Test
    @UnitTestMethod(target = PipelineError.class, name = "getDescription", args = {})
    public void testGetDescription() {
        // show that each description is a unique, non-null and non-empty string
        Set<String> descriptions = new LinkedHashSet<>();
        for (PipelineError personBlockError : PipelineError.values()) {
            String description = personBlockError.getDescription();
            assertNotNull(description, "null description for " + personBlockError);
            assertTrue(description.length() > 0, "empty string for " + personBlockError);
            boolean unique = descriptions.add(description);
            assertTrue(unique, "description for " + personBlockError + " is not unique");
        }
    }
}
