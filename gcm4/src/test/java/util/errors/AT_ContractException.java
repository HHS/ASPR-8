package util.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = ContractException.class)
public class AT_ContractException {

	@Test
	@UnitTestConstructor(args = { ContractError.class })
	public void testConstructor() {

		ContractError contractError = new ContractError() {
			@Override
			public String getDescription() {
				return "description";
			}
		};

		ContractException contractException = new ContractException(contractError);
		assertEquals(contractError, contractException.getErrorType());
		assertEquals(contractError.getDescription(), contractException.getMessage());

		// precondition test: if the contract error is null
		assertThrows(NullPointerException.class, () -> new ContractException(null));
	}

	@Test
	@UnitTestConstructor(args = { ContractError.class, Object.class })
	public void testConstructor_Object() {
		ContractError contractError = new ContractError() {
			@Override
			public String getDescription() {
				return "";
			}
		};

		Object details = "details";
		ContractException contractException = new ContractException(contractError,details);
		assertEquals(contractError, contractException.getErrorType());
		assertEquals(contractError.getDescription()+": "+details.toString(), contractException.getMessage());

		// precondition test: if the contract error is null
		assertThrows(NullPointerException.class, () -> new ContractException(null, details));
		
		// precondition test: if the details value is null
		assertThrows(NullPointerException.class, () -> new ContractException(contractError, null));

	}

	@Test
	@UnitTestMethod(name = "getErrorType",args = {},tags= {UnitTag.LOCAL_PROXY})
	public void testGetErrorType() {
		//covered by constructor tests
	}

}
