package tools.meta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import plugins.reports.support.ReportHeader;

public class Junk {
	public static void main(String[] args) {
		Class<?> targetClass = ReportHeader.Builder.class;
		
		
		System.out.println("methods");
		Method[] methods = targetClass.getMethods();
		for (Method method : methods) {
			System.out.println(method);
			Class<?>[] parameterTypes = method.getParameterTypes();
			for (Class<?> c : parameterTypes) {
				System.out.println("\t" + c);
			}
		}
		System.out.println("constructors");
		Constructor<?>[] constructors = targetClass.getConstructors();
		for(Constructor<?> constructor : constructors) {
			System.out.println(constructor);
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			for (Class<?> c : parameterTypes) {
				System.out.println("\t" + c);
			}
		}
	}
}
