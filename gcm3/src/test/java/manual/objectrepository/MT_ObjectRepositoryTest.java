package manual.objectrepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import manual.objectrepository.testclasses.TestClass1;
import manual.objectrepository.testclasses.TestClass10;
import manual.objectrepository.testclasses.TestClass11;
import manual.objectrepository.testclasses.TestClass12;
import manual.objectrepository.testclasses.TestClass13;
import manual.objectrepository.testclasses.TestClass14;
import manual.objectrepository.testclasses.TestClass15;
import manual.objectrepository.testclasses.TestClass16;
import manual.objectrepository.testclasses.TestClass17;
import manual.objectrepository.testclasses.TestClass18;
import manual.objectrepository.testclasses.TestClass19;
import manual.objectrepository.testclasses.TestClass2;
import manual.objectrepository.testclasses.TestClass20;
import manual.objectrepository.testclasses.TestClass3;
import manual.objectrepository.testclasses.TestClass4;
import manual.objectrepository.testclasses.TestClass5;
import manual.objectrepository.testclasses.TestClass6;
import manual.objectrepository.testclasses.TestClass7;
import manual.objectrepository.testclasses.TestClass8;
import manual.objectrepository.testclasses.TestClass9;
import util.TimeElapser;
import util.objectrepository.MutableObjectRepository;
import util.objectrepository.ObjectRepository;
import util.objectrepository.ObjectRepository.Builder;

public class MT_ObjectRepositoryTest {
	private MT_ObjectRepositoryTest() {

	}

	private void execute() {
		List<Object> list = new ArrayList<>();
		list.add(new TestClass1());
		list.add(new TestClass2());
		list.add(new TestClass3());
		list.add(new TestClass4());
		list.add(new TestClass5());
		list.add(new TestClass6());
		list.add(new TestClass7());
		list.add(new TestClass8());
		list.add(new TestClass9());
		list.add(new TestClass10());
		list.add(new TestClass11());
		list.add(new TestClass12());
		list.add(new TestClass13());
		list.add(new TestClass14());
		list.add(new TestClass15());
		list.add(new TestClass16());
		list.add(new TestClass17());
		list.add(new TestClass18());
		list.add(new TestClass19());
		list.add(new TestClass20());

		HashMap<Class<?>, Object> hashMap = new HashMap<>();
		MutableObjectRepository m = new MutableObjectRepository();

		Builder builder = ObjectRepository.builder();
		for (Object obj : list) {
			builder.add(obj);
			m.add(obj);
			hashMap.put(obj.getClass(), obj);
		}

		Random random = new Random();
		int n = 1000000;

		ObjectRepository objectRepository = builder.build();
		TimeElapser timeElapser = new TimeElapser();
		for (int i = 0; i < n; i++) {
			Class<? extends Object> c = list.get(random.nextInt(list.size())).getClass();
			objectRepository.getMember(c);
			hashMap.get(c);
			m.getMember(c);
		}
		double totalNanos = timeElapser.getElapsedNanoSeconds();
		double nanosPerDraw = totalNanos / n;

		System.out.println(nanosPerDraw);
	}

	public static void main(String[] args) {
		new MT_ObjectRepositoryTest().execute();
	}
}
