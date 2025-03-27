package com.btssio.applicationrftg;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PanierManager {

    private static PanierManager instance;
    private List<String> panier; // On stocke ici les titres des films

    private PanierManager() {
        panier = new ArrayList<>();
    }

    public static PanierManager getInstance() {
        if (instance == null) {
            instance = new PanierManager();
        }
        return instance;
    }

    public void ajouterAuPanier(String filmTitre, String prixFilm) {
        panier.add(filmTitre);
        panier.add(prixFilm);
    }

    public List<String> getPanier() {
        return panier;
    }

    public void viderPanier() {
        panier.clear();
    }
}

