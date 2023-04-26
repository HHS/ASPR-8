package gov.hhs.aspr.translation.core.testsupport.testobject;

import java.util.Objects;

import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexAppObject;

public class TestAppObject {
    private int integer;
    private boolean bool;
    private String string;
    private TestComplexAppObject testComplexAppObject;

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
        String temp = string;
        if(!string.endsWith("APP OBJECT")) {
            temp = new StringBuilder().append(string).append("APP OBJECT").toString();
        }
        this.string = temp;
    }

    public TestComplexAppObject getTestComplexAppObject() {
        return testComplexAppObject;
    }

    public void setTestComplexAppObject(TestComplexAppObject testComplexAppObject) {
        this.testComplexAppObject = testComplexAppObject;
    }

    @Override
    public int hashCode() {
        return Objects.hash(integer, bool, string, testComplexAppObject);
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
        TestAppObject other = (TestAppObject) obj;
        return integer == other.integer && bool == other.bool && Objects.equals(string, other.string)
                && Objects.equals(testComplexAppObject, other.testComplexAppObject);
    }

}
