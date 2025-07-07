package com.example.carcare;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class LogInActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button authButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Seteaza culoarea barii de notificare
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_tint_color));

        mainLayout = findViewById(R.id.main);
        emailEditText = findViewById(R.id.mail_input_text);
        passwordEditText = findViewById(R.id.password_input_text);
        authButton = findViewById(R.id.auth_button);

        initNoFocusInputTextWhenNoKeyboard();
        logInOnClick();
    }

    //Functia pentru click pe butonul de LogIn
    private void logInOnClick(){
        //Adauga un listener pe buton, iar cand acesta este apasat preia textul din campurile de mail si parola
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                String message = "Mail: " + mail + "\nPassword: " + password;

                Toast.makeText(LogInActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    //Functie care face ca casetele de input pentru text sa nu ramana selectate cand tastatura este inactiva
    private void initNoFocusInputTextWhenNoKeyboard(){
        //Se adauga un listener pe tot layout-ul care asculta modificarile
        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeightDiff = 0;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                mainLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = mainLayout.getRootView().getHeight();

                int heightDiff = screenHeight - r.height();

                //Daca diferenta e mare inseamna ca tastatura e deschisa
                //Daca diferenta scade inseamna ca tastatura e ascunsa
                if (previousHeightDiff > heightDiff) {
                    //Tastatura s-a inchis
                    //Scoatem focusul si ascundem tastatura
                    if (emailEditText.hasFocus()) {
                        emailEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
                        }
                    }
                    else if (passwordEditText.hasFocus()) {
                        passwordEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
                        }
                    }
                }
                previousHeightDiff = heightDiff;
            }
        });
    }
}