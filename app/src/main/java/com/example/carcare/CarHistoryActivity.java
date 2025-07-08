package com.example.carcare;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Date;

public class CarHistoryActivity extends AppCompatActivity{

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
        mainLayout = findViewById(R.id.main);

        dateButton = findViewById(R.id.date_picker_button);
        addNote = findViewById(R.id.add_note_card_view);
        addButton = findViewById(R.id.button_add);
        titleEditText = findViewById(R.id.title_input_text);
        descriptionEditText = findViewById(R.id.description_input_text);
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
                CharSequence buttonText = dateButton.getText();
                String date = (buttonText != null) ? buttonText.toString().trim() : "No date selected";

                String message = "Title: " + title + "\nDescription: " + description + "\nDate: "+ date;

                Toast.makeText(CarHistoryActivity.this, message, Toast.LENGTH_LONG).show();

                titleEditText.setText("");
                descriptionEditText.setText("");

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

                // Dacă diferența e mare, înseamnă că tastatura e afișată
                // Dacă diferența scade, tastatura s-a ascuns
                if (previousHeightDiff > heightDiff) {
                    // Tastatura tocmai s-a închis
                    // Scoatem focusul și ascundem tastatura (de precauție)
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
                    }
                }
                previousHeightDiff = heightDiff;
            }
        });
    }
}
