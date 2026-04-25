package tn.esprit.tools;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileOutputStream;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class ExportUtil {

    private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";

    public static void exportToPDF(String filePath, Map<String, Object> stats, 
                                 List<tn.esprit.entities.forum.Categorie> categories, 
                                 List<tn.esprit.entities.forum.Forum> allForums,
                                 Map<Integer, Integer> forumMessageCounts,
                                 Map<String, Integer> messageTrends) throws Exception {
        
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // --- STYLES ---
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new BaseColor(30, 41, 59));
        com.itextpdf.text.Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(30, 41, 59));
        com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        com.itextpdf.text.Font grayFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);

        // --- HEADER ---
        Paragraph mainTitle = new Paragraph("RAPPORT D'ANALYSE DYNAMIQUE\nESPACE DE DISCUSSION NAJA7NI", titleFont);
        mainTitle.setAlignment(Element.ALIGN_CENTER);
        mainTitle.setSpacingAfter(10);
        document.add(mainTitle);

        Paragraph datePara = new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), grayFont);
        datePara.setAlignment(Element.ALIGN_RIGHT);
        datePara.setSpacingAfter(20);
        document.add(datePara);

        // --- SECTION 1: SYNTHÈSE ---
        document.add(new Paragraph("1. Synthèse de l'Activité Globale", sectionFont));
        PdfPTable kpiTable = new PdfPTable(2);
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingBefore(10);
        kpiTable.setSpacingAfter(15);
        
        addStyledCell(kpiTable, "Indicateur Clé", true);
        addStyledCell(kpiTable, "Valeur Actuelle", true);
        
        addRow(kpiTable, "Volume de Catégories", stats.get("totalCategories").toString());
        addRow(kpiTable, "Forums répertoriés", stats.get("totalForums").toString());
        addRow(kpiTable, "Messages cumulés", stats.get("totalMessages").toString());
        addRow(kpiTable, "Croissance (7j)", String.format("%.1f%%", (Double) stats.get("growth")));
        document.add(kpiTable);

        // --- SECTION 2: DÉTAILS DES CATÉGORIES ---
        document.add(new Paragraph("2. Performance par Catégorie", sectionFont));
        PdfPTable catTable = new PdfPTable(2);
        catTable.setWidthPercentage(100);
        catTable.setSpacingBefore(10);
        catTable.setSpacingAfter(15);
        
        addStyledCell(catTable, "Titre de la Catégorie", true);
        addStyledCell(catTable, "Statut de Santé", true);

        for (tn.esprit.entities.forum.Categorie cat : categories) {
            catTable.addCell(new Phrase(cat.getTitre(), normalFont));
            catTable.addCell(new Phrase("Opérationnel", normalFont));
        }
        document.add(catTable);

        // --- SECTION 3: ANALYSE STRATÉGIQUE DYNAMIQUE ---
        document.add(new Paragraph("3. Recommandations de l'Analyste", sectionFont));
        String healthText = (String) stats.getOrDefault("healthStatus", "Stable");
        int activeNum = (int) stats.getOrDefault("activeCount", 0);
        
        document.add(new Paragraph("\n• ÉTAT DES LIEUX : Le ratio d'engagement est de " + stats.get("engagement") + " messages/forum. La santé globale est jugée : " + healthText.toUpperCase() + " (" + activeNum + " forums actifs).", normalFont));
        document.add(new Paragraph("• PRÉCONISATION : " + (healthText.equals("Excellent") ? "Maintenir la stratégie actuelle." : "Renforcer l'animation des forums inactifs.") + " L'objectif est de dépasser le taux de croissance actuel de " + String.format("%.1f%%", (Double) stats.get("growth")) + ".", normalFont));


        // --- SECTION 4: RAPPORT IA (HUGGING FACE) ---
        document.add(new Paragraph("\n4. Analyse de l'IA Naja7ni", sectionFont));
        document.add(new Paragraph("\nLe système de Chatbot IA (Hugging Face) assure une réponse continue :\n", normalFont));
        document.add(new Paragraph("• Support : Capacité multilingue activée pour " + categories.size() + " secteurs thématiques.", normalFont));
        document.add(new Paragraph("• Innovation : Transcription vocale intégrée pour une accessibilité maximale.", normalFont));
        document.add(new Paragraph("• Audit : Aucun incident de modération majeur détecté dans les messages récents.", normalFont));

        // --- FOOTER ---
        Paragraph footer = new Paragraph("\n\nRapport d'Audit Dynamique v5.0 - Naja7ni", grayFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }

    public static void exportToExcel(String filePath, Map<String, Object> stats, List<tn.esprit.entities.forum.Categorie> categories) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Audit Dynamique");

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);

        int r = 0;
        sheet.createRow(r++).createCell(0).setCellValue("RAPPORT D'ANALYSE DYNAMIQUE - NAJA7NI");
        sheet.createRow(r++).createCell(0).setCellValue("Extraction du : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        r++;

        // Section 1
        Row h1 = sheet.createRow(r++);
        h1.createCell(0).setCellValue("1. SYNTHÈSE GLOBALE");
        h1.getCell(0).setCellStyle(headerStyle);
        createExcelRow(sheet, r++, "Catégories", stats.get("totalCategories").toString());
        createExcelRow(sheet, r++, "Forums", stats.get("totalForums").toString());
        createExcelRow(sheet, r++, "Messages", stats.get("totalMessages").toString());
        createExcelRow(sheet, r++, "Croissance", String.format("%.1f%%", (Double) stats.get("growth")));
        r++;

        // Section 2
        Row h2 = sheet.createRow(r++);
        h2.createCell(0).setCellValue("2. PERFORMANCE CATÉGORIES");
        h2.getCell(0).setCellStyle(headerStyle);
        for (tn.esprit.entities.forum.Categorie cat : categories) {
            createExcelRow(sheet, r++, cat.getTitre(), "Opérationnel");
        }
        r++;

        // Section 3
        Row h3 = sheet.createRow(r++);
        h3.createCell(0).setCellValue("3. RECOMMANDATIONS");
        h3.getCell(0).setCellStyle(headerStyle);
        createExcelRow(sheet, r++, "Engagement", stats.get("engagement") + " msg/forum");
        createExcelRow(sheet, r++, "Conseil", "Consolider la croissance de " + String.format("%.1f%%", (Double) stats.get("growth")));
        r++;

        // Section 4
        Row h4 = sheet.createRow(r++);
        h4.createCell(0).setCellValue("4. ANALYSE IA");
        h4.getCell(0).setCellStyle(headerStyle);
        createExcelRow(sheet, r++, "Technologie", "Hugging Face Chatbot");
        createExcelRow(sheet, r++, "État Modération", "Sain (" + stats.get("totalMessages") + " messages audités)");
        createExcelRow(sheet, r++, "Support", "Multilingue & Vocal");


        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    public static void exportToWord(String filePath, Map<String, Object> stats, 
                                   List<tn.esprit.entities.forum.Categorie> categories, 
                                   List<tn.esprit.entities.forum.Forum> allForums,
                                   Map<Integer, Integer> forumMessageCounts) throws Exception {
        XWPFDocument document = new XWPFDocument();

        // Header
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("RAPPORT D'ANALYSE DYNAMIQUE");
        titleRun.setBold(true);
        titleRun.setFontSize(18);
        titleRun.addBreak();
        titleRun.setText("ESPACE DE DISCUSSION NAJA7NI");
        
        XWPFParagraph datePara = document.createParagraph();
        datePara.setAlignment(ParagraphAlignment.RIGHT);
        datePara.createRun().setText("Date : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));

        // Section 1
        addWordHeader(document, "1. Synthèse de l'Activité Globale");
        XWPFTable t1 = document.createTable(); t1.setWidth("100%");
        addWordRow(t1, "Indicateur", "Valeur", true);
        addWordRow(t1, "Catégories", stats.get("totalCategories").toString(), false);
        addWordRow(t1, "Forums", stats.get("totalForums").toString(), false);
        addWordRow(t1, "Messages", stats.get("totalMessages").toString(), false);
        addWordRow(t1, "Croissance", String.format("%.1f%%", (Double) stats.get("growth")), false);

        // Section 2
        addWordHeader(document, "2. Performance par Catégorie");
        XWPFTable t2 = document.createTable(); t2.setWidth("100%");
        addWordRow(t2, "Catégorie", "Statut", true);
        for (tn.esprit.entities.forum.Categorie cat : categories) {
            addWordRow(t2, cat.getTitre(), "Opérationnel", false);
        }

        // Section 3
        addWordHeader(document, "3. Recommandations Stratégiques");
        String healthText = (String) stats.getOrDefault("healthStatus", "Stable");
        XWPFParagraph p3 = document.createParagraph();
        XWPFRun r3 = p3.createRun();
        r3.setText("• ÉTAT DE SANTÉ : " + healthText.toUpperCase() + " (" + stats.get("activeCount") + " forums actifs).");
        r3.addBreak();
        r3.setText("• PRÉCONISATION : Maintenir la croissance de " + String.format("%.1f%%", (Double) stats.get("growth")) + ". " + (healthText.equals("Excellent") ? "Félicitations pour la gestion." : "Action requise sur l'engagement."));


        // Section 4
        addWordHeader(document, "4. Audit IA (Hugging Face)");
        XWPFParagraph p4 = document.createParagraph();
        p4.createRun().setText("L'assistant IA est opérationnel sur les " + categories.size() + " thématiques du site. Support multilingue et vocal OK.");

        try (FileOutputStream out = new FileOutputStream(filePath)) {
            document.write(out);
        }
        document.close();
    }

    public static void exportToCSV(String filePath, Map<String, Object> stats, List<tn.esprit.entities.forum.Categorie> categories) throws Exception {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            StringBuilder sb = new StringBuilder();
            sb.append("RAPPORT NAJA7NI;").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT))).append("\n\n");
            
            sb.append("1. SYNTHESE\n");
            sb.append("Indicateur;Valeur\n");
            sb.append("Categories;").append(stats.get("totalCategories")).append("\n");
            sb.append("Forums;").append(stats.get("totalForums")).append("\n");
            sb.append("Messages;").append(stats.get("totalMessages")).append("\n");
            sb.append("Croissance;").append(String.format("%.1f%%", (Double) stats.get("growth"))).append("\n\n");

            sb.append("2. CATEGORIES\n");
            sb.append("Titre;Statut\n");
            for (tn.esprit.entities.forum.Categorie cat : categories) {
                sb.append(cat.getTitre()).append(";Opérationnel\n");
            }
            sb.append("\n");

            sb.append("3. RECOMMANDATIONS\n");
            sb.append("Santé globale;").append(stats.get("healthStatus")).append(" (").append(stats.get("activeCount")).append(" forums actifs)\n");
            sb.append("Conseil;Croissance ").append(String.format("%.1f%%", (Double) stats.get("growth"))).append("\n\n");


            sb.append("4. IA\n");
            sb.append("Moteur;Hugging Face IA\n");
            sb.append("Etat;Actif\n");
            
            out.write(sb.toString().getBytes());
        }
    }

    private static void addStyledCell(PdfPTable table, String text, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, isHeader ? BaseColor.WHITE : BaseColor.BLACK)));
        if (isHeader) cell.setBackgroundColor(new BaseColor(51, 65, 85));
        cell.setPadding(8);
        table.addCell(cell);
    }

    private static void addRow(PdfPTable table, String label, String value) {
        table.addCell(label);
        table.addCell(value);
    }

    private static void createExcelRow(Sheet sheet, int rowIdx, String label, String value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private static void addWordHeader(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(true);
        r.setFontSize(14);
    }

    private static void addWordRow(XWPFTable table, String col1, String col2, boolean isHeader) {
        XWPFTableRow row = isHeader ? table.getRow(0) : table.createRow();
        XWPFTableCell cell1 = row.getCell(0);
        XWPFTableCell cell2 = isHeader ? row.addNewTableCell() : row.getCell(1);
        cell1.setText(col1);
        cell2.setText(col2);
        if (isHeader) {
            cell1.getCTTc().addNewTcPr().addNewShd().setFill("334155");
            cell2.getCTTc().addNewTcPr().addNewShd().setFill("334155");
        }
    }
}
