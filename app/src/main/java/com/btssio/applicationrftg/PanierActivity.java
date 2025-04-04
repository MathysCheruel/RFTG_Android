package com.btssio.applicationrftg;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date currentDate = new Date();
        String rentalDate = dateFormat.format(currentDate);

        // Calcul de la date de retour (15 jours après)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, 15);
        String returnDate = dateFormat.format(calendar.getTime());

        List<String> panier = PanierManager.getInstance().getPanier();
        if (panier.isEmpty()) {
            Toast.makeText(this, "Le panier est vide.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crée un JSONArray pour envoyer les données de location
        JSONArray rentalsArray = new JSONArray();
        for (String filmInfo : panier) {
            String[] info = filmInfo.split(","); // Assurez-vous du bon format: "id,titre,prix"
            int inventory_id = Integer.parseInt(info[0]);  // ⚠ Vérifie si c'est bien `inventory_id`
            String filmTitre = info[1];  // Peut-être pas utilisé dans l'API, mais à garder si nécessaire
            String prixFilm = info[2];   // Idem

            // Créer un objet JSON pour chaque film
            JSONObject rentalData = new JSONObject();
            try {
                rentalData.put("rental_date", rentalDate);
                rentalData.put("inventory_id", inventory_id);
                rentalData.put("customer_id", getUserId());  // Utilisation de l'ID du client
                rentalData.put("return_date", returnDate);
                rentalData.put("staff_id", 1); // Remplacer par l'ID du staff si nécessaire
                rentalData.put("last_update", rentalDate);
                rentalsArray.put(rentalData);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur dans la création des données de location", Toast.LENGTH_SHORT).show();
            }
        }

        // Si le tableau n'est pas vide, on envoie les données
        if (rentalsArray.length() > 0) {
            envoyerLocations(rentalsArray);
        }
    }

    private void envoyerLocations(JSONArray rentalsArray) {
        new Thread(() -> {
            try {
                for (int i = 0; i < rentalsArray.length(); i++) {
                    JSONObject rentalData = rentalsArray.getJSONObject(i);

                    String rentalDate = rentalData.getString("rental_date");
                    // Corrige le type ici : utilise un entier pour inventory_id
                    int inventoryId = rentalData.getInt("inventory_id");  // Utilise getInt pour récupérer un entier
                    String customerId = rentalData.getString("customer_id");
                    String returnDate = rentalData.optString("return_date", ""); // Si null, envoyer une chaîne vide
                    String staffId = rentalData.getString("staff_id");
                    String lastUpdate = rentalData.getString("last_update");

                    String url = DonneesPartagees.getURLConnexion() + "/toad/rental/add";

                    RequestQueue queue = Volley.newRequestQueue(this);
                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            response -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(PanierActivity.this, "Location validée avec succès", Toast.LENGTH_SHORT).show();
                                    PanierManager.getInstance().viderPanier();
                                    afficherPanier();
                                });
                            },
                            error -> {
                                runOnUiThread(() -> Toast.makeText(PanierActivity.this, "Erreur lors de la validation", Toast.LENGTH_SHORT).show());
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("rental_date", rentalDate);
                            // Convertir inventoryId en String (nécessaire pour l'API)
                            params.put("inventory_id", String.valueOf(inventoryId));  // Utilise String.valueOf pour convertir en chaîne
                            params.put("customer_id", customerId);
                            params.put("return_date", returnDate);
                            params.put("staff_id", staffId);
                            params.put("last_update", lastUpdate);
                            return params;
                        }
                    };

                    queue.add(postRequest);
                }
            } catch (JSONException e) {
                runOnUiThread(() -> Toast.makeText(PanierActivity.this, "Erreur dans la création des données de location", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }


    public int getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1);
    }

}
