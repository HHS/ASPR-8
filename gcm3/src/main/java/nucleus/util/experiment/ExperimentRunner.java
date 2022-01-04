package nucleus.util.experiment;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.jcip.annotations.Immutable;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.util.experiment.output.ConsoleLogItemHandler;
import nucleus.util.experiment.output.LogItem;
import nucleus.util.experiment.output.OutputItemHandler;
import nucleus.util.experiment.output.SimulationStatusItem;
import nucleus.util.experiment.output.SimulationStatusItemHandler;
import nucleus.util.experiment.progress.ExperimentProgressLog;
import nucleus.util.experiment.progress.NIOExperimentProgressLogReader;
import nucleus.util.experiment.progress.NIOExperimentProgressLogWriter;
import plugins.gcm.GCMMonolithicSupport;
import plugins.gcm.experiment.Replication;
import plugins.gcm.experiment.ReplicationId;
import plugins.gcm.experiment.ScenarioId;

//import plugins.gcm.experiment.output.ConsoleLogItemHandler;
//import plugins.gcm.experiment.output.LogItem;
//import plugins.gcm.experiment.output.OutputItemHandler;
//import plugins.gcm.experiment.output.SimulationStatusItem;
//import plugins.gcm.experiment.output.SimulationStatusItemHandler;


//import plugins.gcm.experiment.progress.ExperimentProgressLog;
//import plugins.gcm.experiment.progress.NIOExperimentProgressLogReader;
//import plugins.gcm.experiment.progress.NIOExperimentProgressLogWriter;



import util.TimeElapser;

/**
 * Multi-threaded executor of an experiment using replications, reports and
 * various settings that influence how the experiment is executed.
 * 
 * @author Shawn Hatch
 *
 */

public final class ExperimentRunner {

	/*
	 * A data class for holding the inputs to this builder from its client.
	 */
	private static class Scaffold {
		private final List<OutputItemHandler> outputItemHandlers = new ArrayList<>();
		
		private OutputItemHandler logItemHandler;
		
		private Experiment experiment;
		private int threadCount;
		private boolean produceSimulationStatusOutput;
		
		private Path experimentProgressLogPath;
		private ExperimentProgressLog experimentProgressLog = ExperimentProgressLog.builder().build();
		
	}
	
	private ExperimentRunner(Scaffold scaffold) {
		this.scaffold = scaffold;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Scaffold scaffold = new Scaffold();
		private Builder() {}
		public ExperimentRunner build() {
			try {
				return new ExperimentRunner(scaffold);
			}finally {
				scaffold = new Scaffold();
			}
		}
		/**
		 * Add the output item handler to the experiment run.
		 * 
		 * @param outputItemHandler
		 *            the {@link OutputItemHandler} to add
		 * 
		 * @throws RuntimeException
		 *             if the output item handler is null
		 */
		public void addOutputItemHandler(final OutputItemHandler outputItemHandler) {
			if (outputItemHandler == null) {
				throw new RuntimeException("null output item handler");
			}
			scaffold.outputItemHandlers.add(outputItemHandler);
		}
		
		/**
		 * Adds the given scenarios to the experiment
		 *
		 * @param experiment
		 *            the experiment to be executed
		 */
		public void setExperiment(final Experiment experiment) {
			if (experiment == null) {
				throw new RuntimeException("null experiment");
			}
			scaffold.experiment = experiment;
		}
		/**
		 * Sets the path for experiment progress log. A null path turns off logging
		 * and run resumption. Default value is null.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 */
		public void setExperimentProgressLog(Path path) {
			scaffold.experimentProgressLogPath = path;
		}

		/**
		 * Sets the {@link LogItem} handler for the experiment. Defaulted to null --
		 * no logging.
		 */
		public void setLogItemHandler(OutputItemHandler logItemHandler) {
			scaffold.logItemHandler = logItemHandler;
		}
		
		
		/**
		 * Turns on or off the logging of experiment progress to standard out.
		 * Default value is false.
		 * 
		 * @param produceConsoleOutput
		 *            turns on/off production of the experiment progress reporting
		 */
		public void setProduceSimulationStatusOutput(boolean produceSimulationStatusOutput) {
			scaffold.produceSimulationStatusOutput = produceSimulationStatusOutput;
		}

		/**
		 * Sets the number of scenarios that may run concurrently. Generally this
		 * should be set to one less than the number of virtual processors on the
		 * machine that is running the experiment. Setting the thread count to zero
		 * causes the simulations to execute in the calling thread that invokes
		 * execute() on this ExperimentExecutor.
		 *
		 * @param threadCount
		 *            -- The number of threads to use to run the experiment.
		 * 
		 * @throws RuntimeException
		 *             if the thread count is negative
		 * 
		 */
		public void setThreadCount(final int threadCount) {
			if (threadCount < 0) {
				throw new RuntimeException("negative thread count");
			}
			scaffold.threadCount = threadCount;
		}

	}
	

