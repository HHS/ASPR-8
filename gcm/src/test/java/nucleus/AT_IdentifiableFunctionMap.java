package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_IdentifiableFunctionMap {
	@Test
	@UnitTestMethod(target = IdentifiableFunctionMap.class, name = "builder", args = { Class.class })
	public void testBuilder() {
		// show that the builder is returned
		assertNotNull(IdentifiableFunctionMap.builder(Object.class));

		// precondition test: if the class reference is null
		ContractException contractException = assertThrows(ContractException.class, () -> IdentifiableFunctionMap.builder(null));
		assertEquals(NucleusError.NULL_CLASS_REFERENCE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = IdentifiableFunctionMap.class, name = "get", args = { Object.class })
	public void testGet() {
		IdentifiableFunctionMap<Integer> functionMap = //
				IdentifiableFunctionMap	.builder(Integer.class)//
										.put("A", (n) -> false)//
										.put("B", (n) -> "b")//
										.put("C", (n) -> n)//
										.build();
		IdentifiableFunction<Integer> identifiableFunction = functionMap.get("A");
		assertEquals(false, identifiableFunction.getFunction().apply(13));

		identifiableFunction = functionMap.get("B");
		assertEquals("b", identifiableFunction.getFunction().apply(45));

		identifiableFunction = functionMap.get("C");
		assertEquals(88, identifiableFunction.getFunction().apply(88));

		// if the function id is null
		ContractException contractException = assertThrows(ContractException.class, () -> IdentifiableFunctionMap.builder(Integer.class).build().get(null));//
		assertEquals(NucleusError.NULL_FUNCTION_ID, contractException.getErrorType());

		// if the function id is unknown
		contractException = assertThrows(ContractException.class, () -> IdentifiableFunctionMap.builder(Integer.class).build().get("unknown id"));//
		assertEquals(NucleusError.UNKNOWN_FUNCTION_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = IdentifiableFunctionMap.Builder.class, name = "put", args = { Object.class, Function.class })
	public void testPut() {
		IdentifiableFunctionMap<Integer> functionMap = //
				IdentifiableFunctionMap	.builder(Integer.class)//
										.put("A", (n) -> false)//
										.put("B", (n) -> "b")//
										.put("C", (n) -> n)//
										.build();
		IdentifiableFunction<Integer> identifiableFunction = functionMap.get("A");
		assertEquals(false, identifiableFunction.getFunction().apply(13));

		identifiableFunction = functionMap.get("B");
		assertEquals("b", identifiableFunction.getFunction().apply(45));

		identifiableFunction = functionMap.get("C");
		assertEquals(88, identifiableFunction.getFunction().apply(88));

		// if the function id is null
		ContractException contractException = assertThrows(ContractException.class, () -> IdentifiableFunctionMap.builder(Integer.class).put(null, (n) -> 3));//
		assertEquals(NucleusError.NULL_FUNCTION_ID, contractException.getErrorType());

		// if the function is null
		contractException = assertThrows(ContractException.class, () -> IdentifiableFunctionMap.builder(Integer.class).put("A", null));//
		assertEquals(NucleusError.NULL_FUNCTION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = IdentifiableFunctionMap.Builder.class, name = "build", args = {})
	public void testBuild() {
		// covered by the other tests
	}

}
