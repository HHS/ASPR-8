package plugins.reports.support;

import org.junit.jupiter.api.Test;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = NIOReportItemHandler.class)
public class AT_NIOReportItemHandler {

    @Test
    @UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "build", args = {})
    public void testBuild() {
        NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
        ReportId reportId1 = new SimpleReportId("testReportId1");
        ReportId reportId2 = new SimpleReportId("testReportId2");
        final Path path1 = Path.of("C:\\Users\\varnerbf\\Documents\\TestReports\\1");
        final Path path2 = Path.of("C:\\Users\\varnerbf\\Documents\\TestReports\\2");

        // show that a path collision error happens when 2 reports have the same path
        builder.addReport(reportId1, path1);
        builder.addReport(reportId2, path1);

        ContractException contractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(contractException.getErrorType(), ReportError.PATH_COLLISION);

        // show that what is built is not null
        builder.addReport(reportId1, path1);
        builder.addReport(reportId2, path2);

        assertNotNull(builder.build());

    }

    @Test
    @UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "addReport", args = {ReportId.class, Path.class})
    public void testAddReport() {
        NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
        ReportId reportId = new SimpleReportId("testReportId");
        final Path path1 = Path.of("C:\\Users\\varnerbf\\Documents\\TestReports\\1");

        // null report id check
        ContractException pathContractException = assertThrows(ContractException.class, () -> builder.addReport(null, path1));
        assertEquals(pathContractException.getErrorType(), ReportError.NULL_REPORT_ID);

        // null report path check
        ContractException idContractException = assertThrows(ContractException.class, () -> builder.addReport(reportId, null));
        assertEquals(idContractException.getErrorType(), ReportError.NULL_REPORT_PATH);

    }

    @Test
    @UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "setDisplayExperimentColumnsInReports", args = {boolean.class})
    public void testSetDisplayExperimentColumnsInReports() {
        NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
        final boolean displayExperimentColumnsInReports = true;

        assertNotNull(builder.setDisplayExperimentColumnsInReports(displayExperimentColumnsInReports));

    }

}