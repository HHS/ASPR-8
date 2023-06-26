package util.maps;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MapReindexer {
	
	private MapReindexer() {
		
	}
	
	
	public static <A,B>  Map<A,B> getReindexedMap(Set<A> indexingSet, Map<A,B> targetMap) {
		
		Map<A,B> result = new LinkedHashMap<>();
		
		for(A a : indexingSet) {
			B b = targetMap.get(a);
			if(b != null) {
				result.put(a, b);
			}
		}
		
		for(A a : targetMap.keySet()) {			
			if(!indexingSet.contains(a)) {
				result.put(a, targetMap.get(a));
			}
		}
		
		return result;				
	}
	
	
}
