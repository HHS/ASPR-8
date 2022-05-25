package tools.newpropertysystem;

public class Property<T> {

    private final T defaultValue;

    public Property(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public T defaultValue() {
        return defaultValue;
    }

}
