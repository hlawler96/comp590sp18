package models;

import java.io.IOException;
import java.io.InputStream;

import models.LZWModel.LZWSymbol;
import models.Unsigned8BitModel.Unsigned8BitSymbol;

public class MaxDifferentialModel implements SourceModel {
	int[][][] maxDifferentialVideo;
	MaxDiffSymbolModel[] symbols;

	public MaxDifferentialModel() {
		symbols = new MaxDiffSymbolModel[256];
		for (int i = 0; i < 256; i++) {
			symbols[i] = new MaxDiffSymbolModel( i, 1, this);
		}
	}

	public int getSymbolCount() {
		return 256;
	}

	public SymbolModel getByIndex(int i) {
		return symbols[i];
	}

	public int getCountTotal() {
		return maxDifferentialVideo.length * maxDifferentialVideo[0].length * maxDifferentialVideo[0][0].length + 256;
	}

	public void train(InputStream src, int inputWidth, int inputHeight, long totalLength) throws IOException {
		long numFrames = totalLength / (inputWidth * inputHeight);
		int[][][] originalVideo = new int[(int) numFrames][inputHeight][inputWidth];
		maxDifferentialVideo = new int[(int) numFrames][inputHeight][inputWidth];
		for (int f = 0; f < numFrames; f++) {
			for (int y = 0; y < inputHeight; y++) {
				for (int x = 0; x < inputWidth; x++) {
					originalVideo[f][y][x] = src.read();
					if (f < 2) {
						maxDifferentialVideo[f][y][x] = originalVideo[f][y][x];
					} else {
						int[] diffs = new int[8];
						if ((x > 0) && (y > 0))
							diffs[0] = (originalVideo[(f - 1)][(y - 1)][(x - 1)]
									- originalVideo[(f - 2)][(y - 1)][(x - 1)]);
						if (y > 0)
							diffs[1] = (originalVideo[(f - 1)][(y - 1)][x] - originalVideo[(f - 2)][(y - 1)][x]);
						if ((x < inputWidth - 1) && (y > 0))
							diffs[2] = (originalVideo[(f - 1)][(y - 1)][(x + 1)]
									- originalVideo[(f - 2)][(y - 1)][(x + 1)]);
						if (x > 0)
							diffs[3] = (originalVideo[(f - 1)][y][(x - 1)] - originalVideo[(f - 2)][y][(x - 1)]);
						if (x < inputWidth - 1)
							diffs[4] = (originalVideo[(f - 1)][y][(x + 1)] - originalVideo[(f - 2)][y][(x + 1)]);
						if ((x > 0) && (y < inputHeight - 1))
							diffs[5] = (originalVideo[(f - 1)][(y + 1)][(x - 1)]
									- originalVideo[(f - 2)][(y + 1)][(x - 1)]);
						if (y < inputHeight - 1)
							diffs[6] = (originalVideo[(f - 1)][(y + 1)][x] - originalVideo[(f - 2)][(y + 1)][x]);
						if ((y < inputHeight - 1) && (x < inputWidth - 1))
							diffs[7] = (originalVideo[(f - 1)][(y + 1)][(x + 1)]
									- originalVideo[(f - 2)][(y + 1)][(x + 1)]);
						int max = 0;
						for (int diff : diffs) {
							if (diff < 0) {
								diff += 256;
							}
							if (diff > max) {
								max = diff;
							}
						}

						int actualDiff = originalVideo[f][y][x] - originalVideo[(f - 1)][y][x];
						if (actualDiff < 0)
							actualDiff += 256;
						int diffOfDiffs = actualDiff - max;
						if (diffOfDiffs < 0)
							diffOfDiffs += 256;
						maxDifferentialVideo[f][y][x] = diffOfDiffs;
					}
					symbols[maxDifferentialVideo[f][y][x]].incrementCount();
				}
			}
		}
	}
	
	public static class MaxDiffSymbol implements Symbol {
		private int _value;

		public MaxDiffSymbol(int value) {
			if (value < 0 || value > 255) {
				throw new IllegalArgumentException("Value out of range");
			}
			_value = value;
		}

		public int getValue() {
			return _value;
		}

		@Override
		public int compareTo(Symbol o) {
			if (!(o instanceof MaxDiffSymbol)) {
				throw new IllegalArgumentException("MaxDiffSymbol only comparable to type of same");
			}
			MaxDiffSymbol other = (MaxDiffSymbol) o;
			if (other.getValue() > getValue()) {
				return -1;
			} else if (other.getValue() < getValue()) {
				return 1;
			} else {
				return 0;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Symbol)) {
				return false;
			}
			return (compareTo((Symbol) o) == 0);
		}

		@Override
		public int hashCode() {
			return getValue();
		}

		@Override
		public String toString() {
			return "" + getValue();

		}
	}

	public static class MaxDiffSymbolModel implements SymbolModel {
		private MaxDiffSymbol _symbol;
		private long _count;
		private MaxDifferentialModel _model;

		public MaxDiffSymbolModel(int value, long init_count, MaxDifferentialModel model) {
			_symbol = new MaxDiffSymbol(value);
			_count = init_count;
			_model = model;
		}

		public void incrementCount() {
			_count++;
		}

		@Override
		public long getProbability(long precision) {
			return _count * precision / _model.getCountTotal();
		}

		@Override
		public Symbol getSymbol() {
			return _symbol;
		}

		public long getCount() {
			return _count;
		}
	}
	
}
