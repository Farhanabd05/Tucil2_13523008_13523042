import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

public class GifSequenceWriter {
    protected ImageWriter gifWriter;
    protected ImageWriteParam imageWriteParam;
    protected IIOMetadata imageMetaData;

    /**
     * Membuat objek GifSequenceWriter.
     * 
     * @param outputStream        ImageOutputStream ke file output GIF
     * @param imageType           Tipe gambar (misal BufferedImage.TYPE_INT_RGB)
     * @param timeBetweenFramesMS Delay antar frame (dalam milidetik) untuk default
     * @param loopContinuously    Jika true, GIF akan di-loop terus-menerus
     * @throws IOException        Jika tidak ada writer GIF yang tersedia atau terjadi error lainnya
     */
    public GifSequenceWriter(ImageOutputStream outputStream, int imageType, int timeBetweenFramesMS, boolean loopContinuously) throws IOException {
        // Ambil GIF writer
        gifWriter = getWriter();
        imageWriteParam = gifWriter.getDefaultWriteParam();

        // Dapatkan metadata default
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);
        imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

        // Konfigurasi metadata awal untuk GIF
        String metaFormatName = imageMetaData.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

        // Set Graphic Control Extension (untuk delay dan disposal method)
        IIOMetadataNode gceNode = getNode(root, "GraphicControlExtension");
        gceNode.setAttribute("disposalMethod", "none");
        gceNode.setAttribute("userInputFlag", "FALSE");
        gceNode.setAttribute("transparentColorFlag", "FALSE");
        // Delay default dalam satuan 1/100 detik
        gceNode.setAttribute("delayTime", Integer.toString(timeBetweenFramesMS / 10));
        gceNode.setAttribute("transparentColorIndex", "0");

        // Set Application Extension untuk looping
        IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
        IIOMetadataNode appNode = new IIOMetadataNode("ApplicationExtension");
        appNode.setAttribute("applicationID", "NETSCAPE");
        appNode.setAttribute("authenticationCode", "2.0");
        // Jika loopContinuously true, loop=0 (tak terbatas)
        int loop = loopContinuously ? 0 : 1;
        appNode.setUserObject(new byte[] { 0x1, (byte)(loop & 0xFF), (byte)((loop >> 8) & 0xFF) });
        appExtensionsNode.appendChild(appNode);

        // Perbarui metadata dari tree
        imageMetaData.setFromTree(metaFormatName, root);

        // Siapkan writer
        gifWriter.setOutput(outputStream);
        gifWriter.prepareWriteSequence(null);
    }

    /**
     * Menulis satu frame ke sequence GIF dengan delay yang diberikan.
     * 
     * @param img           BufferedImage frame yang akan ditulis
     * @param frameDelayMS  Delay frame dalam milidetik
     * @throws IOException  Jika terjadi error penulisan
     */
    public void writeToSequence(BufferedImage img, int frameDelayMS) throws IOException {
        // Perbarui metadata untuk frame ini
        String metaFormatName = imageMetaData.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);
        IIOMetadataNode gceNode = getNode(root, "GraphicControlExtension");
        gceNode.setAttribute("delayTime", Integer.toString(frameDelayMS / 10));
        imageMetaData.setFromTree(metaFormatName, root);

        // Tulis frame ke sequence
        IIOImage frameImage = new IIOImage(img, null, imageMetaData);
        gifWriter.writeToSequence(frameImage, imageWriteParam);
    }

    /**
     * Menutup sequence GIF.
     * 
     * @throws IOException jika terjadi error saat menutup writer
     */
    public void close() throws IOException {
        gifWriter.endWriteSequence();
    }

    /**
     * Mengembalikan ImageWriter yang mendukung format GIF.
     * 
     * @return ImageWriter untuk GIF
     * @throws IOException Jika tidak ditemukan writer yang mendukung GIF
     */
    private static ImageWriter getWriter() throws IOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
        if (!iter.hasNext()) {
            throw new IOException("No GIF Image Writers Found");
        }
        return iter.next();
    }

    /**
     * Mengembalikan node metadata dengan nama tertentu dari root node. Jika tidak
     * ada, node akan dibuat dan ditambahkan ke root.
     * 
     * @param rootNode root node metadata
     * @param nodeName nama node yang dicari
     * @return node metadata yang ditemukan atau dibuat baru
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
