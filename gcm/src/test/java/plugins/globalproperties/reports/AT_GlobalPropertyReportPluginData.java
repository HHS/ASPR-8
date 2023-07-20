package plugins.globalproperties.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_GlobalPropertyReportPluginData {

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "builder", args = {})
    public void testBuilder() {
        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();
        assertNotNull(builder);
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.Builder.class, name = "build", args = {})
    public void testBuild() {
        // the specific capabilities are covered elsewhere

        // precondition test: if the report period is not assigned
        ContractException contractException = assertThrows(ContractException.class, () -> //
        GlobalPropertyReportPluginData.builder()//
                .build());
        assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.Builder.class, name = "setReportLabel", args = {
            ReportLabel.class })
    public void testSetReportLabel() {

        for (int i = 0; i < 30; i++) {
            ReportLabel expectedReportLabel = new SimpleReportLabel(i);
            GlobalPropertyReportPluginData personPropertyReportPluginData = //
                    GlobalPropertyReportPluginData.builder()//
                            .setReportLabel(expectedReportLabel)//
                            .build();

            assertEquals(expectedReportLabel, personPropertyReportPluginData.getReportLabel());
        }

        // precondition: if the report label is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            GlobalPropertyReportPluginData.builder().setReportLabel(null);
        });
        assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.Builder.class, name = "setDefaultInclusion", args = {
            boolean.class })
    public void testSetDefaultInclusion() {
        ReportLabel reportLabel = new SimpleReportLabel("report label");

        // show the default value is true
        GlobalPropertyReportPluginData globalPropertyReportPluginData = //
                GlobalPropertyReportPluginData.builder()//
                        .setReportLabel(reportLabel)//
                        .build();
        assertEquals(true, globalPropertyReportPluginData.getDefaultInclusionPolicy());

        globalPropertyReportPluginData = //
                GlobalPropertyReportPluginData.builder()//
                        .setReportLabel(reportLabel)//
                        .setDefaultInclusion(true)//
                        .build();
        assertEquals(true, globalPropertyReportPluginData.getDefaultInclusionPolicy());

        globalPropertyReportPluginData = //
                GlobalPropertyReportPluginData.builder()// .
                        .setReportLabel(reportLabel)//
                        .setDefaultInclusion(false)//
                        .build();
        assertEquals(false, globalPropertyReportPluginData.getDefaultInclusionPolicy());

    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.Builder.class, name = "includeGlobalProperty", args = {
            GlobalPropertyId.class })
    public void testIncludeGlobalProperty() {
        ReportLabel reportLabel = new SimpleReportLabel("report label");

        // show the default is non-inclusion
        GlobalPropertyReportPluginData personPropertyReportPluginData = //
                GlobalPropertyReportPluginData.builder()//
                        .setReportLabel(reportLabel)//
                        .build();
        assertTrue(personPropertyReportPluginData.getIncludedProperties().isEmpty());

        // show that inclusion alone works
        Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();

        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder()//
                .setReportLabel(reportLabel);//

        for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
            builder.includeGlobalProperty(globalPropertyId);
        }

        personPropertyReportPluginData = builder.build();
        assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getIncludedProperties());

        // show that inclusion will override exclusion
        expectedGlobalPropertyIds.clear();

        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

        builder = GlobalPropertyReportPluginData.builder()//
                .setReportLabel(reportLabel);//

        for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
            builder.excludeGlobalProperty(globalPropertyId);
            builder.includeGlobalProperty(globalPropertyId);
        }

        personPropertyReportPluginData = builder.build();
        assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getIncludedProperties());

        // precondition: if the person property id is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            GlobalPropertyReportPluginData.builder().includeGlobalProperty(null);
        });
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.Builder.class, name = "excludeGlobalProperty", args = {
            GlobalPropertyId.class })
    public void testExcludePersonProperty() {
        ReportLabel reportLabel = new SimpleReportLabel("report label");

        // show the default is non-exclusion
        GlobalPropertyReportPluginData personPropertyReportPluginData = //
                GlobalPropertyReportPluginData.builder()//
                        .setReportLabel(reportLabel)//
                        .build();
        assertTrue(personPropertyReportPluginData.getExcludedProperties().isEmpty());

        // show that exclusion alone works
        Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();

        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder()//
                .setReportLabel(reportLabel);//

        for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
            builder.excludeGlobalProperty(globalPropertyId);
        }

        personPropertyReportPluginData = builder.build();
        assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getExcludedProperties());

        // show that exclusion will override inclusion
        expectedGlobalPropertyIds.clear();

        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

        builder = GlobalPropertyReportPluginData.builder()//
                .setReportLabel(reportLabel);//

        for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
            builder.includeGlobalProperty(globalPropertyId);
            builder.excludeGlobalProperty(globalPropertyId);
        }

        personPropertyReportPluginData = builder.build();
        assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getExcludedProperties());

        // precondition: if the person property id is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            GlobalPropertyReportPluginData.builder().excludeGlobalProperty(null);
        });
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "getReportLabel", args = {})
    public void testGetReportLabel() {
        for (int i = 0; i < 30; i++) {
            ReportLabel expectedReportLabel = new SimpleReportLabel(i);
            GlobalPropertyReportPluginData globalPropertyReportPluginData = //
                    GlobalPropertyReportPluginData.builder()//
                            .setReportLabel(expectedReportLabel)//
                            .build();

            assertEquals(expectedReportLabel, globalPropertyReportPluginData.getReportLabel());
        }

    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "getIncludedProperties", args = {})
    public void testGetIncludedProperties() {
        ReportLabel reportLabel = new SimpleReportLabel("report label");

        // show the default is non-inclusion
        GlobalPropertyReportPluginData personPropertyReportPluginData = //
                GlobalPropertyReportPluginData.builder()//

                        .setReportLabel(reportLabel)//
                        .build();
        assertTrue(personPropertyReportPluginData.getIncludedProperties().isEmpty());

        // show that inclusion alone works
        Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();

        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder()//
                .setReportLabel(reportLabel);//

        for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
            builder.includeGlobalProperty(globalPropertyId);
        }

        personPropertyReportPluginData = builder.build();
        assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getIncludedProperties());

        // show that inclusion will override exclusion
        expectedGlobalPropertyIds.clear();

        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

        builder = GlobalPropertyReportPluginData.builder()//
                .setReportLabel(reportLabel);//

        for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
            builder.excludeGlobalProperty(globalPropertyId);
            builder.includeGlobalProperty(globalPropertyId);
        }

        personPropertyReportPluginData = builder.build();
        assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getIncludedProperties());

    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "getExcludedProperties", args = {})
    public void testGetExcludedProperties() {
        ReportLabel reportLabel = new SimpleReportLabel("report label");

        // show the default is non-exclusion
        GlobalPropertyReportPluginData globalPropertyReportPluginData = //
                GlobalPropertyReportPluginData.builder()//
                        .setReportLabel(reportLabel)//
                        .build();
        assertTrue(globalPropertyReportPluginData.getExcludedProperties().isEmpty());

        // show that exclusion alone works
        Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();

        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);

        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder()//
                .setReportLabel(reportLabel);//

        for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
            builder.excludeGlobalProperty(globalPropertyId);
        }

        globalPropertyReportPluginData = builder.build();
        assertEquals(expectedGlobalPropertyIds, globalPropertyReportPluginData.getExcludedProperties());

        // show that exclusion will override inclusion
        expectedGlobalPropertyIds.clear();

        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
        expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);

        builder = GlobalPropertyReportPluginData.builder()//
                .setReportLabel(reportLabel);//

        for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
            builder.includeGlobalProperty(globalPropertyId);
            builder.excludeGlobalProperty(globalPropertyId);
        }

        globalPropertyReportPluginData = builder.build();
        assertEquals(expectedGlobalPropertyIds, globalPropertyReportPluginData.getExcludedProperties());

    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "getDefaultInclusionPolicy", args = {})
    public void testGetDefaultInclusionPolicy() {
        ReportLabel reportLabel = new SimpleReportLabel("report label");

        // show the default value is true
        GlobalPropertyReportPluginData globalPropertyReportPluginData = //
                GlobalPropertyReportPluginData.builder()//
                        .setReportLabel(reportLabel)//
                        .build();
        assertEquals(true, globalPropertyReportPluginData.getDefaultInclusionPolicy());

        globalPropertyReportPluginData = //
                GlobalPropertyReportPluginData.builder()//
                        .setReportLabel(reportLabel)//
                        .setDefaultInclusion(true)//
                        .build();
        assertEquals(true, globalPropertyReportPluginData.getDefaultInclusionPolicy());

        globalPropertyReportPluginData = //
                GlobalPropertyReportPluginData.builder()//
                        .setReportLabel(reportLabel)//
                        .setDefaultInclusion(false)//
                        .build();
        assertEquals(false, globalPropertyReportPluginData.getDefaultInclusionPolicy());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "getCloneBuilder", args = {})
    public void testGetCloneBuilder() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
        for (int i = 0; i < 10; i++) {

            // build a GlobalPropertyReportPluginData from random inputs
            ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

            GlobalPropertyReportPluginData.Builder builder = //
                    GlobalPropertyReportPluginData.builder()//
                            .setReportLabel(reportLabel);

            for (int j = 0; j < 10; j++) {
                TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId
                        .getRandomGlobalPropertyId(randomGenerator);
                if (randomGenerator.nextBoolean()) {
                    builder.includeGlobalProperty(testGlobalPropertyId);
                } else {
                    builder.excludeGlobalProperty(testGlobalPropertyId);
                }
            }

            builder.setDefaultInclusion(randomGenerator.nextBoolean()).build();

            GlobalPropertyReportPluginData globalPropertyReportPluginData = builder.build();

            // create the clone builder and have it build
            GlobalPropertyReportPluginData cloneGlobalPropertyReportPluginData = globalPropertyReportPluginData
                    .getCloneBuilder().build();

            // the result should equal the original if the clone builder was
            // initialized with the correct state
            assertEquals(globalPropertyReportPluginData, cloneGlobalPropertyReportPluginData);

        }
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "equals", args = { Object.class })
    public void testEquals() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7376599865331451959L);
        for (int i = 0; i < 10; i++) {
            // build a GlobalPropertyReportPluginData from the same random
            // inputs
            GlobalPropertyReportPluginData.Builder builder1 = GlobalPropertyReportPluginData.builder();
            GlobalPropertyReportPluginData.Builder builder2 = GlobalPropertyReportPluginData.builder();

            ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
            builder1.setReportLabel(reportLabel);
            builder2.setReportLabel(reportLabel);

            for (int j = 0; j < 10; j++) {
                TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId
                        .getRandomGlobalPropertyId(randomGenerator);
                if (randomGenerator.nextBoolean()) {
                    builder1.includeGlobalProperty(testGlobalPropertyId);
                    builder2.includeGlobalProperty(testGlobalPropertyId);
                } else {
                    builder1.excludeGlobalProperty(testGlobalPropertyId);
                    builder2.excludeGlobalProperty(testGlobalPropertyId);
                }
            }

            boolean defaultInclusion = randomGenerator.nextBoolean();
            builder1.setDefaultInclusion(defaultInclusion).build();
            builder2.setDefaultInclusion(defaultInclusion).build();

            GlobalPropertyReportPluginData globalPropertyReportPluginData1 = builder1.build();
            GlobalPropertyReportPluginData globalPropertyReportPluginData2 = builder2.build();

            assertEquals(globalPropertyReportPluginData1, globalPropertyReportPluginData2);

            // show that plugin datas with different inputs are not equal

            // change the default inclusion
            globalPropertyReportPluginData2 = //
                    globalPropertyReportPluginData1.getCloneBuilder()//
                            .setDefaultInclusion(!defaultInclusion)//
                            .build();
            assertNotEquals(globalPropertyReportPluginData2, globalPropertyReportPluginData1);

            // change the report label
            reportLabel = new SimpleReportLabel(1000);
            globalPropertyReportPluginData2 = //
                    globalPropertyReportPluginData1.getCloneBuilder()//
                            .setReportLabel(reportLabel)//
                            .build();
            assertNotEquals(globalPropertyReportPluginData2, globalPropertyReportPluginData1);

            // change an included property id
            if (!globalPropertyReportPluginData1.getIncludedProperties().isEmpty()) {
                GlobalPropertyId globalPropertyId = globalPropertyReportPluginData1.getIncludedProperties().iterator()
                        .next();
                globalPropertyReportPluginData2 = //
                        globalPropertyReportPluginData1.getCloneBuilder()//
                                .excludeGlobalProperty(globalPropertyId)//
                                .build();
                assertNotEquals(globalPropertyReportPluginData2, globalPropertyReportPluginData1);
            }
            // change an excluded property id
            if (!globalPropertyReportPluginData1.getExcludedProperties().isEmpty()) {
                GlobalPropertyId globalPropertyId = globalPropertyReportPluginData1.getExcludedProperties().iterator()
                        .next();
                globalPropertyReportPluginData2 = //
                        globalPropertyReportPluginData1.getCloneBuilder()//
                                .includeGlobalProperty(globalPropertyId)//
                                .build();
                assertNotEquals(globalPropertyReportPluginData2, globalPropertyReportPluginData1);
            }

        }

    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "hashCode", args = {})
    public void testHashCode() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8483458328908100435L);

        Set<Integer> observedHashCodes = new LinkedHashSet<>();
        for (int i = 0; i < 50; i++) {
            // build a GlobalPropertyReportPluginData from the same random
            // inputs
            GlobalPropertyReportPluginData.Builder builder1 = GlobalPropertyReportPluginData.builder();
            GlobalPropertyReportPluginData.Builder builder2 = GlobalPropertyReportPluginData.builder();

            ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
            builder1.setReportLabel(reportLabel);
            builder2.setReportLabel(reportLabel);

            for (int j = 0; j < 10; j++) {
                TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId
                        .getRandomGlobalPropertyId(randomGenerator);
                if (randomGenerator.nextBoolean()) {
                    builder1.includeGlobalProperty(testGlobalPropertyId);
                    builder2.includeGlobalProperty(testGlobalPropertyId);
                } else {
                    builder1.excludeGlobalProperty(testGlobalPropertyId);
                    builder2.excludeGlobalProperty(testGlobalPropertyId);
                }
            }

            boolean defaultInclusion = randomGenerator.nextBoolean();
            builder1.setDefaultInclusion(defaultInclusion).build();
            builder2.setDefaultInclusion(defaultInclusion).build();

            GlobalPropertyReportPluginData globalPropertyReportPluginData1 = builder1.build();
            GlobalPropertyReportPluginData globalPropertyReportPluginData2 = builder2.build();

            // show that the hash code is stable
            int hashCode = globalPropertyReportPluginData1.hashCode();
            assertEquals(hashCode, globalPropertyReportPluginData1.hashCode());
            assertEquals(hashCode, globalPropertyReportPluginData1.hashCode());
            assertEquals(hashCode, globalPropertyReportPluginData1.hashCode());
            assertEquals(hashCode, globalPropertyReportPluginData1.hashCode());

            // show that equal objects have equal hash codes
            assertEquals(globalPropertyReportPluginData1.hashCode(), globalPropertyReportPluginData2.hashCode());

            // collect the hashcode
            observedHashCodes.add(globalPropertyReportPluginData1.hashCode());
        }

        /*
         * The hash codes should be dispersed -- we only show that they are unique
         * values -- this is dependent on the random seed
         */
        assertEquals(50, observedHashCodes.size());

    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "toString", args = {})
    public void testToString() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
        for (int i = 0; i < 10; i++) {

            // build a GlobalPropertyReportPluginData from random inputs
            ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

            GlobalPropertyReportPluginData.Builder builder = //
                    GlobalPropertyReportPluginData.builder()//
                            .setReportLabel(reportLabel);

            StringBuilder sb = new StringBuilder();

            sb.append("GlobalPropertyReportPluginData [data=").append("Data [reportLabel=").append(reportLabel)
                    .append(", includedProperties=");

            Set<GlobalPropertyId> includedProperties = new LinkedHashSet<>();
            Set<GlobalPropertyId> excludedProperties = new LinkedHashSet<>();

            for (int j = 0; j < 10; j++) {
                TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId
                        .getRandomGlobalPropertyId(randomGenerator);
                if (randomGenerator.nextBoolean()) {
                    builder.includeGlobalProperty(testGlobalPropertyId);
                    includedProperties.add(testGlobalPropertyId);
                    excludedProperties.remove(testGlobalPropertyId);
                } else {
                    builder.excludeGlobalProperty(testGlobalPropertyId);
                    excludedProperties.add(testGlobalPropertyId);
                    includedProperties.remove(testGlobalPropertyId);
                }
            }

            boolean defaultInclusionPolicy = randomGenerator.nextBoolean();
            builder.setDefaultInclusion(defaultInclusionPolicy).build();

            sb.append(includedProperties).append(", excludedProperties=").append(excludedProperties)
                    .append(", defaultInclusionPolicy=").append(defaultInclusionPolicy).append(", locked=").append(true)
                    .append("]").append("]");

            GlobalPropertyReportPluginData globalPropertyReportPluginData = builder.build();

            assertEquals(sb.toString(), globalPropertyReportPluginData.toString());

        }
    }
}
