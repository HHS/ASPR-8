package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.simobjects;

import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;

public class Group {
    private GroupId groupId;
    private GroupTypeId groupTypeId;

    public GroupId getGroupId() {
        return groupId;
    }

    public void setGroupId(GroupId groupId) {
        this.groupId = groupId;
    }

    public GroupTypeId getGroupTypeId() {
        return groupTypeId;
    }

    public void setGroupTypeId(GroupTypeId groupTypeId) {
        this.groupTypeId = groupTypeId;
    }
}
