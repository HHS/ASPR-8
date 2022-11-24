package plugins.personproperties.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportId;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = PersonPropertyInteractionReport.class)
public class AT_PersonPropertyInteractionReport {

    @Test
    @UnitTestConstructor(args = { ReportId.class, ReportPeriod.class, PersonPropertyId[].class })
    public void testConstructor() {
        ReportId reportId = new SimpleReportId(1000);
        ReportPeriod reportPeriod = ReportPeriod.DAILY;
        PersonPropertyId[] personPropertyIds = { TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK,
                TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK };

        PersonPropertyInteractionReport report = new PersonPropertyInteractionReport(reportId, reportPeriod,
                personPropertyIds);

        assertNotNull(report);

        // precondition: report id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> new PersonPropertyInteractionReport(null, reportPeriod, personPropertyIds));
        assertEquals(ReportError.NULL_REPORT_ID, contractException.getErrorType());

        // precondition: report period is null
        contractException = assertThrows(ContractException.class,
                () -> new PersonPropertyInteractionReport(reportId, null, personPropertyIds));
        assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(name = "init", args = { ActorContext.class }, tags = { UnitTag.INCOMPLETE })
    public void testInit() {
        // TBD
    }
}
