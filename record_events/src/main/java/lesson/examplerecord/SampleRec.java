package lesson.examplerecord;

import java.util.ArrayList;
import java.util.List;

public record SampleRec(String name, int begin, int end, List<String> aliases) implements Alpha {

	@Override
	public int getX() {
		return begin * 3;
	}

	@Override
	public void setX(int x) {
		// the value cannot be assigned

		// value = x;
	}

	/*
	 * showing use of compact constructor -- used for validation only and cannot
	 * be declared while a canonical constructor is declared
	 */
	// public SampleRec {
	// if (name == null) {
	// throw new RuntimeException("null name");
	// }
	// if (name.length() == 0) {
	// throw new RuntimeException("name is blank");
	// }
	// }

	// canonical constructor -- used when you might want to set the fields
	// directly
	public SampleRec(String name, int begin, int end, List<String> aliases) {
		if (name == null) {
			throw new RuntimeException("null name");
		}
		if (name.length() == 0) {
			throw new RuntimeException("name is blank");
		}
		this.name = name;
		if (begin > end) {
			this.begin = end;
			this.end = begin;
		} else {
			this.begin = begin;
			this.end = end;
		}
		if (aliases == null) {
			this.aliases = new ArrayList<>();
		} else {
			this.aliases = aliases;
		}
	}

	// non-canonical constructor
	public SampleRec(String name) {
		this(name, 0, 1, null);
	}

	public SampleRec() {
		this("nobody", 0, 1, null);
	}

	/*
	 * you can have alternate accessors -- they will be just a few percent
	 * slower than the accessors built into the record
	 */
	public int getBegin() {
		return begin;
	}

	// you can perform defensive copies
	public List<String> aliases() {
		return new ArrayList<>(aliases);
	}

}
