package com.example.carcare;


import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class CarListActivity extends AppCompatActivity {

    private TextInputEditText searchEditText;
    private TextInputEditText manufacturerEditText;
    private TextInputEditText modelEditText;
    private TextInputEditText registerPlateEditText;
    private Button saveButton;
    private RelativeLayout mainLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton addButton;
    private CardView addCar;

    CarList_RecyclerViewAdapter adapter;

    private ArrayList<Car> userCars = new ArrayList<>();

    private Handler handler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_car_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_tint_color));

        mainLayout = findViewById(R.id.main);
        searchEditText = findViewById(R.id.search_edit_text);
        recyclerView = findViewById(R.id.recycler_view_cars);
        addButton = findViewById(R.id.button_add);
        manufacturerEditText = findViewById(R.id.manufacturer_input_text);
        modelEditText = findViewById(R.id.model_input_text);
        registerPlateEditText = findViewById(R.id.register_plate_input_text);
        saveButton = findViewById(R.id.save_button);
        addCar = findViewById(R.id.add_car_card_view);

        setUpUserCars();

        adapter = new CarList_RecyclerViewAdapter(this, userCars);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initSearch();
        initNoFocusSearchbarWhenNoKeyboard();
        setUpAddButton();
        setUpSaveButton();


    }

    //Metoda pentru butonul de adaugat masina care afiseaza/ascunde un cardview
    private void setUpAddButton(){
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addCar.getVisibility() == View.VISIBLE){
                    addCar.setVisibility(View.INVISIBLE);
                    addButton.setImageResource(R.drawable.add_icon_24dp);
                }else if(addCar.getVisibility() == View.INVISIBLE){
                    addCar.setVisibility(View.VISIBLE);
                    addButton.setImageResource(R.drawable.close_icon_24dp);
                }
            }
        });
    }

    //Metoda care seteaza comportamentul butonului de save
    private void setUpSaveButton(){
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String manufacturer = manufacturerEditText.getText().toString().trim();
                String model = modelEditText.getText().toString().trim();
                String registerPlate = registerPlateEditText.getText().toString().trim();

                String message = "Manufacturer: " + manufacturer + "\nModel: " + model + "\nRegister Plate: " + registerPlate;

                Toast.makeText(CarListActivity.this, message, Toast.LENGTH_LONG).show();

                manufacturerEditText.setText("");
                modelEditText.setText("");
                registerPlateEditText.setText("");

                /*

                userCars.add(new Car(manufacturer,model,registerPlate,"never"));

                adapter = new CarList_RecyclerViewAdapter(CarListActivity.this, userCars);

                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(CarListActivity.this));
                 */

            }
        });
    }

    //Metoda care seteaza implicit niste masini
    private void setUpUserCars(){
        for(int i = 1 ; i <= 20 ; i++){
            userCars.add(new Car());
        }
    }

    //Metoda care face ca un textInput(ca cele din login si signup) sa se comporte ca un search bar
    //Cand textul se modifica, in functie de un delay(contra tastarii rapide), se cauta in lista de obiecte
    private void initSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Oprește runnable-ul anterior dacă tastează din nou rapid
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                //Setează runnable-ul cu delay
                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CarListActivity.this, "Cauți: " + s.toString(), Toast.LENGTH_SHORT).show();
                    }
                };

                handler.postDelayed(searchRunnable, 300); // 500 ms delay
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    //Functia pentru scoaterea focusului cand tastatura nu este activa
    private void initNoFocusSearchbarWhenNoKeyboard(){
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
                    if (searchEditText.hasFocus()) {
                        searchEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                        }
                    }
                    else if (manufacturerEditText.hasFocus()) {
                        manufacturerEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(manufacturerEditText.getWindowToken(), 0);
                        }
                    }else if (modelEditText.hasFocus()) {
                        modelEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(modelEditText.getWindowToken(), 0);
                        }
                    }else if (registerPlateEditText.hasFocus()) {
                        registerPlateEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(registerPlateEditText.getWindowToken(), 0);
                        }
                    }
                }
                previousHeightDiff = heightDiff;
            }
        });
    }
}