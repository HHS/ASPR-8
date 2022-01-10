package nucleus.util.experiment.output;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import nucleus.util.experiment.Experiment;
import nucleus.util.experiment.progress.ExperimentProgressLog;
import plugins.reports.support.ReportItem;
/**
 * An {@link OutputItemHandler} implementor that supports tab delimited text based files
 * that have a header. Manages the loading of old progress consistent with
 * the {@link ExperimentProgressLog}
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public final class LineWriter implements OutputItemHandler {

		private final String experimentHeader;
		private final List<String> experimentFields = new ArrayList<>();
		private final Experiment regularExperiment;
		private final boolean displayExperimentColumnsInReports;
		
		
		
		private static final String lineSeparator = System.getProperty("line.separator");
		private final Object headerLock = new Object();
		@GuardedBy(value = "headerLock")
		private boolean headerWritten;
		private BufferedWriter writer;
		private final Path path;
		/**
		 * Creates this {@link NIOHeaderedOutputItemHandler}		 
		 *            The path to the file that may or may not exist and may contain some
		 *            complete or partial content from a previous execution of the
		 *            experiment. If not empty, this file must have a header, be tab
		 *            delimited and have as its first two columns the scenario and
		 *            replication id values. Partial lines at the end of the file
		 *            due to an ungraceful halt to the previous execution are
		 *            tolerated. If the file does not exist, then its parent
		 *            directory must exist.
		 */
		
		public LineWriter(final Path path, final Experiment regularExperiment, final boolean displayExperimentColumnsInReports, final ExperimentProgressLog experimentProgressLog) {
		

			this.displayExperimentColumnsInReports = displayExperimentColumnsInReports;

			this.regularExperiment = regularExperiment;

			if (displayExperimentColumnsInReports) {
				for (int i = 0; i < regularExperiment.getExperimentFieldCount(); i++) {
					experimentFields.add(regularExperiment.getExperimentFieldName(i));
				}
				final StringBuilder sb = new StringBuilder();
				for (final String experimentField : experimentFields) {
					sb.append("\t");
					sb.append(experimentField);
				}
				experimentHeader = sb.toString();
			} else {
				experimentHeader = "";
			}

		}

		
		public Set<Class<?>> getHandledClasses() {
			final Set<Class<?>> result = new LinkedHashSet<>();
			result.add(ReportItem.class);
			return result;
		}

		
		/*
		 * Returns the header for the file as tab delimited text where the first two
		 * columns are scenario id and replication id. Must not require any
		 * synchronization.
		 */		
		private String getHeader(final Object output) {
			final ReportItem reportItem = (ReportItem) output;
			final StringBuilder sb = new StringBuilder();
			sb.append("Scenario");
			sb.append("\t");
			sb.append("Replication");
			sb.append(experimentHeader);
			final List<String> headerStrings = reportItem.getReportHeader().getHeaderStrings();
			for (final String headerString : headerStrings) {
				sb.append("\t");
				sb.append(headerString);
			}
			return sb.toString();
		}

		
		/*
		 * Returns an output line as tab delimited text where the first two columns
		 * are scenario id and replication id. Must not require any synchronization.
		 */
		private String getOutputLine(int scenarioId, final Object output) {
			final ReportItem reportItem = (ReportItem) output;
			final StringBuilder sb = new StringBuilder();
			sb.append(scenarioId);
			if (displayExperimentColumnsInReports) {
				for (int i = 0; i < regularExperiment.getExperimentFieldCount(); i++) {
					sb.append("\t");
					final Object experimentFieldValue = regularExperiment.getExperimentFieldValue(scenarioId, i);
					sb.append(experimentFieldValue);
				}
			}

			for (int i = 0; i < reportItem.size(); i++) {
				sb.append("\t");
				sb.append(reportItem.getValue(i));
			}
			return sb.toString();
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
		
		@Override
		public void handle(int scenarioId, Object output) {
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
				sb.append(getOutputLine(scenarioId, output));
				sb.append(lineSeparator);
				writer.write(sb.toString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public void closeSimulation(int scenarioId) {
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
		public void openSimulation(int scenarioId) {
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
				 * accept only those lines that correspond to a scenario id
				 * contained in the experiment progress log
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
								int scenarioId = Integer.parseInt(fields[0]);								
								if (experimentProgressLog.contains(scenarioId)) {
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