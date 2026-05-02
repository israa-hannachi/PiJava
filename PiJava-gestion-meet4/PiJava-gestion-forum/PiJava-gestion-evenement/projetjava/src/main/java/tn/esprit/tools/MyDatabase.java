package tn.esprit.tools;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MyDatabase {

    private final String url;
    private final String user;
    private final String password;
    private Connection cnx;
    private static MyDatabase instance;

    private MyDatabase() {
        Properties config = loadConfiguration();
        url = getConfigValue(config, "DB_URL", "jdbc:mysql://localhost:3306/pidev_db");
        user = getConfigValue(config, "DB_USER", "root");
        password = getConfigValue(config, "DB_PASSWORD", "");

        try {
            cnx = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion a la base de donnees etablie.");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
            System.err.println("Verifiez que MySQL est demarre et que la base existe.");
        }
    }

    private Properties loadConfiguration() {
        Properties config = new Properties();

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (stream != null) {
                config.load(stream);
                return config;
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement config.properties depuis le classpath : " + e.getMessage());
        }

        try (InputStream stream = new FileInputStream("src/main/resources/config.properties")) {
            config.load(stream);
        } catch (Exception e) {
            System.err.println("Config DB locale non trouvee, utilisation des valeurs par defaut.");
        }

        return config;
    }

    private String getConfigValue(Properties config, String key, String defaultValue) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }
        return config.getProperty(key, defaultValue);
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
