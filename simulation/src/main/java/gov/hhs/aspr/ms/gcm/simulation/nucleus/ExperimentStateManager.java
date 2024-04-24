package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.util.TriConsumer;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.time.TimeElapser;
import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

/**
 * A utility class used by the experiment to manage experiment context consumers
 * and ensure thread safe access to scenario related state.
 */
@ThreadSafe
public final class ExperimentStateManager {

	private ExperimentStatus experimentStatus = ExperimentStatus.READY;

	private static enum ExperimentStatus {
		READY, OPENED, CLOSED;
	}

	private ExperimentContext experimentContext;

	private TimeElapser timeElapser = new TimeElapser();

	private Map<Integer, ScenarioRecord> scenarioRecords = new LinkedHashMap<>();

	/**
	 * Updates the scenario's status to RUNNING and sets the meta data for the
	 * scenario. Announces the opening of the scenario to subscribed experiment
	 * context consumers. Should be preceded by openExperiment.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_SCENARIO_ID}if
	 *                           the scenario id is null</li>
	 *                           <li>{@linkplain NucleusError#NULL_META_DATA}if the
	 *                           meta data is null</li>
	 *                           <li>{@linkplain NucleusError#NULL_META_DATA}if the
	 *                           meta data contains a null datum</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_SCENARIO_ID}if
	 *                           the scenario is not known</li>
	 *                           <li>{@linkplain NucleusError#SCENARIO_CANNOT_BE_EXECUTED}if
	 *                           the scenario's current status is not
	 *                           {@linkplain ScenarioStatus#READY}</li>
	 *                           </ul>
	 */
	public synchronized void openScenario(Integer scenarioId, List<String> metaData) {

		if (scenarioId == null) {
			throw new ContractException(NucleusError.NULL_SCENARIO_ID);
		}

		ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);
		if (scenarioRecord == null) {
			throw new ContractException(NucleusError.UNKNOWN_SCENARIO_ID, scenarioId);
		}

		if (scenarioRecord.scenarioStatus != ScenarioStatus.READY) {
			throw new ContractException(NucleusError.SCENARIO_CANNOT_BE_EXECUTED,
					"scenario " + scenarioId + " " + scenarioRecord.scenarioStatus);
		}

		if (metaData == null) {
			throw new ContractException(NucleusError.NULL_META_DATA);
		}
		for (String metaDatum : metaData) {
			if (metaDatum == null) {
				throw new ContractException(NucleusError.NULL_META_DATA);
			}
		}

		scenarioRecord.scenarioStatus = ScenarioStatus.RUNNING;
		scenarioRecord.metaData = new ArrayList<>();
		scenarioRecord.metaData.addAll(metaData);

