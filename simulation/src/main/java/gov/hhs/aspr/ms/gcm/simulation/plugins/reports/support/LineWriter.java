package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ExperimentContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ScenarioStatus;
import gov.hhs.aspr.ms.util.resourcehelper.ResourceHelper;
import net.jcip.annotations.ThreadSafe;

/**
 * A thread-safe utility that supports tab delimited text based files that have
 * a header. This utility manages the writing of report items to a single file.
 * It assumes that all such items share a uniform header and establishes the
 * header of the output file on the first report item. If the writer is resuming
 * from a previous experiment, the header remains at originally written.
 * Supports continuation of experiment progress across multiple experiment runs.
 */
@ThreadSafe
public final class LineWriter {

	private final boolean useExperimentColumns;
	private static final String lineSeparator = System.getProperty("line.separator");

	private BufferedWriter writer;
	private OutputStreamWriter outputStreamWriter;
	private final String delimiter;

	/**
	 * Creates this {@link LineWriter} The path to the file that may or may not
	 * exist and may contain some complete or partial content from a previous
	 * execution of the experiment. If not empty, this file must have a header, be
	 * tab delimited and have as its first column be the scenario id. Partial lines
	 * at the end of the file due to an ungraceful halt to the previous execution
	 * are tolerated. If the file does not exist, then its parent directory must
	 * exist.
	 * 
	 * @throws RuntimeException
	 *                          <ul>
	 *                          <li>if an {@link IOException} is thrown during file
	 *                          initialization</li>
	 *                          <li>if the simulation run is continuing from a
	 *                          progress log and the path is not a regular file
	 *                          (path does not exist) during file
	 *                          initialization</li>
	 *                          </ul>
	 */
	public LineWriter(final ExperimentContext experimentContext, final Path path,
			final boolean displayExperimentColumnsInReports, String delimiter) {

		ResourceHelper.validateFilePath(path);

		this.delimiter = delimiter;
		this.useExperimentColumns = displayExperimentColumnsInReports;

		boolean loadedWithPreviousData = !experimentContext.getScenarios(ScenarioStatus.PREVIOUSLY_SUCCEEDED).isEmpty();
		loadedWithPreviousData &= Files.exists(path);

		if (loadedWithPreviousData) {
			initializeWithPreviousContent(path, experimentContext);
		} else {
			initializeWithNoPreviousContent(path);
		}
	}

	/*
	 * The path must correspond to an existing regular file.
	 */
	private void initializeWithPreviousContent(Path path, ExperimentContext experimentContext) {

		ResourceHelper.createNewFile(path.getParent(), "temp.txt");
		Path tempPath = path.getParent().resolve("temp.txt");
		CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
		try {
			/*
			 * Remove the old file and write to the file the header and any retained lines
			 * from the previous execution.
			 */
			OutputStream tempOut = Files.newOutputStream(tempPath, StandardOpenOption.CREATE);
			OutputStreamWriter tempOutStream = new OutputStreamWriter(tempOut, encoder);
			BufferedWriter tempWriter = new BufferedWriter(tempOutStream);
			BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));

			// this is safe because the header for sure always gets written when this line
			// writer gets created.
			String headerLine = reader.readLine();

			tempWriter.write(headerLine);
			tempWriter.newLine();
			String line;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(delimiter, -1);
				/*
				 * It is possible that the last line of a file was only partially written
				 * because neither the writer's close or flush was called during an abrupt
				 * shutdown. We expect that such cases will not correspond to successfully
				 * completed simulation execution, but must ensure that the parsing of the
				 * scenario and replication ids can still be performed
				 */
				if (fields.length > 1) {
					int scenarioId = Integer.parseInt(fields[0]);
					Optional<ScenarioStatus> optional = experimentContext.getScenarioStatus(scenarioId);
					if (optional.isPresent() && optional.get().equals(ScenarioStatus.PREVIOUSLY_SUCCEEDED)) {
						tempWriter.write(line);
						tempWriter.newLine();
					}
				}
			}
			reader.close();
			tempWriter.close();
			tempOutStream.close();

			tempPath.toFile().renameTo(path.toFile());

			OutputStream outStream = Files.newOutputStream(path, StandardOpenOption.APPEND);
			outputStreamWriter = new OutputStreamWriter(outStream, encoder);
			writer = new BufferedWriter(outputStreamWriter);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void initializeWithNoPreviousContent(Path path) {
		try {
			/*
			 * Remove the old file and write to the file the header and any retained lines
			 * from the previous execution.
			 */
			Files.deleteIfExists(path);
			CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
			OutputStream outStream = Files.newOutputStream(path, StandardOpenOption.CREATE);
			outputStreamWriter = new OutputStreamWriter(outStream, encoder);
			writer = new BufferedWriter(outputStreamWriter);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Closes the writer, flushing all buffered outputs.
	 * 
	 * @throws RuntimeException if an {@link IOException} is thrown
	 */
	public void close() {
		try {
			writer.close();
			outputStreamWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void getExperimentMetaData(ExperimentContext experimentContext, StringBuilder sb) {
		if (useExperimentColumns) {
			for (String item : experimentContext.getExperimentMetaData()) {
				sb.append(delimiter);
				sb.append(item);
			}
		}
	}

	protected void writeExperimentReportHeader(ExperimentContext experimentContext) {
		final StringBuilder sb = new StringBuilder();

		sb.append("scenario");

		getExperimentMetaData(experimentContext, sb);

		sb.append(lineSeparator);
		try {
			writer.write(sb.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeReportHeader(ExperimentContext experimentContext, ReportHeader reportHeader) {
		final StringBuilder sb = new StringBuilder();

		sb.append("scenario");

		getExperimentMetaData(experimentContext, sb);

		final List<String> headerStrings = reportHeader.getHeaderStrings();
		for (final String headerString : headerStrings) {
			sb.append(delimiter);
			sb.append(headerString);
		}

		sb.append(lineSeparator);
		try {
			writer.write(sb.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void getScenarioMetaData(ExperimentContext experimentContext, int scenarioId, StringBuilder sb) {
		if (useExperimentColumns) {
			List<String> metaData = experimentContext.getScenarioMetaData(scenarioId);
			for (String item : metaData) {
				sb.append(delimiter);
				sb.append(item);
			}
		}
	}

	protected void writeScenarioMetaData(ExperimentContext experimentContext, int scenarioId) {
		final StringBuilder sb = new StringBuilder();

		sb.append(scenarioId);
		getScenarioMetaData(experimentContext, scenarioId, sb);

		sb.append(lineSeparator);

		try {
			writer.write(sb.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	/**
	 * Writes the report item to file recorded under the given scenario.
	 * 
	 * @throws RuntimeException if an {@link IOException} is thrown
	 */
	protected void writeReportItem(ExperimentContext experimentContext, int scenarioId, ReportItem reportItem) {
		final StringBuilder sb = new StringBuilder();

		sb.append(scenarioId);
		getScenarioMetaData(experimentContext, scenarioId, sb);

		for (int i = 0; i < reportItem.size(); i++) {
			sb.append(delimiter);
			sb.append(reportItem.getValue(i));
		}
		sb.append(lineSeparator);

		try {
			writer.write(sb.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Flushes buffered output. Generally used to force the last the full reporting
	 * of a closed scenario.
	 * 
	 * @throws RuntimeException if an {@link IOException} is thrown
	 */
	public void flush() {
		try {
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}