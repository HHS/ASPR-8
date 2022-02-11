package nucleus.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import nucleus.DataView;
import nucleus.NucleusError;
import util.ContractError;
import util.MutableDouble;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = MockSimulationContext.class)
public class AT_MockContext {

	private static class DataViewA implements DataView {
	}

	private static class DataViewB implements DataView {
	}

	private static class DataViewC implements DataView {
	}

	/**
	 * Show that the data views retrieved by the context match what was
	 * published
	 */
	@Test
	@UnitTestMethod(name = "getDataView", args = { Class.class })
	public void testGetDataView() {
		
		DataView dataView1 = new DataViewA();
		DataView dataView2 = new DataViewB();
		DataView dataView3 = new DataViewA();
		
		Map<Class<?>,Object> dataViewMap = new LinkedHashMap<>();
		dataViewMap.put(dataView1.getClass(),dataView1);
		dataViewMap.put(dataView2.getClass(),dataView2);
		dataViewMap.put(dataView3.getClass(),dataView3);
		
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setDataViewFunction((c)->{
			return dataViewMap.get(c);
		}).build();
		

		Optional<DataViewA> optionalA = mockSimulationContext.getDataView(DataViewA.class);
		assertTrue(optionalA.isPresent());
		assertEquals(dataView3, optionalA.get());

		Optional<DataViewB> optionalB = mockSimulationContext.getDataView(DataViewB.class);
		assertTrue(optionalB.isPresent());
		assertEquals(dataView2, optionalB.get());

		Optional<DataViewC> optionalC = mockSimulationContext.getDataView(DataViewC.class);
		assertFalse(optionalC.isPresent());

	}

	/**
	 * Shows that time can be retrieved and is equal to the last time setting or
	 * zero when time has not yet been set.
	 */
	
	
	
	@Test
	@UnitTestMethod(name = "getTime", args = {})
	public void testGetTime() {
		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setTimeSupplier(()->time.getValue()).build();
		assertEquals(0.0, mockSimulationContext.getTime());
		
		time.setValue(77.3);
		assertEquals(time.getValue(), mockSimulationContext.getTime());

		time.setValue(-600.234);		
		assertEquals(time.getValue(), mockSimulationContext.getTime());

		time.setValue(2.3423);		
		assertEquals(time.getValue(), mockSimulationContext.getTime());

	}

	
	/**
	 * Show that output can be released
	 */
	@Test
	@UnitTestMethod(name = "releaseOutput", args = { Object.class })
	public void testReleaseOutput() {
		List<Object> releasedOutput = new ArrayList<>();
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setReleaseOutputConsumer((o)->releasedOutput.add(o)).build();
		List<Object>expectedOutput = new ArrayList<>();
		
		expectedOutput.add(null);
		expectedOutput.add(10);
		expectedOutput.add("J");
		expectedOutput.add(false);
		expectedOutput.add(null);
		expectedOutput.add(10);
		
		for(Object obj : expectedOutput) {
			mockSimulationContext.releaseOutput(obj);
		}
		
		assertEquals(expectedOutput, releasedOutput);

	}

	

	/**
	 * Show that a contract exception is thrown with the expected ContractError
	 */
	@Test
	@UnitTestMethod(name = "throwContractException", args = { ContractError.class })
	public void testThrowContractException() {
		
		List<ContractError> expectedContractorErrors = new ArrayList<>();
		List<ContractError> observedContractorErrors = new ArrayList<>();
		
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setContractErrorConsumer((c)->observedContractorErrors.add(c)).build();

		for (ContractError contractError : NucleusError.values()) {
			expectedContractorErrors.add(contractError);
			mockSimulationContext.throwContractException(contractError);			
		}
		
		assertEquals(expectedContractorErrors, observedContractorErrors);

	}

	/**
	 * Show that a contract exception is thrown with the expected ContractError
	 * and details
	 */
	@Test
	@UnitTestMethod(name = "throwContractException", args = { ContractError.class, Object.class })
	public void testThrowContractException_Details() {
		List<ContractError> expectedContractorErrors = new ArrayList<>();
		List<Object> expectedDetails = new ArrayList<>();
		List<ContractError> observedContractorErrors = new ArrayList<>();
		List<Object> observedDetails = new ArrayList<>();
		
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setDetailedContractErrorConsumer((c,d)->{
			observedContractorErrors.add(c);
			observedDetails.add(d);
			}).build();

		for (ContractError contractError : NucleusError.values()) {
			Object details = contractError.toString();
			expectedContractorErrors.add(contractError);
			expectedDetails.add(details);
			mockSimulationContext.throwContractException(contractError,details);			
		}
		
		assertEquals(expectedContractorErrors, observedContractorErrors);
		assertEquals(expectedDetails, observedDetails);
		assertEquals(expectedDetails.size(),expectedContractorErrors.size());
	}

}
