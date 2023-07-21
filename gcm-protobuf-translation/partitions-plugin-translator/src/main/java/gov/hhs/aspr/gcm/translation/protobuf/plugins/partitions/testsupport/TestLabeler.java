package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.testsupport;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import nucleus.Event;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.partitions.support.PartitionsContext;
import plugins.people.support.PersonId;

public class TestLabeler implements Labeler {
    private String id;

    public TestLabeler(String id) {
        this.id = id;
    }

    @Override
    public Set<LabelerSensitivity<?>> getLabelerSensitivities() {
        return new LinkedHashSet<>();
    }

    @Override
    public Object getCurrentLabel(PartitionsContext partitionsContext, PersonId personId) {
        return "Current Label";
    }

    @Override
    public Object getPastLabel(PartitionsContext partitionsContext, Event event) {
        return "Past Label";
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof TestLabeler)) {
            return false;
        }

        TestLabeler other = (TestLabeler) obj;

        return Objects.equals(this.id, other.id);
    }
}
