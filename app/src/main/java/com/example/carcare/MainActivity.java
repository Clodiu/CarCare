package com.example.carcare;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carcare.connection.ConnectionClass;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    Connection con;

    String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ConnectionClass connectionClass = new ConnectionClass();
        con = connectionClass.CONN();
        connect();

    }

    public void connect(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {

            try{
                if(con == null){
                    str = "Error";
                }else{
                    str = "Connected with SQL Server";
                }
            }catch (Exception e){
                throw new RuntimeException(e);
            }

            runOnUiThread(()->{
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    throw new RuntimeException();
                }
                Toast.makeText(this,str,Toast.LENGTH_LONG).show();
                TextView txtList = findViewById(R.id.text);
                txtList.setText(str);
            });
        });
    }
}