package com.example.carcare;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerView;

    private NoteList_RecyclerViewAdapter adapter;

    private ArrayList<Note> carNotes = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendarView = view.findViewById(R.id.calendarView);

        setUpCalendarIcons();

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

        recyclerView = view.findViewById(R.id.recycler_view_notes);
        setUpCarNotes();
        adapter = new NoteList_RecyclerViewAdapter(getContext(),carNotes);
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

    //Adauga cateva date pentru test
    void setUpHighlightedDays(){
        List<EventDay> events = new ArrayList<>();

        //Se marcheaza zilele cu event cu ic_circle_event_day

        // Exemplu: marchează 10 iulie 2025
        Calendar day1 = Calendar.getInstance();
        day1.set(2025, Calendar.JULY, 10);
        events.add(new EventDay(day1, R.drawable.ic_circle_event_day));  // icon drawable în res/drawable

        // Exemplu: marchează 15 iulie 2025
        Calendar day2 = Calendar.getInstance();
        day2.set(2025, Calendar.JULY, 15);
        events.add(new EventDay(day2, R.drawable.ic_circle_event_day));

        // Pune lista de events pe CalendarView
        calendarView.setEvents(events);
    }

    void setOnDayClick(){
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDay = eventDay.getCalendar();

            int year = clickedDay.get(Calendar.YEAR);
            int month = clickedDay.get(Calendar.MONTH) + 1; // luna începe de la 0
            int day = clickedDay.get(Calendar.DAY_OF_MONTH);

            String message = "Ziua selectată: " + day + "/" + month + "/" + year;

            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    void setUpCalendarIcons(){
        calendarView.post(() -> {
            int prevId = getResources().getIdentifier("arrow_previous", "id", "com.applandeo.materialcalendarview");
            int nextId = getResources().getIdentifier("arrow_next", "id", "com.applandeo.materialcalendarview");

            View prevButton = calendarView.findViewById(prevId);
            View nextButton = calendarView.findViewById(nextId);

            if (prevButton instanceof ImageButton) {
                ((ImageButton) prevButton).setImageResource(R.drawable.arrow_back_icon_24dp);
            }
            if (nextButton instanceof ImageButton) {
                ((ImageButton) nextButton).setImageResource(R.drawable.arrow_forward_icon_24dp);
            }
        });
    }



}