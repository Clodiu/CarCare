package com.example.carcare;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.carcare.adapters.MyPagerAdapter;
import com.example.carcare.connection.ConnectionClass;
import com.example.carcare.fragments.CalendarFragment;
import com.example.carcare.fragments.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarHistoryActivity extends AppCompatActivity{

    private int userId = 2;

    private int carId = 1005;

    private DatePickerDialog datePickerDialog;
    private Button dateButton;

    private ConstraintLayout mainLayout;

    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager;
    private MyPagerAdapter pagerAdapter;

    private FloatingActionButton addButton;
    private CardView addNote;

    private TextInputEditText titleEditText;
    private TextInputEditText descriptionEditText;

    private TextInputEditText kmEditText;

    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_tint_color));

        // Activeaza iconite intunecate in bara de notificari pentru fundal deschis
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userId = prefs.getInt("USER_ID", -1);
        carId = prefs.getInt("CAR_ID", -1);

        mainLayout = findViewById(R.id.main);

        dateButton = findViewById(R.id.date_picker_button);
        addNote = findViewById(R.id.add_note_card_view);
        addButton = findViewById(R.id.button_add);
        titleEditText = findViewById(R.id.title_input_text);
        descriptionEditText = findViewById(R.id.description_input_text);
        kmEditText = findViewById(R.id.km_input_text);
        saveButton = findViewById(R.id.save_button);

        setUpAddButton();
        setUpSaveButton();
        initDatePicker();

        dateButton.setText(getTodaysDate());


        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        viewPager = findViewById(R.id.view_pager);

        //Setarea adaptorului si creearea legaturii cu viewPager(locul in care se incarca fragmentele)
        pagerAdapter = new MyPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        //Limita de pagini care raman in memorie, 3 in plus fata de cea curenta
        viewPager.setOffscreenPageLimit(3);

        // Sincronizarea barii de navigatie cu view pager, in functie de ce iconita e selectata din cele 4
        //muta pe pagina respectiva(nu creeaza una noua, ci o refolosese pe cea deja deschisa)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                viewPager.setCurrentItem(0, true);
                return true;
            } else if (id == R.id.calendar) {
                viewPager.setCurrentItem(1, true);
                return true;
            } else if (id == R.id.ai_pic) {
                viewPager.setCurrentItem(2, true);
                return true;
            } else if (id == R.id.settings) {
                viewPager.setCurrentItem(3, true);
                return true;
            }
            return false;
        });

        // Sincronizare inversa cand schimbi se da swipe in viewPager se modifica bara de navigatie
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.home);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.calendar);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.ai_pic);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.settings);
                        break;
                }
            }
        });

        initNoFocusSearchbarWhenNoKeyboard();
    }

    private String getTodaysDate(){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        month = month +1;
        return makeDateString(day,month,year);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = makeDateString(dayOfMonth,month, year);
                dateButton.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    public void openDatePicker(View view){
        datePickerDialog.show();
    }

    private String makeDateString(int day, int month, int year){
        return day+"-"+month+"-"+year;
    }

    private void setUpAddButton(){
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addNote.getVisibility() == View.VISIBLE){
                    addNote.setVisibility(View.INVISIBLE);
                    addButton.setImageResource(R.drawable.add_icon_24dp);
                }else if(addNote.getVisibility() == View.INVISIBLE){
                    addNote.setVisibility(View.VISIBLE);
                    addButton.setImageResource(R.drawable.close_icon_24dp);
                }
            }
        });
    }

    private void setUpSaveButton(){
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();
                String km = kmEditText.getText().toString().trim();
                CharSequence buttonText = dateButton.getText();
                String date = (buttonText != null) ? buttonText.toString().trim() : "No date selected";

                if(!title.isEmpty() && !km.isEmpty()){
                    int kilometers = Integer.parseInt(km);
                    ConnectionClass connectionClass = new ConnectionClass();
                    Connection conn = connectionClass.CONN();
                    ExecutorService executorService = Executors.newSingleThreadExecutor();

                    executorService.execute(() -> {
                        try{
                            if(conn == null){

                            }else{
                                Statement stmt = conn.createStatement();

                                //Creeaza query-ul
                                String query = "INSERT INTO Notes (Title,Description,Kilometers,Date,Creator_ID,Car_ID) VALUES (?,?,?,?,?,?)";
                                java.sql.PreparedStatement pstmt = conn.prepareStatement(query);

                                //Seteaza parametrii de inserat in Cars
                                pstmt.setString(1, title);
                                pstmt.setString(2,description);
                                pstmt.setInt(3,kilometers);

                                // Creeaza un formatter cu formatul potrivit
                                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

                                // Transformam String-ul într-un java.util.Date
                                java.util.Date utilDate = formatter.parse(date);

                                // Transformam in java.sql.Date (potrivit pentru SQL)
                                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                pstmt.setDate(4,sqlDate);
                                pstmt.setInt(5,userId);
                                pstmt.setInt(6,carId);

                                //Ruleaza insert-ul
                                pstmt.executeUpdate();
                                pstmt.close();
                                conn.close();

                                //Thread UI pentru update
                                runOnUiThread(() -> {
                                    updateLastServicedFromLatestNote();
                                    HomeFragment homeFragment = (HomeFragment) pagerAdapter.getFragment(0);
                                    if (homeFragment != null) {
                                        homeFragment.loadNotesFromDatabase(); //reactualizam notitele pentru a o contine si pe cea nou adaugata
                                        homeFragment.updateAdapter();
                                    }
                                    CalendarFragment calendarFragment = (CalendarFragment) pagerAdapter.getFragment(1);
                                    if (calendarFragment != null) {
                                        calendarFragment.setUpHighlightedDays();  // metoda care incarca si evidentiaza zilele din calendar calendar
                                    }
                                });

                            }
                        }catch (Exception e){
                            throw new RuntimeException(e);
                        }
                    });



                }else{
                    Toast.makeText(CarHistoryActivity.this,"Make sure to complete all the fields.",Toast.LENGTH_SHORT).show();
                }
                titleEditText.setText("");
                descriptionEditText.setText("");
                kmEditText.setText("");

            }
        });
    }

    private void initNoFocusSearchbarWhenNoKeyboard(){
        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeightDiff = 0;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                mainLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = mainLayout.getRootView().getHeight();

                int heightDiff = screenHeight - r.height();

                // Dacă diferenta e mare, inseamna ca tastatura e afisata
                // Dacă diferenta scade, tastatura s-a ascuns
                if (previousHeightDiff > heightDiff) {
                    // Tastatura tocmai s-a inchis
                    // Scoatem focusul si ascundem tastatura
                    if (titleEditText.hasFocus()) {
                        titleEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
                        }
                    }
                    else if (descriptionEditText.hasFocus()) {
                        descriptionEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(descriptionEditText.getWindowToken(), 0);
                        }
                    } else if (kmEditText.hasFocus()){
                        kmEditText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(kmEditText.getWindowToken(), 0);
                        }
                    }
                }
                previousHeightDiff = heightDiff;
            }
        });
    }

    private void updateLastServicedFromLatestNote() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            ConnectionClass connectionClass = new ConnectionClass();
            Connection conn = connectionClass.CONN();

            if (conn != null) {
                try {
                    //Ia cea mai recenta notita dupa Data DESC
                    String query = "SELECT TOP 1 Date FROM Notes WHERE Car_ID = ? ORDER BY Date DESC";
                    java.sql.PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, carId);

                    java.sql.ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        java.sql.Date latestDate = rs.getDate("Date");

                        //Update Cars.LastServiced
                        String updateQuery = "UPDATE Cars SET LastServiced = ? WHERE Car_ID = ?";
                        java.sql.PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                        updateStmt.setDate(1, latestDate);
                        updateStmt.setInt(2, carId);
                        updateStmt.executeUpdate();

                        updateStmt.close();

                    }

                    rs.close();
                    pstmt.close();
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void navigateToHomeFragment() {
        viewPager.setCurrentItem(0, true); // indexul 0 = HomeFragment
    }

}
