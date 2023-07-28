package gov.hhs.aspr.ms.taskit.core.testsupport.testobject.input;

import java.util.Objects;

import gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject.input.TestComplexInputObject;

public class TestInputObject {
    private int integer;
    private boolean bool;
    private String string;
    private TestComplexInputObject testComplexInputObject;

    public int getInteger() {
        return integer;
    }

    public void setInteger(int integer) {
        this.integer = integer;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public TestComplexInputObject getTestComplexInputObject() {
        return testComplexInputObject;
    }

    public void setTestComplexInputObject(TestComplexInputObject testComplexInputObject) {
        this.testComplexInputObject = testComplexInputObject;
    }

    @Override
    public int hashCode() {
        return Objects.hash(integer, bool, string, testComplexInputObject);
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
        TestInputObject other = (TestInputObject) obj;
        return integer == other.integer && bool == other.bool && Objects.equals(string, other.string)
                && Objects.equals(testComplexInputObject, other.testComplexInputObject);
    }

}
