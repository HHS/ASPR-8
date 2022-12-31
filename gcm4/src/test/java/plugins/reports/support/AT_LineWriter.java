package plugins.reports.support;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import nucleus.ExperimentContext;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public class AT_LineWriter {

	@Test
	@UnitTestConstructor(target = LineWriter.class, args = { ExperimentContext.class, Path.class, boolean.class }, tags = { UnitTag.MANUAL })
	public void testConstructor() {
		// covered by test constructor in manual test
	}

	@Test
	@UnitTestMethod(target = LineWriter.class, name = "write", args = { ExperimentContext.class, int.class, ReportItem.class }, tags = { UnitTag.MANUAL })
	public void testWrite() {
		// covered by test write manual test
	}

	@Test
	@UnitTestMethod(target = LineWriter.class, name = "flush", args = {}, tags = { UnitTag.MANUAL })
	public void testFLush() {
		// covered by test flush in manual test
	}

	@Test
	@UnitTestMethod(target = LineWriter.class, name = "close", args = {}, tags = { UnitTag.MANUAL })
	public void testClose() {
		// covered by test close in manual test
	}

}