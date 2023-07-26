package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.wrappers.MutableBoolean;

public class AT_SimulationStateCollector {
	@Test
	@UnitTestConstructor(target = SimulationStateCollector.class, args = { BiConsumer.class, Consumer.class })
	public void testSimulationStateCollector() {
		// nothing to test
	}

	private static class AlphaPluginDataBuilder implements PluginDataBuilder {
		private int alpha;

		@Override
		public AlphaPluginData build() {
			return new AlphaPluginData(alpha);
		}

		public void setAlpha(int alpha) {
			this.alpha = alpha;
		}

	}

	private static class AlphaPluginData implements PluginData {
		private final int alpha;

		public AlphaPluginData(int alpha) {
			this.alpha = alpha;
		}

		@Override
		public PluginDataBuilder getCloneBuilder() {
			AlphaPluginDataBuilder result = new AlphaPluginDataBuilder();
			result.setAlpha(alpha);
			return result;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("AlphaPluginData [alpha=");
			builder.append(alpha);
			builder.append("]");
			return builder.toString();
		}

	}

	private static class AlphaActor implements Consumer<ActorContext> {
		private final AlphaPluginData alphaPluginData;

		public AlphaActor(AlphaPluginData alphaPluginData) {
			this.alphaPluginData = alphaPluginData;
		}

		@Override
		public void accept(ActorContext c) {
			c.subscribeToSimulationClose((c2) -> {
				c2.releaseOutput(alphaPluginData);
			});
		}

	}

	private static Plugin getAlphaPlugin() {
		AlphaPluginDataBuilder alphaPluginDataBuilder = new AlphaPluginDataBuilder();
		alphaPluginDataBuilder.setAlpha(0);
		AlphaPluginData alphaPluginData = alphaPluginDataBuilder.build();

		return Plugin.builder()//
				.setPluginId(new SimplePluginId("Alpha_Plugin_Id"))//
				.addPluginData(alphaPluginData)//
				.setInitializer((c) -> {
					AlphaPluginData pluginData = c.getPluginData(AlphaPluginData.class).get();
					c.addActor(new AlphaActor(pluginData));
				}).build();
	}

	private static Dimension getAlphaDimension() {
		return FunctionalDimension.builder()//
				.addMetaDatum("Alpha")//
				.addLevel((context) -> {
					AlphaPluginDataBuilder alphaPluginDataBuilder = context
							.getPluginDataBuilder(AlphaPluginDataBuilder.class);
					int value = 17;
					alphaPluginDataBuilder.setAlpha(value);
					List<String> result = new ArrayList<>();
					result.add(Integer.toString(value));
					return result;
				})//
				.addLevel((context) -> {
					AlphaPluginDataBuilder alphaPluginDataBuilder = context
							.getPluginDataBuilder(AlphaPluginDataBuilder.class);
					int value = 25;
					alphaPluginDataBuilder.setAlpha(value);
					List<String> result = new ArrayList<>();
					result.add(Integer.toString(value));
					return result;
				})//
				.build();//
	}

	private static class BetaPluginDataBuilder implements PluginDataBuilder {
		private double beta;

		@Override
		public BetaPluginData build() {
			return new BetaPluginData(beta);
		}

		public void setBeta(double beta) {
			this.beta = beta;
		}

	}

	private static class BetaPluginData implements PluginData {
		private final double beta;

		public BetaPluginData(double beta) {
			this.beta = beta;
		}

		@Override
		public PluginDataBuilder getCloneBuilder() {
			BetaPluginDataBuilder result = new BetaPluginDataBuilder();
			result.setBeta(beta);
			return result;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("BetaPluginData [beta=");
			builder.append(beta);
			builder.append("]");
			return builder.toString();
		}

	}

	private static class BetaActor implements Consumer<ActorContext> {
		private final BetaPluginData betaPluginData;

		public BetaActor(BetaPluginData betaPluginData) {
			this.betaPluginData = betaPluginData;
		}

		@Override
		public void accept(ActorContext c) {
			c.subscribeToSimulationClose((c2) -> {
				c2.releaseOutput(betaPluginData);
			});
		}

	}

	private static Plugin getBetaPlugin() {
		BetaPluginDataBuilder betaPluginDataBuilder = new BetaPluginDataBuilder();
		betaPluginDataBuilder.setBeta(0.0);
		BetaPluginData betaPluginData = betaPluginDataBuilder.build();

		return Plugin.builder()//
				.setPluginId(new SimplePluginId("Beta_Plugin_Id"))//
				.addPluginData(betaPluginData)//
				.setInitializer((c) -> {
					BetaPluginData pluginData = c.getPluginData(BetaPluginData.class).get();
					c.addActor(new BetaActor(pluginData));
				}).build();
	}

