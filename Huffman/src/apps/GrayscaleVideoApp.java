package apps;

import codec.HuffmanEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

import models.DifferentialModel;
import models.DifferentialModel.DiffSymbol;
import models.MaxDifferentialModel;
import models.MaxDifferentialModel.MaxDiffSymbol;
import models.Symbol;
import models.SymbolModel;

public class GrayscaleVideoApp {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String[] files = new String[1];
		files[0] = "/Users/haydenlawler/documents/590_Videos/pinwheel.450p.yuv";
//		files[1] = "/Users/haydenlawler/documents/590_Videos/pinwheel.720p.yuv";
//		files[2] = "/Users/haydenlawler/documents/590_Videos/bunny.450p.yuv";
//		files[3] = "/Users/haydenlawler/documents/590_Videos/bunny.720p.yuv";
//		files[4] = "/Users/haydenlawler/documents/590_Videos/tractor.450p.yuv";
//		files[5] = "/Users/haydenlawler/documents/590_Videos/tractor.720p.yuv";
//		files[6] = "/Users/haydenlawler/documents/590_Videos/candle.450p.yuv";
//		files[7] = "/Users/haydenlawler/documents/590_Videos/candle.720p.yuv";
//		files[8] = "/Users/haydenlawler/documents/590_Videos/jellyfish.450p.yuv";
//		files[9] = "/Users/haydenlawler/documents/590_Videos/jellyfish.720p.yuv";
		for (String filename : files) {
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
			long length = file.length();
			InputStream training_values = new FileInputStream(file);

//			MaxDifferentialModel model = new MaxDifferentialModel();
//			Symbol[] symbols = new MaxDiffSymbol[256];
//			String type = "MaxDiff";
			
			DifferentialModel model = new DifferentialModel();
			Symbol[] symbols = new DiffSymbol[256];
			String type = "Diff";
			

			model.train(training_values, width, height, length);
			training_values.close();

			HuffmanEncoder encoder = new HuffmanEncoder(model, model.getCountTotal());
			Map<Symbol, String> code_map = encoder.getCodeMap();

			double bitCount = 0.0D;
			for (int v = 0; v < 256; v++) {
				SymbolModel s = model.getByIndex(v);
				Symbol sym = s.getSymbol();
				symbols[v] = sym;

				long prob = s.getProbability(model.getCountTotal());
				String huffmanCode = (String) code_map.get(sym);
				bitCount += huffmanCode.length() * prob;
			}

			double ratio = bitCount / (model.getCountTotal() * 8.0D);
			System.out.println("Total Compression Ratio: " + ratio + "for file " + filename);
		}
	}
}
