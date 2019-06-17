package gov.nist.itl.ssd.wipp.imageassemblyplugin;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
	private static final Logger LOG = Logger.getLogger(
			Main.class.getName());

	public static void main(String[] args) {

		Options options = new Options();

		Option helpOption = new Option("h", "help", false,
				"Display this help message and exit.");
		options.addOption(helpOption);

		Option inputOption = new Option("i", "inputImages", true,
				"Input images folder - images to convert to dzi.");
		inputOption.setRequired(true);
		options.addOption(inputOption);

		Option inputSvOption = new Option("v", "inputStitchingVectors", true,
				"Input stitching vectors folder.");
		inputSvOption.setRequired(true);
		options.addOption(inputSvOption);

		Option outputOption = new Option("o", "output", true,
				"Output folder or file where the output will be generated.");
		outputOption.setRequired(true);
		options.addOption(outputOption);

//		Option tileSizeOption = new Option("ts", "tileSize", true,
//				"Tile size (default 1024).");
//		tileSizeOption.setType(PatternOptionBuilder.NUMBER_VALUE);
//		options.addOption(tileSizeOption);
//
//		Option blendingOption = new Option("b", "blending", true,
//				"Blending method when assembling tiles, options are max or overlay.");
//		blendingOption.setRequired(true);
//		options.addOption(blendingOption);


		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine commandLine = parser.parse(options, args);

			if (commandLine.hasOption(helpOption.getOpt())) {
				printHelp(options);
				return;
			}

			File inputImages = new File(
					commandLine.getOptionValue(inputOption.getOpt()));

			File inputStitchingVector = new File(
					commandLine.getOptionValue(inputSvOption.getOpt()));

			File outputFolder = new File(
					commandLine.getOptionValue(outputOption.getOpt()));

//			Number tileSizeNumber = (Number) commandLine.getParsedOptionValue(
//					tileSizeOption.getOpt());
//			int tileSize = tileSizeNumber == null
//					? 1024 : tileSizeNumber.intValue();
//
//			String blendingValue = commandLine.getOptionValue(
//					blendingOption.getOpt());
//			String blending = blendingValue == null
//					? "overlay" : blendingValue;

			try {
				long start = System.currentTimeMillis();

				ImageAssembling ia = new ImageAssembling(inputImages, inputStitchingVector, outputFolder);	                
				ia.run();

				float duration = (System.currentTimeMillis() - start) / 1000F;
				LOG.info("Images assembled in " + duration + "s.");
			} catch (Exception ex) {
				LOG.severe("Error while assembling the image.");
				ex.printStackTrace();
			}     
		} catch (ParseException ex) {
			LOG.severe(ex.getMessage());
			printHelp(options);
			return;
		}
	}

	/**
	 * Print help
	 * @param options
	 */
	private static void printHelp(Options options) {
		new HelpFormatter().printHelp("wipp-image-assembling", options);
	}
}
