package com.example.carcare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements NoteRecyclerViewInterface{

    private int carId = 1005;

    private TextInputEditText searchEditText;

    private RecyclerView recyclerView;

    private NoteList_RecyclerViewAdapter adapter;

    private ArrayList<Note> carNotes = new ArrayList<>();
    private ArrayList<Note> filteredNotes = new ArrayList<>();

    private RelativeLayout mainLayout;

    private Handler handler = new Handler();
    private Runnable searchRunnable;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        carId = prefs.getInt("CAR_ID", -1);
        mainLayout = view.findViewById(R.id.main);
        recyclerView = view.findViewById(R.id.recycler_view_notes);
        searchEditText = view.findViewById(R.id.search_edit_text);
        //setUpCarNotes();
        loadNotesFromDatabase();
        adapter = new NoteList_RecyclerViewAdapter(getContext(),carNotes, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        initSearch();
        initNoFocusSearchbarWhenNoKeyboard();

        return view;
    }

    private void setUpCarNotes(){
        for(int i = 1 ; i <= 20 ; i++){
            carNotes.add(new Note());
        }
    }

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
                        filterNotes(s.toString());
                    }
                };

                handler.postDelayed(searchRunnable, 300); // 500 ms delay
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void filterNotes(String query){
        filteredNotes.clear();
        if (query.isEmpty()) {
            filteredNotes.addAll(carNotes);  // dacă nu ai text, arată tot
        } else {
            for (Note note : carNotes) {
                if (note.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredNotes.add(note);
                }
            }
        }
        getActivity().runOnUiThread(() -> {
            adapter.setNotes(filteredNotes);
            adapter.notifyDataSetChanged();
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

                if (previousHeightDiff > heightDiff) {
                    // Tastatura s-a închis
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (searchEditText.hasFocus()) {
                        searchEditText.clearFocus();
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                        }
                    }
                }
                previousHeightDiff = heightDiff;
            }
        });
    }


    @Override
    public void onItemClick(int position) {
        if(filteredNotes.isEmpty()){
            SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("NOTE_ID", carNotes.get(position).getNote_ID());
            editor.apply();
        }else{
            SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("NOTE_ID", filteredNotes.get(position).getNote_ID());
            editor.apply();
        }
        Intent intent = new Intent(getContext(), NoteViewActivity.class);
        startActivity(intent);
    }

    public void loadNotesFromDatabase(){
        carNotes.clear();
        ConnectionClass connectionClass = new ConnectionClass();
        Connection conn = connectionClass.CONN();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Statement stmt = null;
            ResultSet rs = null;
            try{
                if(conn == null){
                    Toast.makeText(getContext(),"Problema de conexiune",Toast.LENGTH_LONG).show();
                }else{
                    stmt = conn.createStatement();

                    String query = "SELECT * FROM Notes WHERE Car_ID = "+carId;

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

                        carNotes.add(new Note(noteId,title,description,kilometers, dateString,creatorId,carid));
                    }

                    conn.close();

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateAdapter();
                        });
                    }
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

    public void updateAdapter() {
        if (adapter != null) {
            getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
    }
}