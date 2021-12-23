package plugins.gcm.experiment.output;

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

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import plugins.gcm.experiment.ReplicationId;
import plugins.gcm.experiment.ScenarioId;
import plugins.gcm.experiment.progress.ExperimentProgressLog;

/**
 * An abstract {@link OutputItemHandler} implementor that acts as the base class
 * for several other implementors. It supports tab delimited, text based files
 * that have a header and manages the loading of old progress consistent with
 * the {@link ExperimentProgressLog}
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public abstract class NIOHeaderedOutputItemHandler implements OutputItemHandler {
	private static final String lineSeparator = System.getProperty("line.separator");
	private final Object headerLock = new Object();
	@GuardedBy(value = "headerLock")
	private boolean headerWritten;
	private BufferedWriter writer;
	private final Path path;

	/**
	 * Creates this {@link NIOHeaderedOutputItemHandler}
	 * 
	 * @param path
	 *            The path to the file that may or may not exist and may some
	 *            complete or partial content from a previous execution of the
	 *            experiment. If not empty, this file must have a header, be tab
	 *            delimited and have as its first two columns the scenario and
	 *            replication id values. Partial lines at the end of the file
	 *            due to an ungraceful halt to the previous execution are
	 *            tolerated. If the file does not exist, then its parent
	 *            directory must exist.
	 */
	public NIOHeaderedOutputItemHandler(Path path) {
		this.path = path;
	}

	@Override
	public void closeExperiment() {
		if (path == null) {
			return;
		}
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the header for the file as tab delimited text where the first two
	 * columns are scenario id and replication id. Must not require any
	 * synchronization.
	 */
	protected abstract String getHeader(Object output);

	/**
	 * Returns an output line as tab delimited text where the first two columns
	 * are scenario id and replication id. Must not require any synchronization.
	 */
	protected abstract String getOutputLine(ScenarioId scenarioId, ReplicationId replicationId, Object output);

	@Override
	public void handle(ScenarioId scenarioId, ReplicationId replicationId, Object output) {
		if (path == null) {
			return;
		}
		try {
			synchronized (headerLock) {
				if (!headerWritten) {
					final StringBuilder sb = new StringBuilder();
					sb.append(getHeader(output));
					sb.append(lineSeparator);
					writer.write(sb.toString());
					headerWritten = true;
				}
			}

			final StringBuilder sb = new StringBuilder();
			sb.append(getOutputLine(scenarioId, replicationId, output));
			sb.append(lineSeparator);
			writer.write(sb.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void closeSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
		if (path == null) {
			return;
		}
		try {
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void openSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
		// do nothing
	}

	@Override
	public void openExperiment(ExperimentProgressLog experimentProgressLog) {
		if (path == null) {
			return;
		}
		try {

			List<String> outputLines = new ArrayList<>();
			String headerLine = null;

			/*
			 * If the file is readable and there was previous progress, then we
			 * accept only those lines that correspond to a (scenarioId,
			 * replicationId) contained in the experiment progress log
			 */
			if (Files.isRegularFile(path) && !experimentProgressLog.isEmpty()) {
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
							ScenarioId scenarioId = new ScenarioId(Integer.parseInt(fields[0]));
							ReplicationId replicationId = new ReplicationId(Integer.parseInt(fields[1]));
							if (experimentProgressLog.contains(scenarioId, replicationId)) {
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

}
