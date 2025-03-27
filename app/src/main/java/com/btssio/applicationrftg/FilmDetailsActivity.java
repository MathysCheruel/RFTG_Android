package com.btssio.applicationrftg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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

    private TextView errorTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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

        String url = "http://10.0.2.2:8080/toad/film/getById?id=" + filmId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            movieTitle.setText("Titre : " + response.getString("title"));
                            movieDescription.setText("Description : " + response.getString("description"));
                            movieReleaseYear.setText("Année de sortie :  " + response.getInt("releaseYear"));
                            movieRentalRate.setText("Tarif de location : " + response.getDouble("rentalRate") + " €");
                            movieLength.setText("Durée : " + response.getInt("length") + " minutes");
                            movieReplacementCost.setText("Coût de remplacement : " + response.getDouble("replacementCost") + " €");
                            movieRating.setText("Classification : " + response.getString("rating"));
                            movieSpecialFeatures.setText("Fonctionnalités spéciales : " + response.getString("specialFeatures"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(FilmDetailsActivity.this, "Erreur dans les données récupérées", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(FilmDetailsActivity.this, "Erreur de connexion " + url, Toast.LENGTH_SHORT).show();
                    }
                });
        Button btnAjouterAuPanier = findViewById(R.id.btnAddCart);

        btnAjouterAuPanier.setOnClickListener(v -> {
            String titreFilm = movieTitle.getText().toString().replace("Titre :", "").trim();
            String prixFilm = movieRentalRate.getText().toString().replace("Prix :", "").trim();

            if (!titreFilm.isEmpty()) {
                PanierManager.getInstance().ajouterAuPanier(titreFilm, prixFilm);
                Toast.makeText(this, "Ajouté au panier !", Toast.LENGTH_SHORT).show();
            }
        });


        queue.add(request);
    }

}

