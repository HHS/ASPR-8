package gov.hhs.aspr.ms.taskit.protobuf.testsupport.testClasses;

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
