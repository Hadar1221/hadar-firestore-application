package com.katza.myfirebaseapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText etName, etId, etGrade;
    private Button btnInsert, btnSelect, btnDelete;
    private TextView tvcontent;

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

        etName = findViewById(R.id.fullname);
        etId = findViewById(R.id.tz);
        etGrade = findViewById(R.id.grade);
        btnInsert = findViewById(R.id.btnInsert);
        btnSelect= findViewById(R.id.btnSelect);
        tvcontent = findViewById(R.id.tvcontent);

        FirestoreDAO fsDAO = new FirestoreDAO();


        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.createDocument(etName.getText().toString(),
                        etId.getText().toString(),
                        Integer.parseInt(etGrade.getText().toString()));
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvcontent.setText("");
                fsDAO.readAllDocuments(new IFirestoreCallback() {
                    @Override
                    public void onDataReceived(ArrayList<Student> students) {
                        for (int i = 0; i < students.size(); i++)
                            tvcontent.setText(tvcontent.getText().toString() + students.get(i).toString());


                    }
                });
            }
        });
    }
}