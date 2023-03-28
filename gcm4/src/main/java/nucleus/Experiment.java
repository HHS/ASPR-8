package nucleus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * An experiment provides a means for executing the simulation over variants of
 * plugin data. Each such variant is referred to as a scenario. The scenarios
 * correspond to the cross product of a finite number of dimensions, with each
 * dimension having a finite number of variant levels.
 * 
 * For example: An experiment contains several plugins and correspondingly
 * several plugin data objects. The experiment has two dimensions. The first
 * dimension varies one of the plugin data objects with 5 new values. The second
 * dimension varies two values in two separate plugin data objects with 3 new
 * values each. There will be 15 resulting scenarios numbered 0 to 14
 * corresponding to each combination of altered inputs.
 * 
 * The experiment then executes the scenarios concurrently based on the number
 * of threads chosen for the execution.
 *
 *
 */

public final class Experiment {

	public static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		/**
		 * Adds a non-empty dimension to the experiment
		 */
		public Builder addDimension(final Dimension dimension) {
			if (dimension.size() > 0) {
				data.dimensions.add(dimension);
			}
			return this;
		}

		/**
		 * Marks the scenario to be explicitly run. All other scenarios will be
		 * ignored.
		 */
		public Builder addExplicitScenarioId(Integer scenarioId) {
			data.explicitScenarioIds.add(scenarioId);
			return this;
		}

