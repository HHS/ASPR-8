package lesson;

import java.util.function.Consumer;

/* start code_ref=output_consumer_A */
public class OutputConsumer_A implements Consumer<Object> {

	@Override
	public void accept(Object t) {
		System.out.println(t);
	}

}
/* end */