	/*
	 * Class representing the return type of SimulationCallable. This is largely
	 * a placeholder since there has to be a return type for a callable.
	 * SimResult is never utilized.
	 */
	@Immutable
	private static class SimResult {
		private final boolean success;
		private final ScenarioId scenarioId;
		private final ReplicationId replicationId;

		public SimResult(final ScenarioId scenarioId, final ReplicationId replicationId, final boolean success) {
			this.scenarioId = scenarioId;
			this.replicationId = replicationId;
			this.success = success;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("Simulation Run for");
			builder.append(" Scenario ");
			builder.append(scenarioId);
			builder.append(" Replication ");
			builder.append(replicationId);
			if (success) {
				builder.append(" succeeded");
			} else {
				builder.append(" failed");
			}
			return builder.toString();
		}
	}

	/*
	 * A Callable implementor that runs the simulation in a thread from the
	 * completion service. The simulation instance, its Components and
	 * StateChangeListeners are created in the child thread by this
	 * SimulationCallable via the call() invocation. In this manner, they are
	 * thread-contained and thus thread-safe. It is important to note that no
	 * part of any of these objects should leak outside of the child thread.
	 *
	 * The parent and child thread share the following:
	 *
	 * <li>the Scenario <li>the Replication <li> the list of Output Item
	 * Handlers
	 *
	 * Thread safety is maintained by adherence to the following policies:
	 *
	 * The Scenario is a thread safe immutable class.
	 *
	 * The Replication is a thread safe immutable class.
	 * 
	 * The List of OutputItemHandler is a thread safe, immutable list of
	 * OutputItemHandler that are also thread safe.
	 *
	 * The SimResult is a thread safe, immutable class.
	 *
	 * The ReportItems are thread safe, immutable classes.
	 * 
	 */
	private static class SimulationCallable implements Callable<SimResult> {

		private final int scenarioId;

		private final List<OutputItemHandler> outputItemHandlers;

		/*
		 * All construction arguments are thread safe implementations.
		 */
		private SimulationCallable(final Scenario scenario, int scenarioId, final List<OutputItemHandler> outputItemHandlers) {
			this.scenarioId = scenarioId;
			this.replication = replication;
			this.scenario = scenario;
			this.outputItemHandlers = new ArrayList<>(outputItemHandlers);
		}