		/**
		 * Adds the output item handler to the experiment. Consumers of
		 * experiment context must be thread safe.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_OUTPUT_HANDLER} if the
		 *             output item handler is null</li>
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
		 * Builds an experiment from the collected plugins, dimensions and
		 * output handlers.
		 */
		public Experiment build() {
			try {
				return new Experiment(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Sets the path for experiment progress log. A null path turns off
		 * logging and run resumption. Default value is null.
		 */
		public Builder setExperimentProgressLog(final Path path) {
			data.experimentProgressLogPath = path;
			return this;
		}

		/**
		 * Instructs the experiment to continue experiment progress from the
		 * experiment progress log. Defaults to false;
		 * 
		 */
		public Builder setContinueFromProgressLog(boolean continueFromProgressLog) {
			data.continueFromProgressLog = continueFromProgressLog;
			return this;
		}

		/**
		 * Sets the number of scenarios that may run concurrently. Generally
		 * this should be set to one less than the number of virtual processors
		 * on the machine that is running the experiment. Setting the thread
		 * count to zero causes the simulations to execute in the main thread.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NEGATIVE_THREAD_COUNT} if
		 *             the thread count is negative</li>
		 *
		 * 
		 */
		public Builder setThreadCount(final int threadCount) {
			if (threadCount < 0) {
				throw new ContractException(NucleusError.NEGATIVE_THREAD_COUNT);
			}
			data.threadCount = threadCount;
			return this;
		}

		/**
		 * If true, the simulations will produce plugins and a SimulationTime
		 * that reflect the final state of the simulation. Defaults to false.
		 */
		public Builder setProduceSimulationStateOnHalt(boolean produceSimulationStateOnHalt) {
			data.produceSimulationStateOnHalt = produceSimulationStateOnHalt;
			return this;
		}

		/**
		 * When true, the experiment halts on any exception thrown by any of the
		 * simulation instances. The experiment will attempt to gracefully
		 * terminate, halting any ongoing simulation instances and completing
		 * the experiment. When false, the experiment logs the failure with the
		 * experiment context and continues with the remaining simulation
		 * instances. Defaulted to true.
		 */
		public Builder setHaltOnException(final boolean haltOnException) {
			data.haltOnException = haltOnException;
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
		private int threadCount;
		private boolean produceSimulationStateOnHalt;
		private boolean haltOnException = true;
		private Path experimentProgressLogPath;
		private boolean continueFromProgressLog;
		private Set<Integer> explicitScenarioIds = new LinkedHashSet<>();
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
	 * A Callable implementor that runs the simulation in a thread from a
	 * completion service.
	 */
	private static class SimulationCallable implements Callable<SimResult> {
		private final ExperimentStateManager experimentStateManager;
		private final List<Plugin> plugins;
		private final Integer scenarioId;
		private final boolean produceSimulationStateOnHalt;

		/*
		 * All construction arguments are thread safe implementations.
		 */
		private SimulationCallable(final Integer scenarioId, final ExperimentStateManager experimentStateManager, final List<Plugin> plugins, final boolean produceSimulationStateOnHalt) {

			this.scenarioId = scenarioId;
			this.experimentStateManager = experimentStateManager;
			this.plugins = new ArrayList<>(plugins);
			this.produceSimulationStateOnHalt = produceSimulationStateOnHalt;
		}

		/**
		 * Executes the simulation for a scenario. Returns a SimResult
		 * indicating success/failure. If the simulation throws an exception it
		 * is handled by printing a stack trace and reports a failure for the
		 * scenario.
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
			simBuilder.setProduceSimulationStateOnHalt(produceSimulationStateOnHalt);

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

	private ExperimentStateManager experimentStateManager;

	private Experiment(final Data data) {
		this.data = data;
	}

	/**
	 * Executes the experiment using the information collected by the builder.
	 * 
	 */
	public void execute() {

		int scenarioCount = 1;
		for (final Dimension dimension : data.dimensions) {
			scenarioCount *= dimension.size();
		}

		ExperimentStateManager.Builder builder = ExperimentStateManager.builder();
		builder.setScenarioCount(scenarioCount);
		builder.setContinueFromProgressLog(data.continueFromProgressLog);
		builder.setScenarioProgressLogFile(data.experimentProgressLogPath);

		final List<String> experimentMetaData = new ArrayList<>();
		for (final Dimension dimension : data.dimensions) {
			experimentMetaData.addAll(dimension.getMetaData());
		}

		builder.setExperimentMetaData(experimentMetaData);

		// initialize the experiment context consumers so that they can
		// subscribe to experiment level events
		for (final Consumer<ExperimentContext> consumer : data.experimentContextConsumers) {
			builder.addExperimentContextConsumer(consumer);
		}
		for (Integer scenarioId : data.explicitScenarioIds) {
			builder.addExplicitScenarioId(scenarioId);
		}

		experimentStateManager = builder.build();
		// announce to consumers that the experiment is starting
		experimentStateManager.openExperiment();

		try {
			if (data.threadCount > 0) {
				ExecutorService executorService = Executors.newFixedThreadPool(data.threadCount);
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
	 * Executes the experiment utilizing multiple threads. If the simulation
	 * throws an exception it is caught and handled by reporting to standard
	 * error that the failure occurred as well as printing a stack trace.
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
		 * Start the initial threads. Don't exceed the thread count or the job
		 * count. Each time a thread is cleared, a new simulation will be
		 * processed through the CompletionService until we run out of
		 * simulations to run.
		 */
		while (jobIndex < (Math.min(data.threadCount, jobs.size()) - 1)) {
			final Integer scenarioId = jobs.get(jobIndex);
			List<Plugin> plugins = getNewPluginInstancesFromScenarioId(scenarioId);
			completionService.submit(new SimulationCallable(scenarioId, experimentStateManager, plugins, data.produceSimulationStateOnHalt));
			jobIndex++;
		}

		/*
		 * While there are still jobs to be assigned to a thread, or jobs that
		 * have not yet completed processing, we check to see if a new job needs
		 * processing and see if a previous job has completed.
		 */
		int jobCompletionCount = 0;
		while (jobCompletionCount < jobs.size()) {
			if (jobIndex < jobs.size()) {
				final Integer scenarioId = jobs.get(jobIndex);
				List<Plugin> plugins = getNewPluginInstancesFromScenarioId(scenarioId);
				completionService.submit(new SimulationCallable(scenarioId, experimentStateManager, plugins, data.produceSimulationStateOnHalt));
				jobIndex++;
			}

			/*
			 * This call is blocking and waits for a job to complete and a
			 * thread to clear.
			 */
			// try {
			final SimResult simResult = completionService.take().get();
			if (simResult.success) {
				experimentStateManager.closeScenarioAsSuccess(simResult.scenarioId);
			} else {
				experimentStateManager.closeScenarioAsFailure(simResult.scenarioId, simResult.failureCause);

				if (data.haltOnException) {
					throw simResult.failureCause;
				}
			}

			/*
			 * Once the blocking call returns, we increment the
			 * jobCompletionCount
			 */
			jobCompletionCount++;
		}

	}

	/*
	 * Executes the experiment using the main thread. If the simulation throws
	 * an exception it is caught and handled by reporting to standard error that
	 * the failure occurred as well as printing a stack trace.
	 */
	private void executeSingleThreaded() throws Exception {

		// Execute each scenario
		final int scenarioCount = experimentStateManager.getScenarioCount();
		final Simulation.Builder simBuilder = Simulation.builder();
		for (int scenarioId = 0; scenarioId < scenarioCount; scenarioId++) {
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

			simBuilder.setProduceSimulationStateOnHalt(data.produceSimulationStateOnHalt);

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

				if (data.haltOnException) {
					throw failureCause;
				}
			}

		}
	}

	private List<Plugin> getNewPluginInstancesFromScenarioId(final int scenarioId) {

		final DimensionContext.Builder contextBuilder = DimensionContext.builder();

		/*
		 * Set up a map that will allow us to associate with each plugin the new
		 * plugin data data builder instances associated with that plugin
		 */
		Map<Plugin, List<PluginDataBuilder>> map = new LinkedHashMap<>();

		for (final Plugin plugin : data.plugins) {
			List<PluginDataBuilder> list = new ArrayList<>();
			map.put(plugin, list);
			for (final PluginData pluginData : plugin.getPluginDatas()) {
				PluginDataBuilder pluginDataBuilder = pluginData.getCloneBuilder();
				list.add(pluginDataBuilder);
				contextBuilder.add(pluginDataBuilder);
			}
		}
		final DimensionContext dimensionContext = contextBuilder.build();

		// initialize the scenario meta data
		final List<String> scenarioMetaData = new ArrayList<>();

		/*
		 * From the scenario id select the functions from each dimension. Have
		 * the functions mutate the plugin builders and return meta data.
		 */
		int modulus = 1;
		for (int i = 0; i < data.dimensions.size(); i++) {
			Dimension dimension = data.dimensions.get(i);
			int metaDataSize = dimension.getMetaDataSize();

			/*
			 * Determine for the dimension the level within the dimension that
			 * corresponds to the scenario id
			 */
			final int level = (scenarioId / modulus) % dimension.size();
			modulus *= dimension.size();

			// get the function from the dimension
			final Function<DimensionContext, List<String>> levelFunction = dimension.getLevel(level);

			// apply the function that will update the plugin builders and
			// return the meta data for this function

			List<String> list = levelFunction.apply(dimensionContext);
			if (list.size() != metaDataSize) {
				throw new ContractException(NucleusError.DIMENSION_LABEL_MISMATCH,
						"dimension " + i + " has meta data: " + dimension.getMetaData() + " that does not match scenario labels: " + list + " at level " + level);
			}
			scenarioMetaData.addAll(list);

		}

		// update the experiment state manager with the meta data for the
		// scenario
		experimentStateManager.openScenario(scenarioId, scenarioMetaData);

		/*
		 * Rebuild the plugins.
		 */

		final List<Plugin> result = new ArrayList<>();

		for (Plugin plugin : map.keySet()) {
			Plugin.Builder pluginBuilder = Plugin.builder();
			pluginBuilder.setPluginId(plugin.getPluginId());

			Optional<Consumer<PluginContext>> optionalInitializer = plugin.getInitializer();
			if (optionalInitializer.isPresent()) {
				pluginBuilder.setInitializer(optionalInitializer.get());
			}

			for (PluginId pluginId : plugin.getPluginDependencies()) {
				pluginBuilder.addPluginDependency(pluginId);
			}

			List<PluginDataBuilder> pluginDataBuilders = map.get(plugin);
			for (PluginDataBuilder pluginDataBuilder : pluginDataBuilders) {
				pluginBuilder.addPluginData(pluginDataBuilder.build());
			}
			result.add(pluginBuilder.build());
		}

		return result;

	}
}
