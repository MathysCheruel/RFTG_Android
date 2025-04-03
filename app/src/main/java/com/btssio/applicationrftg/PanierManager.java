package com.btssio.applicationrftg;

import java.util.ArrayList;
import java.util.List;

public class PanierManager {

    private static PanierManager instance;
    private List<String> panier; // On stocke ici les titres des films et leurs infos (ID, titre, prix)

    private PanierManager() {
        panier = new ArrayList<>();
    }

    public static PanierManager getInstance() {
        if (instance == null) {
            instance = new PanierManager();
        }
        return instance;
    }

    // Ajouter un film avec son ID, son titre et son prix
    public void ajouterAuPanier(int filmId, String filmTitre, String prixFilm) {
        String filmInfo = filmId + "," + filmTitre + "," + prixFilm; // Concaténation des informations
        panier.add(filmInfo);
    }

    // Récupérer le panier (avec l'ID, le titre et le prix sous forme de chaîne)
    public List<String> getPanier() {
        return panier;
    }

    // Vider le panier
    public void viderPanier() {
        panier.clear();
    }
}