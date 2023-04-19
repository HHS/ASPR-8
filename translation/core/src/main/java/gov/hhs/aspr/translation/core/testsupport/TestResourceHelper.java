package gov.hhs.aspr.translation.core.testsupport;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class TestResourceHelper {
    public static Path getResourceDir(Class<?> classRef) {
        try {
            return Path.of(classRef.getClassLoader().getResource("").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path makeTestOutputDir(Path basePath) {
        Path path = basePath.resolve("json").resolve("test-output");

        path.toFile().mkdirs();

        return path;
    }

    public static void createTestOutputFile(Path filePath, String fileName) {
        try {
            filePath.resolve(fileName).toFile().createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
