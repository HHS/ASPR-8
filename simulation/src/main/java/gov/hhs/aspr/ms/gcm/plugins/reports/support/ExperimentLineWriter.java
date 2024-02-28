package gov.hhs.aspr.ms.gcm.plugins.reports.support;

import java.io.BufferedWriter;
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
import java.util.stream.Stream;

import gov.hhs.aspr.ms.gcm.nucleus.ExperimentContext;
import gov.hhs.aspr.ms.gcm.nucleus.ScenarioStatus;
import net.jcip.annotations.GuardedBy;
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
public final class ExperimentLineWriter {

	private static final String lineSeparator = System.getProperty("line.separator");
	private final Object headerLock = new Object();
	private BufferedWriter writer;
	private final String delimiter;

	@GuardedBy(value = "headerLock")
	private boolean headerWritten;

	/**
	 * Creates this {@link ExperimentLineWriter} The path to the file that may or
	 * may not exist and may contain some complete or partial content from a
	 * previous execution of the experiment. If not empty, this file must have a
	 * header, be tab delimited and have as its first column be the scenario id.
	 * Partial lines at the end of the file due to an ungraceful halt to the
	 * previous execution are tolerated. If the file does not exist, then its parent
	 * directory must exist.
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
	public ExperimentLineWriter(final ExperimentContext experimentContext, final Path path, String delimiter) {

		if (Files.exists(path)) {
			if (!Files.isRegularFile(path)) {
				throw new RuntimeException("Non-regular file at: " + path);
			}
		}

		this.delimiter = delimiter;

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

		try {

			/*
			 * Remove the old file and write to the file the header and any retained lines
			 * from the previous execution.
			 */
			Path tempPath = path.getParent().resolve("temp.txt");
			Files.deleteIfExists(tempPath);
			CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
			OutputStream out = Files.newOutputStream(tempPath, StandardOpenOption.CREATE);
			writer = new BufferedWriter(new OutputStreamWriter(out, encoder));
			Stream<String> lines = Files.lines(path);
			boolean[] header = new boolean[] { true };
			lines.forEach((line) -> {
				if (!header[0]) {
					String[] fields = line.split(delimiter);
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
							try {
								writer.write(line);
								writer.newLine();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
				} else {
					try {
						writer.write(line);
						writer.newLine();
						headerWritten = true;
						header[0] = false;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
			lines.close();

			writer.close();

			tempPath.toFile().renameTo(path.toFile());

			encoder = StandardCharsets.UTF_8.newEncoder();
			out = Files.newOutputStream(path, StandardOpenOption.APPEND);
			writer = new BufferedWriter(new OutputStreamWriter(out, encoder));

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
			OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE);
			writer = new BufferedWriter(new OutputStreamWriter(out, encoder));

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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes the report item to file recorded under the given scenario.
	 * 
	 * @throws RuntimeException if an {@link IOException} is thrown
	 */
	public void write(ExperimentContext experimentContext, int scenarioId) {

		try {
			synchronized (headerLock) {
				if (!headerWritten) {
					final StringBuilder sb = new StringBuilder();

					sb.append("scenario");

					for (String item : experimentContext.getExperimentMetaData()) {
						sb.append(delimiter);
						sb.append(item);
					}

					sb.append(lineSeparator);
					writer.write(sb.toString());
					headerWritten = true;
				}
			}

			final StringBuilder sb = new StringBuilder();

			sb.append(scenarioId);

			List<String> metaData = experimentContext.getScenarioMetaData(scenarioId);
			for (String item : metaData) {
				sb.append(delimiter);
				sb.append(item);
			}

			sb.append(lineSeparator);
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