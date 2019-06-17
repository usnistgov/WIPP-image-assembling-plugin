package gov.nist.itl.ssd.wipp.imageassemblyplugin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


import gov.nist.isg.pyramidio.stitching.MistStitchedImageReader;
import utils.TiledOmeTiffConverter;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.codec.CompressionType;
import loci.formats.gui.BufferedImageWriter;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;
import ome.xml.model.primitives.PositiveInteger;

public class ImageAssembling {
	
	// Stitching vector naming conventions
	private static final String STITCHING_VECTOR_FILENAME_PREFIX = "img-global-positions-";
	private static final String STITCHING_VECTOR_FILENAME_SUFFIX = ".txt";
	
	private static final int TILE_SIZE = 1024;

	private final File tilesFolder;
	private final File stitchingVectorFolder;
	private final File outputFolder;
	
    private static final Logger LOG = Logger.getLogger(
    		ImageAssembling.class.getName());


	public ImageAssembling(File tilesFolder, File stitchingVectorFolder, File outputFolder) {
		this.tilesFolder = tilesFolder;
		this.stitchingVectorFolder = stitchingVectorFolder;
		this.outputFolder = outputFolder;
	}
	
	public Integer run() throws Exception {
		
        File[] timeSlices = stitchingVectorFolder.listFiles((dir, fn)
                -> fn.startsWith(STITCHING_VECTOR_FILENAME_PREFIX)
                && fn.endsWith(STITCHING_VECTOR_FILENAME_SUFFIX));

        outputFolder.mkdirs();
        int timeSlicesBuilt = 0;
        for (File timeSlice : timeSlices) {
            String timeSliceStr = timeSlice.getName()
                    .replace(STITCHING_VECTOR_FILENAME_PREFIX, "")
                    .replace(STITCHING_VECTOR_FILENAME_SUFFIX, "");

            MistStitchedImageReader mistImageReader;
            try {
            	mistImageReader = new MistStitchedImageReader(timeSlice, tilesFolder);
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Ignoring unreadable time slice "
                        + timeSliceStr, ex);
                continue;
            }

            BufferedImage image = mistImageReader.read();                      
            OMEXMLMetadata metadata = getMetadata(mistImageReader);
            
            File tempOutputFile = new File(outputFolder, "temp.ome.tif");
            File outputFile = new File(outputFolder,
                    "image_" + timeSliceStr + ".ome.tif");

            try (BufferedImageWriter imageWriter = new BufferedImageWriter()) {
            	imageWriter.setMetadataRetrieve(metadata);
            	imageWriter.setId(tempOutputFile.getPath());
            	imageWriter.setCompression(CompressionType.LZW.getCompression());
            	imageWriter.saveImage(0, image);
            } catch (FormatException | IOException ex) {
            	throw new RuntimeException("No image writer found for file "
            			+ outputFile, ex);
            }
            
    		TiledOmeTiffConverter tiledOmeTiffConverter = new TiledOmeTiffConverter(tempOutputFile.getPath(), outputFile.toString(), TILE_SIZE, TILE_SIZE);
    		tiledOmeTiffConverter.init();
    		tiledOmeTiffConverter.readWriteTiles();
    		tiledOmeTiffConverter.cleanup();
    		
    		tempOutputFile.delete();
            timeSlicesBuilt++;
        }
        if (timeSlicesBuilt == 0) {
        	throw new RuntimeException("No time slice found.");
        }

        return timeSlicesBuilt;
		
	}
	
    private OMEXMLMetadata getMetadata(MistStitchedImageReader mistImageReader) {
        File exampleTile = mistImageReader.getTiles().get(0).getFile();
        OMEXMLMetadata metadata;
        try {
            OMEXMLService omeXmlService = new ServiceFactory().getInstance(
                    OMEXMLService.class);
            metadata = omeXmlService.createOMEXMLMetadata();
        } catch (DependencyException ex) {
            throw new RuntimeException("Cannot find OMEXMLService", ex);
        } catch (ServiceException ex) {
            throw new RuntimeException("Cannot create OME metadata", ex);
        }
        try (ImageReader imageReader = new ImageReader()) {
            IFormatReader reader;
            reader = imageReader.getReader(exampleTile.getPath());
            reader.setOriginalMetadataPopulated(false);
            reader.setMetadataStore(metadata);
            reader.setId(exampleTile.getPath());
        } catch (FormatException | IOException ex) {
            throw new RuntimeException("No image reader found for file "
                    + exampleTile, ex);
        }

        metadata.setPixelsSizeX(
                new PositiveInteger(mistImageReader.getWidth()), 0);
        metadata.setPixelsSizeY(
                new PositiveInteger(mistImageReader.getHeight()), 0);

        return metadata;
    }

}
