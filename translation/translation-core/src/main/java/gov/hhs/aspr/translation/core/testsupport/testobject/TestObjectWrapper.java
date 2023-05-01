package gov.hhs.aspr.translation.core.testsupport.testobject;

import java.util.Objects;

public class TestObjectWrapper {
    private Object wrappedObject;

    public Object getWrappedObject() {
        return wrappedObject;
    }

    public void setWrappedObject(Object wrappedObject) {
        if(wrappedObject == this || wrappedObject instanceof TestObjectWrapper) {
            throw new RuntimeException("Cant set the wrapped object to an instance of itself");
        }
        this.wrappedObject = wrappedObject;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(wrappedObject);
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
        TestObjectWrapper other = (TestObjectWrapper) obj;
        return Objects.equals(wrappedObject, other.wrappedObject);
    }

}