		/**
		 * Executes the simulation using the scenario, replication and output
		 * item handlers. Returns a SimResult which will indicated
		 * success/failure. If the simulation throws an exception it is caught
		 * and handled by reporting to standard error that the failure occured
		 * as well as printing a stack trace.
		 */
		@Override
		public SimResult call() throws Exception {

			// Sim status handling here is sub-optimal and need to be abstracted away
			List<OutputItemHandler> simStatusHandlers = new ArrayList<>();
			for (OutputItemHandler outputItemHandler : outputItemHandlers) {
				if (outputItemHandler.getHandledClasses().contains(SimulationStatusItem.class)) {
					simStatusHandlers.add(outputItemHandler);
				}
			}


			EngineBuilder engineBuilder =  GCMMonolithicSupport.getEngineBuilder(scenario, replication.getSeed());
			OutputItemConsumerManager outputItemConsumerManager = new OutputItemConsumerManager(scenarioId, replication.getId(), outputItemHandlers);
			engineBuilder.setOutputConsumer(outputItemConsumerManager::resolveEvent);
			Engine engine = engineBuilder.build();			

			// execute the simulation
			boolean success = false;
			try {

				if (simStatusHandlers.isEmpty()) {
					engine.execute();
					success = true;
				} else {
					TimeElapser timeElapser = new TimeElapser();
					try {
						engine.execute();
						success = true;
					} finally {
						SimulationStatusItem simulationStatusItem = new SimulationStatusItem(timeElapser.getElapsedMilliSeconds(), success);
						for (OutputItemHandler outputItemHandler : simStatusHandlers) {
							outputItemHandler.handle(scenarioId, replication.getId(), simulationStatusItem);
						}
					}
				}

			} catch (final Exception e) {

				System.err.println("Simulation failure for scenario " + scenarioId + " and replication " + replication.getId());
				e.printStackTrace();
			}
			return new SimResult(scenarioId, replication.getId(), success);
		}

	}

	private final Scaffold scaffold;

	

	/**
	 * Executes the experiment using the information supplied via the various
	 * mutation methods. Clears all collected data upon completion. Thus this
	 * ExperimentExecutor returns to an empty and idle state.
	 * 
	 * @throws RuntimeException
	 *             if the experiment was not set
	 */
	public void execute() {

		if (scaffold.logItemHandler == null) {
			scaffold.logItemHandler = new ConsoleLogItemHandler();
		}
		addOutputItemHandler(scaffold.logItemHandler);

		if (scaffold.produceSimulationStatusOutput) {
			addOutputItemHandler(new SimulationStatusItemHandler(scaffold.experiment.getScenarioCount(), scaffold.replicationCount, scaffold.logItemHandler));
		}

		if (scaffold.experimentProgressLogPath != null) {
			scaffold.experimentProgressLog = NIOExperimentProgressLogReader.read(scaffold.experimentProgressLogPath);
			addOutputItemHandler(new NIOExperimentProgressLogWriter(scaffold.experimentProgressLogPath));
		}
		

		if (scaffold.experiment == null) {
			throw new RuntimeException("null experiment");
		}

		if (scaffold.threadCount > 0) {
			executeMultiThreaded();
		} else {
			executeSingleThreaded();
		}
	}

	/*
	 * Utility class used for executing the experiment in a multi-threaded mode.
	 * Represents the scenario/replication pair. Allows for sorting where
	 * scenarios are executed with a random order so that long running scenarios
	 * are less likely to occupy all threads at the same time.
	 *
	 */
	private static class Job {// implements Comparable<Job> {
		int scenarioIndex;
		int replicationIndex;
	}

	private static class ScenarioCacheBlock {
		private final Scenario scenario;
		private int replicationCount;

		public ScenarioCacheBlock(Scenario scenario) {
			this.scenario = scenario;
		}
	}

	/*
	 * A cache for scenarios to cut down on scenario generation costs.
	 */
	private static class ScenarioCache {
		private final int replicationCount;
		private final Experiment experiment;
		private Map<Integer, ScenarioCacheBlock> cache = new LinkedHashMap<>();

		public ScenarioCache(int replicationCount, Experiment experiment) {
			this.replicationCount = replicationCount;
			this.experiment = experiment;
		}

