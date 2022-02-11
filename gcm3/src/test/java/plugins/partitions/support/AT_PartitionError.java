package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PartitionError.class)
public class AT_PartitionError {
	@Test
	@UnitTestMethod(name = "getDescription", args = {})
	public void test() {
		// show that each description is a unique, non-null and non-empty string
		Set<String> descriptions = new LinkedHashSet<>();
		for (PartitionError partitionError : PartitionError.values()) {
			String description = partitionError.getDescription();
			assertNotNull(description, "null description for " + partitionError);
			assertTrue(description.length() > 0, "empty string for " + partitionError);
			boolean unique = descriptions.add(description);
			assertTrue(unique, "description for " + partitionError + " is not unique");
		}
	}

}
