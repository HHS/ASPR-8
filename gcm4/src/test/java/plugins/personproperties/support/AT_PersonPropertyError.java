package plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertyError.class)
public class AT_PersonPropertyError {

	@Test
	@UnitTestMethod(name = "getDescription", args = {})
	public void test() {
		//show that each description is a unique, non-null and non-empty string 
		Set<String> descriptions = new LinkedHashSet<>();
		for(PersonPropertyError personPropertyError : PersonPropertyError.values()) {
			String description = personPropertyError.getDescription();			
			assertNotNull(description,"null description for "+personPropertyError);			
			assertTrue(description.length()>0, "empty string for "+personPropertyError);
			boolean unique = descriptions.add(description);
			assertTrue(unique,"description for "+personPropertyError+" is not unique");
		}
	}
}
