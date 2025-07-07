package com.example.carcare;

import android.content.Context;
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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private TextInputEditText searchEditText;

    private RecyclerView recyclerView;

    private NoteList_RecyclerViewAdapter adapter;

    private ArrayList<Note> carNotes = new ArrayList<>();

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

        mainLayout = view.findViewById(R.id.main);
        recyclerView = view.findViewById(R.id.recycler_view_notes);
        searchEditText = view.findViewById(R.id.search_edit_text);
        setUpCarNotes();
        adapter = new NoteList_RecyclerViewAdapter(getContext(),carNotes);
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
                        Toast.makeText(getContext(), "Cauți: " + s.toString(), Toast.LENGTH_SHORT).show();
                    }
                };

                handler.postDelayed(searchRunnable, 300); // 500 ms delay
            }

            @Override
            public void afterTextChanged(Editable s) {

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


}