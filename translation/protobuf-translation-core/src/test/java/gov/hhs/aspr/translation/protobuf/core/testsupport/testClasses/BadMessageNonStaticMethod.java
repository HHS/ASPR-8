package gov.hhs.aspr.translation.protobuf.core.testsupport.testClasses;

/* 
 * Class that has newBuilder method but it is not static
 */
public class BadMessageNonStaticMethod {
    private static class Builder {

    }

    public Builder newBuilder() {
        return new Builder();
    }
}
