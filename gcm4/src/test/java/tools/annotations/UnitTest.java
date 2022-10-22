package tools.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for JUnit test classes. Contains the target variable that is used
 * as the default target values for {@link UnitTestMethod} and
 * {@link UnitTestConstructor} in the same test unit class.
 * 
 * @author Shawn Hatch
 *
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface UnitTest {
	Class<?> target();
}