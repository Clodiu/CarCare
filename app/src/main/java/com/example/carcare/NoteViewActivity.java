package com.example.carcare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteViewActivity extends AppCompatActivity {

    private int noteID;

    private Note currentNote;

    private Button saveButton;
    private Button deleteButton;

    TextInputEditText titleTextView;
    TextInputEditText descTextView;

    TextInputEditText kmTextView;

    Button dateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        titleTextView = findViewById(R.id.title_input_text);
        descTextView = findViewById(R.id.description_input_text);
        kmTextView = findViewById(R.id.km_input_text);
        dateTextView = findViewById(R.id.date_picker_button);

        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        noteID = prefs.getInt("NOTE_ID", -1);

        loadNote();

        setUpSaveButton();
        setUpDeleteButton();
    }

    private void setUpSaveButton(){
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NoteViewActivity.this, "Ai apasat save note", Toast.LENGTH_LONG).show();

            }
        });
    }

    private void setUpDeleteButton(){
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    ConnectionClass connectionClass = new ConnectionClass();
                    Connection conn = connectionClass.CONN();
                    Statement stmt = null;

                    try {
                        if (conn == null) {
                            runOnUiThread(() ->
                                    Toast.makeText(NoteViewActivity.this, "Problema de conexiune", Toast.LENGTH_LONG).show()
                            );
                        } else {
                            stmt = conn.createStatement();

                            String deleteQuery = "DELETE FROM Notes WHERE Note_ID = " + noteID;

                            int rowsAffected = stmt.executeUpdate(deleteQuery);

                            if (rowsAffected > 0) {
                                runOnUiThread(() -> {
                                    Toast.makeText(NoteViewActivity.this, "Nota a fost ștearsă!", Toast.LENGTH_SHORT).show();
                                    // Înapoi la CarHistoryActivity
                                    finish(); // sau folosește Intent dacă vrei să-l forțezi
                                    // startActivity(new Intent(NoteViewActivity.this, CarHistoryActivity.class));
                                });
                            } else {
                                runOnUiThread(() ->
                                        Toast.makeText(NoteViewActivity.this, "Nu s-a găsit nota!", Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(NoteViewActivity.this, "Eroare la ștergere: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
                    } finally {
                        try {
                            if (stmt != null) stmt.close();
                            if (conn != null) conn.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void loadNote(){
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

                    String query = "SELECT * FROM Notes WHERE Note_ID = "+noteID;

                    rs = stmt.executeQuery(query);

                    while (rs.next()) {
                        int noteId = rs.getInt("Note_ID");
                        String title = rs.getString("Title");
                        String description = rs.getString("Description");
                        int kilometers = rs.getInt("Kilometers");
                        Date date = rs.getDate("Date");
                        int creatorId = rs.getInt("Creator_ID");
                        int carid = rs.getInt("Car_ID");

                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                        String dateString = formatter.format(date);

                        currentNote = new Note(noteId,title,description,kilometers, dateString,creatorId,carid);

                        displayNote();
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

    private void displayNote() {
        runOnUiThread(() -> {
            titleTextView.setText(currentNote.getTitle());
            descTextView.setText(currentNote.getDescription());
            kmTextView.setText(String.valueOf(currentNote.getKilometers()));
            dateTextView.setText(currentNote.getDate());
        });
    }
}