package com.example.carcare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carcare.connection.ConnectionClass;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthActivity extends AppCompatActivity {

    //Declaratiile pentru obiectele care trebuiesc preluate din fisierul xml

    //Card view-urile care reprezinta formularele pentru signup si login
    private CardView signUpCardView;
    private CardView logInCardView;

    //Layout-ul general al paginii
    private RelativeLayout mainLayout;


    //Componentele formularului de sign up
    private TextInputEditText signUpNameEditText;
    private TextInputEditText signUpMailEditText;
    private TextInputEditText signUpPasswordEditText;
    private Button signUpButton;

    //Compunentele formularului de login
    private TextInputEditText logInMailEditText;
    private TextInputEditText logInPasswordEditText;
    private Button logInButton;

    //Butonul care face trecerea intre formulare
    private FloatingActionButton swapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Setam manual culoarea barii de notificare
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_tint_color));

        //Conectam variablele din clasa cu corespondentii din fisierul xml
        mainLayout = findViewById(R.id.main);

        signUpNameEditText = findViewById(R.id.signup_name_input_text);
        signUpMailEditText = findViewById(R.id.signup_mail_input_text);
        signUpPasswordEditText = findViewById(R.id.signup_password_input_text);
        signUpButton = findViewById(R.id.signup_button);

        logInMailEditText = findViewById(R.id.login_mail_input_text);
        logInPasswordEditText = findViewById(R.id.login_password_input_text);
        logInButton = findViewById(R.id.login_button);

        signUpCardView = findViewById(R.id.signup_card_view);
        logInCardView = findViewById(R.id.login_card_view);

        swapButton = findViewById(R.id.button_swap);

        initNoFocusInputTextWhenNoKeyboard();
        signUpOnClick();
        logInOnClick();
        swapOnClick();
    }

    //Metoda ce schimba intre formularele de sign up si log in
    private void swapOnClick(){
        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signUpCardView.getVisibility() == View.VISIBLE){
                    signUpCardView.setVisibility(View.INVISIBLE);
                    logInCardView.setVisibility(View.VISIBLE);
                } else if(signUpCardView.getVisibility() == View.INVISIBLE){
                    signUpCardView.setVisibility(View.VISIBLE);
                    logInCardView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    //Metoda ce seteaza onClickListener pentru butonul de signup
    private void signUpOnClick(){
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Se preiau valorile introduse de utilizator
                String name = Objects.requireNonNull(signUpNameEditText.getText()).toString().trim();
                String mail = Objects.requireNonNull(signUpMailEditText.getText()).toString().trim();
                String password = Objects.requireNonNull(signUpPasswordEditText.getText()).toString().trim();

                //Daca unul din aceste input-uri este gol, nu se poate realiza inregistrarea
                if (name.isEmpty() || mail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(AuthActivity.this, "Complete all fields", Toast.LENGTH_SHORT).show();
                }else if(!isValidEmail(mail)){
                    Toast.makeText(AuthActivity.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                } else{
                    //Se creeaza o conexiune cu serverul de pe laptop
                    ConnectionClass connectionClass = new ConnectionClass();
                    Connection conn = connectionClass.CONN();

                    //Se creeaza un executor care face interogarea in paralel(altfel se blocheaza interfata)
                    ExecutorService executorService = Executors.newSingleThreadExecutor();

                    executorService.execute(() -> {
                        PreparedStatement insertStmt = null;
                        Statement idStmt = null;
                        ResultSet rs = null;

                        try {
                            if (conn == null) {
                                runOnUiThread(() ->
                                        Toast.makeText(AuthActivity.this, "Problema de conexiune", Toast.LENGTH_LONG).show()
                                );
                            } else {
                                //Verificam daca emailul exista deja
                                String checkQuery = "SELECT COUNT(*) AS cnt FROM Users WHERE Email = ?";
                                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                                checkStmt.setString(1, mail);
                                ResultSet checkRs = checkStmt.executeQuery();
                                if (checkRs.next() && checkRs.getInt("cnt") > 0) {
                                    runOnUiThread(() ->
                                            Toast.makeText(AuthActivity.this, "Email already exists!", Toast.LENGTH_SHORT).show()
                                    );
                                    checkRs.close();
                                    checkStmt.close();
                                    return;
                                }
                                checkRs.close();
                                checkStmt.close();

                                //Inseram userul in baza de date
                                String insertQuery = "INSERT INTO Users (Name, Email, Password) VALUES (?, ?, ?)";
                                insertStmt = conn.prepareStatement(insertQuery);
                                insertStmt.setString(1, name);
                                insertStmt.setString(2, mail);
                                insertStmt.setString(3, password);
                                insertStmt.executeUpdate();

                                //Obtinem ID-ul generat al userului
                                String getIdQuery = "SELECT User_ID FROM Users WHERE Email = ?";
                                PreparedStatement getIdStmt = conn.prepareStatement(getIdQuery);
                                getIdStmt.setString(1, mail);

                                rs = getIdStmt.executeQuery();

                                int userId = -1;
                                if (rs.next()) {
                                    userId = rs.getInt("User_ID");
                                }

                                if (userId != -1) {
                                    int finalUserId = userId;
                                    runOnUiThread(() -> {
                                        //Salvam ID-ul userului in shared preferences
                                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putInt("USER_ID", finalUserId);
                                        editor.apply();

                                        //Deschidem CarListActivity
                                        Intent intent = new Intent(AuthActivity.this, CarListActivity.class);
                                        intent.putExtra("USER_ID", finalUserId);
                                        startActivity(intent);
                                        finish();
                                    });
                                } else {
                                    runOnUiThread(() ->
                                            Toast.makeText(AuthActivity.this, "Failed to get User ID", Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() ->
                                    Toast.makeText(AuthActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                        } finally {
                            try {
                                if (rs != null) rs.close();
                                if (insertStmt != null) insertStmt.close();
                                if (conn != null) conn.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });


                }



            }
        });
    }

    private boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void logInOnClick(){
        //Adauga un listener pe buton, iar cand acesta este apasat preia textul din campurile de mail si parola
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = Objects.requireNonNull(logInMailEditText.getText()).toString().trim();
                String password = Objects.requireNonNull(logInPasswordEditText.getText()).toString().trim();

                if(!isValidEmail(mail)){
                    Toast.makeText(AuthActivity.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                }else {
                    ConnectionClass connectionClass = new ConnectionClass();
                    Connection conn = connectionClass.CONN();

                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        PreparedStatement stmt = null;
                        ResultSet rs = null;

                        try {
                            if (conn == null) {
                                runOnUiThread(() -> {
                                    Toast.makeText(AuthActivity.this, "Problema de conexiune", Toast.LENGTH_LONG).show();
                                });
                            } else {
                                String query = "SELECT User_ID FROM Users WHERE Email = ? AND Password = ?";
                                stmt = conn.prepareStatement(query);
                                stmt.setString(1, mail);
                                stmt.setString(2, password);

                                rs = stmt.executeQuery();

                                if (rs.next()) {
                                    int userId = rs.getInt("User_ID");

                                    runOnUiThread(() -> {
                                        // Porneste CarListActivity si salveaza userID
                                        Intent intent = new Intent(AuthActivity.this, CarListActivity.class);
                                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putInt("USER_ID", userId);
                                        editor.apply();
                                        startActivity(intent);
                                        finish();
                                    });
                                } else {
                                    runOnUiThread(() ->
                                            Toast.makeText(AuthActivity.this, "Login failed: invalid credentials.", Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(AuthActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        } finally {
                            try {
                                if (rs != null) rs.close();
                                if (stmt != null) stmt.close();
                                if (conn != null) conn.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    //Functia pentru scoaterea selectiei casutei de input cand tastatura nu este vizibila
    private void initNoFocusInputTextWhenNoKeyboard(){
        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeightDiff = 0;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                mainLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = mainLayout.getRootView().getHeight();

                int heightDiff = screenHeight - r.height();

                // Dacă diferența e mare, înseamnă că tastatura e afișată
                // Dacă diferența scade, tastatura s-a ascuns
                if (previousHeightDiff > heightDiff) {
                    // Tastatura tocmai s-a închis
                    // Scoatem focusul și ascundem tastatura (de precauție)
                    if (signUpNameEditText.hasFocus()) {
                        signUpNameEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(signUpNameEditText.getWindowToken(), 0);
                        }
                    }
                    else if (signUpMailEditText.hasFocus()) {
                        signUpMailEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(signUpMailEditText.getWindowToken(), 0);
                        }
                    }
                    else if (signUpPasswordEditText.hasFocus()) {
                        signUpPasswordEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(signUpPasswordEditText.getWindowToken(), 0);
                        }
                    } else if (logInMailEditText.hasFocus()) {
                        logInMailEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(logInMailEditText.getWindowToken(), 0);
                        }
                    }
                    else if (logInPasswordEditText.hasFocus()) {
                        logInPasswordEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(logInPasswordEditText.getWindowToken(), 0);
                        }
                    }
                }
                previousHeightDiff = heightDiff;
            }
        });
    }
}