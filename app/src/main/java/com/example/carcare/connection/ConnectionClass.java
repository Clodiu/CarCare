package com.example.carcare.connection;

import android.os.StrictMode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass {
    String classes = "net.sourceforge.jtds.jdbc.Driver";

    protected static String ip = "192.168.101.9";//Trebuie schimbat in functie de retea pe private si sa fie adresa laptopului din ipconfig

    protected static String port = "1433";

    protected static String db = "CarCareDB";

    protected static String un = "userSQL";

    protected static String password = "P@ssw0rd123!";

    public Connection CONN(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        try {
            Class.forName(classes);
            String conUrl = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+db;
            conn = DriverManager.getConnection(conUrl,un,password);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
}
