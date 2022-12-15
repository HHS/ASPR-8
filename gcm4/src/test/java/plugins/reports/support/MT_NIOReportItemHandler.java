package plugins.reports.support;

import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.ExperimentContext;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = NIOReportItemHandler.class)
public class MT_NIOReportItemHandler {

    private static enum ReportIds implements ReportId {
        ALPHA("ALPHA.txt"),
        BETA("BETA.txt"),
        ;

        private final String fileName;

        private ReportIds(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return this.fileName;
        }
    }

    private final Path dirPath;

    private MT_NIOReportItemHandler(Path dirPath) {
        this.dirPath = dirPath;
    }

    private Dimension getDimension() {
        final Dimension.Builder dimensionBuilder = Dimension.builder();//
        IntStream.range(0, 10).forEach((i) -> {
            dimensionBuilder.addLevel((context) -> {
                final ArrayList<String> result = new ArrayList<>();
                result.add("x_"+Integer.toString(i));
                return result;
                });//
            });
        dimensionBuilder.addMetaDatum("header");//
        return dimensionBuilder.build();
    }

    public static void main(String[] args) {

        assertNotNull(args);
        assertEquals(args.length, 1);
        Path dirPath = Paths.get(args[0]);
        new MT_NIOReportItemHandler(dirPath).execute();
    }

    private void execute() {
//        testBuilder();
//        testAccept();
//        testBuild();
//        testAddReport();
//        testSetDisplayExperimentColumnsInReports();
//        testAcceptWithProgressLog();
    }

    private NIOReportItemHandler getNIOReportItemHandler(){

        NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
        for (ReportIds reportIds : ReportIds.values()) {
            builder.addReport(reportIds, dirPath.resolve(reportIds.getFileName()));
        }
//        builder.setDisplayExperimentColumnsInReports(false);
        return builder.build();
    }

    @UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "build", args = {})
    private void testBuild() {
        NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
        ReportId reportId1 = new SimpleReportId("testReportId1");
        ReportId reportId2 = new SimpleReportId("testReportId2");
        final Path path1 = Path.of("example_path1");
        final Path path2 = Path.of("example_path2");

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

    @UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "addReport", args = {ReportId.class, Path.class})
    private void testAddReport() {
        NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
        ReportId reportId = new SimpleReportId("testReportId");
        final Path path1 = Path.of("example_path3");

        // null report id check
        ContractException pathContractException = assertThrows(ContractException.class, () -> builder.addReport(null, path1));
        assertEquals(pathContractException.getErrorType(), ReportError.NULL_REPORT_ID);

        // null report path check
        ContractException idContractException = assertThrows(ContractException.class, () -> builder.addReport(reportId, null));
        assertEquals(idContractException.getErrorType(), ReportError.NULL_REPORT_PATH);

    }

    @UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "setDisplayExperimentColumnsInReports", args = {boolean.class}, tags = {UnitTag.INCOMPLETE})
    private void testSetDisplayExperimentColumnsInReports() {
        NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
        final boolean displayExperimentColumnsInReports = true;

        assertNotNull(builder.setDisplayExperimentColumnsInReports(displayExperimentColumnsInReports));

        fail("experiment columns still appear when set to false");

    }

    @UnitTestMethod(name = "builder", args = {})
    private void testBuilder() {
        assertNotNull(getNIOReportItemHandler());
    }

    @UnitTestMethod(name = "accept", args = {ExperimentContext.class})
    private void testAccept() {
        /*
        Procedure:

        Select an existing directory that is empty

        Run this method

        Observe that each ENUM element has a corresponding empty file

        Edit each file and put something in it

        Run this method again

        Observe that each file is now empty

         */
        Experiment.builder()//
                .addExperimentContextConsumer(getNIOReportItemHandler())//
                .build()//
                .execute();
    }

    @UnitTestMethod(name = "accept", args = {ExperimentContext.class})
    private void testAcceptWithProgressLog() {
        /*
        Procedure:

        Copy files from src/test/resources/nioreportitemhandlermanualtesting into a local directory

        Observe that the alpha and beta contain lines that aren't scenarios in the progress log

        Run this method

        Observe tha the scenarios not included in the progress log have been removed from alpha and beta

        Observe that the executable terminated with an exit code of 0

         */
        Experiment.builder()//
                .addExperimentContextConsumer(getNIOReportItemHandler())//
                .setContinueFromProgressLog(true)//
                .setExperimentProgressLog(dirPath.resolve("ProgressLog.txt"))//
                .addDimension(getDimension())//
                .build()//
                .execute();
    }
}