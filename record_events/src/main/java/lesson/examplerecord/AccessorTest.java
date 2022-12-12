package lesson.examplerecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.time.TimeElapser;

public class AccessorTest {
	public static void main(String[] args) {
		Random random = new Random();
		List<AccessorRec> accessorRecs = new ArrayList<>();
		List<Accessor> accessors = new ArrayList<>();
		int n = 10_000_000;
		for (int i = 0; i < n; i++) {
			int value = random.nextInt(100)-50;
			accessorRecs.add(new AccessorRec(value));			
			accessors.add(new Accessor(value));
		}
		
		TimeElapser timeElapser = new TimeElapser();
		
		
		timeElapser.reset();
		int count = 0;
		for (AccessorRec accessorRec : accessorRecs) {
			if(accessorRec.x()<0) {
				count++;
			}			
		}
		System.out.println(count+" found in "+timeElapser.getElapsedMilliSeconds());
		
		timeElapser.reset();
		count = 0;
		for (AccessorRec accessorRec : accessorRecs) {
			if(accessorRec.getX()<0) {
				count++;
			}			
		}
		System.out.println(count+" found in "+timeElapser.getElapsedMilliSeconds());

		timeElapser.reset();
		count = 0;
		for (Accessor accessor : accessors) {
			if(accessor.getX()<0) {
				count++;
			}			
		}
		System.out.println(count+" found in "+timeElapser.getElapsedMilliSeconds());

	}

}
