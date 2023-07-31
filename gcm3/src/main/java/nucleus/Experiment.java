package nucleus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
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
 * @author Shawn Hatch
 *
 */

public final class Experiment {

	public static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		/**
		 * Adds a dimension to the experiment
		 */
		public Builder addDimension(final Dimension dimension) {
			data.dimensions.add(dimension);
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
		public Builder addOutputHandler(final Consumer<ExperimentContext> experimentContextConsumer) {
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
		 * Turns on or off the logging of experiment progress to standard out.
		 * Default value is true.
		 *
		 */
		public Builder reportProgressToConsole(final boolean reportProgressToConsole) {
			data.reportProgressToConsole = reportProgressToConsole;
			return this;
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
		 * Sets the policy on reporting scenario failures. Defaults to true.
		 */
		public Builder reportFailuresToConsole(final boolean reportFailuresToConsole) {
			data.reportFailuresToConsole = reportFailuresToConsole;
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
		private boolean reportFailuresToConsole = true;
		private boolean reportProgressToConsole = true;
		private Path experimentProgressLogPath;
		private boolean continueFromProgressLog;

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
		private final boolean reportScenarioFailureToConsole;

		/*
		 * All construction arguments are thread safe implementations.
		 */
		private SimulationCallable(final Integer scenarioId, final ExperimentStateManager experimentStateManager, final List<Plugin> plugins, final boolean reportScenarioFailureToConsole) {
			this.scenarioId = scenarioId;
			this.experimentStateManager = experimentStateManager;
			this.plugins = new ArrayList<>(plugins);
			this.reportScenarioFailureToConsole = reportScenarioFailureToConsole;
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
				if (reportScenarioFailureToConsole) {
					System.err.println("Simulation failure for scenario " + scenarioId);
					e.printStackTrace();
				}
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

		if (data.reportProgressToConsole) {
			data.experimentContextConsumers.add(new ExperimentStatusConsole()::init);
		}

		// initialize the experiment context consumers so that they can
		// subscribe to experiment level events
		for (final Consumer<ExperimentContext> consumer : data.experimentContextConsumers) {
			builder.addExperimentContextConsumer(consumer);
		}

		experimentStateManager = builder.build();
		// announce to consumers that the experiment is starting
		experimentStateManager.openExperiment();

		if (data.threadCount > 0) {
			executeMultiThreaded();
		} else {
			executeSingleThreaded();
		}

		// announce to consumers that the experiment has ended
		experimentStateManager.closeExperiment();

	}

	/*
	 * Executes the experiment utilizing multiple threads. If the simulation
	 * throws an exception it is caught and handled by reporting to standard
	 * error that the failure occured as well as printing a stack trace.
	 */
	private void executeMultiThreaded() {

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
		final ExecutorService executorService = Executors.newFixedThreadPool(data.threadCount);
		final CompletionService<SimResult> completionService = new ExecutorCompletionService<>(executorService);

		/*
		 * Start the initial threads. Don't exceed the thread count or the job
		 * count. Each time a thread is cleared, a new simulation will be
		 * processed through the CompletionService until we run out of
		 * simulations to run.
		 */
		while (jobIndex < (Math.min(data.threadCount, jobs.size()) - 1)) {
			final Integer scenarioId = jobs.get(jobIndex);
			List<Plugin> plugins = preSimActions(scenarioId);
			completionService.submit(new SimulationCallable(scenarioId, experimentStateManager, plugins, data.reportFailuresToConsole));
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
				List<Plugin> plugins = preSimActions(scenarioId);
				completionService.submit(new SimulationCallable(scenarioId, experimentStateManager, plugins, data.reportFailuresToConsole));
				jobIndex++;
			}

			/*
			 * This call is blocking and waits for a job to complete and a
			 * thread to clear.
			 */
			try {
				final SimResult simResult = completionService.take().get();
				if (simResult.success) {
					experimentStateManager.closeScenarioAsSuccess(simResult.scenarioId);
				} else {
					experimentStateManager.closeScenarioAsFailure(simResult.scenarioId, simResult.failureCause);
				}
			} catch (final InterruptedException | ExecutionException e) {
				// Note that this is the completion service failing and
				// not the simulation
				
				//re-thrown as runtime exception
				throw new RuntimeException(e);
			}

			/*
			 * Once the blocking call returns, we increment the
			 * jobCompletionCount
			 */
			jobCompletionCount++;
		}

		/*
		 * Since all jobs are done, the CompletionService is no longer needed so
		 * we shut down the executorService that backs it.
		 */
		executorService.shutdown();

	}

	/*
	 * Executes the experiment using the main thread. If the simulation throws
	 * an exception it is caught and handled by reporting to standard error that
	 * the failure occurred as well as printing a stack trace.
	 */
	private void executeSingleThreaded() {

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
			final List<Plugin> plugins = preSimActions(scenarioId);

			// Load the plugin behaviors into the simulation builder
			for (final Plugin plugin : plugins) {
				simBuilder.addPlugin(plugin);
			}

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
				if (data.reportFailuresToConsole) {
					System.err.println("Simulation failure for scenario " + scenarioId);
					e.printStackTrace();
				}
			}

			if (success) {
				experimentStateManager.closeScenarioAsSuccess(scenarioId);
			}else {
				experimentStateManager.closeScenarioAsFailure(scenarioId, failureCause);
			}

		}
	}

	private List<Plugin> preSimActions(final int scenarioId) {
		/*
		 * Build the type map of the clone plugin data builders from the plugins
		 * supplied to the dimensions of the experiment
		 */

		final PluginDataBuilderContext.Builder contextBuilder = PluginDataBuilderContext.builder();

		/*
		 * Set up a map that will allow us to associate each data builder with
		 * the plugin that should own that data
		 */
		Map<PluginDataBuilder, PluginId> humptyMap = new LinkedHashMap<>();

		for (final Plugin plugin : data.plugins) {
			for (final PluginData pluginData : plugin.getPluginDatas()) {
				PluginDataBuilder pluginDataBuilder = pluginData.getCloneBuilder();
				humptyMap.put(pluginDataBuilder, plugin.getPluginId());
				contextBuilder.add(pluginDataBuilder);
			}
		}
		final PluginDataBuilderContext pluginDataBuilderContext = contextBuilder.build();

		// initialize the scenario meta data
		final List<String> scenarioMetaData = new ArrayList<>();

		/*
		 * From the scenario id select the functions from each dimension. Have
		 * the functions mutate the plugin builders and return meta data.
		 */
		int modulus = 1;
		for (final Dimension dimension : data.dimensions) {
			/*
			 * Determine for the dimension the index within the dimension that
			 * corresponds to the scenario id
			 */
			final int index = (scenarioId / modulus) % dimension.size();
			modulus *= dimension.size();

			// get the function from the dimension
			final Function<PluginDataBuilderContext, List<String>> memberGenerator = dimension.getLevel(index);

			// apply the function that will update the plugin builders and
			// return the meta data for this function
			scenarioMetaData.addAll(memberGenerator.apply(pluginDataBuilderContext));

		}

		// update the experiment state manager with the meta data for the
		// scenario
		experimentStateManager.openScenario(scenarioId, scenarioMetaData);

		/*
		 * Rebuild the plugins.
		 */

		// First, copy each plugin, excluding the plugin data items.
		Map<PluginId, Plugin.Builder> dumptyMap = new LinkedHashMap<>();
		for (final Plugin plugin : data.plugins) {
			Plugin.Builder pluginBuilder = Plugin.builder();
			dumptyMap.put(plugin.getPluginId(), pluginBuilder);
			pluginBuilder.setPluginId(plugin.getPluginId());
			for (PluginId pluginId : plugin.getPluginDependencies()) {
				pluginBuilder.addPluginDependency(pluginId);
			}
			Optional<Consumer<PluginContext>> optionalInitializer = plugin.getInitializer();
			if (optionalInitializer.isPresent()) {
				pluginBuilder.setInitializer(optionalInitializer.get());
			}
		}

		// Get the plugin data builders and create the new plugin datas,
		// associating each with the correct plugin. The plugin datas should be
		// added in the order that they were in in the original plugins
		for (final PluginDataBuilder pluginDataBuilder : pluginDataBuilderContext.getContents()) {
			final PluginData pluginData = pluginDataBuilder.build();
			PluginId pluginId = humptyMap.get(pluginDataBuilder);
			Plugin.Builder pluginBuilder = dumptyMap.get(pluginId);
			pluginBuilder.addPluginData(pluginData);
		}

		/*
		 * Construct the new plugins from the plugin builders
		 */
		final List<Plugin> result = new ArrayList<>();

		for (Plugin.Builder plugingBuilder : dumptyMap.values()) {
			result.add(plugingBuilder.build());
		}

		return result;

	}

}