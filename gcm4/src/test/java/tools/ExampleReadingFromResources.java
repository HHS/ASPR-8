package tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ExampleReadingFromResources {

	private ExampleReadingFromResources() {
	}

	public static void main(String[] args) throws IOException {
		Path dir = Paths.get("src/test/resources");
		Path path = dir.resolve("example.txt");
		List<String> lines = Files.readAllLines(path);
		System.out.println(lines);

	}
}
