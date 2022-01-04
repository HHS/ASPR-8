package nucleus.util.experiment;

import java.util.Optional;

import util.TypeMap;

public class TypeMapTest {
	private static class Alpha {
		private final int index;

		public Alpha(int index) {
			this.index = index;
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName() + " " + index;
		}

	}

	private static class Beta extends Alpha {

		public Beta(int index) {
			super(index);
		}

	}

	private static class Gamma extends Alpha {

		public Gamma(int index) {
			super(index);
		}

	}

	private static class Delta extends Beta{

		public Delta(int index) {
			super(index);
		}

	}

	public static void main(String[] args) {
		
		//TypeMap.Builder<Alpha> alphaBuilder = TypeMap.builder(Alpha.class);

		TypeMap<Alpha> typeMap = TypeMap.builder(Alpha.class)//
				.add(new Beta(15))//
				.add(new Gamma(2))//
				.add(new Delta(66))//
				.add(new Gamma(3))
				.build();//

		System.out.println(typeMap.get(Beta.class).get());
		System.out.println(typeMap.get(Gamma.class).get());
		Optional<Delta> optionalDelta = typeMap.get(Delta.class);
		System.out.println(optionalDelta.isPresent());
		if(optionalDelta.isPresent()) {
			System.out.println(optionalDelta.get());
		}

	}
}
