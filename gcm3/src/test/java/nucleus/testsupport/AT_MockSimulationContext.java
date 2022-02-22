package nucleus.testsupport;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;

@UnitTest(target = MockSimulationContext.class)
@Disabled
public class AT_MockSimulationContext {

	@Test
	public void test() {
		fail();
	}

	// private static class DataViewA implements DataView {
	// }
	//
	// private static class DataViewB implements DataView {
	// }
	//
	// private static class DataViewC implements DataView {
	// }
	//
	// /**
	// * Show that the data views retrieved by the context match what was
	// * published
	// */
	// @Test
	// @UnitTestMethod(name = "getDataView", args = { Class.class })
	// public void testGetDataView() {
	//
	// DataView dataView1 = new DataViewA();
	// DataView dataView2 = new DataViewB();
	// DataView dataView3 = new DataViewA();
	//
	// Map<Class<?>,Object> dataViewMap = new LinkedHashMap<>();
	// dataViewMap.put(dataView1.getClass(),dataView1);
	// dataViewMap.put(dataView2.getClass(),dataView2);
	// dataViewMap.put(dataView3.getClass(),dataView3);
	//
	// MockContext mockContext =
	// MockContext.builder().setDataViewFunction((c)->{
	// return dataViewMap.get(c);
	// }).build();
	//
	//
	// Optional<DataViewA> optionalA = mockContext.getDataView(DataViewA.class);
	// assertTrue(optionalA.isPresent());
	// assertEquals(dataView3, optionalA.get());
	//
	// Optional<DataViewB> optionalB = mockContext.getDataView(DataViewB.class);
	// assertTrue(optionalB.isPresent());
	// assertEquals(dataView2, optionalB.get());
	//
	// Optional<DataViewC> optionalC = mockContext.getDataView(DataViewC.class);
	// assertFalse(optionalC.isPresent());
	//
	// }
	//
	// /**
	// * Shows that time can be retrieved and is equal to the last time setting
	// or
	// * zero when time has not yet been set.
	// */
	//
	//
	//
	// @Test
	// @UnitTestMethod(name = "getTime", args = {})
	// public void testGetTime() {
	// MutableDouble time = new MutableDouble(0);
	// MockContext mockContext =
	// MockContext.builder().setTimeSupplier(()->time.getValue()).build();
	// assertEquals(0.0, mockContext.getTime());
	//
	// time.setValue(77.3);
	// assertEquals(time.getValue(), mockContext.getTime());
	//
	// time.setValue(-600.234);
	// assertEquals(time.getValue(), mockContext.getTime());
	//
	// time.setValue(2.3423);
	// assertEquals(time.getValue(), mockContext.getTime());
	//
	// }
	//
	//
	// /**
	// * Show that output can be released
	// */
	// @Test
	// @UnitTestMethod(name = "releaseOutput", args = { Object.class })
	// public void testReleaseOutput() {
	// List<Object> releasedOutput = new ArrayList<>();
	// MockContext mockContext =
	// MockContext.builder().setReleaseOutputConsumer((o)->releasedOutput.add(o)).build();
	// List<Object>expectedOutput = new ArrayList<>();
	//
	// expectedOutput.add(null);
	// expectedOutput.add(10);
	// expectedOutput.add("J");
	// expectedOutput.add(false);
	// expectedOutput.add(null);
	// expectedOutput.add(10);
	//
	// for(Object obj : expectedOutput) {
	// mockContext.releaseOutput(obj);
	// }
	//
	// assertEquals(expectedOutput, releasedOutput);
	//
	// }
	//
	//
	//
	// /**
	// * Show that a contract exception is thrown with the expected
	// ContractError
	// */
	// @Test
	// @UnitTestMethod(name = "throwContractException", args = {
	// ContractError.class })
	// public void testThrowContractException() {
	//
	// List<ContractError> expectedContractorErrors = new ArrayList<>();
	// List<ContractError> observedContractorErrors = new ArrayList<>();
	//
	// MockContext mockContext =
	// MockContext.builder().setContractErrorConsumer((c)->observedContractorErrors.add(c)).build();
	//
	// for (ContractError contractError : NucleusError.values()) {
	// expectedContractorErrors.add(contractError);
	// mockContext.throwContractException(contractError);
	// }
	//
	// assertEquals(expectedContractorErrors, observedContractorErrors);
	//
	// }
	//
	// /**
	// * Show that a contract exception is thrown with the expected
	// ContractError
	// * and details
	// */
	// @Test
	// @UnitTestMethod(name = "throwContractException", args = {
	// ContractError.class, Object.class })
	// public void testThrowContractException_Details() {
	// List<ContractError> expectedContractorErrors = new ArrayList<>();
	// List<Object> expectedDetails = new ArrayList<>();
	// List<ContractError> observedContractorErrors = new ArrayList<>();
	// List<Object> observedDetails = new ArrayList<>();
	//
	// MockContext mockContext =
	// MockContext.builder().setDetailedContractErrorConsumer((c,d)->{
	// observedContractorErrors.add(c);
	// observedDetails.add(d);
	// }).build();
	//
	// for (ContractError contractError : NucleusError.values()) {
	// Object details = contractError.toString();
	// expectedContractorErrors.add(contractError);
	// expectedDetails.add(details);
	// mockContext.throwContractException(contractError,details);
	// }
	//
	// assertEquals(expectedContractorErrors, observedContractorErrors);
	// assertEquals(expectedDetails, observedDetails);
	// assertEquals(expectedDetails.size(),expectedContractorErrors.size());
	// }

}
