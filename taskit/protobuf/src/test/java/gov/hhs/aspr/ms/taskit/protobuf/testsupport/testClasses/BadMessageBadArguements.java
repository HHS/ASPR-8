package gov.hhs.aspr.ms.taskit.protobuf.testsupport.testClasses;

public class BadMessageBadArguements {

    private BadMessageBadArguements() {

    }

    private static class Builder {

    }

    public static Builder newBuilder(int badArgument) {
        return new Builder();
    }
}
