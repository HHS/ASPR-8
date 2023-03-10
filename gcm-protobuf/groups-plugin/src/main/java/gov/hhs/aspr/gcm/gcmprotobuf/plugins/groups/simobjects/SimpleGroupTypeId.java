package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.simobjects;

import plugins.groups.support.GroupTypeId;

public class SimpleGroupTypeId implements GroupTypeId {
    private Object value;

    public SimpleGroupTypeId(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }
}
