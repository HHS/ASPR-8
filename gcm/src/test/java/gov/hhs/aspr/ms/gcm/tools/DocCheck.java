package gov.hhs.aspr.ms.gcm.tools;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public final class DocCheck {
	private final List<Path> directories;

	private DocCheck(List<Path> directories) {
		this.directories = new ArrayList<>(directories);
	}

	private void execute() {
		for (Path dir : directories) {
			loadSourceClasses(dir);
		}
	}

	private static boolean isJavaFile(Path file) {
		return Files.isRegularFile(file) && file.toString().endsWith(".java");
	}

	private static enum ItemType {
		START, END
	}

	private static class Item {
		private ItemType itemType;
		private String line;
		private int lineNumber;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Item [itemType=");
			builder.append(itemType);
			builder.append(", line=");
			builder.append(line);
			builder.append(", lineNumber=");
			builder.append(lineNumber);
			builder.append("]");
			return builder.toString();
		}

	}

	private List<Item> gatherItems(final Path file) throws IOException {
		List<Item> result = new ArrayList<>();
		List<String> lines = Files.readAllLines(file);

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.toLowerCase().contains("start code_ref")) {
				Item item = new Item();
				item.itemType = ItemType.START;
				item.line = line;
				item.lineNumber = i + 1;
				result.add(item);
			} else if (line.toLowerCase().contains("/* end */")) {
				Item item = new Item();
				item.itemType = ItemType.END;
				item.line = line;
				item.lineNumber = i + 1;
				result.add(item);
			}
		}

		return result;
	}

	private void probeFile(final Path file) throws IOException {

		List<Item> items = gatherItems(file);
		if(items.size()==0) {
			return;
		}
		
		boolean mismatchFound = false;
		if (items.size() % 2 != 0) {
			mismatchFound = true;
		}
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			if (i % 2 == 0) {
				if (item.itemType != ItemType.START) {
					mismatchFound = true;
				}
			} else {
				if (item.itemType != ItemType.END) {
					mismatchFound = true;
				}
			}
		}
		if (mismatchFound) {
			System.out.println(file);
			for (Item item : items) {
				System.out.println(item);
			}
		}
	}

	private final class SourceFileVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) throws IOException {
			if (isJavaFile(file)) {
				probeFile(file);
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private void loadSourceClasses(Path path) {

		final SourceFileVisitor sourceFileVisitor = new SourceFileVisitor();
		try {
			Files.walkFileTree(path, sourceFileVisitor);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static void main(String[] args) {
		List<Path> directories = new ArrayList<>();
		for (String arg : args) {
			Path path = Paths.get(arg);
			directories.add(path);
		}
		new DocCheck(directories).execute();
	}
}
