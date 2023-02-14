package tools.meta.classdependency.classgraph;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import tools.meta.classdependency.classgraph.reports.CircularClassDependencyReport;
import tools.meta.classdependency.classgraph.reports.CircularPackageDependencyReport;
import tools.meta.classdependency.classgraph.reports.WildCardReport;
import tools.meta.classdependency.classgraph.support.ClassDependencyScan;
import tools.meta.classdependency.classgraph.support.ClassDependencyScanner;

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
