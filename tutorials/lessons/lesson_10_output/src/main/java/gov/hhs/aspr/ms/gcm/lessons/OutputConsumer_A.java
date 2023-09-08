package gov.hhs.aspr.ms.gcm.lessons;

import java.util.function.Consumer;

/* start code_ref=output_consumer_A|code_cap=Output consumer A simply prints output to the console.*/
public class OutputConsumer_A implements Consumer<Object> {

	@Override
	public void accept(Object t) {
		System.out.println(t);
	}

}
/* end */
