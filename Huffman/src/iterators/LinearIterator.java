package iterators;

import java.io.InputStream;

public class LinearIterator implements java.util.Iterator<Integer> {
	InputStream input;
	int current;
	int size;

	public LinearIterator(InputStream i, int size) {
		current = 1;
		input = i;
		this.size = size;
	}

	public boolean hasNext() {
		return current <= size;
	}

	public Integer next() {
		try {
			int next = input.read();
			current += 1;
			if (next < 0)
				System.out.println(next);
			return Integer.valueOf(next);
		} catch (java.io.IOException e) {
		}
		return null;
	}
}
