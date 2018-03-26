package apps;

import codec.HuffmanEncoder;
import dictionaries.Dictionary;
import io.BitSink;
import io.OutputStreamBitSink;
import iterators.DifferenceIterator;
import iterators.LinearIterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import models.LZWModel;
import models.LZWModel.LZWSymbol;
import models.Symbol;
import models.SymbolModel;

public class LZWEncoderApp {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		int DICT_SIZE = 65536;
		String filename = "/Users/haydenlawler/documents/590_Videos/pinwheel.450p.yuv";
		System.out.println("Now Encoding File: " + filename + " at " + LocalDateTime.now());
//		Iterator<Integer> iterator = diffSetup(filename);
		Iterator<Integer> iterator = linearSetup(filename);

		int width = 0;
		int height = 0;
		if (filename.substring(filename.length() - 8).equals("450p.yuv")) {
			width = 800;
			height = 450;
		} else {
			width = 1280;
			height = 720;
		}

		int[] initialValues = new int[256];
		for (int k = 0; k < initialValues.length; k++) {
			initialValues[k] = k;
		}

		Dictionary dict = new Dictionary(initialValues);
		ArrayList<Integer> encoding = new ArrayList<Integer>();

		String temp = "" + iterator.next();
		while (iterator.hasNext()) {
			int next = ((Integer) iterator.next()).intValue();
			if (dict.contains(temp + "," + next)) {
				temp = temp + "," + next;
			} else {
				encoding.add(Integer.valueOf(dict.getIndexOf(temp)));
				dict.increaseCount(temp);
				if (dict.size() <= 65536)
					dict.insert(temp + "," + next);
				temp = "" + next;
			}
		}
		encoding.add(Integer.valueOf(dict.getIndexOf(temp)));

		LZWModel model = new LZWModel(dict);
		Symbol[] symbols = new LZWModel.LZWSymbol[dict.size()];
		HuffmanEncoder encoder = new HuffmanEncoder(model, model.getCountTotal());
		Map<Symbol, String> code_map = encoder.getCodeMap();

		double bitCount = 0.0D;
		for (int v = 0; v < model.getSymbolCount(); v++) {
			SymbolModel s = model.getByIndex(v);
			Symbol sym = s.getSymbol();
			symbols[v] = sym;
			long prob = s.getProbability(model.getCountTotal());
			String huffmanCode = (String) code_map.get(sym);
			bitCount += huffmanCode.length() * prob;
		}

		double ratio = bitCount / (width * height * 150 * 8);
		System.out.println("Encoded with compression ratio of " + ratio);

		File out_file = new File("/Users/haydenlawler/documents/590_Videos/output.dat");
		OutputStream out_stream = new FileOutputStream(out_file);
		BitSink bit_sink = new OutputStreamBitSink(out_stream);

		for (Integer next : encoding) {
			encoder.encode(model.getByIndex(next.intValue()).getSymbol(), bit_sink);
		}

		bit_sink.padToWord();
		out_stream.close();

		int encodingCount = 0;
		int[][][] decodedVideo = new int[''][height][width];
		for (int frame = 0; frame < 150; frame++) {
			for (int y = 0; y < height; y++) {
				if (frame >= 150)
					break;
				for (int x = 0; x < width; x++) {
					String[] next = dict.getEntry(((Integer) encoding.get(encodingCount)).intValue()).split(",");
					for (int l = 0; l < next.length; l++) {
						String number = next[l];
						int num = Integer.parseInt(number);
						//Comment this line out for Linear Encoding
//						if(frame > 0) num = decodedVideo[frame-1][y][x] + num;
						decodedVideo[frame][y][x] = num;

						if (l != next.length - 1) {
							x++;
							if (x >= width) {
								x = 0;
								y++;
								if (y >= height) {
									y = 0;
									frame++;
									if (frame >= 150) {
										break;
									}
								}
							}
						}
					}
					encodingCount++;
				}
			}
		}
		File file = new File(filename);
		InputStream input = new FileInputStream(file);
		Iterator<Integer> check_iterator = new LinearIterator(input, width * height * 150);

		for (int frame = 0; frame < 150; frame++) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int expected = ((Integer) check_iterator.next()).intValue();
					if (decodedVideo[frame][y][x] != expected) {
						System.out.println("Difference found at (" + frame + "," + y + "," + x + ") : Expected: "
								+ expected + " Actual: " + decodedVideo[frame][y][x]);
					}
				}
			}
		}

		System.out.println("Finished Check");
	}

	public static Iterator<Integer> linearSetup(String filename) throws FileNotFoundException, IOException {
		int width = 0;
		int height = 0;
		if (filename.substring(filename.length() - 8).equals("450p.yuv")) {
			width = 800;
			height = 450;
		} else {
			width = 1280;
			height = 720;
		}

		File file = new File(filename);
		InputStream training_values = new FileInputStream(file);
		return new LinearIterator(training_values, width * height * 150);
	}

	public static Iterator<Integer> diffSetup(String filename) throws FileNotFoundException, IOException {
		int width = 0;
		int height = 0;
		if (filename.substring(filename.length() - 8).equals("450p.yuv")) {
			width = 800;
			height = 450;
		} else {
			width = 1280;
			height = 720;
		}

		File file = new File(filename);
		InputStream training_values = new FileInputStream(file);

		return new DifferenceIterator(training_values, width, height);
	}
}
