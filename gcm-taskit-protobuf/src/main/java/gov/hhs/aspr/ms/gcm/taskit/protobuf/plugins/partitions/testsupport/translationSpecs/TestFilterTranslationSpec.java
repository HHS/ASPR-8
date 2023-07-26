package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.input.TestFilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.TestFilter;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestFilterInput} and
 * {@linkplain TestFilter}
 */
public class TestFilterTranslationSpec extends ProtobufTranslationSpec<TestFilterInput, TestFilter> {

    @Override
    protected TestFilter convertInputObject(TestFilterInput inputObject) {
        return new TestFilter(inputObject.getFilterId());
    }

    @Override
    protected TestFilterInput convertAppObject(TestFilter appObject) {
        return TestFilterInput.newBuilder().setFilterId(appObject.getFilterId()).build();
    }

    @Override
    public Class<TestFilter> getAppObjectClass() {
        return TestFilter.class;
    }

    @Override
    public Class<TestFilterInput> getInputObjectClass() {
        return TestFilterInput.class;
    }

}
