package gov.hhs.aspr.ms.taskit.core.testsupport;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;

public class AT_TestResourceHelper {

    @Test
    @UnitTestMethod(target = TestResourceHelper.class, name = "getResourceDir", args = { Class.class })
    public void testGetResourceDir() throws MalformedURLException {
        Path path = TestResourceHelper.getResourceDir(this.getClass());

        assertNotNull(path);
        assertTrue(path.toFile().exists());

        // preconditions
        URL url = new URL("file:${my.properties}");

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            TestResourceHelper.getURI(url);
        });

        assertTrue(runtimeException.getCause() instanceof URISyntaxException);
    }

    @Test
    @UnitTestMethod(target = TestResourceHelper.class, name = "makeTestOutputDir", args = { Path.class })
    public void testMakeTestOutputDir() {
        Path path = TestResourceHelper.getResourceDir(this.getClass()).resolve("additional-folder");

        Path newPath = TestResourceHelper.makeTestOutputDir(path);

        assertNotNull(newPath);
        assertTrue(newPath.toFile().exists());
    }

    private class IOExceptionFile extends File {

        public IOExceptionFile(String pathname) {
            super(pathname);
        }

        public IOExceptionFile(File file) {
            super(file, pathSeparator);
        }

        @Override
        public boolean createNewFile() throws IOException {

            throw new IOException();
        }

    }

    @Test
    @UnitTestMethod(target = TestResourceHelper.class, name = "createTestOutputFile", args = { Path.class,
            String.class })
    public void testCreateTestOutputFile() throws IOException {
        Path path = TestResourceHelper.getResourceDir(this.getClass());

        Path newPath = TestResourceHelper.makeTestOutputDir(path);

        String fileName = "foo.txt";
        TestResourceHelper.createTestOutputFile(newPath, fileName);

        assertTrue(newPath.resolve(fileName).toFile().exists());

        // test for delete/recreat file if it exists
        TestResourceHelper.createTestOutputFile(newPath, fileName);

        // preconditions
        // force an IOException
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            IOExceptionFile testFile = new IOExceptionFile(newPath.resolve(fileName).toFile());
            TestResourceHelper.createFile(testFile);
        });

        assertTrue(runtimeException.getCause() instanceof IOException);
    }
}
