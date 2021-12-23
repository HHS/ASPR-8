package util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A reflection-based, stateful utility for calculating the approximate byte
 * cost of the deep memory associated with objects. Both objects and classes can
 * be marked as participating or not participating the in the byte count
 * calculation dynamically. As objects are assessed, they become excluded from
 * subsequent calculations.
 * 
 * @author Shawn Hatch
 *
 */
public final class MemSizer {

	/*
	 * A private utility class for accounting for the internal and external byte
	 * count for an object. The internal cost is the cost of the header, the
	 * primitive fields and object references. The external cost is the deep
	 * cost of the referenced objects.
	 */
	private static class Rec {

		/*
		 * Acts as a tracking index as either array elements or fields are
		 * individually explored. Should start at zero and end with childCount.
		 */
		int childIndex;

		/*
		 * If the focus is an array then this is the length of that array.
		 * Otherwise, it is the number of fields in the object that are being
		 * accounted.
		 */
		int childCount;

		/*
		 * The object that this Rec is accounting
		 */
		private Object focus;

		/*
		 * the cost of object's header, primitive fields and object references
		 */
		long internalCost;

		/*
		 * the deep cost of the referenced objects
		 */
		long externalCost = 0;

		/*
		 * The subset of the fields being accounted for in this object. Arrays
		 * will have field==null.
		 */
		List<Field> fields;

		/*
		 * Returns size rounded up to the next 8 bytes.
		 */
		private long getPaddedSize(final long size) {
			final long r = size % 8;
			if (r == 0) {
				return size;
			}
			return (size + 8L) - r;
		}

		/*
		 * Returns the internal and external byte cost of the focus.
		 */
		public long getSize() {
			/*
			 * Objects fields and array elements are internal and must be padded
			 * to the nearest 8 bytes. External costs will already include the
			 * padding.
			 */
			return getPaddedSize(internalCost) + externalCost;
		}

		/*
		 * Increases the internal byte cost associated with the focus.
		 */
		public void incrementExternalCost(final long value) {
			externalCost += value;
		}

		/*
		 * Increases the external byte cost associated with the focus.
		 */
		public void incrementInternalCost(final long value) {
			internalCost += value;
		}

