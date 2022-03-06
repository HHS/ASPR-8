package plugins.reports.support;

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
import java.util.List;
import java.util.Optional;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import nucleus.ExperimentContext;
import nucleus.ScenarioStatus;

/**
 * An {@link OutputItemHandler} implementor that supports tab delimited text
 * based files that have a header. Supports continuation of experiment progress
 * across multiple experiment runs.
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public final class LineWriter {

	private final boolean useExperimentColumns;
	private static final String lineSeparator = System.getProperty("line.separator");
	private final Object headerLock = new Object();
	private final BufferedWriter writer;

	@GuardedBy(value = "headerLock")
	private boolean headerWritten;

	/**
	 * Creates this {@link NIOHeaderedOutputItemHandler} The path to the file
	 * that may or may not exist and may contain some complete or partial
	 * content from a previous execution of the experiment. If not empty, this
	 * file must have a header, be tab delimited and have as its first two
	 * columns the scenario and replication id values. Partial lines at the end
	 * of the file due to an ungraceful halt to the previous execution are
	 * tolerated. If the file does not exist, then its parent directory must
	 * exist.
	 */

	public LineWriter(final ExperimentContext experimentContext, final Path path, final boolean displayExperimentColumnsInReports) {

		this.useExperimentColumns = displayExperimentColumnsInReports;

		try {

			List<String> outputLines = new ArrayList<>();
			String headerLine = null;

			/*
			 * If the file is readable then we accept only those lines that
			 * correspond to a previously executed scenario
			 */
			if (Files.isRegularFile(path)) {
				List<String> inputLines = Files.readAllLines(path);
				boolean header = true;
				for (String line : inputLines) {
					if (!header) {
						String[] fields = line.split("\t");
						/*
						 * It is possible that the last line of a file was only
						 * partially written because neither the writter's close
						 * or flush was called during an abrupt shutdown. We
						 * expect that such cases will not correspond to
						 * successfully completed simulation execution, but must
						 * ensure that the parsing of the scenario and
						 * replication ids can still be performed
						 */
						if (fields.length > 1) {
							int scenarioId = Integer.parseInt(fields[0]);
							Optional<ScenarioStatus> optional = experimentContext.getScenarioStatus(scenarioId);
							if (optional.isPresent() && optional.get().equals(ScenarioStatus.PREVIOUSLY_SUCCEEDED)) {
								outputLines.add(line);
							}
						}
					} else {
						headerLine = line;
						header = false;
					}
				}
			}

			/*
			 * Remove the old file and write to the file the header and any
			 * retained lines from the previous execution.
			 */
			Files.deleteIfExists(path);
			CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
			OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			writer = new BufferedWriter(new OutputStreamWriter(out, encoder));

			if (!outputLines.isEmpty()) {
				writer.write(headerLine);
				writer.newLine();
				headerWritten = true;
			}

			for (String line : outputLines) {
				writer.write(line);
				writer.newLine();
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {

		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(ExperimentContext experimentContext, int scenarioId, ReportItem reportItem) {

		try {
			synchronized (headerLock) {
				if (!headerWritten) {
					final StringBuilder sb = new StringBuilder();

					sb.append("scenario");

					if (useExperimentColumns) {
						for (String item : experimentContext.getExperimentMetaData()) {
							sb.append("\t");
							sb.append(item);
						}
					}

					final List<String> headerStrings = reportItem.getReportHeader().getHeaderStrings();
					for (final String headerString : headerStrings) {
						sb.append("\t");
						sb.append(headerString);
					}

					sb.append(lineSeparator);
					writer.write(sb.toString());
					headerWritten = true;
				}
			}

			final StringBuilder sb = new StringBuilder();

			sb.append(scenarioId);
			if (useExperimentColumns) {
				List<String> metaData = experimentContext.getScenarioMetaData(scenarioId).get();
				for (String item : metaData) {
					sb.append("\t");
					sb.append(item);
				}
			}

			for (int i = 0; i < reportItem.size(); i++) {
				sb.append("\t");
				sb.append(reportItem.getValue(i));
			}
			sb.append(lineSeparator);
			writer.write(sb.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void flush() {
		try {
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}