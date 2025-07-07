package com.example.carcare;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {

    //Declaratiile pentru obiectele care trebuiesc preluate din fisierul xml
    private RelativeLayout mainLayout;
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button authButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Culoarea barii de notificari
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_tint_color));

        mainLayout = findViewById(R.id.main);
        nameEditText = findViewById(R.id.name_input_text);
        emailEditText = findViewById(R.id.mail_input_text);
        passwordEditText = findViewById(R.id.password_input_text);
        authButton = findViewById(R.id.auth_button);

        initNoFocusInputTextWhenNoKeyboard();
        signUpOnClick();

    }

    private void signUpOnClick(){
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString().trim();
                String mail = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                String message = "Name: " + name + "\nMail: " + mail + "\nPassword: " + password;

                Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_LONG).show();
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
                    if (nameEditText.hasFocus()) {
                        nameEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
                        }
                    }
                    else if (emailEditText.hasFocus()) {
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