		/**
		 * Boilerplate implementation
		 */
		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("Rec [childIndex=");
			builder.append(childIndex);
			builder.append(", childCount=");
			builder.append(childCount);
			builder.append(", focus=");
			builder.append(focus);
			builder.append(", internalCost=");
			builder.append(internalCost);
			builder.append(", externalCost=");
			builder.append(externalCost);
			builder.append(", fields=");
			builder.append(fields);
			builder.append("]");
			return builder.toString();
		}
	}

	/*
	 * Runtime.getRuntime().maxMemory() reports the max memory available to the
	 * application and not the amount allocated in the command line. 8 byte
	 * object references are triggered when allocated memory is 32G or greater.
	 * This has to be adjusted downward by approximately 8/9 to adjust for
	 * reserved GC memory. Through testing, that byte count is 30,542,397,440
	 */
	private final long MEMORY_THRESHOLD_32 = 30542397440L;// ~~8/9 * 32 * 2^30

	private long OBJECT_REFERENCE_COST;

	private final long OBJECT_HEADER_SIZE;

	private final Set<Class<?>> excludedClasses = new LinkedHashSet<>();

	private final Map<Class<?>, List<Field>> fieldMap = new LinkedHashMap<>();

	private final Set<Object> explored = new LinkedHashSet<>();

	private final Set<Object> excluded = new LinkedHashSet<>();

	private final LinkedList<Rec> recs = new LinkedList<>();

	private long byteCount;

	/*
	 * A map from the 8 primitive (not the wrapped primitive) classes to their
	 * memory allocation in bytes.
	 */
	private final Map<Class<?>, Long> primitiveSizeMap = new LinkedHashMap<>();

	private final boolean includeStaticFields;

	/**
	 * Constructs an instance of the MemSizer
	 * 
	 * @param includeStaticFields
	 *            when true the MemSizer instance will explore static fields
	 */
	public MemSizer(boolean includeStaticFields) {
		this.includeStaticFields = includeStaticFields;
		primitiveSizeMap.put(long.class, 8L);
		primitiveSizeMap.put(int.class, 4L);
		primitiveSizeMap.put(short.class, 2L);
		primitiveSizeMap.put(byte.class, 1L);
		primitiveSizeMap.put(double.class, 8L);
		primitiveSizeMap.put(float.class, 4L);
		primitiveSizeMap.put(char.class, 2L);
		primitiveSizeMap.put(boolean.class, 1L);

		final long maxMemory = Runtime.getRuntime().maxMemory();
		if (maxMemory < MEMORY_THRESHOLD_32) {
			OBJECT_REFERENCE_COST = 4;
		} else {
			OBJECT_REFERENCE_COST = 8;
		}

		OBJECT_HEADER_SIZE = 8L + OBJECT_REFERENCE_COST;
	}

	/**
	 * Excludes a class from the byte count assessment. Reference costs will
	 * still be assessed.
	 */
	public void excludeClass(final Class<?> c) {
		excludedClasses.add(c);
	}

	/**
	 * Excludes an object instance from the byte count assessment. Reference
	 * costs will still be assessed.
	 */
	public void excludeObject(final Object object) {
		if (object != null) {
			excluded.add(object);
		}
	}

	/**
	 * Returns the byte count for the Object instance constrained to only
	 * accounting for objects that have 1) not been assessed 2) that are not
	 * excluded objects 3) are not instances of excluded classes and 4) are
	 * consistent with the policy of exploring static fields.
	 */
	public long getByteCount(final Object obj) {
		/*
		 * Note that this is not a recursive algorithm on getByteCount. We must
		 * be careful to avoid mounting a deep stack depth since we will be
		 * encountering data structures that will have millions of nested
		 * reference links, such as many of the java collections framework
		 * implementers.
		 */
		try {
			byteCount = 0;
			push(obj);
			while (!recs.isEmpty()) {
				final Rec rec = recs.getLast();
				if (rec.fields == null) {
					final Class<?> arrayType = rec.focus.getClass().getComponentType();
					if (arrayType.isPrimitive()) {
						rec.incrementExternalCost(Array.getLength(rec.focus) * getPrimitiveSize(arrayType));
						pop();
					} else {
						if (rec.childIndex < rec.childCount) {
							rec.incrementExternalCost(OBJECT_REFERENCE_COST);
							final Object child = Array.get(rec.focus, rec.childIndex);
							rec.childIndex++;
							push(child);
						} else {
							pop();
						}
					}
				} else {
					if (rec.childIndex < rec.childCount) {
						final Field field = rec.fields.get(rec.childIndex);
						if (field.getType().isPrimitive()) {
							if (!Modifier.isStatic(field.getModifiers())) {
								final Class<?> type = field.getType();
								rec.incrementInternalCost(getPrimitiveSize(type));
							}
							rec.childIndex++;
						} else {
							final Object subObj = field.get(rec.focus);
							if (!Modifier.isStatic(field.getModifiers())) {
								rec.incrementInternalCost(OBJECT_REFERENCE_COST);
							}
							rec.childIndex++;
							push(subObj);
						}
					} else {
						pop();
					}
				}
			}
			return byteCount;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Returns a list of the Fields for the given class with static fields being
	 * ignored when includeStaticFields is false.
	 */
	private List<Field> getFields(final Class<?> c) {
		List<Field> result = fieldMap.get(c);
		if (result == null) {
			result = new ArrayList<>();
			fieldMap.put(c, result);
			Class<?> classRef = c;
			while (classRef != null) {
				for (final Field field : classRef.getDeclaredFields()) {
					if (includeStaticFields || !Modifier.isStatic(field.getModifiers())) {
						result.add(field);
						field.setAccessible(true);
					}
				}
				classRef = classRef.getSuperclass();
			}
		}
		return result;
	}

	/*
	 * Returns the size of a primitive type
	 */
	private long getPrimitiveSize(final Class<?> type) {
		return primitiveSizeMap.get(type);
	}

	/**
	 * Includes an object instance for the byte count assessment.
	 */
	public void includeObject(final Object object) {
		if (object != null) {
			excluded.remove(object);
		}
	}

	/*
	 * Removes the last Rec and transfers its byte count. If the resulting recs
	 * list is empty then is transferred to the byte count that will be returned
	 * by the algorithm. Otherwise the byte count is transferred to the new last
	 * Rec as an external cost.
	 */
	private void pop() {
		final Rec removedRec = recs.pollLast();
		final long bytesToTransfer = removedRec.getSize();

		if (recs.isEmpty()) {
			byteCount = bytesToTransfer;
		} else {
			final Rec lastRec = recs.getLast();
			lastRec.incrementExternalCost(bytesToTransfer);
		}
	}

	/*
	 * Adds the object to the recs list. Objects that are null, are in the set
	 * of excluded object, are instances of excluded classes or that have been
	 * accounted for previously will be ignored.
	 */
	private void push(final Object obj) {
		if (verbose) {
			System.out.println("MemSizer.push()" + obj);
		}
		if (obj == null) {
			if (verbose) {
				System.out.println("rejecting null");
			}
			return;
		}

		final Class<?> c = obj.getClass();

		if (excludedClasses.contains(c)) {
			if (verbose) {
				System.out.println("rejecting excluded class");
			}
			return;
		}

		if (excluded.contains(obj)) {
			if (verbose) {
				System.out.println("rejecting excluded object");
			}
			return;
		}

		if (explored.contains(obj)) {
			if (verbose) {
				System.out.println("rejecting explored object " + obj.getClass().getSimpleName() + " " + obj);
			}
			return;
		}

		/*
		 * Make sure that we cannot account for this instance again
		 */
		if (verbose) {
			System.out.println("pushing " + obj.getClass().getSimpleName());
		}
		explored.add(obj);

		/*
		 * Create a Rec wrapper for the object. Account for the header size
		 */
		final Rec rec = new Rec();
		rec.internalCost = OBJECT_HEADER_SIZE;
		rec.focus = obj;

		/*
		 * Establish the fields of the object and the number of children
		 */
		if (c.isArray()) {
			rec.childCount = Array.getLength(obj);
		} else {
			rec.fields = getFields(c);
			rec.childCount = rec.fields.size();
		}

		recs.add(rec);

	}

	private boolean verbose;

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
