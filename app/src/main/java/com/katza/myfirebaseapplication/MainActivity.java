
package com.katza.myfirebaseapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.Gravity;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    Button btnInsert, btnSelect, btnSelectAll, btnUpdate, btnDelete;

    EditText date, startHour, finishHour, payment;
    TextView tvcontent;
    FirestoreDAO fsDAO;

    BottomNavigationView bottomNavigation;
    ScrollView scrollContent, formScroll;

    EditText etSearchDate;
    Button btnSearch;

    LinearLayout searchLayout;
    TextView formTitle, formSubtitle;

    String selectedDeleteId = "";
    String selectedUpdateId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnImages = findViewById(R.id.btnImages);

        btnImages.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ImageLabActivity.class);
            startActivity(intent);
        });

        fsDAO = new FirestoreDAO();

        bottomNavigation = findViewById(R.id.bottomNavigation);
        scrollContent = findViewById(R.id.scrollContent);
        formScroll = findViewById(R.id.formScroll);

        searchLayout = findViewById(R.id.searchLayout);
        formTitle = findViewById(R.id.formTitle);
        formSubtitle = findViewById(R.id.formSubtitle);

        date = findViewById(R.id.date);
        startHour = findViewById(R.id.startHour);
        finishHour = findViewById(R.id.finishHour);
        payment = findViewById(R.id.payment);

        btnInsert = findViewById(R.id.btnInsert);
        btnSelect = findViewById(R.id.btnSelect);
        btnSelectAll = findViewById(R.id.btnShowAll);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        tvcontent = findViewById(R.id.tvcontent);

        etSearchDate = findViewById(R.id.etSearchDate);
        btnSearch = findViewById(R.id.btnSearch);

        // הוספת משמרת
        btnInsert.setOnClickListener(view -> {

            String salaryText = payment.getText().toString().trim();

            if (salaryText.isEmpty()) {
                payment.setError("יש להזין שכר");
                return;
            }

            fsDAO.addShift(
                    date.getText().toString(),
                    startHour.getText().toString(),
                    finishHour.getText().toString(),
                    Integer.parseInt(salaryText)
            );

            clearFields();

            bottomNavigation.setSelectedItemId(R.id.nav_shifts);
        });

        // בחירת משמרת
        btnSelect.setOnClickListener(view -> {

            fsDAO.readDocumentById(
                    date.getText().toString(),
                    new IFirestoreCallback() {
                        @Override
                        public void onDataReceived(ArrayList<Shift> shifts) {

                            if (!shifts.isEmpty()) {
                                tvcontent.setText(shifts.get(0).toString());
                            }
                        }
                    }
            );
        });

        // הצגת המשמרות בכרטיסיות
        btnSelectAll.setOnClickListener(view -> {

            fsDAO.readAllDocuments(new IFirestoreCallback() {
                @Override
                public void onDataReceived(ArrayList<Shift> shifts) {

                    if (bottomNavigation.getSelectedItemId()
                            != R.id.nav_shifts) {
                        return;
                    }

                    showShiftCards(shifts);
                }
            });
        });

        // עדכון משמרת
        btnUpdate.setOnClickListener(view -> {

            if (selectedUpdateId.isEmpty()) {
                Toast.makeText(
                        MainActivity.this,
                        "יש לבחור משמרת לעדכון",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String salaryText = payment.getText().toString().trim();

            if (salaryText.isEmpty()) {
                payment.setError("יש להזין שכר");
                return;
            }

            fsDAO.updateDocument(
                    selectedUpdateId,
                    startHour.getText().toString(),
                    finishHour.getText().toString(),
                    Integer.parseInt(salaryText)
            );

            clearFields();

            date.setEnabled(true);
            selectedUpdateId = "";

            Toast.makeText(
                    MainActivity.this,
                    "בקשת העדכון נשלחה",
                    Toast.LENGTH_SHORT
            ).show();

            showUpdatePage();
        });

        // חיפוש משמרת לעדכון או למחיקה
        btnSearch.setOnClickListener(view -> {

            String searchDate = etSearchDate.getText().toString().trim();

            if (searchDate.isEmpty()) {
                etSearchDate.setError("יש להזין תאריך");
                return;
            }

            selectedDeleteId = "";
            selectedUpdateId = "";

            final int searchPage =
                    bottomNavigation.getSelectedItemId();

            fsDAO.findShiftsForDelete(searchDate, query -> {

                if (bottomNavigation.getSelectedItemId() != searchPage) {
                    return;
                }

                ArrayList<DocumentSnapshot> documents =
                        new ArrayList<>(query.getDocuments());

                if (documents.isEmpty()) {
                    Toast.makeText(
                            MainActivity.this,
                            "לא נמצאו משמרות בתאריך הזה",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                String[] options = new String[documents.size()];

                for (int i = 0; i < documents.size(); i++) {

                    Shift shift =
                            documents.get(i).toObject(Shift.class);

                    if (shift != null) {
                        options[i] =
                                "תאריך: " + shift.getdate()
                                        + "\nשעות: " + shift.getstartHour()
                                        + " - " + shift.getendHour()
                                        + "\nשכר: " + shift.getpayment();
                    } else {
                        options[i] = "משמרת";
                    }
                }

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("בחרי משמרת")
                        .setItems(options, (dialog, which) -> {

                            DocumentSnapshot selectedDoc =
                                    documents.get(which);

                            Shift selectedShift =
                                    selectedDoc.toObject(Shift.class);

                            if (selectedShift == null) {
                                return;
                            }

                            if (bottomNavigation.getSelectedItemId()
                                    == R.id.nav_update) {

                                selectedUpdateId = selectedDoc.getId();

                                date.setText(selectedShift.getdate());
                                startHour.setText(
                                        selectedShift.getstartHour()
                                );
                                finishHour.setText(
                                        selectedShift.getendHour()
                                );
                                payment.setText(
                                        String.valueOf(
                                                selectedShift.getpayment()
                                        )
                                );

                                date.setEnabled(false);

                                scrollContent.setVisibility(View.GONE);
                                formScroll.setVisibility(View.VISIBLE);

                                formTitle.setText("עדכון משמרת");
                                formSubtitle.setText(
                                        "ערכי את פרטי המשמרת שבחרת"
                                );

                                btnInsert.setVisibility(View.GONE);
                                btnUpdate.setVisibility(View.VISIBLE);
                                btnSelect.setVisibility(View.GONE);

                            } else if (bottomNavigation.getSelectedItemId()
                                    == R.id.nav_delete) {

                                selectedDeleteId = selectedDoc.getId();

                                Toast.makeText(
                                        MainActivity.this,
                                        "המשמרת נבחרה. לחצי על מחיקת משמרת",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        })
                        .setNegativeButton("ביטול", null)
                        .show();
            });
        });

        // מחיקת משמרת
        btnDelete.setOnClickListener(view -> {

            if (selectedDeleteId.isEmpty()) {
                Toast.makeText(
                        MainActivity.this,
                        "יש לחפש ולבחור משמרת קודם",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("מחיקת משמרת")
                    .setMessage("האם למחוק את המשמרת שבחרת?")
                    .setPositiveButton("מחקי", (dialog, which) -> {

                        fsDAO.deleteDocument(selectedDeleteId);

                        selectedDeleteId = "";
                        etSearchDate.setText("");

                        Toast.makeText(
                                MainActivity.this,
                                "בקשת המחיקה נשלחה",
                                Toast.LENGTH_SHORT
                        ).show();
                    })
                    .setNegativeButton("ביטול", null)
                    .show();
        });

        // מעבר בין הלשוניות
        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_add) {
                showAddPage();
            }

            else if (id == R.id.nav_shifts) {
                showShiftsPage();
            }

            else if (id == R.id.nav_update) {
                showUpdatePage();
            }

            else if (id == R.id.nav_delete) {
                showDeletePage();
            }

            else if (id == R.id.nav_salary) {
                showSalaryPage();
            }

            return true;
        });

        bottomNavigation.setSelectedItemId(R.id.nav_add);
        showAddPage();
    }

    // הסתרת כל העמודים
    private void hideAllPages() {

        formScroll.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);

        searchLayout.setVisibility(View.GONE);
        tvcontent.setVisibility(View.GONE);

        findViewById(R.id.shiftListLayout)
                .setVisibility(View.GONE);

        btnSearch.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        btnSelectAll.setVisibility(View.GONE);

        btnInsert.setVisibility(View.GONE);
        btnUpdate.setVisibility(View.GONE);
        btnSelect.setVisibility(View.GONE);
    }

    // עמוד הוספה
    private void showAddPage() {

        hideAllPages();

        selectedUpdateId = "";
        selectedDeleteId = "";

        clearFields();
        date.setEnabled(true);

        formTitle.setText("הוספת משמרת");
        formSubtitle.setText("מלאי את פרטי המשמרת");

        formScroll.setVisibility(View.VISIBLE);
        btnInsert.setVisibility(View.VISIBLE);
    }

    // עמוד המשמרות
    private void showShiftsPage() {

        hideAllPages();

        scrollContent.setVisibility(View.VISIBLE);

        findViewById(R.id.shiftListLayout)
                .setVisibility(View.VISIBLE);

        btnSelectAll.setVisibility(View.VISIBLE);

        btnSelectAll.performClick();
    }

    // עמוד עדכון
    private void showUpdatePage() {

        hideAllPages();

        selectedUpdateId = "";
        selectedDeleteId = "";

        date.setEnabled(true);
        etSearchDate.setText("");

        scrollContent.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.VISIBLE);

        btnSearch.setVisibility(View.VISIBLE);
    }

    // עמוד מחיקה
    private void showDeletePage() {

        hideAllPages();

        selectedDeleteId = "";
        selectedUpdateId = "";

        etSearchDate.setText("");

        scrollContent.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.VISIBLE);

        btnSearch.setVisibility(View.VISIBLE);
        btnDelete.setVisibility(View.VISIBLE);
    }

    // עמוד שכר - מעוצב
    private void showSalaryPage() {

        hideAllPages();

        scrollContent.setVisibility(View.VISIBLE);

        findViewById(R.id.shiftListLayout)
                .setVisibility(View.VISIBLE);

        calculateSalary();
    }

    // ניקוי שדות
    private void clearFields() {

        date.setText("");
        startHour.setText("");
        finishHour.setText("");
        payment.setText("");
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

    // הצגת משמרות בכרטיסיות
    private void showShiftCards(ArrayList<Shift> shifts) {

        LinearLayout shiftListLayout =
                findViewById(R.id.shiftListLayout);

        shiftListLayout.removeAllViews();

        tvcontent.setText("");
        tvcontent.setVisibility(View.GONE);

        if (shifts.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("עדיין אין משמרות 💜");
            emptyText.setTextSize(18);
            emptyText.setTextColor(Color.parseColor("#795BAF"));
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(20, 40, 20, 40);
            shiftListLayout.addView(emptyText);
            return;
        }

        int purple = Color.parseColor("#493377");
        int lightPurple = Color.parseColor("#8876A6");
        int accent = Color.parseColor("#795BAF");

        for (Shift shift : shifts) {

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);

            int padding = dp(18);
            card.setPadding(padding, padding, padding, padding);

            GradientDrawable cardBackground = new GradientDrawable();
            cardBackground.setColor(Color.WHITE);
            cardBackground.setCornerRadius(dp(20));

            card.setBackground(cardBackground);
            card.setElevation(dp(3));

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.setMargins(0, dp(5), 0, dp(14));
            card.setLayoutParams(cardParams);

            TextView dateText = new TextView(this);
            dateText.setText("▦  " + shift.getdate());
            dateText.setTextSize(20);
            dateText.setTypeface(null, Typeface.BOLD);
            dateText.setTextColor(purple);
            dateText.setPadding(0, 0, 0, dp(14));

            card.addView(dateText);

            View divider = new View(this);
            divider.setBackgroundColor(
                    Color.parseColor("#F0EAF8")
            );

            card.addView(divider,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(1)
                    ));

            TextView hoursText = new TextView(this);
            hoursText.setText(
                    "שעות העבודה\n"
                            + shift.getstartHour()
                            + " - "
                            + shift.getendHour()
            );
            hoursText.setTextSize(16);
            hoursText.setTextColor(purple);
            hoursText.setLineSpacing(dp(5), 1);
            hoursText.setPadding(0, dp(14), 0, dp(12));

            card.addView(hoursText);

            TextView paymentText = new TextView(this);
            paymentText.setText(
                    "תשלום לשעה: "
                            + shift.getpayment()
                            + " ₪"
            );
            paymentText.setTextSize(15);
            paymentText.setTextColor(lightPurple);
            paymentText.setPadding(0, 0, 0, dp(14));

            card.addView(paymentText);

            String salaryText;

            try {
                String[] start = shift.getstartHour().split(":");
                String[] end = shift.getendHour().split(":");

                int startMinutes =
                        Integer.parseInt(start[0]) * 60
                                + Integer.parseInt(start[1]);

                int endMinutes =
                        Integer.parseInt(end[0]) * 60
                                + Integer.parseInt(end[1]);

                if (endMinutes < startMinutes) {
                    endMinutes += 24 * 60;
                }

                double hours =
                        (endMinutes - startMinutes) / 60.0;

                double salary = hours * shift.getpayment();

                salaryText = String.format(
                        java.util.Locale.getDefault(),
                        "%.2f ₪",
                        salary
                );

            } catch (Exception e) {
                salaryText = "לא ניתן לחשב";
            }

            LinearLayout salaryBox = new LinearLayout(this);
            salaryBox.setOrientation(LinearLayout.HORIZONTAL);
            salaryBox.setGravity(Gravity.CENTER_VERTICAL);
            salaryBox.setPadding(
                    dp(14), dp(13), dp(14), dp(13)
            );

            GradientDrawable salaryBackground =
                    new GradientDrawable();

            salaryBackground.setColor(
                    Color.parseColor("#F7F4FD")
            );
            salaryBackground.setCornerRadius(dp(14));

            salaryBox.setBackground(salaryBackground);

            TextView salaryLabel = new TextView(this);
            salaryLabel.setText("שכר המשמרת");
            salaryLabel.setTextSize(15);
            salaryLabel.setTextColor(accent);

            TextView salaryValue = new TextView(this);
            salaryValue.setText(salaryText);
            salaryValue.setTextSize(19);
            salaryValue.setTypeface(null, Typeface.BOLD);
            salaryValue.setTextColor(accent);
            salaryValue.setGravity(Gravity.END);

            salaryBox.addView(
                    salaryLabel,
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    )
            );

            salaryBox.addView(salaryValue);

            card.addView(salaryBox);

            shiftListLayout.addView(card);
        }
    }

    // המרת dp לפיקסלים
    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density
                        + 0.5f
        );
    }

    // עיצוב סיכום השכר
    private void calculateSalary() {

        fsDAO.readAllDocuments(new IFirestoreCallback() {
            @Override
            public void onDataReceived(ArrayList<Shift> shifts) {

                if (bottomNavigation.getSelectedItemId()
                        != R.id.nav_salary) {
                    return;
                }

                LinearLayout salaryLayout =
                        findViewById(R.id.shiftListLayout);

                salaryLayout.removeAllViews();
                tvcontent.setVisibility(View.GONE);

                int purple = Color.parseColor("#493377");
                int accent = Color.parseColor("#795BAF");
                int muted = Color.parseColor("#8876A6");

                double totalSalary = 0;

                ArrayList<Double> salaries = new ArrayList<>();
                ArrayList<Double> hoursList = new ArrayList<>();

                // חישוב השכר המקורי
                for (Shift shift : shifts) {

                    String[] start = shift.getstartHour().split(":");
                    String[] end = shift.getendHour().split(":");

                    int startMinutes =
                            Integer.parseInt(start[0]) * 60
                                    + Integer.parseInt(start[1]);

                    int endMinutes =
                            Integer.parseInt(end[0]) * 60
                                    + Integer.parseInt(end[1]);

                    if (endMinutes < startMinutes) {
                        endMinutes += 24 * 60;
                    }

                    double hours =
                            (endMinutes - startMinutes) / 60.0;

                    double salary = hours * shift.getpayment();

                    totalSalary += salary;

                    hoursList.add(hours);
                    salaries.add(salary);
                }

                // כותרת
                TextView title = new TextView(MainActivity.this);
                title.setText("סיכום השכר שלי");
                title.setTextSize(23);
                title.setTypeface(null, Typeface.BOLD);
                title.setTextColor(purple);
                title.setPadding(0, dp(8), 0, dp(18));

                salaryLayout.addView(title);

                // כרטיס סך השכר
                LinearLayout totalCard =
                        new LinearLayout(MainActivity.this);

                totalCard.setOrientation(LinearLayout.VERTICAL);
                totalCard.setPadding(
                        dp(22), dp(22), dp(22), dp(22)
                );

                GradientDrawable totalBackground =
                        new GradientDrawable();

                totalBackground.setColor(accent);
                totalBackground.setCornerRadius(dp(22));
                totalCard.setBackground(totalBackground);

                TextView totalLabel =
                        new TextView(MainActivity.this);

                totalLabel.setText("סך כל השכר שלי");
                totalLabel.setTextSize(16);
                totalLabel.setTextColor(
                        Color.parseColor("#F2EAFB")
                );

                totalCard.addView(totalLabel);

                TextView totalValue =
                        new TextView(MainActivity.this);

                totalValue.setText(
                        String.format(
                                java.util.Locale.getDefault(),
                                "₪%,.2f",
                                totalSalary
                        )
                );

                totalValue.setTextSize(32);
                totalValue.setTypeface(null, Typeface.BOLD);
                totalValue.setTextColor(Color.WHITE);
                totalValue.setPadding(
                        0, dp(12), 0, dp(8)
                );

                totalCard.addView(totalValue);

                TextView totalInfo =
                        new TextView(MainActivity.this);

                totalInfo.setText(
                        shifts.size() + " משמרות בסך הכול"
                );
                totalInfo.setTextSize(14);
                totalInfo.setTextColor(
                        Color.parseColor("#E8DCF7")
                );

                totalCard.addView(totalInfo);

                LinearLayout.LayoutParams totalParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                totalParams.setMargins(0, 0, 0, dp(22));

                salaryLayout.addView(totalCard, totalParams);

                // כותרת פירוט המשמרות
                TextView detailsTitle =
                        new TextView(MainActivity.this);

                detailsTitle.setText("פירוט לפי משמרות");
                detailsTitle.setTextSize(19);
                detailsTitle.setTypeface(null, Typeface.BOLD);
                detailsTitle.setTextColor(purple);
                detailsTitle.setPadding(0, 0, 0, dp(12));

                salaryLayout.addView(detailsTitle);

                if (shifts.isEmpty()) {

                    TextView empty =
                            new TextView(MainActivity.this);

                    empty.setText("עדיין אין משמרות להצגת שכר");
                    empty.setTextSize(16);
                    empty.setTextColor(muted);
                    empty.setPadding(
                            0, dp(12), 0, dp(12)
                    );

                    salaryLayout.addView(empty);
                    return;
                }

                // כרטיסייה לכל משמרת
                for (int i = 0; i < shifts.size(); i++) {

                    Shift shift = shifts.get(i);
                    double hours = hoursList.get(i);
                    double salary = salaries.get(i);

                    LinearLayout card =
                            new LinearLayout(MainActivity.this);

                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setPadding(
                            dp(18), dp(18), dp(18), dp(18)
                    );

                    GradientDrawable cardBackground =
                            new GradientDrawable();

                    cardBackground.setColor(Color.WHITE);
                    cardBackground.setCornerRadius(dp(20));

                    card.setBackground(cardBackground);
                    card.setElevation(dp(3));

                    LinearLayout.LayoutParams cardParams =
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );

                    cardParams.setMargins(
                            0, 0, 0, dp(14)
                    );

                    // תאריך ושכר
                    LinearLayout topRow =
                            new LinearLayout(MainActivity.this);

                    topRow.setOrientation(
                            LinearLayout.HORIZONTAL
                    );
                    topRow.setGravity(Gravity.CENTER_VERTICAL);

                    TextView dateText =
                            new TextView(MainActivity.this);

                    dateText.setText("▦  " + shift.getdate());
                    dateText.setTextSize(17);
                    dateText.setTypeface(null, Typeface.BOLD);
                    dateText.setTextColor(purple);

                    topRow.addView(
                            dateText,
                            new LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1
                            )
                    );

                    TextView shiftSalary =
                            new TextView(MainActivity.this);

                    shiftSalary.setText(
                            String.format(
                                    java.util.Locale.getDefault(),
                                    "₪%.2f",
                                    salary
                            )
                    );

                    shiftSalary.setTextSize(19);
                    shiftSalary.setTypeface(
                            null, Typeface.BOLD
                    );
                    shiftSalary.setTextColor(accent);

                    topRow.addView(shiftSalary);
                    card.addView(topRow);

                    // קו הפרדה
                    View divider = new View(MainActivity.this);

                    divider.setBackgroundColor(
                            Color.parseColor("#F0EAF8")
                    );

                    LinearLayout.LayoutParams dividerParams =
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    dp(1)
                            );

                    dividerParams.setMargins(
                            0, dp(15), 0, dp(15)
                    );

                    card.addView(divider, dividerParams);

                    // שעות עבודה
                    TextView timeText =
                            new TextView(MainActivity.this);

                    timeText.setText(
                            "שעות עבודה: "
                                    + shift.getstartHour()
                                    + " - "
                                    + shift.getendHour()
                    );

                    timeText.setTextSize(15);
                    timeText.setTextColor(purple);
                    timeText.setPadding(
                            0, 0, 0, dp(9)
                    );

                    card.addView(timeText);

                    // מספר שעות
                    TextView hoursText =
                            new TextView(MainActivity.this);

                    hoursText.setText(
                            "מספר שעות: "
                                    + String.format(
                                    java.util.Locale.getDefault(),
                                    "%.2f",
                                    hours
                            )
                    );

                    hoursText.setTextSize(15);
                    hoursText.setTextColor(muted);
                    hoursText.setPadding(
                            0, 0, 0, dp(9)
                    );

                    card.addView(hoursText);

                    // תשלום לשעה
                    TextView paymentText =
                            new TextView(MainActivity.this);

                    paymentText.setText(
                            "תשלום לשעה: "
                                    + shift.getpayment()
                                    + " ₪"
                    );

                    paymentText.setTextSize(15);
                    paymentText.setTextColor(muted);

                    card.addView(paymentText);

                    salaryLayout.addView(card, cardParams);
                }
            }
        });
    }
}