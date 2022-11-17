package tools.meta.classgraph;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ClassGraphDriver {

	private ClassGraphDriver() {
	}

	public static void main(String[] args) throws IOException {
		String directoryName = args[0];
		Path path = Paths.get(directoryName);
		ClassGraphTracer.execute(path);
	}

}
