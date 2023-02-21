package nucleus;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;

public class AT_ReportContext {

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "addKeyedPlan", args = { Consumer.class, double.class, Object.class })
	public void testAddKeyedPlan() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getDataManager", args = { Class.class })
	public void testGetDataManager() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getPlan", args = { Object.class })
	public void testGetPlan() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getPlanKeys", args = {})
	public void testGetPlanKeys() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getPlanTime", args = { Object.class })
	public void testGetPlanTime() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getReportId", args = {})
	public void testGetReportId() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getTime", args = {})
	public void testGetTime() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "releaseOutput", args = { Object.class })
	public void testReleaseOutput() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "removePlan", args = { Object.class })
	public void testRemovePlan() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "subscribe", args = { Class.class, BiConsumer.class })
	public void testSubscribe() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "subscribeToSimulationClose", args = { Consumer.class })
	public void testSubscribeToSimulationClose() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "unsubscribe", args = { Class.class })
	public void testUnsubscribe() {
		fail();
	}

}
