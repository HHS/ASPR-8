package plugins.reports.support;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import nucleus.ExperimentContext;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public class AT_LineWriter {

	@Test
	@UnitTestConstructor(target = LineWriter.class, args = { ExperimentContext.class, Path.class, boolean.class }, tags = { UnitTag.MANUAL , UnitTag.CLASS_PROXY})
	public void testConstructor() {
		// covered by manual test of NIOReportItemHandler
	}

	@Test
	@UnitTestMethod(target = LineWriter.class, name = "write", args = { ExperimentContext.class, int.class, ReportItem.class }, tags = { UnitTag.MANUAL , UnitTag.CLASS_PROXY})
	public void testWrite() {
		// covered by manual test of NIOReportItemHandler
	}

	@Test
	@UnitTestMethod(target = LineWriter.class, name = "flush", args = {}, tags = { UnitTag.MANUAL , UnitTag.CLASS_PROXY})
	public void testFLush() {
		// covered by manual test of NIOReportItemHandler
	}

	@Test
	@UnitTestMethod(target = LineWriter.class, name = "close", args = {}, tags = { UnitTag.MANUAL , UnitTag.CLASS_PROXY})
	public void testClose() {
		// covered by manual test of NIOReportItemHandler
	}

}