package gov.hhs.aspr.ms.taskit.protobuf.testsupport.testClasses;

public class BadMessageIllegalAccess {
    private BadMessageIllegalAccess() {

    }

    private static class Builder {

    }

    protected static Builder newBuilder() {
        return new Builder();
    }
}
