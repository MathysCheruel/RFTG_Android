package com.btssio.applicationrftg;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//SPINNER URLs-D
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.ArrayAdapter ;
import android.widget.Toast;
//SPINNER URLs-F

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginCheckActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String[] listeURLs = null;
    private EditText emailEditText, passwordEditText;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //SPINNER URLs-D
        listeURLs = getResources().getStringArray(R.array.listeURLs);
        Spinner spinnerURLs=findViewById(R.id.spinnerURLs);
        spinnerURLs.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence>adapterListeURLs=ArrayAdapter.createFromResource(this, R.array.listeURLs, android.R.layout.simple_spinner_item);
        adapterListeURLs.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerURLs.setAdapter(adapterListeURLs);
        //SPINNER URLs-F



        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        errorTextView = findViewById(R.id.errorTextView);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> validateCredentials());
    }

    private void validateCredentials() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        //NEW URL-D
        EditText edittextURL = findViewById(R.id.URLText);
        DonneesPartagees.setURLConnexion(edittextURL.getText().toString());

        Toast.makeText(getApplicationContext(), DonneesPartagees.getURLConnexion(), Toast.LENGTH_SHORT).show();
        //NEW URL-F

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            errorTextView.setText("Veuillez remplir tous les champs");
            errorTextView.setVisibility(View.VISIBLE);
        } else {
            String apiUrl = DonneesPartagees.getURLConnexion() + "/toad/customer/getByEmail?email=" + email;
            new FetchUserTask(email, password).execute(apiUrl);
        }
    }

    private class FetchUserTask extends AsyncTask<String, Void, JSONObject> {
        private final String inputEmail;
        private final String inputPassword;

        public FetchUserTask(String email, String password) {
            this.inputEmail = email;
            this.inputPassword = password;
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            String urlString = urls[0];
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                Log.d("FetchUserTask", "Connexion à l'API établie : " + urlString);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                Log.d("FetchUserTask", "Réponse reçue : " + result.toString());

                return new JSONObject(result.toString());

            } catch (Exception e) {
                Log.e("FetchUserTask", "Erreur de connexion ou de lecture : ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject user) {
            if (user == null) {
                Log.e("FetchUserTask", "Aucun utilisateur trouvé avec cet email");
                errorTextView.setText("Identifiants incorrects");
                errorTextView.setVisibility(View.VISIBLE);
                return;
            }

            try {
                String storedPassword = user.getString("password");

                if (storedPassword.equals(inputPassword)) {
                    // Stocker les identifiants localement
                    int userId = user.getInt("customerId");
                    saveUserCredentials(inputEmail, inputPassword, userId);

                    // Redirection après connexion réussie
                    errorTextView.setVisibility(View.GONE);
                    Intent intent = new Intent(LoginCheckActivity.this, AfficherListeDvdsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    errorTextView.setText("Mot de passe incorrect");
                    errorTextView.setVisibility(View.VISIBLE);
                }

            } catch (JSONException e) {
                Log.e("FetchUserTask", "Erreur de parsing du JSON : ", e);
            }
        }
    }

    private void saveUserCredentials(String email, String password, int userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putInt("user_id", userId);  // Stocke l'ID de l'utilisateur
        editor.apply();
        Log.d("LoginCheckActivity", "Identifiants stockés localement");
    }

    //SPINNER URLs-D
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Make toast of the name of the course which is selected in the spinner
        //Toast.makeText(getApplicationContext(), listeURLs[position], Toast.LENGTH_SHORT).show();
        //DonneesPartagees.setURLConnexion(listeURLs[position]);
        //Toast.makeText(getApplicationContext(), DonneesPartagees.getURLConnexion(), Toast.LENGTH_SHORT).show();
        //NEW URL-D
        //DonneesPartagees.setURLConnexion(listeURLs[position]);
        EditText URLText = findViewById(R.id.URLText);
        URLText.setText(listeURLs[position]);
        //NEW URL-F
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // No action needed when no selection is made
    }
    //SPINNER URLs-F

}
