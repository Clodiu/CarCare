package com.example.carcare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarFragment extends Fragment implements NoteRecyclerViewInterface{

    //Variabila in care incarcam id-ul salvat al masinii pe care am dat click
    private int carId;

    private CalendarView calendarView;
    private RecyclerView recyclerView;

    //Adaptorul care se ocupa de gestionarea recyclerView
    private NoteList_RecyclerViewAdapter adapter;

    //Lista cu notitele masinii
    private ArrayList<Note> carNotes = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        //Se incarca car_id din memorie
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        carId = prefs.getInt("CAR_ID", -1);

        //Conectarea calendarului la componenta din xml
        calendarView = view.findViewById(R.id.calendarView);

        //Setam niste limite de ani pentru calendar
        Calendar min = Calendar.getInstance();
        min.set(Calendar.YEAR, 2000);
        min.set(Calendar.MONTH, Calendar.JANUARY); // sau 0
        min.set(Calendar.DAY_OF_MONTH, 1);

        Calendar max = Calendar.getInstance();
        max.set(Calendar.YEAR, 2040);
        max.set(Calendar.MONTH, Calendar.DECEMBER); // sau 11
        max.set(Calendar.DAY_OF_MONTH, 31);

        calendarView.setMinimumDate(min);
        calendarView.setMaximumDate(max);

        Calendar today = Calendar.getInstance();

        //Se incarca initial notele pentru ziua curenta
        loadNotesForDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH));

        recyclerView = view.findViewById(R.id.recycler_view_notes);
        //setUpCarNotes();

        //Relatia dintre adaptor si recyclerView
        adapter = new NoteList_RecyclerViewAdapter(getContext(),carNotes, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setUpHighlightedDays();
        setOnDayClick();
        return view;
    }

    private void setUpCarNotes(){
        for(int i = 1 ; i <= 20 ; i++){
            carNotes.add(new Note());
        }
    }

    //Adaugarea datelor in care exista inregistrari de note pentru masina curenta
    void setUpHighlightedDays(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            ConnectionClass connectionClass = new ConnectionClass();
            Connection conn = connectionClass.CONN();

            Statement stmt = null;
            ResultSet rs = null;

            List<EventDay> events = new ArrayList<>();

            try {
                if (conn != null) {
                    stmt = conn.createStatement();

                    // Selectam datele diferite din notitele masinii curente
                    String query = "SELECT DISTINCT Date FROM Notes WHERE Car_ID = " + carId;

                    rs = stmt.executeQuery(query);

                    while (rs.next()) {
                        java.sql.Date sqlDate = rs.getDate("Date");
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(sqlDate);

                        // Adaugam iconita ca event
                        events.add(new EventDay(cal, R.drawable.ic_circle_event_day));
                    }
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // Se actualizeaza calendarul pe ui-thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> calendarView.setEvents(events));
            }
        });
    }

    //Metoda care preia data pe care utilizatorul o apasa si incarca notitele din acea zi
    void setOnDayClick(){
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDay = eventDay.getCalendar();

            int year = clickedDay.get(Calendar.YEAR);
            int month = clickedDay.get(Calendar.MONTH) + 1; // luna începe de la 0
            int day = clickedDay.get(Calendar.DAY_OF_MONTH);

            // Apeleaza metoda de incarcare a notitelor pentru data selectata
            loadNotesForDate(year, month, day);
        });
    }


    @Override
    public void onItemClick(int position) {
        Toast.makeText(getContext(), "Ai apasat din calendar fragment boss", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getContext(), NoteViewActivity.class);
        startActivity(intent);
    }

    //Metoda care incarca notitele dintr-o zi in vector
    public void loadNotesForDate(int year, int month, int day) {
        carNotes.clear();
        ConnectionClass connectionClass = new ConnectionClass();
        Connection conn = connectionClass.CONN();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                if (conn == null) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Problema de conexiune", Toast.LENGTH_LONG).show()
                        );
                    }
                } else {
                    stmt = conn.createStatement();

                    // Formatam data in formatul corespunzator SQL ex: '2025-07-10'
                    String dateString = String.format("%04d-%02d-%02d", year, month, day);

                    // Query care selecteaza notițele pentru data respectiva
                    String query = "SELECT * FROM Notes WHERE Date = '" + dateString + "'" + " AND Car_ID = " + carId;

                    rs = stmt.executeQuery(query);

                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

                    while (rs.next()) {
                        int noteId = rs.getInt("Note_ID");
                        String title = rs.getString("Title");
                        String description = rs.getString("Description");
                        int kilometers = rs.getInt("Kilometers");
                        Date date = rs.getDate("Date");
                        int creatorId = rs.getInt("Creator_ID");
                        int carid = rs.getInt("Car_ID");

                        String dateFormatted = formatter.format(date);

                        carNotes.add(new Note(noteId, title, description, kilometers, dateFormatted, creatorId, carid));
                    }

                    conn.close();

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> updateAdapter());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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

    public void updateAdapter() {
        if (adapter != null) {
            getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
    }
}