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

    public GifSequenceWriter(ImageOutputStream outputStream, int imageType, boolean loopContinuously) throws IOException {
        gifWriter = getWriter();
        imageWriteParam = gifWriter.getDefaultWriteParam();

        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);
        imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

        String metaFormatName = imageMetaData.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

        gifWriter.setOutput(outputStream);
        gifWriter.prepareWriteSequence(null);
    }

    public void writeToSequence(BufferedImage img, int frameDelayMs) throws IOException {
        String metaFormatName = imageMetaData.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(frameDelayMs / 10));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");

        int loop = true ? 0 : 1;
        child.setUserObject(new byte[]{0x1, (byte)(loop & 0xFF), (byte)((loop >> 8) & 0xFF)});
        appExtensionsNode.appendChild(child);
        
        imageMetaData.setFromTree(metaFormatName, root);
        
        gifWriter.writeToSequence(new IIOImage(img, null, imageMetaData), imageWriteParam);
    }

    public void close() throws IOException {
        gifWriter.endWriteSequence();
    }

    private static ImageWriter getWriter() throws IOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
        if (!iter.hasNext()) {
            throw new IOException("No GIF Image Writers Found");
        }
        return iter.next();
    }

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
