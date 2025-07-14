package com.example.carcare;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CarListActivity extends AppCompatActivity implements CarRecyclerViewInterface{

    private Button logOutButton;
    private CardView settingsCardView;
    private TextView usernameTextView;
    private TextView emailTextView;
    private int userId = 2;
    private TextInputEditText searchEditText;
    private TextInputEditText manufacturerEditText;
    private TextInputEditText modelEditText;
    private TextInputEditText registerPlateEditText;
    private Button saveButton;
    private RelativeLayout mainLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton addButton;

    private FloatingActionButton settingsButton;
    private CardView addCar;

    CarList_RecyclerViewAdapter adapter;

    private ArrayList<Car> userCars = new ArrayList<>();
    private ArrayList<Car> filteredCars = new ArrayList<>();

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

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userId = prefs.getInt("USER_ID", -1);

        mainLayout = findViewById(R.id.main);
        searchEditText = findViewById(R.id.search_edit_text);
        recyclerView = findViewById(R.id.recycler_view_cars);
        addButton = findViewById(R.id.button_add);
        manufacturerEditText = findViewById(R.id.manufacturer_input_text);
        modelEditText = findViewById(R.id.model_input_text);
        registerPlateEditText = findViewById(R.id.register_plate_input_text);
        saveButton = findViewById(R.id.save_button);
        addCar = findViewById(R.id.add_car_card_view);
        settingsButton = findViewById(R.id.button_settings);
        logOutButton = findViewById(R.id.logout_button);
        settingsCardView = findViewById(R.id.settings_card_view);
        usernameTextView = findViewById(R.id.username_text_view);
        emailTextView = findViewById(R.id.email_text_view);

        //setUpUserCars();
        loadCarsFromDatabase();

        adapter = new CarList_RecyclerViewAdapter(this, userCars, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initSearch();
        initNoFocusSearchbarWhenNoKeyboard();
        setUpAddButton();
        setUpSaveButton();
        setUpSettingsButton();
        setUpLogOutButton();
        loadUserAndEmailFromDataBase();


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
                if(settingsCardView.getVisibility() == View.VISIBLE){
                    settingsCardView.setVisibility(View.INVISIBLE);
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

                if(!manufacturer.isEmpty() && !model.isEmpty() && !registerPlate.isEmpty()){

                    ConnectionClass connectionClass = new ConnectionClass();
                    Connection conn = connectionClass.CONN();
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        try{
                            if(conn == null){

                            }else{
                                Statement stmt = conn.createStatement();

                                //Creeaza query-ul cu RETURN_GENERATED_KEYS
                                String query = "INSERT INTO Cars (Manufacturer,Model,RegisterPlate,LastServiced,Creator_ID) VALUES (?,?,?,?,?)";
                                java.sql.PreparedStatement pstmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

                                //Seteaza parametrii de inserat in Cars
                                pstmt.setString(1, manufacturer);
                                pstmt.setString(2,model);
                                pstmt.setString(3,registerPlate);

                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.YEAR, 2000);
                                cal.set(Calendar.MONTH, Calendar.JANUARY);  // atenție: lunile sunt 0-based în Calendar
                                cal.set(Calendar.DAY_OF_MONTH, 1);

                                Date sqlDate = new Date(cal.getTimeInMillis());

                                pstmt.setDate(4,sqlDate);
                                pstmt.setInt(5,userId);

                                //Ruleaza insert-ul
                                pstmt.executeUpdate();

                                //Preia cheia generata
                                ResultSet rs = pstmt.getGeneratedKeys();
                                int generatedCarId = -1;

                                if (rs.next()) {
                                    generatedCarId = rs.getInt(1); // indexul coloanei generate
                                }

                                rs.close();
                                pstmt.close();

                                //Insereaza in UserCarAccess perechea User_ID, Car_ID. Ca creatorul sa aiba acces la masina.
                                if (generatedCarId != -1) {
                                    String accessQuery = "INSERT INTO UserCarAccess (User_ID, Car_ID) VALUES (?, ?)";
                                    PreparedStatement accessStmt = conn.prepareStatement(accessQuery);
                                    accessStmt.setInt(1, userId);
                                    accessStmt.setInt(2, generatedCarId);

                                    accessStmt.executeUpdate();
                                    accessStmt.close();
                                }

                                conn.close();

                                // Dupa inserare, incarca din nou datele pe thread-ul UI
                                runOnUiThread(() -> loadCarsFromDatabase());

                            }
                        }catch (Exception e){
                            throw new RuntimeException(e);
                        }
                    });
                }else{
                    Toast.makeText(CarListActivity.this, "All fields must be filled!", Toast.LENGTH_LONG).show();
                }

                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(CarListActivity.this));

            }
        });
    }

    private void setUpSettingsButton(){
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(settingsCardView.getVisibility() == View.INVISIBLE){
                    settingsCardView.setVisibility(View.VISIBLE);
                }else if(settingsCardView.getVisibility() == View.VISIBLE){
                    settingsCardView.setVisibility(View.INVISIBLE);
                }
                if(addCar.getVisibility() == View.VISIBLE){
                    addCar.setVisibility(View.INVISIBLE);
                    addButton.setImageResource(R.drawable.add_icon_24dp);
                }
            }
        });
    }

    private void setUpLogOutButton(){
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sterge USER_ID din SharedPreferences
                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("USER_ID");
                editor.apply();

                // Navigheaza inapoi la AuthActivity
                Intent intent = new Intent(CarListActivity.this, AuthActivity.class);
                startActivity(intent);

                // Inchide activitatea curenta ca sa nu mai poti reveni cu Back
                finish();
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
                //Opreste runnable-ul anterior daca tasteaza din nou rapid
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                //Seteaza runnable-ul cu delay
                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        filterCars(s.toString());
                    }
                };

                handler.postDelayed(searchRunnable, 300); // 500 ms delay
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void filterCars(String query){
        filteredCars.clear();
        if (query.isEmpty()) {
            filteredCars.addAll(userCars);  // dacă nu ai text, arată tot
        } else {
            for (Car car : userCars) {
                if (car.getManufacturer().toLowerCase().contains(query.toLowerCase()) ||
                        car.getModel().toLowerCase().contains(query.toLowerCase()) ||
                        car.getRegisterPlate().toLowerCase().contains(query.toLowerCase())) {
                    filteredCars.add(car);
                }
            }
        }
        runOnUiThread(() -> {
            adapter.setCars(filteredCars);
            adapter.notifyDataSetChanged();
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

    @Override
    public void onItemClick(int position) {
        if(filteredCars.isEmpty()){
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("CAR_ID", userCars.get(position).getCarId());
            editor.apply();
        }else{
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("CAR_ID", filteredCars.get(position).getCarId());
            editor.apply();
        }
        Intent intent = new Intent(CarListActivity.this, CarHistoryActivity.class);
        startActivity(intent);
    }

    private void loadCarsFromDatabase(){
        userCars.clear();
        ConnectionClass connectionClass = new ConnectionClass();
        Connection conn = connectionClass.CONN();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Statement stmt = null;
            ResultSet rs = null;
            try{
                if(conn == null){
                    Toast.makeText(this,"Problema de conexiune",Toast.LENGTH_LONG).show();
                }else{
                    stmt = conn.createStatement();

                    String query = "SELECT c.Car_ID, Manufacturer, Model, RegisterPlate, LastServiced, Creator_ID FROM Cars c,UserCarAccess a WHERE c.Car_ID=a.Car_ID AND a.User_ID = "+userId;

                    rs = stmt.executeQuery(query);

                    while (rs.next()) {
                        int carId = rs.getInt("Car_ID");
                        String manufacturer = rs.getString("Manufacturer");
                        String model = rs.getString("Model");
                        String registerPlate = rs.getString("RegisterPlate");
                        int creatorId = rs.getInt("Creator_ID");
                        Date date = rs.getDate("LastServiced");

                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                        String dateString = formatter.format(date);

                        userCars.add(new Car(carId,manufacturer,model,registerPlate, dateString,creatorId));
                    }

                    conn.close();

                    // Actualizează UI pe thread-ul principal
                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                    });

                }
            }catch (Exception e){
                throw new RuntimeException(e);
            }finally {
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

    private void loadUserAndEmailFromDataBase(){
        ConnectionClass connectionClass = new ConnectionClass();
        Connection conn = connectionClass.CONN();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Statement stmt = null;
            ResultSet rs = null;
            try{
                if(conn == null){
                    Toast.makeText(this,"Problema de conexiune",Toast.LENGTH_LONG).show();
                }else{
                    stmt = conn.createStatement();

                    String query = "SELECT Name,Email FROM Users WHERE User_ID = "+userId;

                    rs = stmt.executeQuery(query);

                    while (rs.next()) {
                        String name =rs.getString("Name");
                        String email =rs.getString("Email");

                        usernameTextView.setText(name);
                        emailTextView.setText(email);
                    }
                    conn.close();
                }
            }catch (Exception e){
                throw new RuntimeException(e);
            }finally {
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