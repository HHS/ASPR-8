package util.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_ContractException {

	@Test
	@UnitTestConstructor(target = ContractException.class, args = { ContractError.class })
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
	@UnitTestConstructor(target = ContractException.class, args = { ContractError.class, Object.class })
	public void testConstructor_Object() {
		ContractError contractError = new ContractError() {
			@Override
			public String getDescription() {
				return "";
			}
		};

		Object details = "details";
		ContractException contractException = new ContractException(contractError, details);
		assertEquals(contractError, contractException.getErrorType());
		assertEquals(contractError.getDescription() + ": " + details.toString(), contractException.getMessage());

		// precondition test: if the contract error is null
		assertThrows(NullPointerException.class, () -> new ContractException(null, details));

		// precondition test: if the details value is null
		assertThrows(NullPointerException.class, () -> new ContractException(contractError, null));

	}

	@Test
	@UnitTestMethod(target = ContractException.class, name = "getErrorType", args = {}, tags = { UnitTag.LOCAL_PROXY })
	public void testGetErrorType() {
		// covered by constructor tests
	}

}
