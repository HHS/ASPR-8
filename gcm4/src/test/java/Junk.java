import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Junk {
	public static void main(String[] args) throws IOException {
		Path path = Paths.get("src/test/resources/example.txt");
		List<String> lines = Files.readAllLines(path);
		System.out.println(lines);

	}
}
