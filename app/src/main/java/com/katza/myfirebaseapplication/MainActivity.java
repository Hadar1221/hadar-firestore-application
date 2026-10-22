package com.katza.myfirebaseapplication;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etDate, etStartHour, etFinishHour, etPayment;
    private EditText etSearchDate;

    private Button btnInsert, btnSelect, btnUpdate, btnDelete;
    private Button btnSearch, btnShowAll;

    private TextView tvcontent;
    private ScrollView scrollContent, formScroll;
    private LinearLayout shiftListLayout, searchLayout;

    private FirestoreDAO fsDAO;

    private String selectedShiftDate = null;
    private String activeSearchDate = null;

    private int currentPage = R.id.nav_add;
    private int listenerVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDate = findViewById(R.id.date);
        etStartHour = findViewById(R.id.startHour);
        etFinishHour = findViewById(R.id.finishHour);
        etPayment = findViewById(R.id.payment);

        etSearchDate = findViewById(R.id.etSearchDate);

        btnInsert = findViewById(R.id.btnInsert);
        btnSelect = findViewById(R.id.btnSelect);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        btnSearch = findViewById(R.id.btnSearch);
        btnShowAll = findViewById(R.id.btnShowAll);

        tvcontent = findViewById(R.id.tvcontent);
        scrollContent = findViewById(R.id.scrollContent);
        formScroll = findViewById(R.id.formScroll);
        shiftListLayout = findViewById(R.id.shiftListLayout);
        searchLayout = findViewById(R.id.searchLayout);

        fsDAO = new FirestoreDAO();

        BottomNavigationView navigation =
                findViewById(R.id.bottomNavigation);

        navigation.setOnItemSelectedListener(item -> {
            openPage(item.getItemId());
            return true;
        });

        openPage(R.id.nav_add);

        btnSearch.setOnClickListener(v -> {
            String date = etSearchDate.getText()
                    .toString().trim();

            if (date.isEmpty()) {
                showToast("יש להזין תאריך");
                return;
            }

            activeSearchDate = date;
            startShiftListener();
        });

        btnShowAll.setOnClickListener(v -> {
            etSearchDate.setText("");
            activeSearchDate = null;
            startShiftListener();
        });

        btnInsert.setOnClickListener(v -> {
            if (!validateFields()) return;

            fsDAO.createDocument(
                    getText(etDate),
                    getText(etStartHour),
                    getText(etFinishHour),
                    Integer.parseInt(getText(etPayment)));

            showToast("בקשת הוספת המשמרת נשלחה");
        });

        btnUpdate.setOnClickListener(v -> {
            if (selectedShiftDate == null) {
                showToast("יש לבחור משמרת");
                return;
            }

            if (getText(etStartHour).isEmpty() ||
                    getText(etFinishHour).isEmpty() ||
                    !validPayment()) {
                showToast("יש למלא את כל השדות");
                return;
            }

            fsDAO.updateDocument(
                    selectedShiftDate,
                    getText(etStartHour),
                    getText(etFinishHour),
                    Integer.parseInt(getText(etPayment)));

            showToast("בקשת העדכון נשלחה");
        });

        btnDelete.setOnClickListener(v -> {
            String date = getText(etDate);

            if (date.isEmpty()) {
                showToast("יש להזין תאריך למחיקה");
                return;
            }

            fsDAO.deleteDocument(date);
            showToast("בקשת המחיקה נשלחה");
        });
    }

    private void openPage(int page) {
        hideAll();
        currentPage = page;

        if (page == R.id.nav_add) {
            showAddPage();

        } else if (page == R.id.nav_shifts) {
            scrollContent.setVisibility(View.VISIBLE);
            searchLayout.setVisibility(View.VISIBLE);
            activeSearchDate = null;
            startShiftListener();

        } else if (page == R.id.nav_update) {
            scrollContent.setVisibility(View.VISIBLE);
            tvcontent.setText("בחרי משמרת לעדכון");
            loadShiftsForUpdate();

        } else if (page == R.id.nav_delete) {
            formScroll.setVisibility(View.VISIBLE);
            etDate.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            etDate.setText("");

        } else if (page == R.id.nav_salary) {
            scrollContent.setVisibility(View.VISIBLE);
            tvcontent.setText("סיכום השכר שלי");
            loadSalarySummary();
        }
    }

    private void showAddPage() {
        clearFields();

        formScroll.setVisibility(View.VISIBLE);

        etDate.setVisibility(View.VISIBLE);
        etStartHour.setVisibility(View.VISIBLE);
        etFinishHour.setVisibility(View.VISIBLE);
        etPayment.setVisibility(View.VISIBLE);

        btnInsert.setVisibility(View.VISIBLE);
    }

    private void startShiftListener() {
        final int version = ++listenerVersion;

        tvcontent.setText("המשמרות שלי");
        shiftListLayout.removeAllViews();

        IFirestoreCallback callback = shifts -> {
            if (currentPage != R.id.nav_shifts ||
                    version != listenerVersion) {
                return;
            }

            displayShiftCards(shifts);
        };

        if (activeSearchDate == null) {
            fsDAO.listenToAllShifts(callback);
        } else {
            fsDAO.listenToShiftsByDate(
                    activeSearchDate, callback);
        }
    }

    private void displayShiftCards(ArrayList<Shift> shifts) {
        shiftListLayout.removeAllViews();

        tvcontent.setText(
                "המשמרות שלי  •  " + shifts.size() + " משמרות");

        if (shifts.isEmpty()) {
            TextView empty = makeText(
                    "לא נמצאו משמרות", 17, false);
            empty.setGravity(Gravity.CENTER);
            shiftListLayout.addView(empty);
            return;
        }

        long total = 0;

        for (Shift shift : shifts) {
            total += shift.getpayment();

            LinearLayout card = createCard();

            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);

            TextView date = makeText(
                    "📅  " + shift.getdate(), 18, true);

            date.setTextColor(
                    getColor(R.color.shiftips_dark_purple));

            top.addView(date);
            card.addView(top);

            TextView hours = makeText(
                    "◷  " + shift.getstartHour() +
                            " - " + shift.getfinishHour(),
                    16, false);

            hours.setPadding(0, dp(12), 0, dp(10));
            card.addView(hours);

            TextView payment = makeText(
                    "₪  " + shift.getpayment() + " שכר למשמרת",
                    18, true);

            payment.setTextColor(
                    getColor(R.color.shiftips_purple));

            card.addView(payment);

            shiftListLayout.addView(card);
        }

        LinearLayout summary = createCard();
        summary.setBackgroundTintList(
                getColorStateList(
                        R.color.shiftips_light_purple));

        TextView totalText = makeText(
                "סה״כ שכר: " + total + " ₪", 21, true);

        totalText.setTextColor(
                getColor(R.color.shiftips_dark_purple));

        totalText.setGravity(Gravity.CENTER);
        summary.addView(totalText);

        shiftListLayout.addView(summary);
    }

    private void loadShiftsForUpdate() {
        fsDAO.readAllDocuments(shifts -> {
            if (currentPage != R.id.nav_update) return;

            shiftListLayout.removeAllViews();

            if (shifts.isEmpty()) {
                tvcontent.setText("אין משמרות לעדכון");
                return;
            }

            for (Shift shift : shifts) {
                LinearLayout card = createCard();

                TextView info = makeText(
                        "📅  " + shift.getdate() +
                                "\n◷  " + shift.getstartHour() +
                                " - " + shift.getfinishHour() +
                                "\n₪  " + shift.getpayment(),
                        17, true);

                card.addView(info);

                Button choose = new Button(this);
                choose.setText("בחרי משמרת זו");
                choose.setTextColor(getColor(R.color.white));
                choose.setBackgroundTintList(
                        getColorStateList(
                                R.color.shiftips_purple));

                card.addView(choose);
                shiftListLayout.addView(card);

                choose.setOnClickListener(v -> {
                    selectedShiftDate = shift.getdate();

                    etDate.setText(shift.getdate());
                    etStartHour.setText(shift.getstartHour());
                    etFinishHour.setText(shift.getfinishHour());
                    etPayment.setText(
                            String.valueOf(shift.getpayment()));

                    etDate.setEnabled(false);

                    etDate.setVisibility(View.VISIBLE);
                    etStartHour.setVisibility(View.VISIBLE);
                    etFinishHour.setVisibility(View.VISIBLE);
                    etPayment.setVisibility(View.VISIBLE);
                    btnUpdate.setVisibility(View.VISIBLE);

                    scrollContent.setVisibility(View.GONE);
                    formScroll.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void loadSalarySummary() {
        fsDAO.readAllDocuments(shifts -> {
            if (currentPage != R.id.nav_salary) return;

            shiftListLayout.removeAllViews();

            long total = 0;

            for (Shift shift : shifts) {
                total += shift.getpayment();
            }

            double average = shifts.isEmpty()
                    ? 0 : (double) total / shifts.size();

            LinearLayout card = createCard();

            TextView title = makeText(
                    "💜  סיכום השכר שלי", 22, true);
            card.addView(title);

            TextView sum = makeText(
                    "סה״כ שכר\n" + total + " ₪",
                    26, true);

            sum.setTextColor(
                    getColor(R.color.shiftips_purple));
            sum.setPadding(0, dp(20), 0, dp(20));
            card.addView(sum);

            card.addView(makeText(
                    "מספר משמרות: " + shifts.size(),
                    17, false));

            card.addView(makeText(
                    "ממוצע למשמרת: " +
                            String.format(Locale.getDefault(),
                                    "%.2f", average) + " ₪",
                    17, false));

            shiftListLayout.addView(card);

            for (Shift shift : shifts) {
                LinearLayout detail = createCard();

                detail.addView(makeText(
                        shift.getdate() + "   •   " +
                                shift.getpayment() + " ₪",
                        17, true));

                detail.addView(makeText(
                        shift.getstartHour() + " - " +
                                shift.getfinishHour(),
                        15, false));

                shiftListLayout.addView(detail);
            }
        });
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(
                dp(20), dp(18), dp(20), dp(18));
        card.setBackgroundResource(R.drawable.rounded_card);
        card.setElevation(dp(3));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);

        return card;
    }

    private TextView makeText(
            String value, int size, boolean bold) {

        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(getColor(R.color.shiftips_dark));
        text.setGravity(Gravity.RIGHT);
        text.setTextDirection(View.TEXT_DIRECTION_RTL);

        if (bold) {
            text.setTypeface(null,
                    android.graphics.Typeface.BOLD);
        }

        return text;
    }

    private void hideAll() {
        listenerVersion++;
        fsDAO.stopListening();

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
        searchLayout.setVisibility(View.GONE);

        shiftListLayout.removeAllViews();
        tvcontent.setText("");

        selectedShiftDate = null;
    }

    private void clearFields() {
        etDate.setText("");
        etStartHour.setText("");
        etFinishHour.setText("");
        etPayment.setText("");
    }

    private boolean validPayment() {
        try {
            int payment = Integer.parseInt(
                    getText(etPayment));
            return payment >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateFields() {
        if (getText(etDate).isEmpty() ||
                getText(etStartHour).isEmpty() ||
                getText(etFinishHour).isEmpty() ||
                !validPayment()) {

            showToast("יש למלא את כל השדות בצורה תקינה");
            return false;
        }

        return true;
    }

    private String getText(EditText editText) {
        return editText.getText().toString().trim();
    }

    private void showToast(String message) {
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (fsDAO != null &&
                currentPage == R.id.nav_shifts) {
            startShiftListener();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        listenerVersion++;

        if (fsDAO != null) {
            fsDAO.stopListening();
        }
    }
}
