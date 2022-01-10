package de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IFloatingPointRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;

import java.util.*;

/**
 * Just a class to make some random operations easier.
 */

public class RandomGenHelper {

	private static final Random mRandom = new Random(1234567890L); // We statically seed this generator for deterministic repeatability
	private static long callCount = 0L;

	// Helper lists for 'pretty number' generation
	private static final SortedMap<Long, Integer> potentialValues = new TreeMap<>(Map.ofEntries(
			Map.entry(0L, 10),
			Map.entry(1L, 10),
			Map.entry(2L, 10),
			Map.entry(4L, 10),
			Map.entry(5L, 10),
			Map.entry(8L, 10),
			Map.entry(10L, 10),
			Map.entry(16L, 10),
			Map.entry(25L, 5),
			Map.entry(32L, 10),
			Map.entry(50L, 5),
			Map.entry(64L, 10),
			Map.entry(100L, 5),
			Map.entry(128L, 10),
			Map.entry(200L, 2),
			Map.entry(256L, 10),
			Map.entry(500L, 2),
			Map.entry(1000L, 1),
			Map.entry(10000L, 1)
	));
	private static final SortedMap<Long, Integer> potentialFloatDivisors = new TreeMap<>(Map.ofEntries(
			Map.entry(2L, 20),
			Map.entry(4L, 20),
			Map.entry(5L, 20),
			Map.entry(8L, 10),
			Map.entry(10L, 10),
			Map.entry(20L, 5),
			Map.entry(40L, 2),
			Map.entry(50L, 1),
			Map.entry(100L, 1),
			Map.entry(1000L, 1)
	));

	static {
		for (int i = 3; i < 16; i++) {
			potentialValues.put((long) Math.pow(10, i), 1);
		}
	}

	public static long getCurrentCallCount() {
		return callCount;
	}

	/*
	 * Call this method at program start when the user requests non-deterministic output or specifies the seed (both via commandline arguments)
	 */
	public static void randomize(long seed) {
		mRandom.setSeed(seed);
		System.out.println("Seed: " + seed);
		callCount = 0L;
	}

	/**
	 * @param from Lower bound (inclusive)
	 * @param to Upper bound (inclusive)
	 * @return Random integer within the given range (uniformly distributed)
	 */
	public static synchronized int randomInt(int from, int to) {
		++callCount;
		return mRandom.nextInt(to + 1 - from) + from;
	}

	public static synchronized long randomLong(long from, long to) {
		if (from > to) {
			throw new RuntimeException("Can not generate long with invalid range from " + from + " to " + to + "!");
		}

		final long scaledN = to - from + 1L;
		if (scaledN == 0L) {
			++callCount;
			return mRandom.nextLong();
		}
		return nextLong(to - from + 1L) + from;
	}

	public static synchronized float randomFloat(float from, float to) {
		if (from > to) {
			throw new RuntimeException("Can not generate float with invalid range from " + from + " to " + to + "!");
		}

		++callCount;
		return from + mRandom.nextFloat() * (to - from);
	}

	public static synchronized double randomDouble(double from, double to) {
		if (from > to) {
			throw new RuntimeException("Can not generate double with invalid range from " + from + " to " + to + "!");
		}

		++callCount;
		return from + mRandom.nextDouble() * (to - from);
	}

	private static long nextLong(long n) {
		// error checking and 2^x checking removed for simplicity.
		long bits, val;
		do {
			++callCount;
			bits = (mRandom.nextLong() << 1) >>> 1;
			val = bits % n;
		} while (bits - val + (n - 1) < 0L);
		return val;
	}

	public static boolean randomChance(int chance, int total) {
		int result = randomInt(1, total);
		return (result <= chance);
	}

	public static <T> T randomElement(List<T> items) {
		return items.get(randomInt(0, items.size() - 1));
	}

	public static <T> Set<T> randomSubset(List<T> items, int count) {
		assert (count <= items.size());

		Set<T> result = new HashSet<>();
		Set<Integer> usedIndexes = new HashSet<>();
		while (result.size() < count) {
			final Integer index = randomInt(0, items.size() - 1);
			if (!usedIndexes.contains(index)) {
				usedIndexes.add(index);
				result.add(items.get(index));
			}
		}
		return result;
	}

	public static <T> Set<T> randomSubset (Set<T> items, int chance, int total) {
		Set<T> result = new HashSet<>();
		for (T t: items) {
			if (randomChance(chance, total)) {
				result.add(t);
			}
		}
		return result;
	}

	public static <T> List<T> randomSubset(List<T> items, int chance, int total) {
		List<T> result = new ArrayList<>();
		for (T t : items) {
			if (randomChance(chance, total)) {
				result.add(t);
			}
		}
		return result;
	}

	public static List<Integer> splitIntoRandomParts(int total, int numberOfSplits) {
		List<Integer> splitPoints = new ArrayList<>();
		List<Integer> nodeAmount = new ArrayList<>();

		if (numberOfSplits > 1) {
			for (int i = 0; i < numberOfSplits; i++) {
				splitPoints.add(randomInt(1, total));
			}
			Collections.sort(splitPoints);
			for (int i = 0; i < numberOfSplits; i++) {
				if (i == 0) {
					nodeAmount.add(splitPoints.get(i));
				} else if (i == numberOfSplits - 1) {
					nodeAmount.add(total - splitPoints.get(i - 1));
				} else {
					nodeAmount.add(splitPoints.get(i) - splitPoints.get(i - 1));
				}
			}
		} else {
			nodeAmount.add(total);
		}
		return nodeAmount;
	}

