package tools.meta;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DependencyTracer {

	private static boolean isJavaFile(Path file) {
		return Files.isRegularFile(file) && file.toString().endsWith(".java");
	}

	private final class SourceFileVisitor extends SimpleFileVisitor<Path> {
		private Set<String> imports = new LinkedHashSet<>();

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) {
			if (isJavaFile(file)) {
				try {
					List<String> lines = Files.readAllLines(file);
					for (String line : lines) {
						if (line.startsWith(importSuffix)) {
							if (compact) {
								int n = importSuffix.length() + 1;
								while (n < line.length()) {
									if (line.charAt(n) == '.') {
										break;
									}
									n++;
								}

								String dirString = dir.toString();
								dirString = dirString.substring(47, dirString.length());
								String lineString = line.substring(15, n);
								if (!dirString.equals(lineString)) {
									imports.add(dirString + "\t" + lineString);
								}
							} else {
								imports.add(line);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private final Path dir;
	private final String importSuffix;
	private final boolean compact;

	private DependencyTracer(Path dir, String importSuffix, boolean compact) {
		this.dir = dir;
		this.importSuffix = importSuffix;
		this.compact = compact;
	}

	private void execute() throws IOException {
		final SourceFileVisitor sourceFileVisitor = new SourceFileVisitor();
		Files.walkFileTree(dir, sourceFileVisitor);
		List<String> imports = new ArrayList<>(sourceFileVisitor.imports);
		Collections.sort(imports);
		for (String importLine : imports) {
			System.out.println(importLine);
		}
	}

	private static void big() throws IOException {
		Path dir = Paths.get("C:\\git_repos\\ASPR-6\\gcm2\\src\\main\\java\\plugins");

		List<String> pluginNames = new ArrayList<>();
		
		pluginNames.add("components");
		
		pluginNames.add("groups");
		pluginNames.add("globals");
		pluginNames.add("materials");
		pluginNames.add("partitions");
		pluginNames.add("people");
		pluginNames.add("personproperties");
		pluginNames.add("regions");
		pluginNames.add("reports");
		pluginNames.add("resources");
		pluginNames.add("stochastics");

		for (String pluginName : pluginNames) {
			Path pluginDir = dir.resolve(pluginName);

			String importSuffix = "import plugins.";
			boolean compact = true;
			new DependencyTracer(pluginDir, importSuffix, compact).execute();
		}

	}

	private static void small(String pluginName) throws IOException {
		Path dir = Paths.get("C:\\git_repos\\ASPR-6\\gcm2\\src\\main\\java\\plugins");
		Path pluginDir = dir.resolve(pluginName);
		String importSuffix = "import plugins.";
		boolean compact = false;
		new DependencyTracer(pluginDir, importSuffix, compact).execute();
	}

	public static void main(String[] args) throws IOException {
		boolean useBig = true;
		if (useBig) {
			big();
		} else {
			small("compartments");
		}
	}

}
