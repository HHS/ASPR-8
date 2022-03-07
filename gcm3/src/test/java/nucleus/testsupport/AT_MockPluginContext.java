package nucleus.testsupport;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;

@UnitTest(target = MockPluginContext.class)
@Disabled
public class AT_MockPluginContext {
	
	@Test
	public void test() {
		fail();
	}

//	/**
//	 * Shows that an added plugin dependency can be retrieved
//	 */
//	@Test
//	@UnitTestMethod(name = "addPluginDependency", args = { PluginId.class })
//	public void testAddPluginDependency() {
//
//		Set<PluginId> expectedPluginIds = new LinkedHashSet<>();
//
//		expectedPluginIds.add(new SimplePluginId("A"));
//		expectedPluginIds.add(new SimplePluginId("B"));
//		expectedPluginIds.add(new SimplePluginId("C"));
//
//		MockPluginContext mockPluginContext = new MockPluginContext();
//		for (PluginId pluginId : expectedPluginIds) {
//			mockPluginContext.addPluginDependency(pluginId);
//		}
//		assertEquals(expectedPluginIds, mockPluginContext.getPluginDependencies());
//
//		// show that adding duplicates has no effect
//		for (PluginId pluginId : expectedPluginIds) {
//			mockPluginContext.addPluginDependency(pluginId);
//		}
//		assertEquals(expectedPluginIds, mockPluginContext.getPluginDependencies());
//
//		// precondition tests
//
//		// null plugin id
//		assertThrows(RuntimeException.class, () -> new MockPluginContext().addPluginDependency(null));
//
//	}
//
//	@Test
//	@UnitTestMethod(name = "defineResolver", args = { ResolverId.class, Consumer.class })
//	public void testDefineResolver() {
//
//		Map<ResolverId, Consumer<ResolverContext>> expectedResolvers = new LinkedHashMap<>();
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
//
//	}
//
//	/**
//	 * Shows plugin ids are properly retrieved
//	 * 
//	 * Covered by {@linkplain AT_MockPluginContext#testAddPluginDependency() }
//	 */
//	@Test
//	@UnitTestMethod(name = "getPluginDependencies", args = {})
//	public void testGetPluginDependencies() {
//		// covered by test for adding plugin ids
//
//	}
//
//	/**
//	 * Show resolver context consumers are property retrieved
//	 * 
//	 * Covered by {@linkplain AT_MockPluginContext#testDefineResolver() }
//	 * 
//	 */
//	@Test
//	@UnitTestMethod(name = "getResolverMap", args = { ResolverId.class, Consumer.class })
//	public void testGetResolverMap() {
//		// covered by test for adding resolver context consumers
//	}

}