		for (BiConsumer<ExperimentContext, Integer> consumer : simOpenConsumers) {
			consumer.accept(experimentContext, scenarioId);
		}
	}

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/**
	 * Announces the closure of the scenario to subscribed experiment context
	 * consumers. Records the scenario in the scenario progress file if the file is
	 * active.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_SCENARIO_ID} if
	 *                           the scenario id is null</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_SCENARIO_ID}
	 *                           if the scenario id is not in the range [0,scenario
	 *                           count)</li>
	 *                           </ul>
	 */
	public synchronized void closeScenarioAsSuccess(Integer scenarioId) {

		if (scenarioId == null) {
			throw new ContractException(NucleusError.NULL_SCENARIO_ID);
		}

		ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);

		if (scenarioRecord == null) {
			throw new ContractException(NucleusError.UNKNOWN_SCENARIO_ID, scenarioId);
		}

		scenarioRecord.scenarioStatus = ScenarioStatus.SUCCEDED;

		for (BiConsumer<ExperimentContext, Integer> consumer : simCloseConsumers) {
			consumer.accept(experimentContext, scenarioId);
		}

		if (writer != null) {
			try {
				writer.write(scenarioId.toString());
				for (String metaDatum : scenarioRecord.metaData) {
					writer.write("\t");
					writer.write(metaDatum);
				}
				writer.write(LINE_SEPARATOR);
				writer.flush();
			} catch (final IOException e) {
				// re-thrown as runtime exception
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Announces the closure of the scenario to the subscribed experiment context
	 * consumers. Records the failure status of the scenario and its failure cause.
	 * Failed scenarios are not reported to the progress log.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_SCENARIO_ID} if
	 *                           the scenario id is null</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_SCENARIO_ID}
	 *                           if the scenario id is not in the range [0,scenario
	 *                           count)</li>
	 *                           </ul>
	 */
	public synchronized void closeScenarioAsFailure(Integer scenarioId, Exception exception) {

		if (scenarioId == null) {
			throw new ContractException(NucleusError.NULL_SCENARIO_ID);
		}

		ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);

		if (scenarioRecord == null) {
			throw new ContractException(NucleusError.UNKNOWN_SCENARIO_ID);
		}

		scenarioRecord.scenarioStatus = ScenarioStatus.FAILED;
		scenarioRecord.failureCause = exception;

		for (BiConsumer<ExperimentContext, Integer> consumer : simCloseConsumers) {
			consumer.accept(experimentContext, scenarioId);
		}

	}

	/**
	 * Returns the current status for the given scenario id if the scenario exists.
	 */
	public synchronized Optional<ScenarioStatus> getScenarioStatus(int scenarioId) {
		ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);
		ScenarioStatus result = null;
		if (scenarioRecord != null) {
			result = scenarioRecord.scenarioStatus;
		}
		return Optional.ofNullable(result);
	}

	public synchronized Optional<Exception> getScenarioFailureCause(int scenarioId) {
		ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);
		Exception result = null;
		if (scenarioRecord != null) {
			result = scenarioRecord.failureCause;
		}
		return Optional.ofNullable(result);
	}

	/**
	 * Returns the current number of scenarios with the given status
	 */
	public synchronized int getStatusCount(ScenarioStatus scenarioStatus) {
		int result = 0;
		for (Integer scenarioId : scenarioRecords.keySet()) {
			ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);
			if (scenarioRecord.scenarioStatus == scenarioStatus) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Returns the list of meta data for the experiment. These meta data are
	 * descriptors of the scenario meta data produced by each execution of the
	 * simulation.
	 */
	public synchronized List<String> getExperimentMetaData() {
		return new ArrayList<>(data.experimentMetaData);
	}

	/**
	 * Returns the list of currently available meta data for the given scenario id.
	 * Note that READY and SKIPPED scenarios will not have associated meta data.
	 */
	public synchronized List<String> getScenarioMetaData(Integer scenarioId) {
		ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);
		List<String> result = new ArrayList<>();
		if (scenarioRecord != null) {
			result.addAll(scenarioRecord.metaData);
		}
		return result;
	}

	/**
	 * Returns the current list of scenario ids for the given scenario status
	 */
	public synchronized List<Integer> getScenarios(ScenarioStatus scenarioStatus) {
		List<Integer> result = new ArrayList<>();
		for (Integer scenarioId : scenarioRecords.keySet()) {
			ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);
			if (scenarioRecord.scenarioStatus == scenarioStatus) {
				result.add(scenarioId);
			}
		}
		return result;
	}

	private List<BiConsumer<ExperimentContext, Integer>> simOpenConsumers = new ArrayList<>();

	private List<BiConsumer<ExperimentContext, Integer>> simCloseConsumers = new ArrayList<>();

	private List<Consumer<ExperimentContext>> experimentOpenConsumers = new ArrayList<>();

	private List<Consumer<ExperimentContext>> experimentCloseConsumers = new ArrayList<>();

	private Map<Class<?>, Set<TriConsumer<ExperimentContext, Integer, Object>>> outputConsumerMap = new LinkedHashMap<>();

	private static class MetaOutputConsumer<T> {
		private MetaOutputConsumer(TriConsumer<ExperimentContext, Integer, T> consumer) {
			this.consumer = consumer;
		}

		private TriConsumer<ExperimentContext, Integer, T> consumer;

		@SuppressWarnings("unchecked")
		public void accept(ExperimentContext experimentContext, Integer scenarioId, Object object) {
			consumer.accept(experimentContext, scenarioId, (T) object);
		}
	}

	/**
	 * Returns the number of seconds that have elapsed since the start of the
	 * experiment
	 */
	public synchronized double getElapsedSeconds() {
		return timeElapser.getElapsedSeconds();
	}

	/**
	 * Returns the number of scenarios in the experiment
	 */
	public synchronized int getScenarioCount() {
		return data.scenarioCount;
	}

	private static class ScenarioRecord {
		private ScenarioStatus scenarioStatus;
		private List<String> metaData = new ArrayList<>();
		private Exception failureCause;
	}

	private static class Data {
		private Integer scenarioCount = 0;
		private Path progressLogFile;
		private List<String> experimentMetaData = new ArrayList<>();
		private List<Consumer<ExperimentContext>> contextConsumers = new ArrayList<>();
		private boolean continueFromProgressLog;
		private Set<Integer> explicitScenarioIds = new LinkedHashSet<>();

		public Data() {
		}

		public Data(Data data) {
			scenarioCount = data.scenarioCount;
			progressLogFile = data.progressLogFile;
			experimentMetaData.addAll(data.experimentMetaData);
			contextConsumers.addAll(data.contextConsumers);
			continueFromProgressLog = data.continueFromProgressLog;
			explicitScenarioIds.addAll(data.explicitScenarioIds);
		}
	}

	/**
	 * Returns a new Builder instance for the ExperimentStateManager
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for ExperimentStateManager
	 */
	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}

		/**
		 * Builds the ExperimentStateManager from the collected data.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain NucleusError#UNKNOWN_SCENARIO_ID}
		 *                           if an explicit scenario id is not in the span of
		 *                           the experiment's scenario ids</li>
		 *                           <li>{@linkplain NucleusError#NULL_SCENARIO_PROGRESS_FILE}
		 *                           if continue from progress file was chosen, but the
		 *                           path to the file is null</li>
		 *                           <li>{@linkplain NucleusError#NON_EXISTANT_SCEANARIO_PROGRESS}
		 *                           if continue from progress file was chosen, but the
		 *                           path to the file does not exist</li>
		 *                           <li>{@linkplain NucleusError#UNREADABLE_SCEANARIO_PROGRESS}
		 *                           if continue from progress file was chosen, but the
		 *                           path lead to a non-file</li>
		 *                           <li>{@linkplain NucleusError#UNREADABLE_SCEANARIO_PROGRESS}
		 *                           if the lines of the file cannot be loaded</li>
		 *                           <li>{@linkplain NucleusError#INCOMPATIBLE_SCEANARIO_PROGRESS}
		 *                           if the header line of the file does not match the
		 *                           expected header line for the current
		 *                           experiment</li>
		 *                           <li>{@linkplain NucleusError#INCOMPATIBLE_SCEANARIO_PROGRESS}
		 *                           if a scenario id is encountered that is not valid
		 *                           for the the current experiment</li>
		 *                           </ul>
		 */
		public ExperimentStateManager build() {
			ExperimentStateManager result = new ExperimentStateManager(new Data(data));
			result.init();
			return result;
		}

		/**
		 * Sets the scenario count as calculated from the dimensions.
		 */
		public Builder setScenarioCount(Integer scenarioCount) {
			data.scenarioCount = scenarioCount;
			return this;
		}

		/**
		 * Sets the file that is used to report scenario progress.
		 */
		public Builder setScenarioProgressLogFile(Path progressLogFile) {
			data.progressLogFile = progressLogFile;
			return this;
		}

		/**
		 * Marks the scenario to be explicitly run. All other scenarios will be ignored.
		 */
		public Builder addExplicitScenarioId(Integer scenarioId) {
			data.explicitScenarioIds.add(scenarioId);
			return this;
		}

		/**
		 * Sets the list of string meta data for the experiment. These meta data are
		 * descriptors of the scenario meta data produced by each execution of the
		 * simulation.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain NucleusError#NULL_META_DATA} if the
		 *                           experiment meta data is null</li>
		 *                           <li>{@linkplain NucleusError#NULL_META_DATA} if the
		 *                           experiment meta data is null</li>
		 *                           <li>{@linkplain NucleusError#NULL_META_DATA} if the
		 *                           experiment meta data is null</li>
		 *                           </ul>
		 */
		public Builder setExperimentMetaData(List<String> experimentMetaData) {
			if (experimentMetaData == null) {
				throw new ContractException(NucleusError.NULL_META_DATA);
			}
			for (String metaDatum : experimentMetaData) {
				if (metaDatum == null) {
					throw new ContractException(NucleusError.NULL_META_DATA);
				}
			}
			data.experimentMetaData = new ArrayList<>(experimentMetaData);
			return this;
		}

		/**
		 * Adds a experiment context consumer that will be initialized at the start of
		 * the experiment.
		 * 
		 * @throws ContractException {@linkplain NucleusError#NULL_EXPERIMENT_CONTEXT_CONSUMER}
		 *                           if the context consumer is null
		 */
		public Builder addExperimentContextConsumer(Consumer<ExperimentContext> contextConsumer) {
			if (contextConsumer == null) {
				throw new ContractException(NucleusError.NULL_EXPERIMENT_CONTEXT_CONSUMER);
			}
			data.contextConsumers.add(contextConsumer);
			return this;
		}

		/**
		 * Sets the option to continue the current experiment from the progress.
		 */
		public Builder setContinueFromProgressLog(boolean continueFromProgressLog) {
			data.continueFromProgressLog = continueFromProgressLog;
			return this;
		}
	}

	private Data data;

	private BufferedWriter writer;

	private ExperimentStateManager(Data data) {
		this.data = data;
	}

	private void init() {
		experimentContext = new ExperimentContext(this);

		// validate the explicit scenario ids
		for (int id : data.explicitScenarioIds) {
			if ((id < 0) || (id >= data.scenarioCount)) {
				throw new ContractException(NucleusError.UNKNOWN_SCENARIO_ID, id);
			}
		}

		for (int i = 0; i < data.scenarioCount; i++) {
			ScenarioRecord scenarioRecord = new ScenarioRecord();
			scenarioRecord.scenarioStatus = ScenarioStatus.READY;
			scenarioRecords.put(i, scenarioRecord);
		}

		/*
		 * Mark any READY scenarios that were previously executed as
		 * PREVIOUSLY_SUCCEEDED
		 */
		if (data.continueFromProgressLog) {
			readProgressFile();
		}

		/*
		 * If there are any explicit scenario ids, adjust the status of the sceanrios
		 */
		if (!data.explicitScenarioIds.isEmpty()) {
			/*
			 * Force the explicit scenarios to READY. Explicitly selected scenarios must be
			 * executed.
			 */
			for (int id : data.explicitScenarioIds) {
				ScenarioRecord scenarioRecord = scenarioRecords.get(id);
				scenarioRecord.scenarioStatus = ScenarioStatus.READY;
			}

			/*
			 * Any remaining scenarios that are currently marked READY are now marked
			 * SKIPPED
			 */
			for (int id = 0; id < data.scenarioCount; id++) {
				ScenarioRecord scenarioRecord = scenarioRecords.get(id);
				if (scenarioRecord.scenarioStatus == ScenarioStatus.READY) {
					if (!data.explicitScenarioIds.contains(id)) {
						scenarioRecord.scenarioStatus = ScenarioStatus.SKIPPED;
					}
				}
			}
		}

		/*
		 * refresh the progress file as needed -- some of the PREVIOUSLY_SUCCEEDED may
		 * have been marked as READY since explicitly included scenarios are always run
		 */
		writeProgressFile();

	}

	protected synchronized void subscribeToSimulationOpen(BiConsumer<ExperimentContext, Integer> consumer) {
		simOpenConsumers.add(consumer);
	}

	protected synchronized void subscribeToSimulationClose(BiConsumer<ExperimentContext, Integer> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_EXPERIMENT_CONTEXT_CONSUMER);
		}
		simCloseConsumers.add(consumer);
	}

	protected synchronized void subscribeToExperimentOpen(Consumer<ExperimentContext> consumer) {
		experimentOpenConsumers.add(consumer);
	}

	protected synchronized void subscribeToExperimentClose(Consumer<ExperimentContext> consumer) {
		experimentCloseConsumers.add(consumer);
	}

	protected synchronized <T> void subscribeToOutput(Class<T> outputClass,
			TriConsumer<ExperimentContext, Integer, T> consumer) {
		Set<TriConsumer<ExperimentContext, Integer, Object>> outputConsumers = outputConsumerMap.get(outputClass);
		if (outputConsumers == null) {
			outputConsumers = new LinkedHashSet<>();
			outputConsumerMap.put(outputClass, outputConsumers);
		}
		outputConsumers.add(new MetaOutputConsumer<>(consumer)::accept);
	}

	private final static String SCENARIO_LABEL = "scenario";

	private void readProgressFile() {

		/*
		 * if the client does not want to continue from the progress log, we return.
		 */
		if (!data.continueFromProgressLog) {
			return;
		}

		/*
		 * If the progress file is null, then we can't proceed.
		 */
		if (data.progressLogFile == null) {
			throw new ContractException(NucleusError.NULL_SCENARIO_PROGRESS_FILE);
		}

		if (!Files.exists(data.progressLogFile)) {
			throw new ContractException(NucleusError.NON_EXISTANT_SCEANARIO_PROGRESS);
		}

		if (!Files.isRegularFile(data.progressLogFile)) {
			throw new ContractException(NucleusError.UNREADABLE_SCEANARIO_PROGRESS, "not a regular file");
		}

		/*
		 * Try to read in the tab delimited lines.
		 */
		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(data.progressLogFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ContractException(NucleusError.UNREADABLE_SCEANARIO_PROGRESS, " failed to load lines");
		}

		/*
		 * Corruption of lines in this file is expected since it may not have been
		 * closed properly. We will default to not loading input gracefully.
		 */
		if (lines.isEmpty()) {
			return;
		}

		/*
		 * If the header line does not match the current meta data, then we will throw
		 * an exception since the client may not want the existing file to be
		 * overwritten
		 */
		List<String> expectedHeader = new ArrayList<>();
		expectedHeader.add(SCENARIO_LABEL);
		expectedHeader.addAll(data.experimentMetaData);

		List<String> actualHeader = Arrays.asList(lines.get(0).split("\t"));
		if (!expectedHeader.equals(actualHeader)) {
			throw new ContractException(NucleusError.INCOMPATIBLE_SCEANARIO_PROGRESS, "wrong file header");
		}

		// Load the remaining lines
		int n = lines.size();
		for (int i = 1; i < n; i++) {
			List<String> entries = Arrays.asList(lines.get(i).split("\t"));
			if (entries.size() != expectedHeader.size()) {
				/*
				 * Potential ungraceful termination of previous experiment
				 */
				break;
			}

			// try to get the scenario id
			int scenarioId = -1;
			try {
				scenarioId = Integer.parseInt(entries.get(0));
			} catch (Exception e) {
				/*
				 * Potential ungraceful termination of previous experiment
				 */
				break;
			}

			ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);

			/*
			 * If the scenario is not recognized, then we throw an exception so that the
			 * file is not overwritten
			 */
			if (scenarioRecord == null) {
				throw new ContractException(NucleusError.INCOMPATIBLE_SCEANARIO_PROGRESS,
						"scenario " + scenarioId + " is out of bounds");
			}

			// record the scenario status and meta data
			scenarioRecord.scenarioStatus = ScenarioStatus.PREVIOUSLY_SUCCEEDED;

			scenarioRecord.metaData = new ArrayList<>();
			for (int j = 1; j < entries.size(); j++) {
				scenarioRecord.metaData.add(entries.get(j));
			}
			scenarioRecords.put(scenarioId, scenarioRecord);
		}

	}

	private void writeProgressFile() {
		if (data.progressLogFile == null) {
			return;
		}
		/*
		 * We will clear out the old content from the file and replace it with the items
		 * in the experiment progress log.
		 */
		final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
		OutputStream out;
		try {
			out = Files.newOutputStream(data.progressLogFile, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (final IOException e) {
			// re-thrown as runtime exception
			throw new RuntimeException(e);
		}
		writer = new BufferedWriter(new OutputStreamWriter(out, encoder));
		try {
			writer.write("scenario");
			for (String metaDatum : data.experimentMetaData) {
				writer.write("\t");
				writer.write(metaDatum);
			}
			writer.write(LINE_SEPARATOR);

			for (Integer scenarioId : scenarioRecords.keySet()) {
				ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);
				if (scenarioRecord.scenarioStatus == ScenarioStatus.PREVIOUSLY_SUCCEEDED) {

					writer.write(scenarioId.toString());
					for (String metaDatum : scenarioRecord.metaData) {
						writer.write("\t");
						writer.write(metaDatum);
					}
					writer.write(LINE_SEPARATOR);
				}
			}
			writer.flush();
		} catch (final IOException e) {
			// re-thrown as runtime exception
			throw new RuntimeException(e);
		}
	}

	/**
	 * Invokes all context consumers. Announces the opening of the experiment to
	 * subscribed experiment context consumers. Can only be called once.
	 * 
	 * @throws ContractException {@linkplain NucleusError#DUPLICATE_EXPERIMENT_OPEN}
	 *                           if invoked more that once
	 */
	public synchronized void openExperiment() {
		if (experimentStatus != ExperimentStatus.READY) {
			throw new ContractException(NucleusError.DUPLICATE_EXPERIMENT_OPEN);
		}
		experimentStatus = ExperimentStatus.OPENED;

		// handshake with the consumers
		for (Consumer<ExperimentContext> consumer : data.contextConsumers) {
			consumer.accept(experimentContext);
		}

		// announce the opening of the experiment
		for (Consumer<ExperimentContext> consumer : experimentOpenConsumers) {
			consumer.accept(experimentContext);
		}
	}

	/**
	 * Announces the closure of the experiment to subscribed experiment context
	 * consumers. Closes the experiment progress file if it is being used.
	 * 
	 * @throws ContractException {@linkplain NucleusError#UNCLOSABLE_EXPERIMENT} if
	 *                           the experiment status manager is not currently in
	 *                           the open state
	 */
	public synchronized void closeExperiment() {

		if (experimentStatus != ExperimentStatus.OPENED) {
			throw new ContractException(NucleusError.UNCLOSABLE_EXPERIMENT);
		}
		experimentStatus = ExperimentStatus.CLOSED;

		for (Consumer<ExperimentContext> consumer : experimentCloseConsumers) {
			consumer.accept(experimentContext);
		}

		try {
			if (writer != null) {
				writer.close();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NotThreadSafe
	private static class OutputItemConsumerManager {

		private final Map<Class<?>, Set<TriConsumer<ExperimentContext, Integer, Object>>> baseMap;
		private final Map<Class<?>, Set<TriConsumer<ExperimentContext, Integer, Object>>> workingMap = new LinkedHashMap<>();
		private final Integer scenarioId;
		private ExperimentContext experimentContext;

		public OutputItemConsumerManager(ExperimentContext experimentContext, Integer scenarioId,
				Map<Class<?>, Set<TriConsumer<ExperimentContext, Integer, Object>>> consumerMap) {
			this.experimentContext = experimentContext;
			this.scenarioId = scenarioId;
			this.baseMap = new LinkedHashMap<>(consumerMap);
		}

		public void handleOutput(Object output) {
			if (output == null) {
				throw new ContractException(NucleusError.NULL_OUTPUT_ITEM);
			}

			Set<TriConsumer<ExperimentContext, Integer, Object>> consumers = workingMap.get(output.getClass());
			if (consumers == null) {
				consumers = new LinkedHashSet<>();
				Class<? extends Object> outputClass = output.getClass();
				for (Class<?> c : baseMap.keySet()) {
					if (c.isAssignableFrom(outputClass)) {
						consumers.addAll(baseMap.get(c));
					}
				}
				workingMap.put(outputClass, consumers);
			}

			for (TriConsumer<ExperimentContext, Integer, Object> triConsumer : consumers) {
				triConsumer.accept(experimentContext, scenarioId, output);
			}
		}
	}

	/**
	 * Returns a non-threadsafe consumer of output that will distribute output
	 * objects to the appropriate class-mapped output consumers. Each simulation
	 * instance running a scenario should have its own instance of this consumer
	 * that is confined to the thread running the simulation. This limits the thread
	 * collisions to the specific output consumer end points.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_SCENARIO_ID} if
	 *                           the scenario id is null</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_SCENARIO_ID}
	 *                           if the scenario id is not in the range [0,scenario
	 *                           count)</li>
	 *                           </ul>
	 */
	public synchronized Consumer<Object> getOutputConsumer(Integer scenarioId) {
		if (scenarioId == null) {
			throw new ContractException(NucleusError.NULL_SCENARIO_ID);
		}
		ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);

		if (scenarioRecord == null) {
			throw new ContractException(NucleusError.UNKNOWN_SCENARIO_ID);
		}

		return new OutputItemConsumerManager(experimentContext, scenarioId, outputConsumerMap)::handleOutput;
	}

}