	private static Dimension getBetaDimension() {
		return FunctionalDimension.builder()//
				.addMetaDatum("Beta")//
				.addLevel((context) -> {
					BetaPluginDataBuilder betaPluginDataBuilder = context
							.getPluginDataBuilder(BetaPluginDataBuilder.class);
					double value = 12.9;
					betaPluginDataBuilder.setBeta(value);
					List<String> result = new ArrayList<>();
					result.add(Double.toString(value));
					return result;
				})//
				.addLevel((context) -> {
					BetaPluginDataBuilder betaPluginDataBuilder = context
							.getPluginDataBuilder(BetaPluginDataBuilder.class);
					double value = 16.8;
					betaPluginDataBuilder.setBeta(value);
					List<String> result = new ArrayList<>();
					result.add(Double.toString(value));
					return result;
				})//
				.addLevel((context) -> {
					BetaPluginDataBuilder betaPluginDataBuilder = context
							.getPluginDataBuilder(BetaPluginDataBuilder.class);
					double value = 38.6;
					betaPluginDataBuilder.setBeta(value);
					List<String> result = new ArrayList<>();
					result.add(Double.toString(value));
					return result;
				})//
				.build();//
	}

	@Test
	@UnitTestMethod(target = SimulationStateCollector.class, name = "accept", args = {ExperimentContext.class})
	public void testAccept() {

		// create a few maps to hold information collected from the execution of the
		// experiment
		MutableBoolean experimentOpenConsumerInvoked = new MutableBoolean();
		Map<Integer, SimulationState> outputSimulationStates = new LinkedHashMap<>();
		Map<Integer, Set<Class<?>>> outputPluginDataClasses = new LinkedHashMap<>();

		/*
		 * Create the two consumers that form the SimulationStateCollector. These
		 * consumers will fill in the maps above.
		 */
		BiConsumer<Integer, List<Object>> scenarioConsumer = (scenarioId, list) -> {

			for (Object item : list) {
				if (item instanceof SimulationState) {
					SimulationState simulationState = (SimulationState) item;
					outputSimulationStates.put(scenarioId, simulationState);
				} else if (item instanceof PluginData) {
					PluginData pluginData = (PluginData) item;
					Set<Class<?>> pluginClasses = outputPluginDataClasses.get(scenarioId);
					if (pluginClasses == null) {
						pluginClasses = new LinkedHashSet<>();
						outputPluginDataClasses.put(scenarioId, pluginClasses);
					}
					pluginClasses.add(pluginData.getClass());
				}
			}
		};

		Consumer<ExperimentContext> experimentOpenConsumer = (c) -> {
			experimentOpenConsumerInvoked.setValue(true);
		};

		// create the SimulationStateCollector
		SimulationStateCollector simulationStateCollector = new SimulationStateCollector(scenarioConsumer,
				experimentOpenConsumer);

		// Collect the experiment parameter data
		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setRecordState(true)//
				.setSimulationHaltTime(100.0)//
				.build();//

		/*
		 * Execute the experiment. We will execute 6 scenarios over two dimensions --
		 * there will be two values for the Alpha plugin and three values for the Beta
		 * plugin
		 */
		Experiment.builder()//
				.setExperimentParameterData(experimentParameterData)//
				.addDimension(getAlphaDimension())//
				.addPlugin(getAlphaPlugin())//
				.addPlugin(getBetaPlugin())//
				.addDimension(getBetaDimension())//
				.addExperimentContextConsumer(simulationStateCollector)//
				.build()//
				.execute();//

		// show that the experiment consumer associated with the experiment opening was
		// invoked
		assertTrue(experimentOpenConsumerInvoked.getValue());

		/*
		 * show that the collected SimulationState objects all indicate that the
		 * simulation terminated at time 100 and that there is one such value for each
		 * scenario
		 */
		Set<Integer> expectedScearioIds = new LinkedHashSet<>();
		for (int i = 0; i < 6; i++) {
			expectedScearioIds.add(i);
		}
		assertEquals(expectedScearioIds, outputSimulationStates.keySet());

		for (SimulationState simulationState : outputSimulationStates.values()) {
			assertEquals(100.0, simulationState.getStartTime());
		}

		// show that the plugin data classes were collected for each scenario
		assertEquals(expectedScearioIds, outputPluginDataClasses.keySet());

		Set<Class<?>> expectedPluginDataClasses = new LinkedHashSet<>();
		expectedPluginDataClasses.add(AlphaPluginData.class);
		expectedPluginDataClasses.add(BetaPluginData.class);

		// show that the correct plugin data classes were returned from each scenario
		// execution
		for (Set<Class<?>> classes : outputPluginDataClasses.values()) {
			assertEquals(expectedPluginDataClasses, classes);
		}

	}

}
