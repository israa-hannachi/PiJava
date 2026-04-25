package tn.esprit.services.game;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class QuizApiService {

    public static final Map<String, Integer> CATEGORY_MAP = new LinkedHashMap<>() {{
        put("Finance (Debutant)",           9);
        put("Finance (Intermediaire)",      9);
        put("Sciences (Debutant)",         17);
        put("Sciences (Intermediaire)",    17);
        put("Informatique (Debutant)",     18);
        put("Informatique (Intermediaire)",18);
        put("Informatique (Avance)",       18);
        put("Mathematiques",               19);
        put("Geographie (Debutant)",       22);
        put("Histoire (Intermediaire)",    23);
    }};

    public static final Map<String, String> DIFFICULTY_MAP = new LinkedHashMap<>() {{
        put("Finance (Debutant)",           "easy");
        put("Finance (Intermediaire)",      "medium");
        put("Sciences (Debutant)",         "easy");
        put("Sciences (Intermediaire)",    "medium");
        put("Informatique (Debutant)",     "easy");
        put("Informatique (Intermediaire)","medium");
        put("Informatique (Avance)",       "hard");
        put("Mathematiques",               "medium");
        put("Geographie (Debutant)",       "easy");
        put("Histoire (Intermediaire)",    "medium");
    }};

    public List<Map<String, String>> fetchQuestions(
            int categoryId, String difficulty,
            String type, int amount) throws Exception {

        String urlStr = "https://opentdb.com/api.php"
                + "?amount=" + amount
                + "&category=" + categoryId
                + "&difficulty=" + difficulty
                + "&type=" + type;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        InputStream is = conn.getInputStream();
        String json = new String(is.readAllBytes());
        conn.disconnect();

        JSONObject root = new JSONObject(json);
        if (root.getInt("response_code") != 0) {
            throw new Exception("OpenTDB error: " + root.getInt("response_code"));
        }

        JSONArray results = root.getJSONArray("results");
        List<Map<String, String>> questions = new ArrayList<>();

        for (int i = 0; i < results.length(); i++) {
            JSONObject q = results.getJSONObject(i);
            String question = decodeHtml(q.getString("question"));
            String correct  = decodeHtml(q.getString("correct_answer"));

            StringBuilder answers = new StringBuilder();
            if (type.equals("boolean")) {
                answers.append("Vrai\nFaux");
            } else {
                List<String> all = new ArrayList<>();
                JSONArray incorrect = q.getJSONArray("incorrect_answers");
                for (int j = 0; j < incorrect.length(); j++)
                    all.add(decodeHtml(incorrect.getString(j)));
                all.add(correct);
                Collections.shuffle(all);
                for (String a : all) answers.append("• ").append(a).append("\n");
            }

            Map<String, String> row = new LinkedHashMap<>();
            row.put("question",   question);
            row.put("answers",    answers.toString().trim());
            row.put("correct",    correct);
            row.put("difficulty", q.getString("difficulty"));
            questions.add(row);
        }
        return questions;
    }

    private String decodeHtml(String text) {
        return text
                .replace("&quot;",  "\"")
                .replace("&#039;",  "'")
                .replace("&amp;",   "&")
                .replace("&lt;",    "<")
                .replace("&gt;",    ">")
                .replace("&eacute;","e")
                .replace("&egrave;","e")
                .replace("&agrave;","a")
                .replace("&ccedil;","c");
    }
}