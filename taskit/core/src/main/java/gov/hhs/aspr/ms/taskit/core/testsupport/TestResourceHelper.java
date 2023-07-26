package gov.hhs.aspr.ms.taskit.core.testsupport;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class TestResourceHelper {

    private TestResourceHelper() {
    }

    public static Path getResourceDir(Class<?> classRef) {
        return Path.of(getURI(classRef.getClassLoader().getResource("")));
    }

    protected static URI getURI(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path makeTestOutputDir(Path basePath) {
        Path path = basePath.resolve("test-output");

        path.toFile().mkdirs();

        return path;
    }

    public static void createTestOutputFile(Path filePath, String fileName) {

        File isAfile = filePath.resolve(fileName).toFile();

        if (isAfile.exists()) {
            isAfile.delete();
        }
        createFile(isAfile);
    }

    protected static void createFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
