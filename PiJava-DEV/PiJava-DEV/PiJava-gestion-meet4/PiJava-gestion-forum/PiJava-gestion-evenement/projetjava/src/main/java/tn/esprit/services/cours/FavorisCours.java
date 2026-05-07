package tn.esprit.services.cours;

import tn.esprit.entities.cours.Cours;

import java.util.*;

/**
 * Gestionnaire des favoris de cours (session courante).
 *
 * Les favoris sont stockés en mémoire pendant la session JavaFX.
 * Pour une persistance entre sessions, on peut sérialiser dans un fichier JSON
 * (voir méthode saveToFile / loadFromFile si besoin).
 */
public class FavorisCours {

    private static FavorisCours instance;
    /** Map userId → Set d'IDs de cours favoris */
    private final Map<Integer, Set<Integer>> favMap = new HashMap<>();

    private FavorisCours() {}

    public static FavorisCours getInstance() {
        if (instance == null) instance = new FavorisCours();
        return instance;
    }

    /** Ajoute un cours aux favoris d'un utilisateur. */
    public void ajouter(int userId, Cours c) {
        favMap.computeIfAbsent(userId, k -> new LinkedHashSet<>()).add(c.getId());
    }

    /** Retire un cours des favoris. */
    public void retirer(int userId, int coursId) {
        Set<Integer> set = favMap.get(userId);
        if (set != null) set.remove(coursId);
    }

    /** Bascule favori ON/OFF et renvoie l'état final. */
    public boolean toggle(int userId, Cours c) {
        Set<Integer> set = favMap.computeIfAbsent(userId, k -> new LinkedHashSet<>());
        if (set.contains(c.getId())) {
            set.remove(c.getId());
            return false;
        } else {
            set.add(c.getId());
            return true;
        }
    }

    /** Est-ce que ce cours est en favori pour cet utilisateur ? */
    public boolean estFavori(int userId, int coursId) {
        Set<Integer> set = favMap.get(userId);
        return set != null && set.contains(coursId);
    }

    /**
     * Renvoie la liste ordonnée des cours favoris d'un utilisateur,
     * filtrée sur la liste complète des cours fournie.
     */
    public List<Cours> getFavoris(int userId, List<Cours> tousLesCours) {
        Set<Integer> ids = favMap.getOrDefault(userId, Collections.emptySet());
        List<Cours> result = new ArrayList<>();
        for (Cours c : tousLesCours) {
            if (ids.contains(c.getId())) result.add(c);
        }
        return result;
    }

    /** Vide les favoris d'un utilisateur (ex : logout). */
    public void clear(int userId) {
        favMap.remove(userId);
    }

    /** Nombre de favoris pour un utilisateur. */
    public int count(int userId) {
        Set<Integer> set = favMap.get(userId);
        return set == null ? 0 : set.size();
    }
}