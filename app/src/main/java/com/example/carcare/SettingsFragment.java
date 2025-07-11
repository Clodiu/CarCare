package com.example.carcare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
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

    private Button addAccesButton;

    private Button removeAccessButton;

    private TextInputEditText emailInputText;



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
        addAccesButton = view.findViewById(R.id.add_access);
        removeAccessButton = view.findViewById(R.id.remove_access);
        deleteCarButton = view.findViewById(R.id.delete_car);
        emailInputText = view.findViewById(R.id.mail_input_text);
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
        addAccessButtonSetUp();
        removeAccessButtonSetUp();
        return view;
    }

    private void addAccessButtonSetUp(){
        addAccesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = emailInputText.getText().toString().trim();

                if(!mail.isEmpty()){
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        ConnectionClass connectionClass = new ConnectionClass();
                        Connection conn = connectionClass.CONN();

                        if (conn == null) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Problema de conexiune", Toast.LENGTH_LONG).show()
                            );
                            return;
                        }

                        PreparedStatement userStmt = null;
                        PreparedStatement creatorStmt = null;
                        PreparedStatement insertAccessStmt = null;
                        ResultSet rsUser = null;
                        ResultSet rsCreator = null;

                        try {
                            //Ia User_ID pentru mail-ul introdus
                            String userQuery = "SELECT User_ID FROM Users WHERE Email = ?";
                            userStmt = conn.prepareStatement(userQuery);
                            userStmt.setString(1, mail);
                            rsUser = userStmt.executeQuery();

                            int targetUserId = -1;
                            if (rsUser.next()) {
                                targetUserId = rsUser.getInt("User_ID");
                            } else {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Email-ul nu a fost gasit!", Toast.LENGTH_LONG).show()
                                );
                                return;
                            }

                            //Verifica daca user-ul curent este creatorul masinii
                            String creatorQuery = "SELECT Creator_ID FROM Cars WHERE Car_ID = ?";
                            creatorStmt = conn.prepareStatement(creatorQuery);
                            creatorStmt.setInt(1, carId);  // Foloseste ID-ul masinii curente
                            rsCreator = creatorStmt.executeQuery();

                            int creatorId = -1;
                            if (rsCreator.next()) {
                                creatorId = rsCreator.getInt("Creator_ID");
                            }

                            if (creatorId != userId) {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Nu ai voie să oferi acces la această masina!", Toast.LENGTH_LONG).show()
                                );
                                return;
                            }

                            //Insereaza perechea in UserCarAccess
                            String accessQuery = "INSERT INTO UserCarAccess (User_ID, Car_ID) VALUES (?, ?)";
                            insertAccessStmt = conn.prepareStatement(accessQuery);
                            insertAccessStmt.setInt(1, targetUserId);
                            insertAccessStmt.setInt(2, carId);  // ID-ul mașinii curente

                            insertAccessStmt.executeUpdate();

                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Acces adaugat cu succes!", Toast.LENGTH_SHORT).show()
                            );

                        } catch (Exception e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Eroare: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                        } finally {
                            try {
                                if (rsUser != null) rsUser.close();
                                if (rsCreator != null) rsCreator.close();
                                if (userStmt != null) userStmt.close();
                                if (creatorStmt != null) creatorStmt.close();
                                if (insertAccessStmt != null) insertAccessStmt.close();
                                if (conn != null) conn.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }

            }
        });
    }

    private void removeAccessButtonSetUp() {
        removeAccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = emailInputText.getText().toString().trim();

                if (!mail.isEmpty()) {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        ConnectionClass connectionClass = new ConnectionClass();
                        Connection conn = connectionClass.CONN();

                        if (conn == null) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Problema de conexiune", Toast.LENGTH_LONG).show()
                            );
                            return;
                        }

                        PreparedStatement userStmt = null;
                        PreparedStatement creatorStmt = null;
                        PreparedStatement deleteAccessStmt = null;
                        ResultSet rsUser = null;
                        ResultSet rsCreator = null;

                        try {
                            //Gaseste User_ID dupa mail
                            String userQuery = "SELECT User_ID FROM Users WHERE Email = ?";
                            userStmt = conn.prepareStatement(userQuery);
                            userStmt.setString(1, mail);
                            rsUser = userStmt.executeQuery();

                            int targetUserId = -1;
                            if (rsUser.next()) {
                                targetUserId = rsUser.getInt("User_ID");
                            } else {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Email-ul nu a fost găsit!", Toast.LENGTH_LONG).show()
                                );
                                return;
                            }

                            //Verifica daca user-ul curent este creatorul masinii
                            String creatorQuery = "SELECT Creator_ID FROM Cars WHERE Car_ID = ?";
                            creatorStmt = conn.prepareStatement(creatorQuery);
                            creatorStmt.setInt(1, carId);  // ID-ul mașinii curente
                            rsCreator = creatorStmt.executeQuery();

                            int creatorId = -1;
                            if (rsCreator.next()) {
                                creatorId = rsCreator.getInt("Creator_ID");
                            }

                            if (creatorId != userId) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Nu ai voie sa modifici accesul acestei masini!", Toast.LENGTH_LONG).show()
                                );
                                return;
                            }

                            //Sterge din UserCarAccess
                            String deleteQuery = "DELETE FROM UserCarAccess WHERE User_ID = ? AND Car_ID = ?";
                            deleteAccessStmt = conn.prepareStatement(deleteQuery);
                            deleteAccessStmt.setInt(1, targetUserId);
                            deleteAccessStmt.setInt(2, carId);

                            int rowsAffected = deleteAccessStmt.executeUpdate();

                            if (rowsAffected > 0) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Acces șters cu succes!", Toast.LENGTH_SHORT).show()
                                );
                            } else {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Utilizatorul nu avea acces la această mașină.", Toast.LENGTH_SHORT).show()
                                );
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Eroare: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                        } finally {
                            try {
                                if (rsUser != null) rsUser.close();
                                if (rsCreator != null) rsCreator.close();
                                if (userStmt != null) userStmt.close();
                                if (creatorStmt != null) creatorStmt.close();
                                if (deleteAccessStmt != null) deleteAccessStmt.close();
                                if (conn != null) conn.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Te rog să introduci un email!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void deleteCarButtonSetUp() {
        deleteCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    ConnectionClass connectionClass = new ConnectionClass();
                    Connection conn = connectionClass.CONN();

                    if (conn == null) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Problema de conexiune", Toast.LENGTH_LONG).show()
                        );
                        return;
                    }

                    PreparedStatement creatorStmt = null;
                    PreparedStatement deleteNotesStmt = null;
                    PreparedStatement deleteAccessStmt = null;
                    PreparedStatement deleteCarStmt = null;
                    ResultSet rsCreator = null;

                    try {
                        //Verifica daca user-ul curent este creatorul masinii
                        String creatorQuery = "SELECT Creator_ID FROM Cars WHERE Car_ID = ?";
                        creatorStmt = conn.prepareStatement(creatorQuery);
                        creatorStmt.setInt(1, carId); // ID-ul mașinii curente
                        rsCreator = creatorStmt.executeQuery();

                        int creatorId = -1;
                        if (rsCreator.next()) {
                            creatorId = rsCreator.getInt("Creator_ID");
                        }

                        if (creatorId != userId) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Nu ai voie să ștergi această mașină!", Toast.LENGTH_LONG).show()
                            );
                            return;
                        }

                        //Sterge notele masinii
                        String deleteNotesQuery = "DELETE FROM Notes WHERE Car_ID = ?";
                        deleteNotesStmt = conn.prepareStatement(deleteNotesQuery);
                        deleteNotesStmt.setInt(1, carId);
                        deleteNotesStmt.executeUpdate();

                        //Sterge accesul tuturor userilor
                        String deleteAccessQuery = "DELETE FROM UserCarAccess WHERE Car_ID = ?";
                        deleteAccessStmt = conn.prepareStatement(deleteAccessQuery);
                        deleteAccessStmt.setInt(1, carId);
                        deleteAccessStmt.executeUpdate();

                        //sterge masina
                        String deleteCarQuery = "DELETE FROM Cars WHERE Car_ID = ?";
                        deleteCarStmt = conn.prepareStatement(deleteCarQuery);
                        deleteCarStmt.setInt(1, carId);
                        int rowsAffected = deleteCarStmt.executeUpdate();

                        if (rowsAffected > 0) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Mașina a fost ștearsă cu succes!", Toast.LENGTH_SHORT).show()
                            );

                            //Inchiderea filei existente de CarListActivity si redeschiderea uneia noi(pentru a reincarca lista)
                            requireActivity().runOnUiThread(() -> {
                                Intent intent = new Intent(requireContext(), CarListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                                //Se inchide si activitatea curenta
                                requireActivity().finish();
                            });
                        } else {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Nu s-a putut șterge mașina!", Toast.LENGTH_SHORT).show()
                            );
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Eroare la ștergere: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
                    } finally {
                        try {
                            if (rsCreator != null) rsCreator.close();
                            if (creatorStmt != null) creatorStmt.close();
                            if (deleteNotesStmt != null) deleteNotesStmt.close();
                            if (deleteAccessStmt != null) deleteAccessStmt.close();
                            if (deleteCarStmt != null) deleteCarStmt.close();
                            if (conn != null) conn.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
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