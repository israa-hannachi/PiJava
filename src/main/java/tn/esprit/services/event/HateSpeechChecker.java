package tn.esprit.services.event;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HateSpeechChecker {
    private final List<String> badWordsList = new ArrayList<>();

    public HateSpeechChecker() {
        loadBadWordsFromExcel();
        if (badWordsList.isEmpty()) {
            badWordsList.addAll(Arrays.asList(
                    "hate",
                    "idiot",
                    "stupid",
                    "racist",
                    "violent",
                    "insulte",
                    "haine"
            ));
        }
    }

    private void loadBadWordsFromExcel() {
        // Primary path: classpath (works when packaged as JAR or run from IDE)
        try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("tn/esprit/Db/hatespeech.xlsx")) {
            if (resourceStream != null) {
                loadWorkbook(resourceStream);
                System.out.println("[HateSpeechChecker] Loaded " + badWordsList.size() + " bad words from classpath.");
                return;
            }
        } catch (IOException e) {
            System.err.println("[HateSpeechChecker] Failed to load from classpath: " + e.getMessage());
        }

        // Fallback path: filesystem (useful when running directly from the project root)
        try (FileInputStream file = new FileInputStream("src/main/resources/tn/esprit/Db/hatespeech.xlsx")) {
            loadWorkbook(file);
            System.out.println("[HateSpeechChecker] Loaded " + badWordsList.size() + " bad words from filesystem.");
        } catch (IOException e) {
            System.err.println("[HateSpeechChecker] Failed to load from filesystem: " + e.getMessage());
        }
    }

    private void loadWorkbook(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            boolean firstRow = true;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                // Skip header row
                if (firstRow) { firstRow = false; continue; }
                Cell cell = row.getCell(0);
                if (cell != null) {
                    String badWord = null;
                    if (cell.getCellType() == CellType.STRING) {
                        badWord = cell.getStringCellValue();
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        badWord = String.valueOf((long) cell.getNumericCellValue());
                    }
                    if (badWord != null && !badWord.isBlank()) {
                        badWordsList.add(badWord.trim().toLowerCase());
                    }
                }
            }
        }
    }

    public boolean containsBadWord(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lowerText = text.toLowerCase();
        for (String badWord : badWordsList) {
            if (lowerText.contains(badWord.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
