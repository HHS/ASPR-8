package plugins.reports.support;

import java.io.*;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import nucleus.ExperimentContext;
import nucleus.ScenarioStatus;
import util.wrappers.MutableBoolean;

/**
 * A thread-safe utility that supports tab delimited text based files that have
 * a header. This utility manages the writing of report items to a single file.
 * It assumes that all such items share a uniform header and establishes the
 * header of the output file on the first report item. If the writer is resuming
 * from a previous experiment, the header remains at originally written.
 * Supports continuation of experiment progress across multiple experiment runs.
 * 
 *
 */
@ThreadSafe
public final class LineWriter {

	private final boolean useExperimentColumns;
	private static final String lineSeparator = System.getProperty("line.separator");
	private final Object headerLock = new Object();
	private BufferedWriter writer;

	@GuardedBy(value = "headerLock")
	private boolean headerWritten;

	/**
	 * Creates this {@link NIOHeaderedOutputItemHandler} The path to the file
	 * that may or may not exist and may contain some complete or partial
	 * content from a previous execution of the experiment. If not empty, this
	 * file must have a header, be tab delimited and have as its first column be
	 * the scenario id. Partial lines at the end of the file due to an
	 * ungraceful halt to the previous execution are tolerated. If the file does
	 * not exist, then its parent directory must exist.
	 * 
	 * @throws RuntimeException
	 *             <li>if an {@link IOException} is thrown during file initialization</li>
	 *             <li>if the simulation run is continuing from a progress log and
	 *             the path is not a regular file (path does not exist) during
	 *             file initialization</li>
	 * 
	 */

	public LineWriter(final ExperimentContext experimentContext, final Path path, final boolean displayExperimentColumnsInReports) {

		this.useExperimentColumns = displayExperimentColumnsInReports;

		boolean previouslyExists = experimentContext.getScenarios(ScenarioStatus.PREVIOUSLY_SUCCEEDED).isEmpty();

		if (previouslyExists) {
			init1(path, experimentContext);
		} else {
			init2(path);
		}

//		try {
//
//			List<String> outputLines = new ArrayList<>();
//			String headerLine = null;
//
//			/*
//			 * If the file is readable then we accept only those lines that
//			 * correspond to a previously executed scenario
//			 */
//			if (Files.isRegularFile(path)) {
//				List<String> inputLines = Files.readAllLines(path);
//				boolean header = true;
//				for (String line : inputLines) {
//					if (!header) {
//						String[] fields = line.split("\t");
//						/*
//						 * It is possible that the last line of a file was only
//						 * partially written because neither the writter's close
//						 * or flush was called during an abrupt shutdown. We
//						 * expect that such cases will not correspond to
//						 * successfully completed simulation execution, but must
//						 * ensure that the parsing of the scenario and
//						 * replication ids can still be performed
//						 */
//						if (fields.length > 1) {
//							int scenarioId = Integer.parseInt(fields[0]);
//							Optional<ScenarioStatus> optional = experimentContext.getScenarioStatus(scenarioId);
//							if (optional.isPresent() && optional.get().equals(ScenarioStatus.PREVIOUSLY_SUCCEEDED)) {
//								outputLines.add(line);
//							}
//						}
//					} else {
//						headerLine = line;
//						header = false;
//					}
//				}
//			}
//
//			/*
//			 * Remove the old file and write to the file the header and any
//			 * retained lines from the previous execution.
//			 */
//			Files.deleteIfExists(path);
//			CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
//			OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//			writer = new BufferedWriter(new OutputStreamWriter(out, encoder));
//
//			if (!outputLines.isEmpty()) {
//				writer.write(headerLine);
//				writer.newLine();
//				headerWritten = true;
//			}
//
//			for (String line : outputLines) {
//				writer.write(line);
//				writer.newLine();
//			}
//
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
	}

	private void init1(Path path, ExperimentContext experimentContext) {

		try {

			/*
			 * Remove the old file and write to the file the header and any
			 * retained lines from the previous execution.
			 */
			Path tempPath = path.resolve("C:\\Users\\varnerbf\\Documents\\ASPR\\ASPR-8\\tutorials\\plugins\\gcm4_report_refactor\\output\\progresslog.txt");
			Files.deleteIfExists(tempPath);
			CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
			OutputStream out = Files.newOutputStream(tempPath, StandardOpenOption.CREATE);
			writer = new BufferedWriter(new OutputStreamWriter(out, encoder));

			/*
			 * If the file is readable then we accept only those lines that
			 * correspond to a previously executed scenario
			 */
			if (Files.isRegularFile(path)) {
				Stream<String> lines = Files.lines(path);
				boolean[] header = new boolean[] {true};
				lines.forEach((line) -> {
					if (!header[0]) {
						String[] fields = line.split("\t");
						/*
						 * It is possible that the last line of a file was only
						 * partially written because neither the writer's close
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
			} else {
				throw new RuntimeException("Non-regular file at: " + path);
			}

			writer.close();

			tempPath.toFile().renameTo(path.toFile());

			encoder = StandardCharsets.UTF_8.newEncoder();
			out = Files.newOutputStream(path, StandardOpenOption.APPEND);
			writer = new BufferedWriter(new OutputStreamWriter(out, encoder));

//			Files.createTempFile(); //???


		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void init2(Path path) {

		try {
			/*
			 * Remove the old file and write to the file the header and any
			 * retained lines from the previous execution.
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
	 * @throws RuntimeException
	 * <li>if an {@link IOException} is thrown</li>
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
	 * @throws RuntimeException
	 *             <li>if an {@link IOException} is thrown</li> 
	 */
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

	/**
	 * Flushes buffered output. Generally used to force the last the full
	 * reporting of a closed scenario.
	 * 
	 * @throws RuntimeException
	 *             <li>if an {@link IOException} is thrown</li> 
	 */
	public void flush() {
		try {
			writer.flush();
		} catch (IOException e) {			
			throw new RuntimeException(e);
		}
	}

}