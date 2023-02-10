package util.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for JUnit test methods that are testing public source constructors.
 * 
 *
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface UnitTestConstructor {
	Class<?> target();

	Class<?>[] args();

	UnitTag[] tags() default {};
}