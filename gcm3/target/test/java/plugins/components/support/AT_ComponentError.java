package plugins.components.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = ComponentError.class)
public class AT_ComponentError {

	@Test
	@UnitTestMethod(name = "getDescription", args = {})
	public void test() {
		//show that each description is a unique, non-null and non-empty string 
		Set<String> descriptions = new LinkedHashSet<>();
		for(ComponentError componentError : ComponentError.values()) {
			String description = componentError.getDescription();			
			assertNotNull(description,"null description for "+componentError);			
			assertTrue(description.length()>0, "empty string for "+componentError);
			boolean unique = descriptions.add(description);
			assertTrue(unique,"description for "+componentError+" is not unique");
		}
	}
}
