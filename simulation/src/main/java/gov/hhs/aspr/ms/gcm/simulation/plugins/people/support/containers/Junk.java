package gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.containers;

public class Junk {

	public static void main(String[] args) {
		
		Bucket bucket = new Bucket();
		bucket.unsafeAdd(78);
		bucket.unsafeAdd(45);
		bucket.unsafeAdd(16);
		bucket.unsafeAdd(27);
		bucket.unsafeAdd(4);
		System.out.println(bucket);
		
		bucket.remove(12);
		bucket.remove(27);
		bucket.remove(78);
		System.out.println(bucket);
		
		
		bucket.unsafeAdd(66);
		bucket.unsafeAdd(32);
		bucket.unsafeAdd(99);
		System.out.println(bucket);
	}
}
