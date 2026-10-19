
package com.katza.myfirebaseapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etDate, etStartHour, etFinishHour, etPayment;
    private Button btnInsert, btnSelect, btnUpdate, btnDelete;
    private TextView tvcontent;
    private ScrollView scrollContent, formScroll;
    private LinearLayout shiftListLayout;

    private String selectedShiftDate = null;
    private int currentPage = R.id.nav_add;

    FirestoreDAO fsDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top,
                            bars.right, bars.bottom);
                    return insets;
                });

        etDate = findViewById(R.id.date);
        etStartHour = findViewById(R.id.startHour);
        etFinishHour = findViewById(R.id.finishHour);
        etPayment = findViewById(R.id.payment);

        btnInsert = findViewById(R.id.btnInsert);
        btnSelect = findViewById(R.id.btnSelect);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        tvcontent = findViewById(R.id.tvcontent);
        scrollContent = findViewById(R.id.scrollContent);
        formScroll = findViewById(R.id.formScroll);
        shiftListLayout = findViewById(R.id.shiftListLayout);

        fsDAO = new FirestoreDAO();

        BottomNavigationView bottomNavigation =
                findViewById(R.id.bottomNavigation);

        showAddPage();

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();
            currentPage = id;
            hideAll();

            // הוספת משמרת
            if (id == R.id.nav_add) {
                showAddPage();
                return true;
            }

            // הצגת משמרות
            if (id == R.id.nav_shifts) {

                scrollContent.setVisibility(View.VISIBLE);
                tvcontent.setText("המשמרות שלי\n\n");

                fsDAO.readAllDocuments(new IFirestoreCallback() {
                    @Override
                    public void onDataReceived(ArrayList<Shift> shifts) {

                        if (currentPage != R.id.nav_shifts)
                            return;

                        StringBuilder result =
                                new StringBuilder("המשמרות שלי\n\n");

                        for (Shift shift : shifts) {
                            if (shift != null) {
                                result.append(shift.toString());
                            }
                        }

                        tvcontent.setText(result.toString());
                    }
                });

                return true;
            }

            // עדכון משמרת
            if (id == R.id.nav_update) {

                scrollContent.setVisibility(View.VISIBLE);
                tvcontent.setText("בחרי משמרת לעדכון:");

                fsDAO.readAllDocuments(new IFirestoreCallback() {
                    @Override
                    public void onDataReceived(ArrayList<Shift> shifts) {

                        if (currentPage != R.id.nav_update)
                            return;

                        shiftListLayout.removeAllViews();

                        if (shifts.isEmpty()) {
                            tvcontent.setText("אין משמרות לעדכון");
                            return;
                        }

                        for (Shift shift : shifts) {

                            if (shift == null)
                                continue;

                            Button button = new Button(MainActivity.this);

                            button.setText(
                                    shift.getdate() + " | " +
                                            shift.getstartHour() + " - " +
                                            shift.getfinishHour()
                            );

                            shiftListLayout.addView(button);

                            button.setOnClickListener(v -> {

                                selectedShiftDate = shift.getdate();

                                etDate.setText(shift.getdate());
                                etStartHour.setText(shift.getstartHour());
                                etFinishHour.setText(shift.getfinishHour());
                                etPayment.setText(
                                        String.valueOf(shift.getpayment())
                                );

                                etDate.setEnabled(false);

                                etDate.setVisibility(View.VISIBLE);
                                etStartHour.setVisibility(View.VISIBLE);
                                etFinishHour.setVisibility(View.VISIBLE);
                                etPayment.setVisibility(View.VISIBLE);
                                btnUpdate.setVisibility(View.VISIBLE);

                                // הצגת הטופס המעוצב
                                formScroll.setVisibility(View.VISIBLE);
                                scrollContent.setVisibility(View.GONE);
                            });
                        }
                    }
                });

                return true;
            }

            // מחיקת משמרת
            if (id == R.id.nav_delete) {

                formScroll.setVisibility(View.VISIBLE);

                etDate.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);

                return true;
            }

            // סיכום שכר
            if (id == R.id.nav_salary) {

                scrollContent.setVisibility(View.VISIBLE);
                tvcontent.setText("טוען סיכום שכר...");

                fsDAO.readAllDocuments(new IFirestoreCallback() {
                    @Override
                    public void onDataReceived(ArrayList<Shift> shifts) {

                        if (currentPage != R.id.nav_salary)
                            return;

                        long totalPayment = 0;
                        int shiftCount = 0;

                        StringBuilder details = new StringBuilder();

                        for (Shift shift : shifts) {

                            if (shift != null) {

                                totalPayment += shift.getpayment();
                                shiftCount++;

                                details.append("תאריך: ")
                                        .append(shift.getdate())
                                        .append("\nשעת התחלה: ")
                                        .append(shift.getstartHour())
                                        .append("\nשעת סיום: ")
                                        .append(shift.getfinishHour())
                                        .append("\nשכר: ")
                                        .append(shift.getpayment())
                                        .append(" ₪\n\n")
                                        .append("------------------------\n\n");
                            }
                        }

                        double averagePayment = 0;

                        if (shiftCount > 0) {
                            averagePayment =
                                    (double) totalPayment / shiftCount;
                        }

                        String summary =
                                "סיכום שכר\n\n" +
                                        "סה״כ שכר: " + totalPayment + " ₪\n\n" +
                                        "מספר משמרות: " + shiftCount + "\n\n" +
                                        "ממוצע שכר למשמרת: " +
                                        String.format(
                                                Locale.getDefault(),
                                                "%.2f",
                                                averagePayment
                                        ) + " ₪\n\n" +
                                        "------------------------\n\n" +
                                        "פירוט המשמרות:\n\n" +
                                        details.toString();

                        tvcontent.setText(summary);
                    }
                });

                return true;
            }

            return false;
        });

        // שמירת משמרת חדשה
        btnInsert.setOnClickListener(v -> {

            String date = etDate.getText().toString().trim();
            String start = etStartHour.getText().toString().trim();
            String finish = etFinishHour.getText().toString().trim();
            String payment = etPayment.getText().toString().trim();

            if (date.isEmpty() || start.isEmpty() ||
                    finish.isEmpty() || payment.isEmpty()) {

                Toast.makeText(this,
                        "יש למלא את כל השדות",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                fsDAO.createDocument(
                        date,
                        start,
                        finish,
                        Integer.parseInt(payment)
                );

                Toast.makeText(this,
                        "בקשת ההוספה נשלחה",
                        Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {

                Toast.makeText(this,
                        "שכר לא תקין",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // שמירת שינויים במשמרת קיימת
        btnUpdate.setOnClickListener(v -> {

            if (selectedShiftDate == null)
                return;

            String start = etStartHour.getText().toString().trim();
            String finish = etFinishHour.getText().toString().trim();
            String payment = etPayment.getText().toString().trim();

            if (start.isEmpty() || finish.isEmpty() ||
                    payment.isEmpty()) {

                Toast.makeText(this,
                        "יש למלא את כל השדות",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try {

                fsDAO.updateDocument(
                        selectedShiftDate,
                        start,
                        finish,
                        Integer.parseInt(payment)
                );

                Toast.makeText(this,
                        "בקשת העדכון נשלחה",
                        Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {

                Toast.makeText(this,
                        "שכר לא תקין",
                        Toast.LENGTH_SHORT).show();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fsDAO.deleteDocument(etDate.getText().toString());
            }
        });

        Button btnZZZ = findViewById(R.id.ZZZ);
        btnZZZ.setOnClickListener(new View.OnClickListener() {
            @Override
            String searchDate = ed.getText().toString();
            public void onClick(View v) {
                fsDAO.readAllDocumentsByDate(searchDate, new IFirestoreCallback() {
                    @Override
                    public void onDataReceived(ArrayList<Shift> shifts) {
                                tvshift.setText("");

                                if (shifts != null && !shifts.isEmpty()) {
                                    Shift shift = shifts.get(0);

                                    Intent intent = new Intent(MainActivity.this, Shift.class);
                                    intent.putExtra("shift", shift);
                                    startActivity(intent);

                                } else {
                                    tvcontent.setText("Couldn't find a show on " + searchDate);
                                }
                            }
                        });
            }
        });
    }

    // הסתרת כל התוכן במעבר בין עמודים
    private void hideAll() {

        etDate.setEnabled(true);

        etDate.setVisibility(View.GONE);
        etStartHour.setVisibility(View.GONE);
        etFinishHour.setVisibility(View.GONE);
        etPayment.setVisibility(View.GONE);

        btnInsert.setVisibility(View.GONE);
        btnSelect.setVisibility(View.GONE);
        btnUpdate.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);

        scrollContent.setVisibility(View.GONE);
        formScroll.setVisibility(View.GONE);

        tvcontent.setText("");
        shiftListLayout.removeAllViews();

        selectedShiftDate = null;
    }

    // הצגת עמוד הוספת משמרת
    private void showAddPage() {

        hideAll();

        etDate.setText("");
        etStartHour.setText("");
        etFinishHour.setText("");
        etPayment.setText("");

        etDate.setVisibility(View.VISIBLE);
        etStartHour.setVisibility(View.VISIBLE);
        etFinishHour.setVisibility(View.VISIBLE);
        etPayment.setVisibility(View.VISIBLE);

        btnInsert.setVisibility(View.VISIBLE);

        // הצגת הטופס המעוצב
        formScroll.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fsDAO.listenToADocumentChange("16.10.26");
    }

    @Override
    protected void onStop() {
        super.onStop();
        fsDAO.stopListening();
    }


}