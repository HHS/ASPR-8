package tools.newpropertysystem;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MT_TypeReference {
	public static void main(String[] args) {
		TypeReference<Integer> t = new TypeReference<>() {
			
		};		
		System.out.println(t.getType());
		
		Integer x = 6;		
		System.out.println(x.getClass());
		
		List<Integer> list = new ArrayList<>();
		Type superClass = list.getClass().getGenericSuperclass();
		Type _type =((ParameterizedType)superClass).getActualTypeArguments()[0];
		System.out.println(_type);
		

	}
}
