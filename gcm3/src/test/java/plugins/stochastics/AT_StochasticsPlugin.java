package plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.PluginContext;
import nucleus.ResolverContext;
import nucleus.ResolverId;
import nucleus.testsupport.MockPluginContext;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = StochasticsPlugin.class)
public class AT_StochasticsPlugin {

	

	@Test
	@UnitTestMethod(name = "init", args = { PluginContext.class })
	public void testInit() {

		// Create a mock plugin context
		MockPluginContext mockPluginContext = new MockPluginContext();

		/*
		 * Create a stochastic plugin
		 */
		StochasticsPlugin.builder().setSeed(23452345L).build().init(mockPluginContext);

		// show that there are no plugin dependencies

		assertTrue(mockPluginContext.getPluginDependencies().isEmpty());

		/*
		 * Show that a single Resolver was added to the mock plugin context. There is
		 * no good way to prove it is the correct one, but if it were wrong,
		 * then the tests of event resolution in this plugin would fail.
		 */
		Map<ResolverId, Consumer<ResolverContext>> resolverMap = mockPluginContext.getResolverMap();
		assertEquals(1, resolverMap.size());

	}
	
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		// show that the builder returns a non-null instance of
		// StochasticsInitialData.Builder
		assertNotNull(StochasticsPlugin.builder());
	}

	@Test
	@UnitTestMethod(target = StochasticsPlugin.Builder.class, name = "build", args = {})
	public void testBuild() {
		// test covered by remaining tests
	}

	@Test
	@UnitTestMethod(target = StochasticsPlugin.Builder.class, name = "addRandomGeneratorId", args = { RandomNumberGeneratorId.class })
	public void testAddRandomGeneratorId() {
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			StochasticsPlugin stochasticsPlugin = StochasticsPlugin.builder().setSeed(4300202782621809065L).addRandomGeneratorId(testRandomGeneratorId).build();
			assertTrue(stochasticsPlugin.getRandomNumberGeneratorIds().contains(testRandomGeneratorId));
		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> StochasticsPlugin.builder().setSeed(1130627593613916615L).addRandomGeneratorId(null));
		assertEquals(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = StochasticsPlugin.Builder.class, name = "setSeed", args = { RandomNumberGeneratorId.class })
	public void testSetSeed() {
		
		long seed = 235234623445234756L;

		StochasticsPlugin stochasticsPlugin = StochasticsPlugin.builder().setSeed(seed).build();
		assertEquals(seed, stochasticsPlugin.getSeed());
	}

	@Test
	@UnitTestMethod(name = "getRandomNumberGeneratorIds", args = {})
	public void testGetRandomNumberGeneratorIds() {
		Set<RandomNumberGeneratorId> expectedRandomNumberGeneratorIds = new LinkedHashSet<>();
		StochasticsPlugin.Builder builder = StochasticsPlugin.builder();
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			expectedRandomNumberGeneratorIds.add(testRandomGeneratorId);
			builder.addRandomGeneratorId(testRandomGeneratorId);
		}
		builder.setSeed(3244635455542808061L);
		StochasticsPlugin stochasticsPlugin = builder.build();
		Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds = stochasticsPlugin.getRandomNumberGeneratorIds();
		assertEquals(expectedRandomNumberGeneratorIds, actualRandomNumberGeneratorIds);
	}


}
