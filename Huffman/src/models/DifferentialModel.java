package models;

import java.io.InputStream;

public class DifferentialModel implements SourceModel {
	int[][][] differentialVideo;
	DiffSymbolModel[] symbols;

	public DifferentialModel() {
		symbols = new DiffSymbolModel[256];
		for (int i = 0; i < 256; i++) {
			symbols[i] = new DiffSymbolModel(i, 1, this);
		}
	}

	public int getSymbolCount() {
		return 256;
	}

	public SymbolModel getByIndex(int i) {
		return symbols[i];
	}

	public int getCountTotal() {
		return differentialVideo.length * differentialVideo[0].length * differentialVideo[0][0].length + 256;
	}

	public void train(InputStream src, int inputWidth, int inputHeight, long totalLength) throws java.io.IOException {
		long numFrames = totalLength / (inputWidth * inputHeight);
		int[][][] originalVideo = new int[(int) numFrames][inputHeight][inputWidth];
		differentialVideo = new int[(int) numFrames][inputHeight][inputWidth];
		for (int f = 0; f < numFrames; f++) {
			for (int y = 0; y < inputHeight; y++) {
				for (int x = 0; x < inputWidth; x++) {
					originalVideo[f][y][x] = src.read();
					if (f < 1) {
						differentialVideo[f][y][x] = originalVideo[f][y][x];
					} else {
						int diff = originalVideo[f][y][x] - originalVideo[(f - 1)][y][x];
						if (diff < 0)
							diff += 256;
						differentialVideo[f][y][x] = diff;
					}
					symbols[differentialVideo[f][y][x]].incrementCount();
				}
			}
		}
	}

	public static class DiffSymbol implements Symbol {
		private int _value;

		public DiffSymbol(int value) {
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
			if (!(o instanceof DiffSymbol)) {
				throw new IllegalArgumentException("Unsigned8BitSymbol only comparable to type of same");
			}
			DiffSymbol other = (DiffSymbol) o;
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

	public static class DiffSymbolModel implements SymbolModel {
		private DiffSymbol _symbol;
		private long _count;
		private DifferentialModel _model;

		public DiffSymbolModel(int value, long init_count, DifferentialModel model) {
			_symbol = new DiffSymbol(value);
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
