package com.example.carcare;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    private int userId;
    private int carId;

    private TextView usernameTextView;

    private TextView emailTextView;

    private TextView manufacturerTextView;

    private TextView modelTextView;

    private TextView registerPlateTextView;



    Button deleteCarButton;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        deleteCarButton = view.findViewById(R.id.delete_car);
        deleteCarButtonSetUp();
        usernameTextView = view.findViewById(R.id.username_text_view);
        emailTextView = view.findViewById(R.id.email_text_view);
        manufacturerTextView = view.findViewById(R.id.manufacturer_text_view);
        modelTextView = view.findViewById(R.id.model_text_view);
        registerPlateTextView = view.findViewById(R.id.register_plate_text_view);
        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        userId = prefs.getInt("USER_ID", -1);
        carId = prefs.getInt("CAR_ID", -1);
        loadUserAndEmailFromDataBase();
        return view;
    }

    private void deleteCarButtonSetUp(){
        deleteCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Ai apasat pe butonul de stergere masina", Toast.LENGTH_LONG).show();
            }
        });;
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
                    Toast.makeText(getContext(),"Problema de conexiune",Toast.LENGTH_LONG).show();
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

                    query = "SELECT Manufacturer, Model, RegisterPlate FROM Cars WHERE Car_ID = " + carId;

                    rs = stmt.executeQuery(query);

                    while(rs.next()){
                        String manufacturer = rs.getString("Manufacturer");
                        String model = rs.getString("Model");
                        String plate = rs.getString("RegisterPlate");

                        manufacturerTextView.setText(manufacturer);
                        modelTextView.setText(model);
                        registerPlateTextView.setText(plate);
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