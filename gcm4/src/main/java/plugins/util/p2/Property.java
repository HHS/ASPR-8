package plugins.util.p2;

import java.util.Optional;

import net.jcip.annotations.Immutable;

@Immutable
public class Property<T> {

	private boolean valuesAreMutable;

	private T defaultValue;

	public Property() {

	}

	public Property(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Property(boolean valuesAreMutable) {
		this.valuesAreMutable = valuesAreMutable;
	}

	public Property(T defaultValue, boolean valuesAreMutable) {
		this.defaultValue = defaultValue;
		this.valuesAreMutable = valuesAreMutable;
	}

	public Optional<T> defaultValue() {
		return Optional.ofNullable(defaultValue);
	}

	public boolean valuesAreMutable() {
		return valuesAreMutable;
	}

}
