package tools.dependencyanalysis.classgraph.support;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;

public class ClassDependencyScanner {

	private static boolean isJavaFile(Path file) {
		return Files.isRegularFile(file) && file.toString().endsWith(".java");
	}

	private static final class SourceFileVisitor extends SimpleFileVisitor<Path> {
		private final Path baseDirectory;

		private SourceFileVisitor(Path baseDirectory) {
			this.baseDirectory = baseDirectory;
		}

		private ClassDependencyScan.Builder builder = ClassDependencyScan.builder();

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			Objects.requireNonNull(dir);
			Objects.requireNonNull(attrs);
			int baseLength = baseDirectory.toString().length();
			String dirString = dir.toString();
			String reducedString = dirString.substring(baseLength, dirString.length());
			if (reducedString.startsWith("\\")) {
				reducedString = reducedString.substring(1);
			}
			reducedString = reducedString.replaceAll("\\\\", ".");
			builder.addLocalPackageName(reducedString);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) {
			String packageName = file.getParent().toString();
			int baseLength = baseDirectory.toString().length();
			packageName = packageName.substring(baseLength);
			if (packageName.startsWith("\\")) {
				packageName = packageName.substring(1);
			}
			packageName = packageName.replaceAll("\\\\", ".");
			String fileName = file.getFileName().toString();
			fileName = fileName.substring(0, fileName.length() - 5);
			JavaRef dependentRef = new JavaRef(fileName, packageName);

			if (isJavaFile(file)) {
				try {

					List<String> lines = Files.readAllLines(file);
					for (String line : lines) {
						if (line.startsWith("import ")) {

							String linePackage = line.substring(7);
							int index = linePackage.length();
							for (int i = 0; i < linePackage.length(); i++) {
								if (linePackage.charAt(i) == '.') {
									index = i;
								}
							}
							fileName = linePackage.substring(index + 1, linePackage.length() - 1);
							linePackage = linePackage.substring(0, index);
							JavaRef supportRef = new JavaRef(fileName, linePackage);
							JavaDependency javaDependency = new JavaDependency(dependentRef, supportRef);
							builder.addJavaDependency(javaDependency);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private ClassDependencyScanner() {		
	}

	public static ClassDependencyScan execute(Path dir) throws IOException {
		final SourceFileVisitor sourceFileVisitor = new SourceFileVisitor(dir);
		Files.walkFileTree(dir, sourceFileVisitor);
		return sourceFileVisitor.builder.build();
		
	}

}
