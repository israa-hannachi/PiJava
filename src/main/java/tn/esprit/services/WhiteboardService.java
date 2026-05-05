package tn.esprit.services;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WhiteboardService {

    private static final int CANVAS_WIDTH  = 1000;
    private static final int CANVAS_HEIGHT = 650;

    /**
     * Exporte le snapshot du canvas en PNG.
     */
    public void exportToPNG(WritableImage image, File file) throws IOException {
        if (image == null || file == null) return;
        BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
        if (!ImageIO.write(bi, "png", file)) {
            throw new IOException("Impossible d'écrire le fichier PNG : " + file.getAbsolutePath());
        }
    }

    /**
     * Exporte un squelette SVG avec fond grille identique au bgCanvas (grille 30px, #F8FAFC).
     */
    public void exportToSVG(File file) throws IOException {
        if (file == null) return;

        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append("<svg width=\"").append(CANVAS_WIDTH)
           .append("\" height=\"").append(CANVAS_HEIGHT)
           .append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");

        // Fond blanc cassé identique au canvas JavaFX
        svg.append("  <rect width=\"").append(CANVAS_WIDTH)
           .append("\" height=\"").append(CANVAS_HEIGHT)
           .append("\" fill=\"#F8FAFC\"/>\n");

        // Grille 30px (comme drawGrid() dans le controller)
        svg.append("  <defs>\n");
        svg.append("    <pattern id=\"grid\" width=\"30\" height=\"30\" patternUnits=\"userSpaceOnUse\">\n");
        svg.append("      <path d=\"M 30 0 L 0 0 0 30\" fill=\"none\" stroke=\"rgba(180,180,200,0.25)\" stroke-width=\"0.5\"/>\n");
        svg.append("    </pattern>\n");
        svg.append("  </defs>\n");
        svg.append("  <rect width=\"").append(CANVAS_WIDTH)
           .append("\" height=\"").append(CANVAS_HEIGHT)
           .append("\" fill=\"url(#grid)\"/>\n");

        svg.append("  <!-- Contenu exporté -->\n");
        svg.append("</svg>");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(svg.toString().getBytes("UTF-8"));
        }
    }

    /**
     * Exporte le snapshot en ZIP contenant snapshot.png + whiteboard.svg.
     */
    public void exportToZIP(WritableImage image, File file) throws IOException {
        if (image == null || file == null) return;

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {

            // snapshot.png
            zos.putNextEntry(new ZipEntry("snapshot.png"));
            BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            zos.write(baos.toByteArray());
            zos.closeEntry();

            // whiteboard.svg (fond grille)
            zos.putNextEntry(new ZipEntry("whiteboard.svg"));
            StringBuilder svg = new StringBuilder();
            svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            svg.append("<svg width=\"").append(CANVAS_WIDTH)
               .append("\" height=\"").append(CANVAS_HEIGHT)
               .append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
            svg.append("  <rect width=\"").append(CANVAS_WIDTH)
               .append("\" height=\"").append(CANVAS_HEIGHT)
               .append("\" fill=\"#F8FAFC\"/>\n");
            svg.append("  <defs>\n");
            svg.append("    <pattern id=\"grid\" width=\"30\" height=\"30\" patternUnits=\"userSpaceOnUse\">\n");
            svg.append("      <path d=\"M 30 0 L 0 0 0 30\" fill=\"none\" stroke=\"rgba(180,180,200,0.25)\" stroke-width=\"0.5\"/>\n");
            svg.append("    </pattern>\n");
            svg.append("  </defs>\n");
            svg.append("  <rect width=\"").append(CANVAS_WIDTH)
               .append("\" height=\"").append(CANVAS_HEIGHT)
               .append("\" fill=\"url(#grid)\"/>\n");
            svg.append("</svg>");
            zos.write(svg.toString().getBytes("UTF-8"));
            zos.closeEntry();
        }
    }
}