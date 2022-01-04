package nucleus.util.experiment.progress;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import nucleus.util.experiment.progress.ExperimentProgressLog.Builder;


/**
 * A static utility for loading an {@link ExperimentProgressLog} from a file
 * using the java.nio API
 * 
 * @author Shawn Hatch
 *
 */
public final class NIOExperimentProgressLogReader {
	/**
	 * Returns an {@link ExperimentProgressLog} from the contents of the given
	 * file.
	 * 
	 * @param experimentProgressLogFile
	 *            the file containing the log info from the previous experiment
	 *            execution. The file must be a tab delimited text file
	 *            containing one (scenario id value,replication id value pair)
	 *            per line. Example: 12/t37/n/r
	 * 
	 * 
	 */
	public static ExperimentProgressLog read(Path experimentProgressLogFile) {
		/*
		 * If the file is not a regular file then there is nothing to read.
		 * Otherwise read in the tab delimited lines.
		 */
		List<String> lines = new ArrayList<>();
		if (Files.isRegularFile(experimentProgressLogFile)) {

			try {
				lines = Files.readAllLines(experimentProgressLogFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		/*
		 * Parse the lines and add the values to the
		 * ExperimentProgressLogBuilder. If any line is corrupt, ignore it and
		 * return just the content that was already read in.
		 */
		Builder builder = ExperimentProgressLog.builder();

		for (String line : lines) {
			try {				
				int scenarioId = Integer.parseInt(line);				
				builder.add(scenarioId);
			} catch (Exception e) {
				break;
			}
		}
		return builder.build();
	}
}
