package tn.esprit.services.cours;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import tn.esprit.entities.cours.Cours;
import tn.esprit.entities.cours.Cours_Categorie;
import tn.esprit.entities.cours.Cours_Module;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service d'export PDF du catalogue de cours.
 * Utilise iText 5.
 */
public class PdfExportService {

    // Couleurs charte Naja7ni
    private static final BaseColor COLOR_PRIMARY   = new BaseColor(15,  181, 169);   // #0FB5A9
    private static final BaseColor COLOR_DARK      = new BaseColor(30,  41,  59);    // #1e293b
    private static final BaseColor COLOR_LIGHT_BG  = new BaseColor(240, 255, 254);   // #F0FFFE
    private static final BaseColor COLOR_GRAY      = new BaseColor(100, 116, 139);   // #64748b
    private static final BaseColor COLOR_WHITE     = BaseColor.WHITE;
    private static final BaseColor COLOR_GREEN     = new BaseColor(22,  163, 74);
    private static final BaseColor COLOR_BLUE      = new BaseColor(37,  99,  235);
    private static final BaseColor COLOR_RED       = new BaseColor(220, 38,  38);

    private static final Font FONT_TITLE     = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD,   COLOR_WHITE);
    private static final Font FONT_SUBTITLE  = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, COLOR_WHITE);
    private static final Font FONT_SECTION   = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD,   COLOR_DARK);
    private static final Font FONT_BODY      = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, COLOR_DARK);
    private static final Font FONT_SMALL     = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, COLOR_GRAY);
    private static final Font FONT_BOLD      = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   COLOR_DARK);
    private static final Font FONT_TABLE_HDR = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   COLOR_WHITE);

    // ──────────────────────────────────────────────────────────────────────────────

    /**
     * Exporte le catalogue complet : Catégories → Modules → Cours
     *
     * @param destFile  fichier de sortie (.pdf)
     * @param categories liste de toutes les catégories
     * @param modules    liste de tous les modules
     * @param cours      liste de tous les cours
     * @param moduleMap  map moduleId → titre module
     * @param catMap     map categorieId → titre catégorie
     * @throws DocumentException, IOException
     */
    public static void exportCatalogue(
            File destFile,
            List<Cours_Categorie> categories,
            List<Cours_Module>    modules,
            List<Cours>           cours,
            Map<Integer, String>  moduleMap,
            Map<Integer, String>  catMap
    ) throws DocumentException, IOException {

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(destFile));

        // En-tête & pied de page personnalisés
        writer.setPageEvent(new HeaderFooterEvent());

        doc.open();

        // ── Page de garde ─────────────────────────────────────────────────────────
        addCoverPage(doc, categories.size(), modules.size(), cours.size());
        doc.newPage();

        // ── Résumé stats ──────────────────────────────────────────────────────────
        addStatsSection(doc, categories, modules, cours);
        doc.newPage();

        // ── Catalogue par catégorie ───────────────────────────────────────────────
        for (Cours_Categorie cat : categories) {
            // Titre catégorie
            addCategoryHeader(doc, cat);

            List<Cours_Module> catModules = modules.stream()
                    .filter(m -> m.getCategorieId() == cat.getId())
                    .toList();

            if (catModules.isEmpty()) {
                Paragraph noMod = new Paragraph("Aucun module dans cette catégorie.", FONT_SMALL);
                noMod.setSpacingBefore(6);
                doc.add(noMod);
            }

            for (Cours_Module mod : catModules) {
                addModuleSection(doc, mod);

                List<Cours> modCours = cours.stream()
                        .filter(c -> c.getModuleId() == mod.getId())
                        .sorted((a, b) -> Integer.compare(a.getOrdre(), b.getOrdre()))
                        .toList();

                if (!modCours.isEmpty()) {
                    addCoursTable(doc, modCours);
                } else {
                    Paragraph noCours = new Paragraph("Aucun cours dans ce module.", FONT_SMALL);
                    noCours.setIndentationLeft(20);
                    noCours.setSpacingBefore(4);
                    doc.add(noCours);
                }
            }
        }

        doc.close();
    }

    // ──────────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────────────────────

    private static void addCoverPage(Document doc, int nbCat, int nbMod, int nbCours)
            throws DocumentException {

        // Bande supérieure colorée
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_PRIMARY);
        cell.setPadding(40);
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph title = new Paragraph("Naja7ni", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        Paragraph sub   = new Paragraph("Catalogue de Cours", FONT_SUBTITLE);
        sub.setAlignment(Element.ALIGN_CENTER);
        Paragraph date  = new Paragraph(
                "Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, COLOR_WHITE));
        date.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(title);
        cell.addElement(sub);
        cell.addElement(date);
        banner.addCell(cell);
        doc.add(banner);

        doc.add(Chunk.NEWLINE);

        // Stats résumé
        PdfPTable stats = new PdfPTable(3);
        stats.setWidthPercentage(80);
        stats.setHorizontalAlignment(Element.ALIGN_CENTER);
        stats.setSpacingBefore(30);
        addStatCell(stats, String.valueOf(nbCat),   "Catégories", COLOR_PRIMARY);
        addStatCell(stats, String.valueOf(nbMod),   "Modules",    COLOR_DARK);
        addStatCell(stats, String.valueOf(nbCours), "Cours",      COLOR_BLUE);
        doc.add(stats);
    }

    private static void addStatCell(PdfPTable table, String value, String label, BaseColor color)
            throws DocumentException {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(color);
        cell.setBorderWidth(2);
        cell.setPadding(14);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font valFont = new Font(Font.FontFamily.HELVETICA, 28, Font.BOLD, color);
        Paragraph valP = new Paragraph(value, valFont);
        valP.setAlignment(Element.ALIGN_CENTER);
        Paragraph lblP = new Paragraph(label, FONT_SMALL);
        lblP.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(valP);
        cell.addElement(lblP);
        table.addCell(cell);
    }

    private static void addStatsSection(Document doc, List<Cours_Categorie> cats,
                                        List<Cours_Module> mods, List<Cours> cours)
            throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Statistiques", FONT_SECTION);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(10);
        doc.add(sectionTitle);

        // Niveau
        long deb  = mods.stream().filter(m -> "Débutant".equalsIgnoreCase(m.getNiveau())).count();
        long inter = mods.stream().filter(m -> "Intermédiaire".equalsIgnoreCase(m.getNiveau())).count();
        long adv  = mods.stream().filter(m -> "Avancé".equalsIgnoreCase(m.getNiveau())).count();
        long withPdf = cours.stream().filter(c -> c.getFichierContenu() != null && !c.getFichierContenu().isEmpty()).count();
        long totalMin = cours.stream().mapToLong(Cours::getDuree).sum();

        PdfPTable tbl = new PdfPTable(2);
        tbl.setWidthPercentage(60);
        tbl.setSpacingBefore(6);
        addStatRow(tbl, "Modules débutant",       String.valueOf(deb));
        addStatRow(tbl, "Modules intermédiaire",  String.valueOf(inter));
        addStatRow(tbl, "Modules avancé",          String.valueOf(adv));
        addStatRow(tbl, "Cours avec PDF",          String.valueOf(withPdf));
        addStatRow(tbl, "Durée totale (minutes)",  String.valueOf(totalMin));
        doc.add(tbl);
    }

    private static void addStatRow(PdfPTable table, String label, String value) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, FONT_BODY));
        lbl.setBorder(Rectangle.BOTTOM);
        lbl.setBorderColor(new BaseColor(226, 232, 240));
        lbl.setPadding(6);

        PdfPCell val = new PdfPCell(new Phrase(value, FONT_BOLD));
        val.setBorder(Rectangle.BOTTOM);
        val.setBorderColor(new BaseColor(226, 232, 240));
        val.setPadding(6);
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(lbl);
        table.addCell(val);
    }

    private static void addCategoryHeader(Document doc, Cours_Categorie cat)
            throws DocumentException {
        PdfPTable tbl = new PdfPTable(1);
        tbl.setWidthPercentage(100);
        tbl.setSpacingBefore(16);
        tbl.setSpacingAfter(8);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_DARK);
        cell.setPadding(10);
        cell.setBorder(Rectangle.NO_BORDER);

        Font f = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, COLOR_WHITE);
        Paragraph p = new Paragraph("📂 " + cat.getNom(), f);
        if (cat.getDescription() != null && !cat.getDescription().isEmpty()) {
            p.add(new Chunk("\n" + cat.getDescription(),
                    new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, new BaseColor(203,213,225))));
        }
        cell.addElement(p);
        tbl.addCell(cell);
        doc.add(tbl);
    }

    private static void addModuleSection(Document doc, Cours_Module mod)
            throws DocumentException {
        BaseColor niveauColor = switch (mod.getNiveau() == null ? "" : mod.getNiveau().toLowerCase()) {
            case "débutant"      -> COLOR_GREEN;
            case "intermédiaire" -> COLOR_BLUE;
            case "avancé"        -> COLOR_RED;
            default              -> COLOR_GRAY;
        };

        PdfPTable tbl = new PdfPTable(new float[]{6, 2});
        tbl.setWidthPercentage(100);
        tbl.setSpacingBefore(10);
        tbl.setSpacingAfter(4);

        PdfPCell modCell = new PdfPCell();
        modCell.setBorder(Rectangle.LEFT);
        modCell.setBorderColor(COLOR_PRIMARY);
        modCell.setBorderWidth(4);
        modCell.setPaddingLeft(10);
        modCell.setPaddingTop(6);
        modCell.setPaddingBottom(6);

        Font modFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, COLOR_DARK);
        modCell.addElement(new Paragraph("📦 " + mod.getTitre(), modFont));
        if (mod.getDescription() != null && !mod.getDescription().isEmpty()) {
            modCell.addElement(new Paragraph(mod.getDescription(), FONT_SMALL));
        }
        tbl.addCell(modCell);

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph niveauP = new Paragraph(mod.getNiveau() != null ? mod.getNiveau() : "—",
                new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, niveauColor));
        niveauP.setAlignment(Element.ALIGN_RIGHT);
        Paragraph dureeP = new Paragraph("⏱ " + mod.getDuree() + "h", FONT_SMALL);
        dureeP.setAlignment(Element.ALIGN_RIGHT);
        infoCell.addElement(niveauP);
        infoCell.addElement(dureeP);
        tbl.addCell(infoCell);

        doc.add(tbl);
    }

    private static void addCoursTable(Document doc, List<Cours> coursList)
            throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1, 4, 2, 1.5f, 1.5f});
        table.setWidthPercentage(96);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(4);
        table.setSpacingAfter(8);

        // En-tête
        String[] headers = {"#", "Titre", "Description", "Durée (min)", "PDF"};
        for (String h : headers) {
            PdfPCell hCell = new PdfPCell(new Phrase(h, FONT_TABLE_HDR));
            hCell.setBackgroundColor(COLOR_PRIMARY);
            hCell.setPadding(6);
            hCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(hCell);
        }

        boolean alt = false;
        for (Cours c : coursList) {
            BaseColor rowBg = alt ? new BaseColor(247, 250, 252) : COLOR_WHITE;
            alt = !alt;

            addTableCell(table, String.valueOf(c.getOrdre()), FONT_SMALL, rowBg, Element.ALIGN_CENTER);
            addTableCell(table, c.getTitre(), FONT_BOLD, rowBg, Element.ALIGN_LEFT);
            String desc = (c.getDescription() != null && !c.getDescription().isEmpty())
                    ? c.getDescription() : "—";
            if (desc.length() > 60) desc = desc.substring(0, 57) + "...";
            addTableCell(table, desc, FONT_SMALL, rowBg, Element.ALIGN_LEFT);
            addTableCell(table, String.valueOf(c.getDuree()), FONT_BODY, rowBg, Element.ALIGN_CENTER);
            boolean hasPdf = c.getFichierContenu() != null && !c.getFichierContenu().isEmpty();
            addTableCell(table, hasPdf ? "✓" : "—",
                    hasPdf ? new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, COLOR_GREEN) : FONT_SMALL,
                    rowBg, Element.ALIGN_CENTER);
        }

        doc.add(table);
    }

    private static void addTableCell(PdfPTable table, String text, Font font,
                                     BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setBorderColor(new BaseColor(226, 232, 240));
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    // ──────────────────────────────────────────────────────────────────────────────
    //  Page event : en-tête / pied de page
    // ──────────────────────────────────────────────────────────────────────────────
    private static class HeaderFooterEvent extends PdfPageEventHelper {
        private final Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL,
                new BaseColor(148, 163, 184));

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // Ligne de pied de page
            cb.setColorStroke(new BaseColor(226, 232, 240));
            cb.setLineWidth(0.5f);
            cb.moveTo(document.leftMargin(), document.bottomMargin() - 5);
            cb.lineTo(document.right(), document.bottomMargin() - 5);
            cb.stroke();

            // Texte pied de page
            try {
                BaseFont baseFont = BaseFont.createFont(
                        BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
                
                cb.beginText();
                cb.setFontAndSize(baseFont, 8);
                cb.showTextAligned(Element.ALIGN_LEFT, "Naja7ni - Catalogue de Cours",
                        document.leftMargin(), document.bottomMargin() - 18, 0);
                cb.showTextAligned(Element.ALIGN_RIGHT, "Page " + writer.getPageNumber(),
                        document.right(), document.bottomMargin() - 18, 0);
                cb.endText();
            } catch (Exception e) {
                // Si le BaseFont échoue, on saute le footer
                System.err.println("Erreur BaseFont: " + e.getMessage());
            }
        }
    }
}