package gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject;

import java.util.Objects;

public class TestComplexAppObject {
    private String testString;
    private double startTime;
    private int numEntities;

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public int getNumEntities() {
        return numEntities;
    }

    public void setNumEntities(int numEntities) {
        this.numEntities = numEntities;
    }

    @Override
    public int hashCode() {
        return Objects.hash(testString, startTime, numEntities);
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
        TestComplexAppObject other = (TestComplexAppObject) obj;
        return Objects.equals(testString, other.testString)
                && Double.doubleToLongBits(startTime) == Double.doubleToLongBits(other.startTime)
                && numEntities == other.numEntities;
    }

}
