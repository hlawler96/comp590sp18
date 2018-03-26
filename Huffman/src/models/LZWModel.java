
package models;

import dictionaries.Dictionary;
import models.Unsigned8BitModel.Unsigned8BitSymbol;

public class LZWModel implements SourceModel {
	private Dictionary dict;
	private LZWSymbolModel[] symbols;

	public LZWModel(Dictionary dict) {
		this.dict = dict;
		symbols = new LZWSymbolModel[dict.size()];
		for (int i = 0; i < dict.size(); i++) {
			int count = dict.getFrequency(dict.getEntry(i));
			symbols[i] = new LZWSymbolModel(i, count, this);
		}
	}

	public int getCountTotal() {
		return dict.getCountTotal();
	}

	public int getSymbolCount() {
		return symbols.length;
	}

	public SymbolModel getByIndex(int i) {
		return symbols[i];
	}

	public static class LZWSymbol implements Symbol {
		private int _value;

		public LZWSymbol(int value) {
			_value = value;
		}

		public int getValue() {
			return _value;
		}

		@Override
		public int compareTo(Symbol o) {
			if (!(o instanceof LZWSymbol)) {
				throw new IllegalArgumentException("LZWSymbol only comparable to type of same");
			}
			LZWSymbol other = (LZWSymbol) o;
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

	public static class LZWSymbolModel implements SymbolModel {
		private LZWSymbol _symbol;
		private long _count;
		private LZWModel _model;

		public LZWSymbolModel(int value, long init_count, LZWModel model) {
			_symbol = new LZWSymbol(value);
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
