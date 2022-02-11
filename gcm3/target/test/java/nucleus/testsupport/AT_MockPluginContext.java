package nucleus.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nucleus.AgentId;
import nucleus.DataManager;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = MockPluginContext.class)
@Disabled
public class AT_MockPluginContext {

	@Test
	@UnitTestMethod(name = "addPluginData", args = { PluginData.class })
	public void testAddPluginData() {
		fail();

	}

	@Test
	@UnitTestMethod(name = "addPluginDependency", args = { PluginId.class })
	public void testAddPluginDependency() {
		fail();
		Set<PluginId> expectedPluginIds = new LinkedHashSet<>();

		expectedPluginIds.add(new SimplePluginId("A"));
		expectedPluginIds.add(new SimplePluginId("B"));
		expectedPluginIds.add(new SimplePluginId("C"));

		MockPluginContext mockPluginContext = new MockPluginContext();
		for (PluginId pluginId : expectedPluginIds) {
			mockPluginContext.addPluginDependency(pluginId);
		}
		assertEquals(expectedPluginIds, mockPluginContext.getPluginDependencies());

		// show that adding duplicates has no effect
		for (PluginId pluginId : expectedPluginIds) {
			mockPluginContext.addPluginDependency(pluginId);
		}
		assertEquals(expectedPluginIds, mockPluginContext.getPluginDependencies());

		// precondition tests

		// null plugin id
		assertThrows(RuntimeException.class, () -> new MockPluginContext().addPluginDependency(null));

	}

	@Test
	@UnitTestMethod(name = "addDataManager", args = { DataManager.class })
	public void testAddDataManager() {
		fail();
//		Map<ResolverId, Consumer<DataManagerContext>> expectedResolvers = new LinkedHashMap<>();
//
//		expectedResolvers.put(new SimpleResolverId("A"), (c) -> {
//		});
//		expectedResolvers.put(new SimpleResolverId("B"), (c) -> {
//		});
//		expectedResolvers.put(new SimpleResolverId("C"), (c) -> {
//		});
//
//		MockPluginContext mockPluginContext = new MockPluginContext();
//		for (ResolverId resolverId : expectedResolvers.keySet()) {
//			mockPluginContext.defineResolver(resolverId, expectedResolvers.get(resolverId));
//		}
//
//		assertEquals(expectedResolvers, mockPluginContext.getResolverMap());
//
//		// show that repeated additions have no effect
//		for (ResolverId resolverId : expectedResolvers.keySet()) {
//			mockPluginContext.defineResolver(resolverId, expectedResolvers.get(resolverId));
//		}
//		assertEquals(expectedResolvers, mockPluginContext.getResolverMap());
//
//		// precondition tests
//
//		// null resolver id
//		assertThrows(RuntimeException.class, () -> new MockPluginContext().defineResolver(null, (c) -> {
//		}));
//
//		// null context consumer
//		assertThrows(RuntimeException.class, () -> new MockPluginContext().defineResolver(new SimpleResolverId("resolver"), null));

	}

	@Test
	@UnitTestMethod(name = "addAgent", args = { AgentId.class, Consumer.class })
	public void testAddAgent() {
		fail();

	}

	@Test
	@UnitTestMethod(name = "getPluginData", args = { Class.class })
	public void testGetPluginData() {
		fail();
	}

	@Test
	@UnitTestMethod(name = "getAgents", args = {})
	public void testGetAgents() {
		fail();
	}

	@Test
	@UnitTestMethod(name = "getPluginDependencies", args = {})
	public void testGetPluginDependencies() {
		fail();
	}

	@Test
	@UnitTestMethod(name = "getDataManagers", args = {})
	public void testGetDataManagers() {
		fail();
	}

}
