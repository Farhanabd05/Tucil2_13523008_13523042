import java.awt.image.*;
import java.io.*;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;

public class GifSequenceWriter {
    protected ImageWriter gifWriter;
    protected ImageWriteParam imageWriteParam;
    protected IIOMetadata imageMetaData;

    /**
     * Create a GIF sequence writer
     * 
     * @param outputStream the ImageOutputStream to which the images will be written
     * @param imageType the type of image to create (BufferedImage.TYPE_INT_RGB, etc)
     * @param loopContinuously if true, the GIF will loop forever
     * @throws IOException if no GIF ImageWriters are found or an error occurs
     */
    public GifSequenceWriter(ImageOutputStream outputStream, int imageType, boolean loopContinuously) throws IOException {
        // Get GIF writer
        gifWriter = getWriter();
        imageWriteParam = gifWriter.getDefaultWriteParam();

        // Get default metadata
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);
        imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

        // Configure metadata
        String metaFormatName = imageMetaData.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

        // Set GIF as output
        gifWriter.setOutput(outputStream);
        gifWriter.prepareWriteSequence(null);
    }

    /**
     * Write a frame to the GIF sequence
     * 
     * @param img the image to write
     * @param frameDelayMs delay in milliseconds
     * @throws IOException if an error occurs during writing
     */
    public void writeToSequence(BufferedImage img, int frameDelayMs) throws IOException {
        // Prepare metadata for this frame
        String metaFormatName = imageMetaData.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

        // Set frame delay
        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(frameDelayMs / 10));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        // Set loop parameters
        IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");

        int loop = true ? 0 : 1; // Loop forever
        child.setUserObject(new byte[]{0x1, (byte)(loop & 0xFF), (byte)((loop >> 8) & 0xFF)});
        appExtensionsNode.appendChild(child);
        
        imageMetaData.setFromTree(metaFormatName, root);
        
        // Write frame
        gifWriter.writeToSequence(new IIOImage(img, null, imageMetaData), imageWriteParam);
    }

    /**
     * Close the GIF writer
     * 
     * @throws IOException if an error occurs during closing
     */
    public void close() throws IOException {
        gifWriter.endWriteSequence();
    }

    /**
     * Get a GIF ImageWriter
     * 
     * @return an ImageWriter for GIF format
     * @throws IOException if no GIF ImageWriter is found
     */
    private static ImageWriter getWriter() throws IOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
        if (!iter.hasNext()) {
            throw new IOException("No GIF Image Writers Found");
        }
        return iter.next();
    }

    /**
     * Get a node from the metadata node tree
     * 
     * @param rootNode the root node of the metadata
     * @param nodeName the name of the node to find or create
     * @return the found or created node
     */
    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }
}
