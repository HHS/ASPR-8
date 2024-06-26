package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;

public enum TestGroupPropertyId implements GroupPropertyId {

    GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK(TestGroupTypeId.GROUP_TYPE_1, //
            PropertyDefinition.builder()//
                    .setType(Boolean.class)//
                    .setDefaultValue(false)//
                    .setPropertyValueMutability(true)//
                    .build()), //
    GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK(TestGroupTypeId.GROUP_TYPE_1, //
            PropertyDefinition.builder()//
                    .setType(Integer.class)//
                    .setDefaultValue(0)//
                    .setPropertyValueMutability(true)//
                    .build() //
    ), //
    GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK(TestGroupTypeId.GROUP_TYPE_1, //
            PropertyDefinition.builder()//
                    .setType(Double.class)//
                    .setDefaultValue(0.0)//
                    .setPropertyValueMutability(true)//
                    .build() //
    ), //
    GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK(TestGroupTypeId.GROUP_TYPE_2, //
            PropertyDefinition.builder()//
                    .setType(Boolean.class)//
                    .setDefaultValue(false)//
                    .setPropertyValueMutability(true)//
                    .build() //
    ), //
    GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK(TestGroupTypeId.GROUP_TYPE_2, //
            PropertyDefinition.builder()//
                    .setType(Integer.class)//
                    .setDefaultValue(0)//
                    .setPropertyValueMutability(true)//
                    .build() //
    ), //
    GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK(TestGroupTypeId.GROUP_TYPE_2, //
            PropertyDefinition.builder()//
                    .setType(Double.class)//
                    .setDefaultValue(0.0)//
                    .setPropertyValueMutability(true)//
                    .build() //
    ), //
    GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK(TestGroupTypeId.GROUP_TYPE_3, //
            PropertyDefinition.builder()//
                    .setType(Boolean.class)//
                    .setDefaultValue(false)//
                    .setPropertyValueMutability(false)//
                    .build() //
    ), //
    GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK(TestGroupTypeId.GROUP_TYPE_3, //
            PropertyDefinition.builder()//
                    .setType(Integer.class)//
                    .setDefaultValue(0)//
                    .setPropertyValueMutability(false)//
                    .build() //
    ), //
    GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK(TestGroupTypeId.GROUP_TYPE_3, //
            PropertyDefinition.builder()//
                    .setType(Double.class)//
                    .setDefaultValue(0.0)//
                    .setPropertyValueMutability(false)//
                    .build() //
    );//

    private final PropertyDefinition propertyDefinition;
    private final TestGroupTypeId testGroupTypeId;

    /**
     * Returns the property definition associated with this member
     */
    public PropertyDefinition getPropertyDefinition() {
        return propertyDefinition;
    }

    private TestGroupPropertyId(TestGroupTypeId testGroupTypeId, PropertyDefinition propertyDefinition) {
        this.testGroupTypeId = testGroupTypeId;
        this.propertyDefinition = propertyDefinition;
    }

    /**
     * Returns the TestGroupTypeId that should be the type associated with the
     * property
     */
    public TestGroupTypeId getTestGroupTypeId() {
        return testGroupTypeId;
    }

    /**
     * Returns the TestGroupPropertyId associated with the given TestGroupTypeId
     * Preconditions: The TestGroupTypeId should not be null
     */
    public static Set<TestGroupPropertyId> getTestGroupPropertyIds(TestGroupTypeId testGroupTypeId) {
        Set<TestGroupPropertyId> result = new LinkedHashSet<>();
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            if (testGroupPropertyId.testGroupTypeId == testGroupTypeId) {
                result.add(testGroupPropertyId);
            }
        }
        return result;
    }

    /**
     * Returns a unique GroupPropertyId instance that is not a member of this
     * enumeration
     */
    public static GroupPropertyId getUnknownGroupPropertyId() {
        return new GroupPropertyId() {
        };
    }

    /**
     * Returns a randomly selected value that is compatible with this member's
     * associated property definition.
     */
    public Object getRandomPropertyValue(final RandomGenerator randomGenerator) {
        switch (this) {
        case GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK:
            return randomGenerator.nextBoolean();
        case GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK:
            return randomGenerator.nextInt();
        case GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK:
            return randomGenerator.nextDouble();
        case GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK:
            return randomGenerator.nextBoolean();
        case GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK:
            return randomGenerator.nextInt();
        case GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK:
            return randomGenerator.nextDouble();
        case GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK:
            return randomGenerator.nextBoolean();
        case GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK:
            return randomGenerator.nextInt();
        case GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK:
            return randomGenerator.nextDouble();
        default:
            throw new RuntimeException("unhandled case: " + this);

        }
    }

    /*
     * Returns the next GroupPropertyId with wrap around
     */
    public TestGroupPropertyId next() {
        int index = this.ordinal();
        index++;
        index %= TestGroupPropertyId.values().length;
        return TestGroupPropertyId.values()[index];
    }

    /**
     * Returns a randomly selected TestGroupPropertyId.
     */
    public static TestGroupPropertyId getRandomTestGroupPropertyId(final RandomGenerator randomGenerator) {
        int index = randomGenerator.nextInt(TestGroupPropertyId.values().length);
        return TestGroupPropertyId.values()[index];
    }

    public static List<TestGroupPropertyId> getShuffledTestGroupPropertyIds(TestGroupTypeId testGroupTypeId,
            RandomGenerator randomGenerator) {
        List<TestGroupPropertyId> result = new ArrayList<>(getTestGroupPropertyIds(testGroupTypeId));

        Collections.shuffle(result, new Random(randomGenerator.nextLong()));
        return result;
    }
}
