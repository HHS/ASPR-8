package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * An experiment provides a means for executing the simulation over variants of
 * plugin data. Each such variant is referred to as a scenario. The scenarios
 * correspond to the cross product of a finite number of dimensions, with each
 * dimension having a finite number of variant levels. For example: An
 * experiment contains several plugins and correspondingly several plugin data
 * objects. The experiment has two dimensions. The first dimension varies one of
 * the plugin data objects with 5 new values. The second dimension varies two
 * values in two separate plugin data objects with 3 new values each. There will
 * be 15 resulting scenarios numbered 0 to 14 corresponding to each combination
 * of altered inputs. The experiment then executes the scenarios concurrently
 * based on the number of threads chosen for the execution.
 */
public final class Experiment {

	private static class DimensionRec {
		private final int index;
		private final int levelCount;
		private final Dimension dimension;
		private List<String> experimentMetaData = new ArrayList<>();
		private Map<Integer, List<String>> scenarioMetaData = new LinkedHashMap<>();

		public List<String> getExperimentMetaData() {
			return experimentMetaData;
		}

		public DimensionRec(Dimension dimension, int index) {
			this.dimension = dimension;
			this.index = index;
			for (int i = 0; i < dimension.levelCount(); i++) {
				scenarioMetaData.put(i, null);
			}
			experimentMetaData.addAll(dimension.getExperimentMetaData());
			levelCount = dimension.levelCount();
		}

		public List<String> executeLevel(DimensionContext dimensionContext, int level) {
			List<String> list = dimension.executeLevel(dimensionContext, level);
			if (list.size() != experimentMetaData.size()) {
				throw new ContractException(NucleusError.DIMENSION_LABEL_MISMATCH,
						"dimension[" + index + "] has meta data: " + experimentMetaData
								+ " that does not match scenario meta data: " + list + " at level " + level);
			}

			List<String> baseList = scenarioMetaData.get(level);
			if (baseList == null) {
				scenarioMetaData.put(level, list);
			} else {
				if (!baseList.equals(list)) {
					throw new ContractException(NucleusError.DIMENSION_LABEL_MISMATCH, "execution of level " + level
							+ " of dimension[" + index + "] resulted in inconsistent meta scenario data");
				}
			}
			return list;
		}

		public int getLevelCount() {
			return levelCount;
		}
	}

	public static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		/**
		 * Adds a non-empty dimension to the experiment
		 */
		public Builder addDimension(final Dimension dimension) {
			if (dimension.levelCount() > 0) {
				data.dimensions.add(dimension);
			}
			return this;
		}

		/**
		 * Adds the output item handler to the experiment. Consumers of experiment
		 * context must be thread safe.
		 * 
		 * @throws ContractException {@linkplain NucleusError#NULL_OUTPUT_HANDLER} if
		 *                           the output item handler is null
		 */
		public Builder addExperimentContextConsumer(final Consumer<ExperimentContext> experimentContextConsumer) {
			if (experimentContextConsumer == null) {
				throw new ContractException(NucleusError.NULL_OUTPUT_HANDLER);
			}
			data.experimentContextConsumers.add(experimentContextConsumer);
			return this;
		}

		/**
		 * Adds a plugin to the experiment.
		 */
		public Builder addPlugin(final Plugin plugin) {
			data.plugins.add(plugin);
			return this;
		}

		/**
		 * Builds an experiment from the collected plugins, dimensions and output
		 * handlers.
		 */
		public Experiment build() {
			return new Experiment(new Data(data));
		}

		/**
		 * Sets the experiment parameters. Defaults to the default build of
		 * ExperimentParameterData.
		 */
		public Builder setExperimentParameterData(ExperimentParameterData experimentParameterData) {
			data.experimentParameterData = experimentParameterData;
			return this;
		}

