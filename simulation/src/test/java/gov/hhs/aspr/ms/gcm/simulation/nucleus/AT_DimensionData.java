package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_DimensionData {

    private static class TestDimensionData extends DimensionData {

        private static class Data {
            private final List<String> levelNames = new ArrayList<>();

            private boolean locked;

            private Data() {
            }

            private Data(Data data) {
                levelNames.addAll(data.levelNames);
                locked = data.locked;
            }

            @Override
            public int hashCode() {
                return Objects.hash(levelNames);
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }

                if (!(obj instanceof Data)) {
                    return false;
                }

                Data other = (Data) obj;

                return Objects.equals(levelNames, other.levelNames);
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("Data [");
                builder.append("levelNames=");
                builder.append(levelNames);
                builder.append("]");
                return builder.toString();
            }

        }

        private static class Builder {
            private Data data;

            private Builder(Data data) {
                this.data = data;
            }

            public Builder addLevel(String levelName) {
                ensureDataMutability();
                this.data.levelNames.add(levelName);

                return this;
            }

            public TestDimensionData build() {
                if (!data.locked) {
                    validateData();
                }
                ensureImmutability();
                return new TestDimensionData(data);
            }

            private void ensureDataMutability() {
                if (data.locked) {
                    data = new Data(data);
                    data.locked = false;
                }
            }

            private void ensureImmutability() {
                if (!data.locked) {
                    data.locked = true;
                }
            }

            private void validateData() {

            }

        }

        public static Builder builder() {
            return new Builder(new Data());
        }

        private final Data data;

        protected TestDimensionData(Data data) {
            super(data.levelNames);
            this.data = data;
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TestDimensionData)) {
                return false;
            }
            TestDimensionData other = (TestDimensionData) obj;
            return Objects.equals(data, other.data);
        }

        @Override
        public String toString() {
            return "TestDimensionData [data=" + data + "]";
        }

        public Builder toBuilder() {
            return new Builder(data);
        }
    }

    @Test
    @UnitTestMethod(target = DimensionData.class, name = "getLevelCount", args = {})
    public void testGetLevelCount() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3226863657242118228L);

        for (int i = 0; i < 10; i++) {
            int expectedLevelCount = randomGenerator.nextInt(100);
            TestDimensionData.Builder builder = TestDimensionData.builder();

            for (int j = 0; j < expectedLevelCount; j++) {
                builder.addLevel(Integer.toString(j));
            }

            TestDimensionData dimensionData = builder.build();

            assertEquals(expectedLevelCount, dimensionData.getLevelCount());
        }
    }

    @Test
    @UnitTestMethod(target = DimensionData.class, name = "getLevelName", args = { int.class })
    public void testGetLevelName() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(612509749724622534L);

        for (int i = 0; i < 10; i++) {
            int levelCount = randomGenerator.nextInt(100);
            TestDimensionData.Builder builder = TestDimensionData.builder();

            for (int j = 0; j < levelCount; j++) {
                builder.addLevel(Integer.toString(j));
            }

            for (int j = 0; j < levelCount; j++) {
                builder.addLevel(Integer.toString(j + 100));
            }

            for (int j = 0; j < levelCount; j++) {
                builder.addLevel(Integer.toString(j + 1000));
            }

            TestDimensionData dimensionData = builder.build();

            for (int j = 0; j < levelCount; j++) {
                assertNotNull(dimensionData.getLevelName(j));
                assertNotNull(dimensionData.getLevelName(levelCount + j));
                assertNotNull(dimensionData.getLevelName(levelCount * 2 + j));

                assertEquals(Integer.toString(j), dimensionData.getLevelName(j));
                assertEquals(Integer.toString(j + 100), dimensionData.getLevelName(levelCount + j));
                assertEquals(Integer.toString(j + 1000), dimensionData.getLevelName(levelCount * 2 + j));
            }
        }

        // preconditions
        // level is < 0
        ContractException contractException = assertThrows(ContractException.class, () -> {
            TestDimensionData dimensionData = TestDimensionData.builder().build();
            dimensionData.getLevelName(-1);
        });

        assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());

        // level is > level count
        contractException = assertThrows(ContractException.class, () -> {
            TestDimensionData dimensionData = TestDimensionData.builder().build();
            dimensionData.getLevelName(1);
        });

        assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = DimensionData.class, name = "getLevelNames", args = {})
    public void testGetLevelNames() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(612509749724622534L);

        for (int i = 0; i < 10; i++) {
            int levelCount = randomGenerator.nextInt(100);
            List<String> expectedLevelNames = new ArrayList<>();
            TestDimensionData.Builder builder = TestDimensionData.builder();

            for (int j = 0; j < levelCount; j++) {
                builder.addLevel(Integer.toString(j));
                expectedLevelNames.add(Integer.toString(j));
            }

            for (int j = 0; j < levelCount; j++) {
                builder.addLevel(Integer.toString(j + 100));
                expectedLevelNames.add(Integer.toString(j + 100));
            }

            for (int j = 0; j < levelCount; j++) {
                builder.addLevel(Integer.toString(j + 1000));
                expectedLevelNames.add(Integer.toString(j + 1000));
            }

            TestDimensionData dimensionData = builder.build();
            List<String> actualLevelNames = dimensionData.getLevelNames();

            assertEquals(expectedLevelNames.size(), actualLevelNames.size());
            assertEquals(expectedLevelNames, actualLevelNames);

            for (int j = 0; j < expectedLevelNames.size(); j++) {
                assertEquals(expectedLevelNames.get(j), actualLevelNames.get(j));
            }
        }
    }

    @Test
    @UnitTestMethod(target = DimensionData.class, name = "getLevel", args = { String.class })
    public void testGetLevel() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(612509749724622534L);

        for (int i = 0; i < 10; i++) {
            int levelCount = randomGenerator.nextInt(100);
            TestDimensionData.Builder builder = TestDimensionData.builder();

            for (int j = 0; j < levelCount; j++) {
                builder.addLevel(Integer.toString(j));
            }

            for (int j = 0; j < levelCount; j++) {
                builder.addLevel(Integer.toString(j + 100));
            }

            for (int j = 0; j < levelCount; j++) {

                builder.addLevel(Integer.toString(j + 1000));
            }

            TestDimensionData dimensionData = builder.build();

            for (int j = 0; j < levelCount; j++) {
                assertEquals(j, dimensionData.getLevel(Integer.toString(j)));
                assertEquals(levelCount + j, dimensionData.getLevel(Integer.toString(j + 100)));
                assertEquals(levelCount * 2 + j, dimensionData.getLevel(Integer.toString(j + 1000)));
            }
        }

        // preconditions
        // level name is unknown
        ContractException contractException = assertThrows(ContractException.class, () -> {
            TestDimensionData dimensionData = TestDimensionData.builder().build();
            dimensionData.getLevel("BAD_LEVEL_NAME");
        });

        assertEquals(NucleusError.UNKNOWN_DIMENSION_LEVEL_NAME, contractException.getErrorType());
    }
}
