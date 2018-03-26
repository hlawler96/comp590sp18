package iterators;

import java.io.InputStream;

public class DifferenceIterator implements java.util.Iterator<Integer> {
	public int[][][] pixels;
	int width;
	int height;
	int frame;
	int y;
	int x;

	public DifferenceIterator(InputStream input, int width, int height) {
		pixels = new int[''][height][width];
		this.width = width;
		this.height = height;
		for (int frame = 0; frame < pixels.length; frame++) {
			for (int y = 0; y < pixels[0].length; y++) {
				for (int x = 0; x < pixels[0][0].length; x++) {
					int next = -1;
					try {
						next = input.read();
					} catch (java.io.IOException e) {
						e.printStackTrace();
					}
					if (frame > 0) {
						pixels[frame][y][x] = (next - pixels[(frame - 1)][y][x]);
						if (pixels[frame][y][x] < 0)
							pixels[frame][y][x] += 256;
					} else {
						pixels[frame][y][x] = next;
					}
				}
			}
		}
	}

	public boolean hasNext() {
		return y < height;
	}

	public Integer next() {
		int next = pixels[frame][y][x];
		frame += 1;
		if (frame >= 150) {
			frame = 0;
			x += 1;
			if (x >= width) {
				y += 1;
				x = 0;
			}
		}
		return Integer.valueOf(next);
	}
}
