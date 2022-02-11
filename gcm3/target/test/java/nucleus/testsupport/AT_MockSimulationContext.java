package nucleus.testsupport;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.math3.util.MathArrays.Function;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = MockSimulationContext.class)
public class AT_MockSimulationContext {

	// private static class DataViewA extends ActionDataManager {
	// }
	//
	// private static class DataViewB extends ActionDataManager {
	// }
	//
	// private static class DataViewC extends ActionDataManager {
	// }

	@Test
	@UnitTestMethod(name = "builder", args = { Class.class })
	public void testBuilder() {
		fail();
	}

	@Test
	@UnitTestMethod(name = "getDataManager", args = { Class.class })
	public void testGetDataManager() {
		fail();
		// DataViewA dataView1 = new DataViewA();
		// DataViewB dataView2 = new DataViewB();
		// DataViewC dataView3 = new DataViewC();
		//
		// Map<Class<?>,Object> dataViewMap = new LinkedHashMap<>();
		// dataViewMap.put(dataView1.getClass(),dataView1);
		// dataViewMap.put(dataView2.getClass(),dataView2);
		// dataViewMap.put(dataView3.getClass(),dataView3);
		//
		// MockSimulationContext mockSimulationContext =
		// MockSimulationContext.builder().setDataViewFunction((c)->{
		// return dataViewMap.get(c);
		// }).build();
		//
		//
		// Optional<DataViewA> optionalA =
		// mockSimulationContext.getDataView(DataViewA.class);
		// assertTrue(optionalA.isPresent());
		// assertEquals(dataView3, optionalA.get());
		//
		// Optional<DataViewB> optionalB =
		// mockSimulationContext.getDataView(DataViewB.class);
		// assertTrue(optionalB.isPresent());
		// assertEquals(dataView2, optionalB.get());
		//
		// Optional<DataViewC> optionalC =
		// mockSimulationContext.getDataView(DataViewC.class);
		// assertFalse(optionalC.isPresent());

	}

	@Test
	@UnitTestMethod(name = "getTime", args = {})
	public void testGetTime() {
		fail();
		// MutableDouble time = new MutableDouble(0);
		// MockSimulationContext mockSimulationContext =
		// MockSimulationContext.builder().setTimeSupplier(()->time.getValue()).build();
		// assertEquals(0.0, mockSimulationContext.getTime());
		//
		// time.setValue(77.3);
		// assertEquals(time.getValue(), mockSimulationContext.getTime());
		//
		// time.setValue(-600.234);
		// assertEquals(time.getValue(), mockSimulationContext.getTime());
		//
		// time.setValue(2.3423);
		// assertEquals(time.getValue(), mockSimulationContext.getTime());

	}

	@Test
	@UnitTestMethod(name = "releaseOutput", args = { Object.class })
	public void testReleaseOutput() {
		fail();
		// List<Object> releasedOutput = new ArrayList<>();
		// MockSimulationContext mockSimulationContext =
		// MockSimulationContext.builder().setReleaseOutputConsumer((o)->releasedOutput.add(o)).build();
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
		// mockSimulationContext.releaseOutput(obj);
		// }
		//
		// assertEquals(expectedOutput, releasedOutput);

	}

	@Test
	@UnitTestMethod(target = MockSimulationContext.Builder.class, name = "build", args = {})
	public void testBuild() {
		fail();
	}

	@Test
	@UnitTestMethod(target = MockSimulationContext.Builder.class, name = "setDataManagerFunction", args = { Function.class })
	public void testSetDataManagerFunction() {
		fail();
	}

	@Test
	@UnitTestMethod(target = MockSimulationContext.Builder.class, name = "setReleaseOutputConsumer", args = { Consumer.class })
	public void testSetReleaseOutputConsumer() {
		fail();
	}

	@Test
	@UnitTestMethod(target = MockSimulationContext.Builder.class, name = "setTimeSupplier", args = { Supplier.class })
	public void testSetTimeSupplier() {
		fail();
	}

}
