package com.btssio.applicationrftg;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

public class FilmDetailsActivity extends AppCompatActivity {

    private TextView movieTitle;
    private TextView movieDescription;
    private TextView movieReleaseYear;
    private TextView movieRentalRate;
    private TextView movieLength;
    private TextView movieReplacementCost;
    private TextView movieRating;
    private TextView movieSpecialFeatures;

    private static final String TAG = "FilmDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_details);

        movieTitle = findViewById(R.id.movie_title);
        movieDescription = findViewById(R.id.movie_description);
        movieReleaseYear = findViewById(R.id.movie_release_year);
        movieRentalRate = findViewById(R.id.movie_rental_rate);
        movieLength = findViewById(R.id.movie_length);
        movieReplacementCost = findViewById(R.id.movie_replacement_cost);
        movieRating = findViewById(R.id.movie_rating);
        movieSpecialFeatures = findViewById(R.id.movie_special_features);

        int filmId = getIntent().getIntExtra("filmId", -1);
        Log.d(TAG, "ID du film reçu : " + filmId);

        if (filmId == -1) {
            Toast.makeText(this, "ID du film manquant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button btnRetour = findViewById(R.id.btnBack);
        btnRetour.setOnClickListener(v -> finish());

        fetchFilmDetails(filmId);
    }

    private void fetchFilmDetails(int filmId) {
        String url = DonneesPartagees.getURLConnexion() + "/toad/film/getById?id=" + filmId;
        Log.d(TAG, "URL de la requête : " + url);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            movieTitle.setText("Titre : " + response.getString("title"));
                            movieDescription.setText("Description : " + response.getString("description"));
                            movieReleaseYear.setText("Année de sortie : " + response.getInt("releaseYear"));
                            movieRentalRate.setText("Tarif de location : " + response.getDouble("rentalRate") + " €");
                            movieLength.setText("Durée : " + response.getInt("length") + " minutes");
                            movieReplacementCost.setText("Coût de remplacement : " + response.getDouble("replacementCost") + " €");
                            movieRating.setText("Classification : " + response.getString("rating"));
                            movieSpecialFeatures.setText("Fonctionnalités spéciales : " + response.getString("specialFeatures"));
                        } catch (JSONException e) {
                            Log.e(TAG, "Erreur dans l'analyse de la réponse JSON", e);
                            Toast.makeText(FilmDetailsActivity.this, "Erreur dans les données récupérées", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Erreur Volley", error);
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String responseData = new String(error.networkResponse.data);
                            Toast.makeText(FilmDetailsActivity.this, "Erreur " + statusCode + " : " + responseData, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(FilmDetailsActivity.this, "Erreur réseau inconnue", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        queue.add(request);

        Button btnAjouterAuPanier = findViewById(R.id.btnAddCart);
        btnAjouterAuPanier.setOnClickListener(v -> {
            String titreFilm = movieTitle.getText().toString().replace("Titre :", "").trim();
            String prixFilm = movieRentalRate.getText().toString().replace("Tarif de location :", "").trim();
            verifierEtAjouterAuPanier(filmId, titreFilm, prixFilm);
        });
    }

    private void verifierEtAjouterAuPanier(int filmId, String titreFilm, String prixFilm) {
        String urlStock = DonneesPartagees.getURLConnexion() + "/toad/inventory/available/getById?id=" + filmId;
        Log.d(TAG, "URL de vérification du stock : " + urlStock);

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, urlStock,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Réponse reçue de l'API : " + response);

                        try {
                            // Convertir la réponse en entier
                            int stock = Integer.parseInt(response.trim());

                            if (stock > 0) {
                                Log.d(TAG, "Film disponible avec stock : " + stock);
                                PanierManager.getInstance().ajouterAuPanier(filmId, titreFilm, prixFilm);
                                Toast.makeText(FilmDetailsActivity.this, "Ajouté au panier !", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "Stock insuffisant pour le film");
                                Toast.makeText(FilmDetailsActivity.this, "Aucun stock disponible pour ce film.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Erreur de conversion de la réponse en entier", e);
                            Toast.makeText(FilmDetailsActivity.this, "Réponse inattendue de l'API", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Erreur Volley lors de la vérification du stock", error);

                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String responseData = new String(error.networkResponse.data);
                            Log.e(TAG, "Erreur HTTP " + statusCode + " : " + responseData);
                            Toast.makeText(FilmDetailsActivity.this, "Erreur " + statusCode + " : " + responseData, Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "Problème réseau : pas de réponse du serveur");
                            Toast.makeText(FilmDetailsActivity.this, "Impossible de contacter le serveur. Vérifiez votre connexion.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        queue.add(request);
    }
}
