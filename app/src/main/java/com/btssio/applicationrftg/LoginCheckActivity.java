package com.btssio.applicationrftg;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoginCheckActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private TextView errorTextView;
    private Map<String, String> userCredentials = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        errorTextView = findViewById(R.id.errorTextView);
        Button loginButton = findViewById(R.id.loginButton);

        String apiUrl = "http://10.0.2.2:8080/toad/user/all";
        new FetchUserCredentialsTask().execute(apiUrl);

        loginButton.setOnClickListener(v -> validateCredentials());
    }

    private void validateCredentials() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            errorTextView.setText("Veuillez remplir tous les champs");
            errorTextView.setVisibility(View.VISIBLE);
        } else if (!userCredentials.containsKey(email) || !userCredentials.get(email).equals(password)) {
            errorTextView.setText("Identifiants incorrects");
            errorTextView.setVisibility(View.VISIBLE);
        } else {
            errorTextView.setVisibility(View.GONE);
            Intent intent = new Intent(LoginCheckActivity.this, AfficherListeDvdsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private class FetchUserCredentialsTask extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... urls) {
            String urlString = urls[0];
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                Log.d("FetchUserCredentialsTask", "Connexion à l'API établie avec succès");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                Log.d("FetchUserCredentialsTask", "Réponse reçue : " + result.toString());

                return new JSONArray(result.toString());

            } catch (Exception e) {
                Log.e("FetchUserCredentialsTask", "Erreur de connexion ou de lecture : ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray users) {
            if (users == null) {
                Log.e("FetchUserCredentialsTask", "Erreur : la liste des utilisateurs récupérée est nulle");
                errorTextView.setText("Erreur lors du chargement des utilisateurs");
                errorTextView.setVisibility(View.VISIBLE);
                return;
            }

            try {
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    String email = user.getString("email");
                    String password = user.getString("password");

                    userCredentials.put(email, password);
                }

                Log.d("FetchUserCredentialsTask", "Liste des utilisateurs mise à jour avec succès");

            } catch (JSONException e) {
                Log.e("FetchUserCredentialsTask", "Erreur de parsing du JSON : ", e);
            }
        }
    }
}