	public static List<Integer> splitIntoRandomPartsLessVariance(int total, int numberOfSplits) {
		List<Integer> splitValues = new ArrayList<>();
		int halftotal = (total) / 2;
		List<Integer> randomPart = splitIntoRandomParts(halftotal, numberOfSplits);
		int assignedPoints = 0;
		for (int i = 0; i < numberOfSplits; i++) {
			splitValues.add(i, (halftotal / numberOfSplits) + randomPart.get(i));
			assignedPoints += splitValues.get(i);
		}
		splitValues.set(0, splitValues.get(0) + total - assignedPoints);

		return splitValues;
	}

	/**
	 * @param probabilityMap Mapping the keys K to a probability. The probability is an int divided by the sum of all probabilities.
	 * @return A random Element of the given subset according to the probabilities in the map.
	 */
	public static <K> K getRandomElementFromMap(Map<K, Integer> probabilityMap) {
		return getRandomElementFromMap(probabilityMap, new ArrayList<>(probabilityMap.keySet()));
	}

	/**
	 * @param probabilityMap   Mapping the keys K to a probability. The probability is an int divided by the sum of all probabilities.
	 * @param possibleElements A set containing the elements to choose from. Has to be a subset of the keySet of the Map.
	 * @return A random Element of the given subset according to the probabilities in the map.
	 */
	public static <K> K getRandomElementFromMap(Map<K, Integer> probabilityMap, List<K> possibleElements) {
		int sum = 0;
		for (K key : possibleElements) {
			Integer value = probabilityMap.get(key);
			if (value == null) throw new RuntimeException("No probability given for key " + key.toString());
			sum += value;
		}
		int value = randomInt(0, sum);
		sum = 0;
		for (K key : possibleElements) {
			sum += probabilityMap.get(key);
			if (value <= sum) {
				return key;
			}
		}
		throw new RuntimeException("Could not select a value based on given set.");
	}

	/**
	 * Only returns the operators with a greater than zero probability
	 * @param probabilityMap Probability map to choose the operators from
	 * @return EnumSet with all Operators that have a greater zero probability
	 */
	public static EnumSet<Operators> getNonZeroOperators(EnumMap<Operators, Integer> probabilityMap) {
		EnumSet<Operators> availableOperators = EnumSet.noneOf(Operators.class);
		for (Operators operator : probabilityMap.keySet()) {
			if (probabilityMap.get(operator) > 0) {
				availableOperators.add(operator);
			}
		}
		return availableOperators;
	}

	/**
	 * Helper method to generate a prettier number within a given range. If no such number is found, a normal random number will be generated.
	 * @param range Range defining the bounds and exceptions.
	 * @param exceptions Custom exceptions which are not to be generated (especially the number 0). This is in addition to the exceptions the range already has so the range itself is not being modified.
	 * @return A pretty long, if possible.
	 */
	public static long getRandomPrettyLong(IIntegerRange range, Set<Long> exceptions) {
		Map<Long, Integer> choices = new HashMap<>();
		long value;
		for (long i : potentialValues.keySet()) {
		if (range.isValueAllowed(i) && !exceptions.contains(i)) choices.put(i, potentialValues.get(i));
			if (range.isValueAllowed(-i) && !exceptions.contains(-i)) choices.put(-i, potentialValues.get(i));
		}
		if (choices.size() > 0) value = getRandomElementFromMap(choices);
		else {
			do {
				value = randomLong(range.getLowerLimit(), range.getUpperLimit());
			} while (!range.isValueAllowed(value));
		}
		return value;
	}

	/**
	 * Helper method to generate a prettier floating point number within a given range. If no such number is found, a normal random number will be generated.
	 * @param range Range defining the bounds and exceptions.
	 * @param exceptions Custom exceptions which are not to be generated (especially the number 0). This is in addition to the exceptions the range already has so the range itself is not being modified.
	 * @return A pretty double, if possible.
	 */
	public static double getRandomPrettyDouble(IFloatingPointRange range, Set<Long> exceptions) {
		Map<Double, Integer> choices = new HashMap<>();
		double value;
		for (long i : potentialValues.keySet()) {
			for (long d : potentialFloatDivisors.keySet()) {
				double d_temp = i + (double) randomInt(1, (int) d-1) / d;
				if (range.isValueAllowed(d_temp)) choices.put(d_temp, potentialValues.get(i) * potentialFloatDivisors.get(d));
				d_temp = i - (double) randomInt(1, (int) d-1) / d;
				if (range.isValueAllowed(d_temp)) choices.put(d_temp, potentialValues.get(i) * potentialFloatDivisors.get(d));
			}
		}
		if (!exceptions.contains(0L)) choices.put(0.0d, potentialValues.get(0L) * 5);
		int test = choices.values().stream().reduce(0, Integer::sum);
		if (choices.size() > 0) value = getRandomElementFromMap(choices);
		else {
			do {
				value = randomDouble(range.getLowerLimit(), range.getUpperLimit());
			} while (!range.isValueAllowed(value));
		}
		return value;
	}
}
