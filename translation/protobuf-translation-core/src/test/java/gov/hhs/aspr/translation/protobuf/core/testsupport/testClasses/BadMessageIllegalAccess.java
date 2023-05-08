package gov.hhs.aspr.translation.protobuf.core.testsupport.testClasses;

public class BadMessageIllegalAccess {
    private BadMessageIllegalAccess() {

    }
    
    private static class Builder {

    }

    protected static Builder newBuilder() {
        return new Builder();
    }
}