		/**
		 * Set the simulation state. Defaults to the current date and a start time of
		 * zero.
		 * 
		 * @throws ContractException {@link NucleusError#NULL_SIMULATION_TIME} if the
		 *                           simulation time is null
		 */
		public Builder setSimulationState(SimulationState simulationState) {
			if (simulationState == null) {
				throw new ContractException(NucleusError.NULL_SIMULATION_TIME);
			}
			data.simulationState = simulationState;
			return this;
		}

	}

	/*
	 * A data class for holding the inputs to this builder from its client.
	 */
	private static class Data {
		private final List<Dimension> dimensions = new ArrayList<>();
		private final List<Plugin> plugins = new ArrayList<>();
		private final List<Consumer<ExperimentContext>> experimentContextConsumers = new ArrayList<>();

		private SimulationState simulationState = SimulationState.builder().build();

		private ExperimentParameterData experimentParameterData = ExperimentParameterData.builder().build();

		public Data() {
		}

		public Data(Data data) {
			dimensions.addAll(data.dimensions);
			plugins.addAll(data.plugins);
			experimentContextConsumers.addAll(data.experimentContextConsumers);
			experimentParameterData = data.experimentParameterData;
			simulationState = data.simulationState;
		}
	}

	/*
	 * The result of the execution of a SimulationCallable
	 */
	@Immutable
	private static class SimResult {
		private final boolean success;
		private final int scenarioId;
		private final Exception failureCause;

		public SimResult(final int scenarioId, final boolean success, Exception failureCause) {
			this.scenarioId = scenarioId;
			this.success = success;
			this.failureCause = failureCause;
		}
	}

	/*
	 * A Callable implementor that runs the simulation in a thread from a completion
	 * service.
	 */
	private static class SimulationCallable implements Callable<SimResult> {
		private final ExperimentStateManager experimentStateManager;
		private final List<Plugin> plugins;
		private final Integer scenarioId;
		private final boolean produceSimulationStateOnHalt;
		private final Double simulationHaltTime;
		private final SimulationState simulationState;

		/*
		 * All construction arguments are thread safe implementations.
		 */
		private SimulationCallable(final Integer scenarioId, final ExperimentStateManager experimentStateManager,
				final List<Plugin> plugins, final boolean produceSimulationStateOnHalt, final Double simulationHaltTime,
				final SimulationState simulationState) {
			this.scenarioId = scenarioId;
			this.experimentStateManager = experimentStateManager;
			this.plugins = new ArrayList<>(plugins);
			this.produceSimulationStateOnHalt = produceSimulationStateOnHalt;
			this.simulationHaltTime = simulationHaltTime;
			this.simulationState = simulationState;
		}

		/**
		 * Executes the simulation for a scenario. Returns a SimResult indicating
		 * success/failure. If the simulation throws an exception it is handled by
		 * printing a stack trace and reports a failure for the scenario.
		 */
		@Override
		public SimResult call() throws Exception {

			final Simulation.Builder simBuilder = Simulation.builder();

			// Load the plugins into the simulation builder
			for (final Plugin plugin : plugins) {
				simBuilder.addPlugin(plugin);
			}

			// direct output from the simulation to the subscribed consumers
			simBuilder.setOutputConsumer(experimentStateManager.getOutputConsumer(scenarioId));
			simBuilder.setRecordState(produceSimulationStateOnHalt);
			simBuilder.setSimulationHaltTime(simulationHaltTime);
			simBuilder.setSimulationState(simulationState);
			// build the simulation
			final Simulation simulation = simBuilder.build();

			// run the simulation
			boolean success = false;
			Exception failureCause = null;
			try {
				simulation.execute();
				success = true;
			} catch (final Exception e) {
				failureCause = e;
			}
			return new SimResult(scenarioId, success, failureCause);
		}

	}

	/**
	 * Returns a builder for Experiment
	 */
	public static Builder builder() {
		return new Builder();
	}

	private final Data data;

	private List<DimensionRec> dimensionRecs = new ArrayList<>();

	private ExperimentStateManager experimentStateManager;

	private Experiment(final Data data) {
		this.data = data;
	}

	/**
	 * Executes the experiment using the information collected by the builder.
	 */
	public void execute() {

		int scenarioCount = 1;
		for (int i = 0; i < data.dimensions.size(); i++) {
			Dimension dimension = data.dimensions.get(i);
			DimensionRec dimensionRec = new DimensionRec(dimension, i);
			dimensionRecs.add(dimensionRec);
			scenarioCount *= dimensionRec.getLevelCount();
		}

		ExperimentStateManager.Builder builder = ExperimentStateManager.builder();
		builder.setScenarioCount(scenarioCount);
		builder.setContinueFromProgressLog(data.experimentParameterData.continueFromProgressLog());
		Optional<Path> optionalExperimentProgressLogPath = data.experimentParameterData.getExperimentProgressLogPath();
		if (optionalExperimentProgressLogPath.isPresent()) {
			builder.setScenarioProgressLogFile(optionalExperimentProgressLogPath.get());
		}

		final List<String> experimentMetaData = new ArrayList<>();
		for (final DimensionRec dimensionRec : dimensionRecs) {
			experimentMetaData.addAll(dimensionRec.getExperimentMetaData());
		}

		builder.setExperimentMetaData(experimentMetaData);

		// initialize the experiment context consumers so that they can
		// subscribe to experiment level events
		for (final Consumer<ExperimentContext> consumer : data.experimentContextConsumers) {
			builder.addExperimentContextConsumer(consumer);
		}
		for (Integer scenarioId : data.experimentParameterData.getExplicitScenarioIds()) {
			builder.addExplicitScenarioId(scenarioId);
		}

		experimentStateManager = builder.build();
		// announce to consumers that the experiment is starting
		experimentStateManager.openExperiment();

		try {

			int threadCount = data.experimentParameterData.getThreadCount();
			if (threadCount > 0) {
				ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
				try {
					executeMultiThreaded(executorService);
					executorService.shutdown();
				} catch (Exception e) {
					executorService.shutdownNow();
					throw new RuntimeException(e);
				}
			} else {
				try {
					executeSingleThreaded();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		} finally {
			// announce to consumers that the experiment has ended
			experimentStateManager.closeExperiment();
		}

	}

	/*
	 * Executes the experiment utilizing multiple threads. If the simulation throws
	 * an exception it is caught and handled by reporting to standard error that the
	 * failure occurred as well as printing a stack trace.
	 */
	private void executeMultiThreaded(final ExecutorService executorService) throws Exception {

		// Determine the scenarios
		final List<Integer> jobs = new ArrayList<>();

		final int scenarioCount = experimentStateManager.getScenarioCount();

		for (int scenarioId = 0; scenarioId < scenarioCount; scenarioId++) {
			final ScenarioStatus scenarioStatus = experimentStateManager.getScenarioStatus(scenarioId).get();
			if (scenarioStatus == ScenarioStatus.READY) {
				jobs.add(scenarioId);
			}
		}

		/*
		 * If there is nothing to do, then do not engage.
		 */
		if (jobs.isEmpty()) {
			return;
		}

		int jobIndex = 0;

		// Create the Completion Service using the suggested thread
		// count
		// final ExecutorService executorService =
		// Executors.newFixedThreadPool(data.threadCount);

		final CompletionService<SimResult> completionService = new ExecutorCompletionService<>(executorService);

		/*
		 * Start the initial threads. Don't exceed the thread count or the job count.
		 * Each time a thread is cleared, a new simulation will be processed through the
		 * CompletionService until we run out of simulations to run.
		 */
		int threadCount = data.experimentParameterData.getThreadCount();
		while (jobIndex < (Math.min(threadCount, jobs.size()) - 1)) {
			final Integer scenarioId = jobs.get(jobIndex);
			List<Plugin> plugins = getNewPluginInstancesFromScenarioId(scenarioId);
			completionService.submit(new SimulationCallable(scenarioId, experimentStateManager, plugins,
					data.experimentParameterData.stateRecordingIsScheduled(),
					data.experimentParameterData.getSimulationHaltTime().orElse(null), data.simulationState));
			jobIndex++;
		}

		/*
		 * While there are still jobs to be assigned to a thread, or jobs that have not
		 * yet completed processing, we check to see if a new job needs processing and
		 * see if a previous job has completed.
		 */
		int jobCompletionCount = 0;
		while (jobCompletionCount < jobs.size()) {
			if (jobIndex < jobs.size()) {
				final Integer scenarioId = jobs.get(jobIndex);
				List<Plugin> plugins = getNewPluginInstancesFromScenarioId(scenarioId);
				completionService.submit(new SimulationCallable(scenarioId, experimentStateManager, plugins,
						data.experimentParameterData.stateRecordingIsScheduled(),
						data.experimentParameterData.getSimulationHaltTime().orElse(null), data.simulationState));
				jobIndex++;
			}

			/*
			 * This call is blocking and waits for a job to complete and a thread to clear.
			 */
			// try {
			final SimResult simResult = completionService.take().get();
			if (simResult.success) {
				experimentStateManager.closeScenarioAsSuccess(simResult.scenarioId);
			} else {
				experimentStateManager.closeScenarioAsFailure(simResult.scenarioId, simResult.failureCause);

				if (data.experimentParameterData.haltOnException()) {
					throw simResult.failureCause;
				}
			}

			/*
			 * Once the blocking call returns, we increment the jobCompletionCount
			 */
			jobCompletionCount++;
		}

	}

	/*
	 * Executes the experiment using the main thread. If the simulation throws an
	 * exception it is caught and handled by reporting to standard error that the
	 * failure occurred as well as printing a stack trace.
	 */
	private void executeSingleThreaded() throws Exception {

		// Execute each scenario
		final int scenarioCount = experimentStateManager.getScenarioCount();

		for (int scenarioId = 0; scenarioId < scenarioCount; scenarioId++) {
			Simulation.Builder simBuilder = Simulation.builder();
			final ScenarioStatus scenarioStatus = experimentStateManager.getScenarioStatus(scenarioId).get();
			if (scenarioStatus != ScenarioStatus.READY) {
				continue;
			}

			// generate the plugins that will form the simulation for the given
			// scenario id
			final List<Plugin> plugins = getNewPluginInstancesFromScenarioId(scenarioId);

			// Load the plugins into the simulation builder
			for (final Plugin plugin : plugins) {
				simBuilder.addPlugin(plugin);
			}

			simBuilder.setRecordState(data.experimentParameterData.stateRecordingIsScheduled());
			simBuilder.setSimulationHaltTime(data.experimentParameterData.getSimulationHaltTime().orElse(null));
			simBuilder.setSimulationState(data.simulationState);
			// direct output from the simulation to the subscribed consumers
			simBuilder.setOutputConsumer(experimentStateManager.getOutputConsumer(scenarioId));

			// build the simulation
			final Simulation simulation = simBuilder.build();

			// run the simulation
			boolean success = false;
			Exception failureCause = null;

			try {
				simulation.execute();
				success = true;
			} catch (final Exception e) {
				failureCause = e;
			}

			if (success) {
				experimentStateManager.closeScenarioAsSuccess(scenarioId);
			} else {
				experimentStateManager.closeScenarioAsFailure(scenarioId, failureCause);

				if (data.experimentParameterData.haltOnException()) {
					throw failureCause;
				}
			}

		}
	}

	private List<Plugin> getNewPluginInstancesFromScenarioId(final int scenarioId) {

		final DimensionContext.Builder contextBuilder = DimensionContext.builder();

		/*
		 * Set up a map that will allow us to associate with each plugin the new plugin
		 * data data builder instances associated with that plugin.
		 * 
		 * We need to avoid using the plugin as a key. Plugins contain plugin datas,
		 * which may contain a huge amount of dataa and thus have expensive hash code
		 * costs.
		 */
		Map<PluginId, List<PluginDataBuilder>> dataBuilderMap = new LinkedHashMap<>();
		Map<PluginId, Plugin> pluginMap = new LinkedHashMap<>();

		for (final Plugin plugin : data.plugins) {
			List<PluginDataBuilder> list = new ArrayList<>();
			pluginMap.put(plugin.getPluginId(), plugin);
			dataBuilderMap.put(plugin.getPluginId(), list);
			for (final PluginData pluginData : plugin.getPluginDatas()) {
				list.add(contextBuilder.add(pluginData));
			}
		}
		final DimensionContext dimensionContext = contextBuilder.build();

		// initialize the scenario meta data
		final List<String> scenarioMetaData = new ArrayList<>();

		/*
		 * From the scenario id select the functions from each dimension. Have the
		 * functions mutate the plugin builders and return meta data.
		 */
		int modulus = 1;
		for (int i = 0; i < dimensionRecs.size(); i++) {
			DimensionRec dimensionRec = dimensionRecs.get(i);

			/*
			 * Determine for the dimension the level within the dimension that corresponds
			 * to the scenario id
			 */
			int levelCount = dimensionRec.getLevelCount();
			final int level = (scenarioId / modulus) % levelCount;
			modulus *= levelCount;

			// get the function from the dimension
			List<String> dimensionMetaData = dimensionRec.executeLevel(dimensionContext, level);

			scenarioMetaData.addAll(dimensionMetaData);
		}

		// update the experiment state manager with the meta data for the
		// scenario
		experimentStateManager.openScenario(scenarioId, scenarioMetaData);

		/*
		 * Rebuild the plugins.
		 */
		final List<Plugin> result = new ArrayList<>();

		for (PluginId pluginId : pluginMap.keySet()) {
			Plugin plugin = pluginMap.get(pluginId);
			List<PluginDataBuilder> pluginDataBuilders = dataBuilderMap.get(pluginId);

			Plugin.Builder pluginBuilder = Plugin.builder();
			pluginBuilder.setPluginId(plugin.getPluginId());

			Optional<Consumer<PluginContext>> optionalInitializer = plugin.getInitializer();
			if (optionalInitializer.isPresent()) {
				pluginBuilder.setInitializer(optionalInitializer.get());
			}

			for (PluginId dependencyPluginId : plugin.getPluginDependencies()) {
				pluginBuilder.addPluginDependency(dependencyPluginId);
			}

			for (PluginDataBuilder pluginDataBuilder : pluginDataBuilders) {
				pluginBuilder.addPluginData(pluginDataBuilder.build());
			}
			result.add(pluginBuilder.build());
		}

		return result;

	}
}
