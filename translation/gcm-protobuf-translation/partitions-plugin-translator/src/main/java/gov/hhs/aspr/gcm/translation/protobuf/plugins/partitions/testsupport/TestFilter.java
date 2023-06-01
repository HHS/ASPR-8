package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.testsupport;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionsContext;
import plugins.partitions.support.filters.Filter;
import plugins.people.support.PersonId;

public class TestFilter extends Filter {
    private int filterId;

    public TestFilter(int filterId) {
        this.filterId = filterId;
    }

    public int getFilterId() {
        return this.filterId;
    }

    @Override
    public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
        return true;
    }

    @Override
    public void validate(PartitionsContext partitionsContext) {
        // do nothing
    }

    @Override
    public Set<FilterSensitivity<?>> getFilterSensitivities() {
        return new LinkedHashSet<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof TestFilter)) {
            return false;
        }

        TestFilter other = (TestFilter) obj;

        return Objects.equals(this.filterId, other.filterId);
    }

}
