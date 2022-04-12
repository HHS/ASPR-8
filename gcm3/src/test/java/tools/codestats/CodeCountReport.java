package tools.codestats;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import util.tree.MutableTree;
import util.tree.Tree;
import util.tree.impl.SortedTree;

public class CodeCountReport {
	
	private CodeCountReport() {}

	private int totalCodeLineCount;

	private int totalCommentLineCount;

	private int totalBlankLineCount;

	private String detailsReport;

	public int getTotalCodeLineCount() {
		return totalCodeLineCount;
	}

	public int getTotalCommentLineCount() {
		return totalCommentLineCount;
	}

	public int getTotalBlankLineCount() {
		return totalBlankLineCount;
	}

	public String getDetailsReport() {
		return detailsReport;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Builder() {
		}

		private void cascadeLineCounts(Tree<Node> tree, Node node) {
			for (Node childNode : tree.getChildNodes(node)) {
				cascadeLineCounts(tree, childNode);
			}
			for (Node childNode : tree.getChildNodes(node)) {
				for (LineType lineType : LineType.values()) {
					node.increment(lineType, childNode.getLineCount(lineType));
				}
			}

		}

		public CodeCountReport build() {
			try {

				final CounterFileVisitor counterFileVisitor = new CounterFileVisitor();
				for (Path directory : directories) {
					try {
						Files.walkFileTree(directory, counterFileVisitor);
					} catch (final IOException e) {
						throw new RuntimeException(e);
					}
				}

				MutableTree<Node> tree = counterFileVisitor.tree;

				Node rootNode = new Node(Paths.get("All"));

				List<Node> currentRootNodes = new ArrayList<>();
				for (Node node : tree.getRootNodes()) {
					currentRootNodes.add(node);

				}
				for (Node node : currentRootNodes) {
					tree.addLink(rootNode, node);
				}
				cascadeLineCounts(tree, rootNode);

				CodeCountReport codeCountReport = new CodeCountReport();
				codeCountReport.totalBlankLineCount = rootNode.getLineCount(LineType.BLANK);
				codeCountReport.totalCodeLineCount = rootNode.getLineCount(LineType.CODE);
				codeCountReport.totalCommentLineCount = rootNode.getLineCount(LineType.COMMENT);
				int totalLineCount = codeCountReport.totalBlankLineCount + codeCountReport.totalCodeLineCount + codeCountReport.totalCommentLineCount;

				StringBuilder sb = new StringBuilder();
				for (LineType lineType : LineType.values()) {
					sb.append(lineType);
					sb.append("\t");
				}
				sb.append(totalLineCount);
				sb.append("\n");

				for (Node node : counterFileVisitor.tree.getAllNodes()) {

					for (LineType lineType : LineType.values()) {
						sb.append(node.getLineCount(lineType));
						sb.append("\t");
					}
					sb.append("\t");
					// sb.append(node.getPath());

					Iterator<Path> iterator = node.getPath().iterator();
					while (iterator.hasNext()) {
						sb.append("\t");
						sb.append(iterator.next());
					}
					sb.append("\n");

				}

				codeCountReport.detailsReport = sb.toString();
				return codeCountReport;

			} finally {
				directories = new LinkedHashSet<>();
			}
		}

		private Set<Path> directories = new LinkedHashSet<>();

		public Builder addDirectory(String directoryName) {
			Path dir = Paths.get(directoryName);
			if (Files.isDirectory(dir)) {
				directories.add(dir);
			}
			return this;
		}
	}

	private static class CounterFileVisitor implements FileVisitor<Path> {

		private MutableTree<Node> tree = new SortedTree<>();
		private Map<Path, Node> map = new LinkedHashMap<>();

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			Objects.requireNonNull(dir);
			Objects.requireNonNull(attrs);

			Node node = new Node(dir);
			tree.addNode(node);
			map.put(dir, node);
			Node parentCounter = map.get(dir.getParent());
			if (parentCounter != null) {
				tree.addLink(parentCounter, node);
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
			Node node = new Node(file);
			map.put(file, node);
			Node parentCounter = map.get(file.getParent());

			tree.addLink(parentCounter, node);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			Objects.requireNonNull(file);
			if (exc != null) {
				throw exc;
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			Objects.requireNonNull(dir);
			if (exc != null) {
				throw exc;
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private static class Node implements Comparable<Node> {
		private Map<LineType, Counter> counterMap = new LinkedHashMap<>();

		private final Path path;

		public Node(Path path) {
			for (LineType lineType : LineType.values()) {
				counterMap.put(lineType, new Counter());
			}
			this.path = path;
			if (Files.isRegularFile(path)) {
				exploreFile();
			}
		}

		private void exploreFile() {
			/*
			 * 1234
			 */

			// String importString = "import";

			if (path.toString().endsWith(".java")) {
				List<String> lines;
				try {
					lines = Files.readAllLines(path);
				} catch (IOException e) {
					lines = new ArrayList<>();
				}
				boolean blockCommentActive = false;
				for (String line : lines) {

					boolean commentFound = false;
					int n = line.length();
					StringBuilder sb = new StringBuilder();

					int i = 0;
					while (i < n) {
						char c = line.charAt(i);
						if (blockCommentActive) {
							commentFound = true;
							if (c == '/') {
								if (i > 0) {
									char b = line.charAt(i - 1);
									if (b == '*') {
										blockCommentActive = false;
									}
								}
							}
						} else {
							if (c == '/') {
								if (i < n - 1) {
									char d = line.charAt(i + 1);
									if (d == '/') {
										commentFound = true;
										i = n;
										break;
									} else {
										if (d == '*') {
											commentFound = true;
											blockCommentActive = true;
											i++;
										}
									}
								}
							} else {

								sb.append(c);
							}
						}
						i++;
					}

					line = sb.toString().trim();

					if (line.length() > 0) {
						increment(LineType.CODE, 1);
					} else {
						if (commentFound) {
							increment(LineType.COMMENT, 1);
						} else {
							increment(LineType.BLANK, 1);
						}
					}
				}
			} else if (path.toString().endsWith(".js")) {
				List<String> lines;
				try {
					lines = Files.readAllLines(path);
				} catch (IOException e) {
					lines = new ArrayList<>();
				}
				increment(LineType.CODE, lines.size());
			}

		}

		public void increment(LineType lineType, int count) {
			counterMap.get(lineType).count += count;
		}

		public int getLineCount(LineType lineType) {
			return counterMap.get(lineType).count;
		}

		public Path getPath() {
			return path;
		}

		@Override
		public int compareTo(Node node) {
			return path.compareTo(node.path);
		}
	}

	private static enum LineType {
		CODE, COMMENT, BLANK;

	}

	private static class Counter {
		private int count;
	}
}
