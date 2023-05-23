package util.meta.classdependency.classgraph.reports;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import util.meta.classdependency.classgraph.support.ClassDependencyScan;
import util.meta.classdependency.classgraph.support.JavaDependency;
import util.meta.classdependency.classgraph.support.JavaRef;

public final class WildCardReport {
	private WildCardReport() {
	}

	public static void report(ClassDependencyScan classDependencyScan) {
		System.out.println();
		System.out.println("Import statements with wildcards");

		Map<JavaRef, List<JavaRef>> map = new LinkedHashMap<>();
		for (JavaDependency javaDependency : classDependencyScan.getJavaDependencies()) {
			if (javaDependency.getSupportRef().getClassName().equals("*")) {
				JavaRef dependentRef = javaDependency.getDependentRef();
				JavaRef supportRef = javaDependency.getSupportRef();
				List<JavaRef> list = map.get(dependentRef);
				if (list == null) {
					list = new ArrayList<>();
					map.put(dependentRef, list);
				}
				list.add(supportRef);
			}
		}

		for (JavaRef dependentJavaRef : map.keySet()) {
			System.out.println(dependentJavaRef.getFullName());
			List<JavaRef> list = map.get(dependentJavaRef);
			for (JavaRef supportJavaRef : list) {
				System.out.println("\t" + supportJavaRef.getFullName());
			}
		}
	}
}
