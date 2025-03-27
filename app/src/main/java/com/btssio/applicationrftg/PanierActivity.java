package com.btssio.applicationrftg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PanierActivity extends AppCompatActivity {

    private LinearLayout panierContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);
        panierContainer = findViewById(R.id.panierContainer);
        Button btnViderPanier = findViewById(R.id.btnViderPanier);
        Button btnRetour = findViewById(R.id.btnRetour);

        // Afficher les films dans le panier
        afficherPanier();

        // Vider le panier
        btnViderPanier.setOnClickListener(v -> {
            PanierManager.getInstance().viderPanier();
            afficherPanier();
            Toast.makeText(this, "Panier vidé !", Toast.LENGTH_SHORT).show();
        });

        // Retour à la liste de films
        btnRetour.setOnClickListener(v -> finish());
    }

    private void afficherPanier() {
        panierContainer.removeAllViews();
        List<String> panier = PanierManager.getInstance().getPanier();

        if (panier.isEmpty()) {
            TextView txtVide = new TextView(this);
            txtVide.setText("Votre panier est vide.");
            txtVide.setTextSize(18);
            txtVide.setPadding(20, 20, 20, 20);
            panierContainer.addView(txtVide);
            return;
        }

        for (String film : panier) {
            TextView txtFilm = new TextView(this);
            txtFilm.setText(film);
            txtFilm.setTextSize(18);
            txtFilm.setPadding(8, 8, 8, 8);
            panierContainer.addView(txtFilm);
        }

        Button btnVoirPanier = findViewById(R.id.btnValiderPanier);

        btnVoirPanier.setOnClickListener(v -> envoyerDonneesAPI());
    }
    private void envoyerDonneesAPI() {
        // Obtenir la date et l'heure actuelles
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date currentDate = new Date();
        String rentalDate = dateFormat.format(currentDate);
        // Calculer return_date (rental_date + 15 jours)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, 15);
        String returnDate = dateFormat.format(calendar.getTime());

        new Thread(() -> {
            String urlString = "http://10.0.2.2:8080/toad/rental/add";

            String postData = "rental_date=" + rentalDate +
                    "&inventory_id=1" +
                    "&customer_id=1" +
                    "&return_date=" + returnDate +
                    "&staff_id=1" +
                    "&last_update=" + rentalDate;

            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.getBytes("utf-8"));
                }

                int responseCode = conn.getResponseCode();
                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(PanierActivity.this, "Données envoyées avec succès", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PanierActivity.this, "Erreur: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(PanierActivity.this, "Échec de l'envoi", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
