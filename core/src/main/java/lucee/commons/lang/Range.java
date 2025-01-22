package lucee.commons.lang;

public class Range {
	private final int min;
	private final int max;

	public Range(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("min (" + min + ") cannot be greater than max (" + max + ")");
		}
		this.min = min;
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	@Override
	public String toString() {
		return min + ":" + max;
	}
}