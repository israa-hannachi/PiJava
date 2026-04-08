package tn.esprit.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    // Paramètres de connexion
    private final String url = "jdbc:mysql://localhost:3306/eventssss";
    private final String user = "root";
    private final String password = "";

    // Objet connexion
    private Connection cnx;

    // Instance unique (Singleton)
    private static MyDatabase instance;

    // Constructeur privé
    private MyDatabase() {
        try {
            cnx = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connexion à la base de données 'eventssss' établie !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
            System.err.println("👉 Vérifiez que MySQL est démarré et que la base 'eventssss' existe.");
        }
    }

    // Méthode pour récupérer l’instance unique
    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    // Getter pour récupérer la connexion
    public Connection getCnx() {
        return cnx;
    }
}
