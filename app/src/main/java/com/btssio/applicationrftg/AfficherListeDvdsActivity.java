package com.btssio.applicationrftg;

import android.content.Intent;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AfficherListeDvdsActivity extends AppCompatActivity {

    private SimpleCursorAdapter adapter;
    private MatrixCursor dvdCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficherlistedvds);

        Button btnVoirPanier = findViewById(R.id.btnNavigate);

        btnVoirPanier.setOnClickListener(v -> {
            Intent intent = new Intent(AfficherListeDvdsActivity.this, PanierActivity.class);
            startActivity(intent);
        });


        String[] columns = new String[]{"_id", "title", "releaseYear"};
        dvdCursor = new MatrixCursor(columns);

        String[] from = new String[]{"title", "releaseYear"};
        int[] to = new int[]{R.id.filmName, R.id.filmDate};
        adapter = new SimpleCursorAdapter(this, R.layout.activity_afficherlisteitemsdvds, dvdCursor, from, to, 0);

        ListView listviewDvds = findViewById(R.id.listView);
        listviewDvds.setAdapter(adapter);
        listviewDvds.setTextFilterEnabled(true);

        listviewDvds.setOnItemClickListener((parent, view, position, id) -> {
            dvdCursor.moveToPosition(position);
            int filmId = dvdCursor.getInt(dvdCursor.getColumnIndex("_id"));

            Intent intent = new Intent(AfficherListeDvdsActivity.this, FilmDetailsActivity.class);
            intent.putExtra("filmId", filmId);
            startActivity(intent);
        });

        String apiUrl = DonneesPartagees.getURLConnexion() + "/toad/film/all";
        new AppelerServiceRestGETAfficherListeDvdsTask().execute(apiUrl);
    }

    private class AppelerServiceRestGETAfficherListeDvdsTask extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... urls) {
            String urlString = urls[0];
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                Log.d("AppelerServiceRestGETAfficherListeDvdsTask", "Connexion à l'API établie avec succès");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                Log.d("AppelerServiceRestGETAfficherListeDvdsTask", "Réponse reçue : " + result.toString());

                return new JSONArray(result.toString());

            } catch (Exception e) {
                Log.e("AppelerServiceRestGETAfficherListeDvdsTask", "Erreur de connexion ou de lecture : ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray films) {
            if (films == null) {
                Log.e("AppelerServiceRestGETAfficherListeDvdsTask", "Erreur : la liste films récupérée est nul");
                return;
            }

            try {
                dvdCursor.close();
                dvdCursor = new MatrixCursor(new String[]{"_id", "title", "releaseYear"});

                for (int i = 0; i < films.length(); i++) {
                    JSONObject film = films.getJSONObject(i);
                    int filmId = film.getInt("filmId");
                    String title = film.getString("title");
                    String annee = film.getString("releaseYear");

                    dvdCursor.addRow(new Object[]{filmId, title, annee});
                }

                adapter.changeCursor(dvdCursor);
                Log.d("AppelerServiceRestGETAfficherListeDvdsTask", "Liste mise à jour avec succès");

            } catch (JSONException e) {
                Log.e("AppelerServiceRestGETAfficherListeDvdsTask", "Erreur de parsing du JSON : ", e);
            }
        }
    }

}
