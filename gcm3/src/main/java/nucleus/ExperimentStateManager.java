package nucleus;

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

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;
import util.ContractException;
import util.TimeElapser;
import util.TriConsumer;

/**
 * A utility class used by the experiment to manage experiment context consumers
 * and ensure thread safe access to scenario related state.
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public final class ExperimentStateManager {

	private ExperimentContext experimentContext;

	private class ExperimentContextImpl implements ExperimentContext {

		
		@Override
		public void subscribeToSimulationOpen(BiConsumer<ExperimentContext, Integer> consumer) {
			ExperimentStateManager.this.subscribeToSimulationOpen(consumer);
			
		}

		@Override
		public void subscribeToSimulationClose(BiConsumer<ExperimentContext, Integer> consumer) {
			ExperimentStateManager.this.subscribeToSimulationClose(consumer);
		}

		@Override
		public void subscribeToExperimentOpen(Consumer<ExperimentContext> consumer) {
			ExperimentStateManager.this.subscribeToExperimentOpen(consumer);
		}

		@Override
		public void subscribeToExperimentClose(Consumer<ExperimentContext> consumer) {
			ExperimentStateManager.this.subscribeToExperimentClose(consumer);
		}

		@Override
		public <T> void subscribeToOutput(Class<T> outputClass, TriConsumer<ExperimentContext, Integer, T> consumer) {
			ExperimentStateManager.this.subscribeToOutput(outputClass, consumer);
		}

		@Override
		public Optional<List<String>> getScenarioMetaData(int scenarioId) {
			return ExperimentStateManager.this.getScenarioMetaData(scenarioId);
		}

		@Override
		public List<String> getExperimentMetaData() {
			return ExperimentStateManager.this.getExperimentMetaData();
		}

		@Override
		public int getScenarioCount() {
			return ExperimentStateManager.this.getScenarioCount();
		}

		@Override
		public Optional<ScenarioStatus> getScenarioStatus(int scenarioId) {
			return ExperimentStateManager.this.getScenarioStatus(scenarioId);
		}

		@Override
		public int getStatusCount(ScenarioStatus scenarioStatus) {
			return ExperimentStateManager.this.getStatusCount(scenarioStatus);
		}

		@Override
		public double getElapsedSeconds() {
			return ExperimentStateManager.this.getElapsedSeconds();
		}

		@Override
		public List<Integer> getScenarios(ScenarioStatus scenarioStatus) {
			return ExperimentStateManager.this.getScenarios(scenarioStatus);
		}

	}

	private TimeElapser timeElapser = new TimeElapser();

	private Map<Integer, ScenarioRecord> scenarioRecords = new LinkedHashMap<>();

	public synchronized void openScenario(Integer scenarioId, List<String> metaData) {

		ScenarioRecord scenarioRecord = new ScenarioRecord();
		scenarioRecord.scenarioStatus = ScenarioStatus.RUNNING;
		scenarioRecord.metaData = metaData;

		scenarioRecords.put(scenarioId, scenarioRecord);

		for (BiConsumer<ExperimentContext, Integer> consumer : simOpenConsumers) {
			consumer.accept(experimentContext, scenarioId);
		}
	}

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public synchronized void closeScenario(Integer scenarioId, boolean success) {

		ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);

		if (success) {
			scenarioRecord.scenarioStatus = ScenarioStatus.SUCCEDED;
		} else {
			scenarioRecord.scenarioStatus = ScenarioStatus.FAILED;
		}

		for (BiConsumer<ExperimentContext, Integer> consumer : simCloseConsumers) {
			consumer.accept(experimentContext, scenarioId);
		}

		if (success && writer!=null) {
			try {
				writer.write(scenarioId);
				for (String metaDatum : scenarioRecord.metaData) {
					writer.write("\t");
					writer.write(metaDatum);
				}
				writer.write(LINE_SEPARATOR);
				writer.flush();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public synchronized Optional<ScenarioStatus> getScenarioStatus(int scenarioId) {
		ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);
		ScenarioStatus result = null;
		if (scenarioRecord != null) {
			result = scenarioRecord.scenarioStatus;
		}
		return Optional.ofNullable(result);
	}

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

	public synchronized List<String> getExperimentMetaData() {
		return new ArrayList<>(data.experimentMetaData);
	}

	public synchronized Optional<List<String>> getScenarioMetaData(Integer scenarioId) {
		ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);
		List<String> result = null;
		if (scenarioRecord != null) {
			result = new ArrayList<>(scenarioRecord.metaData);
		}
		return Optional.ofNullable(result);
	}

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

	public synchronized double getElapsedSeconds() {
		return timeElapser.getElapsedSeconds();
	}

	public synchronized int getScenarioCount() {
		return data.scenarioCount;
	}

	private static class ScenarioRecord {
		private ScenarioStatus scenarioStatus;
		private List<String> metaData = new ArrayList<>();
	}

	private static class Data {
		private Integer scenarioCount;
		private Path progressLogFile;
		private List<String> experimentMetaData = new ArrayList<>();
		private List<Consumer<ExperimentContext>> contextConsumers = new ArrayList<>();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}

		public ExperimentStateManager build() {
			try {
				return new ExperimentStateManager(data);
			} finally {
				data = new Data();
			}
		}

		public Builder setScenarioCount(Integer scenarioCount) {
			data.scenarioCount = scenarioCount;
			return this;
		}

		public Builder setScenarioProgressLogFile(Path progressLogFile) {
			data.progressLogFile = progressLogFile;
			return this;
		}

		public Builder setExperimentMetaData(List<String> experimentMetaData) {
			data.experimentMetaData = new ArrayList<>(experimentMetaData);
			return this;
		}

		public Builder addExperimentContextConsumer(Consumer<ExperimentContext> contextConsumer) {
			data.contextConsumers.add(contextConsumer);
			return this;
		}
	}

	private Data data;

	private BufferedWriter writer;

	private ExperimentStateManager(Data data) {
		this.data = data;
		experimentContext = new ExperimentContextImpl();
	}

	private void subscribeToSimulationOpen(BiConsumer<ExperimentContext, Integer> consumer) {
		simOpenConsumers.add(consumer);
	}

	private void subscribeToSimulationClose(BiConsumer<ExperimentContext, Integer> consumer) {
		simCloseConsumers.add(consumer);
	}

	private void subscribeToExperimentOpen(Consumer<ExperimentContext> consumer) {
		experimentOpenConsumers.add(consumer);
	}

	private void subscribeToExperimentClose(Consumer<ExperimentContext> consumer) {
		experimentCloseConsumers.add(consumer);
	}

	private <T> void subscribeToOutput(Class<T> outputClass, TriConsumer<ExperimentContext, Integer, T> consumer) {
		Set<TriConsumer<ExperimentContext, Integer, Object>> outputConsumers = outputConsumerMap.get(outputClass);
		if (outputConsumers == null) {
			outputConsumers = new LinkedHashSet<>();
			outputConsumerMap.put(outputClass, outputConsumers);
		}
		outputConsumers.add(new MetaOutputConsumer<>(consumer)::accept);
	}

	private final static String SCENARIO_LABEL = "scenario";

	private void readProgressFile() {

		if (data.progressLogFile == null) {
			return;
		}

		if (!Files.isRegularFile(data.progressLogFile)) {
			throw new ContractException(NucleusError.UNREADABLE_SCEANARIO_PROGRESS);
		}

		/*
		 * If the file is not a regular file then there is nothing to read.
		 * Otherwise read in the tab delimited lines.
		 */
		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(data.progressLogFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ContractException(NucleusError.UNREADABLE_SCEANARIO_PROGRESS);
		}

		/*
		 * Corruption of lines in this file is expected since it may not have
		 * been closed properly. We will default to not loading input
		 * gracefully.
		 */
		if (lines.isEmpty()) {
			return;
		}

		/*
		 * If the header line does not match the current meta data, then we will
		 * ignore the file
		 */

		List<String> expectedHeader = new ArrayList<>();
		expectedHeader.add(SCENARIO_LABEL);
		expectedHeader.addAll(data.experimentMetaData);

		List<String> actualHeader = Arrays.asList(lines.get(0).split("\t"));
		if (!expectedHeader.equals(actualHeader)) {
			return;
		}

		// Load the remaining lines
		int n = lines.size();
		for (int i = 0; i < n; i++) {
			List<String> entries = Arrays.asList(lines.get(i).split("\t"));
			if (entries.size() != expectedHeader.size()) {
				// something is wrong with the line and we stop reading
				break;
			}

			// try to get the scenario id
			int scenarioId = -1;
			try {
				scenarioId = Integer.parseInt(entries.get(0));
			} catch (Exception e) {
				// the line is corrupt and we stop reading
				break;
			}

			/*
			 * if the scenario was previously encountered, then the whole file
			 * is corrupt and we remove all scenario records
			 */
			if (scenarioRecords.containsKey(scenarioId)) {
				scenarioRecords.clear();
				break;
			}

			// record the scenario record
			ScenarioRecord scenarioRecord = new ScenarioRecord();

			scenarioRecord.metaData = new ArrayList<>();
			for (int j = 0; j < entries.size(); j++) {
				scenarioRecord.scenarioStatus = ScenarioStatus.PREVIOUSLY_SUCCEEDED;
				scenarioRecord.metaData.add(entries.get(j));
			}
			scenarioRecords.put(scenarioId, scenarioRecord);
		}

	}

	private void writeProgressFile() {
		if(data.progressLogFile == null) {
			return;
		}
		/*
		 * We will clear out the old content from the file and replace it with
		 * the items in the experiment progress log.
		 */
		final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
		OutputStream out;
		try {
			out = Files.newOutputStream(data.progressLogFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		writer = new BufferedWriter(new OutputStreamWriter(out, encoder));

		try {
			for (Integer scenarioId : scenarioRecords.keySet()) {
				writer.write(scenarioId);
				ScenarioRecord scenarioRecord = scenarioRecords.get(scenarioId);
				for (String metaDatum : scenarioRecord.metaData) {
					writer.write("\t");
					writer.write(metaDatum);
				}
				writer.write(LINE_SEPARATOR);				
			}
			writer.flush();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public synchronized void openExperiment() {

		readProgressFile();
		writeProgressFile();

		

		// handshake with the consumers
		for (Consumer<ExperimentContext> consumer : data.contextConsumers) {
			consumer.accept(experimentContext);
		}

		// announce the opening of the experiment
		for (Consumer<ExperimentContext> consumer : experimentOpenConsumers) {
			consumer.accept(experimentContext);
		}
	}

	public synchronized void closeExperiment() {
		for (Consumer<ExperimentContext> consumer : experimentCloseConsumers) {
			consumer.accept(experimentContext);
		}

		try {
			writer.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NotThreadSafe
	private static class OutputItemConsumerManager {

		private final Map<Class<?>, Set<TriConsumer<ExperimentContext, Integer, Object>>> map;
		private final Integer scenarioId;
		private ExperimentContext experimentContext;

		public OutputItemConsumerManager(ExperimentContext experimentContext, Integer scenarioId, Map<Class<?>, Set<TriConsumer<ExperimentContext, Integer, Object>>> consumerMap) {
			this.experimentContext = experimentContext;
			this.scenarioId = scenarioId;
			this.map = new LinkedHashMap<>(consumerMap);
		}

		public void handleOutput(Object output) {
			if (output == null) {
				throw new ContractException(NucleusError.NULL_OUTPUT_ITEM);
			}

			Set<TriConsumer<ExperimentContext, Integer, Object>> set = map.get(output.getClass());
			if (set != null) {
				for (TriConsumer<ExperimentContext, Integer, Object> triConsumer : set) {
					triConsumer.accept(experimentContext, scenarioId, output);
				}
			}

			// Set<OutputItemHandler> handlers =
			// handlerMap.get(output.getClass());
			//
			// /*
			// * It may happen that the class of an output item do not
			// explicitly
			// * match any handler, but that it compatible with that handler.
			// *
			// * For example suppose handlerX lists OutputItemY class as a type
			// it
			// * handles. When we encounter an instance of OutputItemZ that is a
			// * descendant of OutputItemY then we would want to extend the
			// content of
			// * the handlerMap so that all OutputItemZ are mapped to handlerX.
			// */
			// if (handlers == null) {
			// handlers = new LinkedHashSet<>();
			// for (Class<?> outputItemClass : handlerMap.keySet()) {
			// if (outputItemClass.isAssignableFrom(output.getClass())) {
			// handlers.addAll(handlerMap.get(outputItemClass));
			// }
			// }
			// handlerMap.put(output.getClass(), handlers);
			// }
			//
			// /*
			// * It is possible that the handlers set is empty. In that case the
			// * output item will be ignored.
			// */
			// for (OutputItemHandler outputItemHandler : handlers) {
			// outputItemHandler.handle(scenarioId, output);
			// }
		}

	}

	/**
	 * Returns a non-threadsafe consumer of output that will distribute output
	 * objects to the appropriate class-mapped output consumers. Each simulation
	 * instance running a scenario should have its own instance of this consumer
	 * that is confined to the thread running the simulation. This limits the
	 * thread collisions to the specific output consumer end points.
	 */
	public synchronized Consumer<Object> getOutputConsumer(Integer scenarioId) {
		return new OutputItemConsumerManager(experimentContext, scenarioId, outputConsumerMap)::handleOutput;
	}

}