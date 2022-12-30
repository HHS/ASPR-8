package tools.meta.classgraph;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import tools.meta.classgraph.reports.CircularClassDependencyReport;
import tools.meta.classgraph.reports.CircularPackageDependencyReport;
import tools.meta.classgraph.reports.WildCardReport;
import tools.meta.classgraph.support.ClassDependencyScan;
import tools.meta.classgraph.support.ClassDependencyScanner;

public final class ClassGraphDriver {

	private ClassGraphDriver() {
	}

	public static void main(String[] args) throws IOException {
		String directoryName = args[0];
		Path path = Paths.get(directoryName);
		ClassDependencyScan classDependencyScan = ClassDependencyScanner.execute(path);
		
		WildCardReport.report(classDependencyScan);
		
		CircularPackageDependencyReport.report(classDependencyScan);
		
		CircularClassDependencyReport.report(classDependencyScan);
	}

}
