package dictionaries;

import java.util.ArrayList;
import java.util.HashMap;

public class Dictionary {
	private java.util.ArrayList<String> symbols;
	private HashMap<String, Integer> symbolsMap;
	private HashMap<String, Integer> symbolIndex;
	private int countTotal;

	public Dictionary(int[] initialValues) {
		symbols = new ArrayList<String>();
		symbolsMap = new HashMap<String, Integer>();
		symbolIndex = new HashMap<String, Integer>();
		for (int i = 0; i < initialValues.length; i++) {
			insert("" + initialValues[i]);
		}
		countTotal = 0;
	}

	public String getEntry(int index) {
		return (String) symbols.get(index);
	}

	public boolean contains(String entry) {
		return symbolIndex.get(entry) != null;
	}

	public int insert(String entry) {
		symbols.add(entry);
		symbolsMap.put(entry, Integer.valueOf(0));
		int index = symbols.size() - 1;
		symbolIndex.put(entry, Integer.valueOf(index));

		return index;
	}

	public int getIndexOf(String entry) {
		Integer index = (Integer) symbolIndex.get(entry);
		if (index == null) {
			System.out.println("Entry: " + entry + " , Contained in symbols?: " + symbols.contains(entry));
		}
		return ((Integer) symbolIndex.get(entry)).intValue();
	}

	public int size() {
		return symbols.size();
	}

	public void increaseCount(String entry) {
		symbolsMap.put(entry, Integer.valueOf(((Integer) symbolsMap.get(entry)).intValue() + 1));
		countTotal += 1;
	}

	public int getFrequency(String entry) {
		return ((Integer) symbolsMap.get(entry)).intValue();
	}

	public int getCountTotal() {
		return countTotal;
	}
}
