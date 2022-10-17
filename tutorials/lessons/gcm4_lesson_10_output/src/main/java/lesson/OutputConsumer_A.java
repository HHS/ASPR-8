package lesson;

import java.util.function.Consumer;

 public class OutputConsumer_A implements Consumer<Object> {

	@Override
	public void accept(Object t) {
		System.out.println(t);
	}
	
 } 
 
 
 


