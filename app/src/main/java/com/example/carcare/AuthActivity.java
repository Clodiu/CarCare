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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class AuthActivity extends AppCompatActivity {

    //Declaratiile pentru obiectele care trebuiesc preluate din fisierul xml

    private CardView signUpCardView;
    private CardView logInCardView;
    private RelativeLayout mainLayout;
    private TextInputEditText signUpNameEditText;
    private TextInputEditText signUpMailEditText;
    private TextInputEditText signUpPasswordEditText;
    private Button signUpButton;

    private TextInputEditText logInMailEditText;
    private TextInputEditText logInPasswordEditText;
    private Button logInButton;

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

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_tint_color));

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


    private void signUpOnClick(){
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = signUpNameEditText.getText().toString().trim();
                String mail = signUpMailEditText.getText().toString().trim();
                String password = signUpPasswordEditText.getText().toString().trim();

                String message = "Name: " + name + "\nMail: " + mail + "\nPassword: " + password;

                Toast.makeText(AuthActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logInOnClick(){
        //Adauga un listener pe buton, iar cand acesta este apasat preia textul din campurile de mail si parola
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = logInMailEditText.getText().toString().trim();
                String password = logInPasswordEditText.getText().toString().trim();

                String message = "Mail: " + mail + "\nPassword: " + password;

                Toast.makeText(AuthActivity.this, message, Toast.LENGTH_LONG).show();
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