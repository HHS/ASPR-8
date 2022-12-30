package util.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public class AT_KahanSum {

	@Test
	@UnitTestMethod(target = KahanSum.class, name = "add", args = { double.class }, tags = { UnitTag.MANUAL })
	public void testAdd() {

		/*
		 * This is a sketch of how one might go about manually testing the
		 * KahanSum capability.
		 * 
		 * We have a relatively large number (a) and add a very small number (b)
		 * to it about a billion times. Modeling this with big decimal gives a
		 * very good answer, but the kahan sum is nearly as good and is an order
		 * of magnitude faster. Of course, the slowness of the big decimal
		 * approach is due to having converted the double values into big
		 * decimals, yielding big decimal values that are very long. Had we used
		 * the string based constructors with just the digits listed for the
		 * doubles, the big decimal performance would be much better, but this
		 * would not have properly represented the addition of (a) and (b).
		 * 
		 * On balance, it seems that use of the kahan sum is justified for the
		 * special use case of large summations over widely varying values.
		 */

		// should be 1_000_000_000 for a proper manual test
		int numberOfSummations = 100;

		double a = 123456789;
		double b = 0.000000001;

		BigDecimal bigA = new BigDecimal(a);
		BigDecimal bigB = new BigDecimal(b);

		KahanSum k = new KahanSum();
		k.add(a);

		// TimeElapser timeElapser = new TimeElapser();
		for (int i = 0; i < numberOfSummations; i++) {
			bigA = bigA.add(bigB);
			a += b;
			k.add(b);
		}
		// System.out.println(timeElapser.getElapsedMilliSeconds());
		// System.out.println("bigA = " + bigA);
		// System.out.println("a = " + new BigDecimal(a));
		// System.out.println("k = " + new BigDecimal(k.getSum()));
	}

	@Test
	@UnitTestMethod(target = KahanSum.class, name = "getSum", args = {}, tags = { UnitTag.LOCAL_PROXY })
	public void testGetSum() {
		// covered by testAdd()
	}

	@Test
	@UnitTestConstructor(target = KahanSum.class, args = {})
	public void testConstructor() {
		assertEquals(0.0, new KahanSum().getSum());
	}

}
