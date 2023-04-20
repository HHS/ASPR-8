package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects;

import java.util.Objects;

public class TestMessageSimObject {
    private Layer1SimObject layer1;

    public void setLayer1(Layer1SimObject layer1) {
        this.layer1 = layer1;
    }

    public Layer1SimObject getLayer1() {
        return this.layer1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(layer1);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestMessageSimObject other = (TestMessageSimObject) obj;
        return Objects.equals(layer1, other.layer1);
    }

}
