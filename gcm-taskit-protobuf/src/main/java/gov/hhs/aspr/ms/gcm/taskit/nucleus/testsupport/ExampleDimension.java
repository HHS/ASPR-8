package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import nucleus.Dimension;
import nucleus.DimensionContext;

public class ExampleDimension implements Dimension {
    String levelName;

    public ExampleDimension(String levelName) {
        this.levelName = levelName;
    }

    public String getLevelName() {
        return this.levelName;
    }

    @Override
    public List<String> getExperimentMetaData() {
        return new ArrayList<>();
    }

    @Override
    public int levelCount() {
        return 5;
    }

    @Override
    public List<String> executeLevel(DimensionContext dimensionContext, int level) {
        return new ArrayList<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(levelName);
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
        ExampleDimension other = (ExampleDimension) obj;
        return Objects.equals(levelName, other.levelName);
    }

}
