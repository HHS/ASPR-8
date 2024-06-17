package gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.containers;

/*
 * Manual performance tuning test turned off until adoption of JOL or another memory assessment tool for post-8 java
 */


//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Random;
//import java.util.Set;
//
//import gov.hhs.aspr.ms.util.time.TimeElapser;

/**
 * Test class for {@link ArrayIntSet}
 * 
 * A manual performance test for comparing the runtime and memory measures for a
 * baseline LinkedHashSet and an ArrayIntSet for modeling a large population
 * that transitions through a data container. This test is also designed to aid
 * in the selection of a fitting factor (target depth) used in the ArrayIntSet.
 * The test models 10,000,000 people moving through a compartment with a rolling
 * time averaged concurrent population in the compartment of approximately
 * 2,500,000 people.
 * 
 * 
 *
 */
public class MT_ArrayIntSet {

//	private static class Event {
//		private IdValued id;
//		private boolean arriving;
//		private int time;
//	}
//
//	/*
//	 * A class to represent the runtime and memory performance of an algorithm.
//	 * Memory is recorded at the peak of the population count within the
//	 * container being measured and then at the end when the container will have
//	 * contracted to its final size.
//	 */
//	private static class Performance {
//		public double milliseconds;
//		public long memorySizeAtEnd;
//		public long memorySizeAtPeak;
//
//		public Performance(long memorySizeAtPeak, long memorySizeAtEnd, double milliseconds) {
//			super();
//			this.memorySizeAtPeak = memorySizeAtPeak;
//			this.memorySizeAtEnd = memorySizeAtEnd;
//			this.milliseconds = milliseconds;
//		}
//
//	}
//
//	private Comparator<Event> eventComparator = new Comparator<Event>() {
//
//		@Override
//		public int compare(Event event1, Event event2) {
//			return Integer.compare(event1.time, event2.time);
//		}
//	};
//
//	/*
//	 * Generates randomized events. Events are generated in pairs for each
//	 * integer person as then enter into and then leave the container. The
//	 * duration for a person in the container is randomized and time is measured
//	 * as an integer.
//	 */
//	private List<Event> generateEvents(int n, int lowDuration, int highDuration, long seed) {
//		List<Event> result = new ArrayList<>();
//
//		Random random = new Random(seed);
//		for (int i = 0; i < n; i++) {
//			Event event = new Event();
//			event.id = new IdValued(i);
//			event.time = i;
//			event.arriving = true;
//			result.add(event);
//
//			event = new Event();
//			event.id = new IdValued(i);
//			event.time = i + random.nextInt(highDuration - lowDuration) + lowDuration;
//			event.arriving = false;
//			result.add(event);
//		}
//
//		result.sort(eventComparator);
//
//		return result;
//	}
//
//	/*
//	 * Returns the performance of using a LinkedHashSet to process a list of
//	 * events.
//	 * 
//	 */
//	private Performance getBaseLineSetPerformance(List<Event> events) {
//		int peakOccupancyIndex = getPeakOccupancyIndex(events);
//		Set<IdValued> set = new LinkedHashSet<>();
//		long sizeAtPeak = 0;
//		TimeElapser timeElapser = new TimeElapser();
//		for (int i = 0; i < events.size(); i++) {
//			Event event = events.get(i);
//			if (event.arriving) {
//				set.add(event.id);
//			} else {
//				set.remove(event.id);
//			}
//			if (i == peakOccupancyIndex) {
//				MemSizer memSizer = new MemSizer(false);
//				memSizer.excludeClass(Class.class);
//				sizeAtPeak = memSizer.getByteCount(set);
//			}
//		}
//		MemSizer memSizer = new MemSizer(false);
//		memSizer.excludeClass(Class.class);
//		long sizeAtEnd = memSizer.getByteCount(set);
//		return new Performance(sizeAtPeak, sizeAtEnd, timeElapser.getElapsedMilliSeconds());
//	}
//
//	private int getPeakOccupancyIndex(List<Event> events) {
//		int occupancy = 0;
//		int maxOccpancy = 0;
//		int result = 0;
//		for (int i = 0; i < events.size(); i++) {
//			if (events.get(i).arriving) {
//				occupancy++;
//			} else {
//				occupancy--;
//			}
//			if (occupancy > maxOccpancy) {
//				maxOccpancy = occupancy;
//				result = i;
//			}
//		}
//
//		return result;
//	}
//	/*
//	 * Returns the performance of using an ArrayIntSet to process a list of
//	 * events.
//	 */
//
//	private Performance getIntSetPerformance(List<Event> events, float targetDepth) {
//
//		int peakOccupancyIndex = getPeakOccupancyIndex(events);
//		long sizeAtPeak = 0;
//
//		TimeElapser timeElapser = new TimeElapser();
//		ArrayIntSet<IdValued> intSet = new ArrayIntSet<>(targetDepth);
//
//		for (int i = 0; i < events.size(); i++) {
//			Event event = events.get(i);
//			if (event.arriving) {
//				intSet.add(event.id);
//			} else {
//				intSet.remove(event.id);
//			}
//			if (i == peakOccupancyIndex) {
//				MemSizer memSizer = new MemSizer(false);
//				memSizer.excludeClass(Class.class);
//				sizeAtPeak = memSizer.getByteCount(intSet);
//			}
//		}
//		MemSizer memSizer = new MemSizer(false);
//		memSizer.excludeClass(Class.class);
//		long sizeAtEnd = memSizer.getByteCount(intSet);
//		Performance result = new Performance(sizeAtPeak, sizeAtEnd, timeElapser.getElapsedMilliSeconds());
//		return result;
//	}
//
//	/**
//	 * Conducts a test of ArrayIntSet performance as a function of target depth
//	 * against a baseline performance based on LinkedHashSet.
//	 */
//
//	private void test() {
//
//		for (int targetDepth = 55; targetDepth < 100; targetDepth += 5) {
//			float f = targetDepth;
//			testMod(f);
//		}
//	}
//
//	/*
//	 * Sends 10 million Integer values through as ArrayIntSet by simulating
//	 * randomized entries and removals from the set.
//	 */
//	private void testMod(float targetDepth) {
//		int n = 10_000_000;
//		int lowDuration = 1_000_000;
//		int highDuration = 3_000_000;
//		long seed = 2457567456457645L;
//
//		List<Event> events = generateEvents(n, lowDuration, highDuration, seed);
//
//		Performance baseLineSetPerformance = getBaseLineSetPerformance(events);
//
//		Performance intSetPerformance = getIntSetPerformance(events, targetDepth);
//
//		StringBuilder sb = new StringBuilder();
//
//		sb.append(n);
//		sb.append("\t");
//		sb.append(targetDepth);
//		sb.append("\t");
//		sb.append(baseLineSetPerformance.milliseconds);
//		sb.append("\t");
//		sb.append(baseLineSetPerformance.memorySizeAtPeak);
//		sb.append("\t");
//		sb.append(baseLineSetPerformance.memorySizeAtEnd);
//		sb.append("\t");
//		sb.append(intSetPerformance.milliseconds);
//		sb.append("\t");
//		sb.append(intSetPerformance.memorySizeAtPeak);
//		sb.append("\t");
//		sb.append(intSetPerformance.memorySizeAtEnd);
//		
//	}
//
//	private MT_ArrayIntSet() {
//
//	}
//
//	public static void main(String[] args) {
//		new MT_ArrayIntSet().test();
//	}

}
