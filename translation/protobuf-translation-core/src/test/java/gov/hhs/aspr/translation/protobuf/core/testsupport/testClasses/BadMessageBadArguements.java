package gov.hhs.aspr.translation.protobuf.core.testsupport.testClasses;

public class BadMessageBadArguements {

    private BadMessageBadArguements() {

    }
    
    private static class Builder {

    }

    public static Builder newBuilder(int badArgument) {
        return new Builder();
    }
}
