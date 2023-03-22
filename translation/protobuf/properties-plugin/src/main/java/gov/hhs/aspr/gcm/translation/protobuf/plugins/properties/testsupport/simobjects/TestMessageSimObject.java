package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((layer1 == null) ? 0 : layer1.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestMessageSimObject other = (TestMessageSimObject) obj;
        if (layer1 == null) {
            if (other.layer1 != null)
                return false;
        } else if (!layer1.equals(other.layer1))
            return false;
        return true;
    }

    
}