		public Scenario getScenario(int scenarioIndex) {
			ScenarioCacheBlock scenarioCacheBlock = cache.get(scenarioIndex);
			if (scenarioCacheBlock == null) {
				Scenario scenario = experiment.getScenario(scenarioIndex);
				scenarioCacheBlock = new ScenarioCacheBlock(scenario);
				cache.put(scenarioIndex, scenarioCacheBlock);
			}
			scenarioCacheBlock.replicationCount++;
			if (scenarioCacheBlock.replicationCount >= replicationCount) {
				cache.remove(scenarioIndex);
			}
			return scenarioCacheBlock.scenario;
		}

	}

	/*
	 * Executes the experiment utilizing multiple threads. If the simulation
	 * throws an exception it is caught and handled by reporting to standard
	 * error that the failure occured as well as printing a stack trace.
	 */
	private void executeMultiThreaded() {
		try {

			/*
			 * Let all the output item handlers know that the experiment is
			 * starting
			 */
			for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
				outputItemHandler.openExperiment(scaffold.experimentProgressLog);
			}

			/*
			 * Get the replications
			 */

			ScenarioCache scenarioCache = new ScenarioCache(scaffold.replicationCount, scaffold.experiment);

			final List<Replication> replications = Replication.getReplications(scaffold.replicationCount, scaffold.seed);

			// Create the jobs and sort them to help avoid long running
			// scenarios from bunching up in the queue. Execute only the
			// scenario/replication pairs that are not contained in the
			// experiment progress log.
			List<Job> jobs = new ArrayList<>();

			for (int i = 0; i < scaffold.experiment.getScenarioCount(); i++) {
				for (int j = 0; j < replications.size(); j++) {
					ScenarioId scenarioId = scaffold.experiment.getScenarioId(i);
					ReplicationId replicationId = replications.get(j).getId();
					if (!scaffold.experimentProgressLog.contains(scenarioId, replicationId)) {
						Job job = new Job();
						job.scenarioIndex = i;
						job.replicationIndex = j;

						jobs.add(job);
					}
				}
			}

			/*
			 * If there is nothing to do, then do not engage.
			 */
			if (!jobs.isEmpty()) {

				int jobIndex = 0;

				// Create the Completion Service using the suggested thread
				// count
				final ExecutorService executorService = Executors.newFixedThreadPool(scaffold.threadCount);
				final CompletionService<SimResult> completionService = new ExecutorCompletionService<>(executorService);

				/*
				 * Start the initial threads. Don't exceed the thread count or
				 * the job count. Each time a thread is cleared, a new
				 * simulation will be processed through the CompletionService
				 * until we run out of simulations to run.
				 */
				while (jobIndex < Math.min(scaffold.threadCount, jobs.size()) - 1) {
					Job job = jobs.get(jobIndex);
					// Scenario scenario =
					// scaffold.experiment.getScenario(job.scenarioIndex);
					Scenario scenario = scenarioCache.getScenario(job.scenarioIndex);
					ScenarioId scenarioId = scaffold.experiment.getScenarioId(job.scenarioIndex);
					Replication replication = replications.get(job.replicationIndex);
					completionService.submit(new SimulationCallable(scenario, scenarioId, replication, scaffold.outputItemHandlers));
					jobIndex++;
				}

				/*
				 * While there are still jobs to be assigned to a thread, or
				 * jobs that have not yet completed processing, we check to see
				 * if a new job needs processing and see if a previous job has
				 * completed.
				 */
				int jobCompletionCount = 0;
				while (jobCompletionCount < jobs.size()) {
					if (jobIndex < jobs.size()) {
						Job job = jobs.get(jobIndex);
						// Scenario scenario =
						// scaffold.experiment.getScenario(job.scenarioIndex);
						Scenario scenario = scenarioCache.getScenario(job.scenarioIndex);
						ScenarioId scenarioId = scaffold.experiment.getScenarioId(job.scenarioIndex);
						Replication replication = replications.get(job.replicationIndex);
						completionService.submit(new SimulationCallable(scenario, scenarioId, replication, scaffold.outputItemHandlers));
						jobIndex++;
					}

					/*
					 * This call is blocking and waits for a job to complete and
					 * a thread to clear.
					 */
					try {
						completionService.take().get();
					} catch (final InterruptedException | ExecutionException e) {
						// Note that this is the completion service failing and
						// not the simulation
						throw new RuntimeException(e);
					}

					/*
					 * Once the blocking call returns, we increment the
					 * jobCompletionCount
					 */
					jobCompletionCount++;
				}

				/*
				 * Since all jobs are done, the CompletionService is no longer
				 * needed so we shut down the executorService that backs it.
				 */
				executorService.shutdown();
			}
			/*
			 * We let the output items handlers know that the experiment is
			 * finished.
			 */
			for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
				outputItemHandler.closeExperiment();
			}
		} finally {
			scaffold = new Scaffold();
		}
	}

	/*
	 * Executes the experiment using the main thread. If the simulation throws
	 * an exception it is caught and handled by reporting to standard error that
	 * the failure occurred as well as printing a stack trace.
	 */
	private void executeSingleThreaded() {
		try {

			// this is a temporary solution until we build an experiment
			// level engine corresponding to the sim engine event management
			// mechanisms
			List<OutputItemHandler> simStatusHandlers = new ArrayList<>();
			for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
				if (outputItemHandler.getHandledClasses().contains(SimulationStatusItem.class)) {
					simStatusHandlers.add(outputItemHandler);
				}
			}

			/*
			 * Let all the output item handlers know that the experiment is
			 * starting
			 */
			for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
				outputItemHandler.openExperiment(scaffold.experimentProgressLog);
			}

			/*
			 * Retrieve the replications.
			 */
			final List<Replication> replications = Replication.getReplications(scaffold.replicationCount, scaffold.seed);

			/*
			 * The number of simulation runs is the product of the number of
			 * scenarios and the number of replications
			 */
			final int jobCount = replications.size() * scaffold.experiment.getScenarioCount();
			/*
			 * If there is nothing to do, then do not engage.
			 */
			if (jobCount == 0) {
				return;
			}

			/*
			 * Execute each scenario/replication pair that is not contained in
			 * the experiment progress log.
			 */

			

			for (int i = 0; i < scaffold.experiment.getScenarioCount(); i++) {
				Scenario scenario = scaffold.experiment.getScenario(i);
				ScenarioId scenarioId = scaffold.experiment.getScenarioId(i);
				for (final Replication replication : replications) {
					if (!scaffold.experimentProgressLog.contains(scenarioId, replication.getId())) {

					
						EngineBuilder engineBuilder =  GCMMonolithicSupport.getEngineBuilder(scenario, replication.getSeed());
						OutputItemConsumerManager outputItemConsumerManager = new OutputItemConsumerManager(scenarioId, replication.getId(), scaffold.outputItemHandlers);
						engineBuilder.setOutputConsumer(outputItemConsumerManager::resolveEvent);
						Engine engine = engineBuilder.build();			

						try {
							boolean success = false;
							if (simStatusHandlers.isEmpty()) {
								engine.execute();
								success = true;
							} else {
								TimeElapser timeElapser = new TimeElapser();
								try {
									engine.execute();
									success = true;
								} finally {
									SimulationStatusItem simulationStatusItem = new SimulationStatusItem(timeElapser.getElapsedMilliSeconds(), success);
									for (OutputItemHandler outputItemHandler : simStatusHandlers) {
										outputItemHandler.handle(scenarioId, replication.getId(), simulationStatusItem);
									}
								}
							}
						} catch (final Exception e) {
							System.err.println("Simulation failure for scenario " + scenarioId + " and replication " + replication.getId());
							e.printStackTrace();
						}
					}
				}
			}

			for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
				outputItemHandler.closeExperiment();
			}
		} finally {
			scaffold = new Scaffold();
		}
	}

	
	
	

	
	

	
	
	
}
