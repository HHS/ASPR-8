package plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;

public class AT_ReportError {

	@Test
	@UnitTestMethod(target = ReportError.class,name = "getDescription", args = {})
	public void test() {
		//show that each description is a unique, non-null and non-empty string 
		Set<String> descriptions = new LinkedHashSet<>();
		for(ReportError reportError : ReportError.values()) {
			String description = reportError.getDescription();			
			assertNotNull(description,"null description for "+reportError);			
			assertTrue(description.length()>0, "empty string for "+reportError);
			boolean unique = descriptions.add(description);
			assertTrue(unique,"description for "+reportError+" is not unique");
		}
	}
}
