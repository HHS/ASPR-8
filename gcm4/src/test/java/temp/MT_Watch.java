package temp;

import util.time.Stopwatch;

public class MT_Watch {
	
	private MT_Watch() {}
	
	public static void main(String[] args) {
		Stopwatch outerWatch = new Stopwatch();
		Stopwatch innerWatch = new Stopwatch();
		outerWatch.start();
		int n = 0;
		for(int i = 0;i<2_000_000;i++) {
			innerWatch.start();
			n+=i;
			innerWatch.stop();
		}
		
		outerWatch.stop();
		System.out.println(outerWatch.getElapsedMilliSeconds()+"\t"+outerWatch.getExecutionCount());
		System.out.println(innerWatch.getElapsedMilliSeconds()+"\t"+innerWatch.getExecutionCount());
		System.out.println(n);
	}
}
