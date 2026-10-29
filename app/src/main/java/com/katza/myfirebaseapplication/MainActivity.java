
package com.katza.myfirebaseapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button btnInsert, btnSelect, btnSelectAll, btnUpdate, btnDelete;

    EditText date, startHour, finishHour, payment;
    TextView tvcontent;
    FirestoreDAO fsDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fsDAO = new FirestoreDAO();

        date = findViewById(R.id.date);
        startHour = findViewById(R.id.startHour);
        finishHour = findViewById(R.id.finishHour);
        payment = findViewById(R.id.payment);
        btnInsert = findViewById(R.id.btnInsert);
        btnSelect = findViewById(R.id.btnSelect);
        tvcontent = findViewById(R.id.tvcontent);
        btnSelectAll = findViewById(R.id.btnShowAll);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String salaryText = payment.getText().toString().trim();

                if (salaryText.isEmpty()) {
                    payment.setError("יש להזין שכר");
                    return;
                }

                fsDAO.addShift(
                        date.getText().toString(),
                        startHour.getText().toString(),
                        finishHour.getText().toString(),
                        Integer.parseInt(payment.getText().toString())
                );
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.readDocumentById(date.getText().toString(), new IFirestoreCallback() {
                    @Override
                    public void onDataReceived(ArrayList<Shift> shifts) {
                        for (Shift shift : shifts) {
                            Log.d("DAO_FIREBASE", shift.toString());
                        }
                    }
                });
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.readDocumentById(date.getText().toString(), new IFirestoreCallback() {
                    @Override
                    public void onDataReceived(ArrayList<Shift> shifts) {
                        tvcontent.setText(shifts.get(0).toString());
                    }
                });
            }
        });

        btnSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvcontent.setText("");
                fsDAO.readAllDocuments(new IFirestoreCallback() {
                    @Override
                    public void onDataReceived(ArrayList<Shift> shifts) {
                        for (int i = 0; i < shifts.size(); i++) {
                            tvcontent.setText(tvcontent.getText().toString() + shifts.get(i).toString());
                        }
                    }
                });
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.updateDocument(
                        date.getText().toString(),
                        startHour.getText().toString(),
                        finishHour.getText().toString()
                );
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.deleteDocument(date.getText().toString());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (fsDAO == null) {
            fsDAO = new FirestoreDAO();
        }

        fsDAO.listenToAllDocumentsChange();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (fsDAO != null) {
            fsDAO.stopListening();
        }
    }
}
