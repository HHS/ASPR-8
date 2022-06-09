package util.time;

import java.util.Map;
import java.util.TreeMap;

public final class StopwatchManager {

	private static Map<Watch, Stopwatch> stopwatches = new TreeMap<>();

	public static void start(Watch key) {
		
//		Stopwatch stopwatch = stopwatches.get(key);
//		if(stopwatch == null) {
//			stopwatch = new Stopwatch();
//			stopwatches.put(key,stopwatch);					
//		}
//		stopwatch.start();
	}

	public static void stop(Watch key) {
//		Stopwatch stopwatch = stopwatches.get(key);
//		stopwatch.stop();
	}

	public static void report() {

		for (Watch key : stopwatches.keySet()) {
			Stopwatch stopwatch = stopwatches.get(key);
			if(stopwatch.isRunning()) {
				System.out.println(key + "\t" + stopwatch.getElapsedMilliSeconds() + "\t" + "IS STILL RUNNING");
			}else {
				System.out.println(key + "\t" + stopwatch.getElapsedMilliSeconds());
			}
		}
	}

}
