package plugins.reports.support;

import nucleus.Experiment;
import nucleus.ExperimentContext;
import org.junit.jupiter.api.Test;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = NIOReportItemHandler.class)
public class MT_NIOReportItemHandler {

    private static enum ReportIds implements ReportId {
        ALPHA("ALPHA.text"),
        BETA("BETA.text"),
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

    public static void main(String[] args) {

        assertNotNull(args);
        assertEquals(args.length, 1);
        Path dirPath = Paths.get(args[0]);
        new MT_NIOReportItemHandler(dirPath).execute();
    }

    private void execute() {
        testBuilder();
        testAccept();
    }

    private NIOReportItemHandler getNIOReportItemHandler(){

        NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
        for (ReportIds reportIds : ReportIds.values()) {
            builder.addReport(reportIds, dirPath.resolve(reportIds.getFileName()));
        }
        return builder.build();
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
}