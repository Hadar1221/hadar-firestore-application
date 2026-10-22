
package com.katza.myfirebaseapplication;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreDAO {

    private static final String COLLECTION_NAME = "shift";

    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private ListenerRegistration listenerRegistration;

    // הוספת משמרת חדשה
    public void createDocument(
            String date,
            String startHour,
            String finishHour,
            int payment) {

        Map<String, Object> shiftRecord = new HashMap<>();

        shiftRecord.put("date", date);
        shiftRecord.put("startHour", startHour);
        shiftRecord.put("finishHour", finishHour);
        shiftRecord.put("payment", payment);

        db.collection(COLLECTION_NAME)
                .add(shiftRecord)
                .addOnSuccessListener(documentReference ->
                        Log.d("DAO_FIREBASE",
                                "SUCCESSFUL WRITE"))
                .addOnFailureListener(e ->
                        Log.e("DAO_FIREBASE",
                                "WRITE FAILED", e));
    }

    // עדכון משמרת לפי מזהה המסמך
    public void updateDocument(
            String documentID,
            String startHour,
            String finishHour,
            int payment) {

        Map<String, Object> shiftRecord = new HashMap<>();

        shiftRecord.put("startHour", startHour);
        shiftRecord.put("finishHour", finishHour);
        shiftRecord.put("payment", payment);

        db.collection(COLLECTION_NAME)
                .document(documentID)
                .update(shiftRecord)
                .addOnSuccessListener(unused ->
                        Log.d("DAO_FIREBASE",
                                "SUCCESSFUL UPDATE"))
                .addOnFailureListener(e ->
                        Log.e("DAO_FIREBASE",
                                "UPDATE FAILED", e));
    }

    // קריאת משמרת לפי מזהה
    public void readDocumentById(
            String docID,
            IFirestoreCallback callback) {

        db.collection(COLLECTION_NAME)
                .document(docID)
                .get()
                .addOnSuccessListener(document -> {

                    ArrayList<Shift> shifts =
                            new ArrayList<>();

                    if (document.exists()) {
                        Shift shift =
                                document.toObject(Shift.class);

                        if (shift != null) {
                            shifts.add(shift);
                        }
                    }

                    callback.onDataReceived(shifts);
                })
                .addOnFailureListener(e -> {
                    Log.e("DAO_FIREBASE",
                            "READ FAILED", e);

                    callback.onDataReceived(
                            new ArrayList<>());
                });
    }

    // שליפת כל המשמרות
    public void readAllDocuments(
            IFirestoreCallback callback) {

        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(snapshots -> {

                    ArrayList<Shift> shifts =
                            new ArrayList<>();

                    for (DocumentSnapshot document :
                            snapshots.getDocuments()) {

                        Shift shift =
                                document.toObject(Shift.class);

                        if (shift != null) {
                            shifts.add(shift);
                        }
                    }

                    callback.onDataReceived(shifts);
                })
                .addOnFailureListener(e -> {
                    Log.e("DAO_FIREBASE",
                            "READ ALL FAILED", e);

                    callback.onDataReceived(
                            new ArrayList<>());
                });
    }

    // שליפה לפי תאריך
    public void readAllDocumentsByDate(
            String searchDate,
            IFirestoreCallback callback) {

        db.collection(COLLECTION_NAME)
                .whereEqualTo("date", searchDate)
                .get()
                .addOnSuccessListener(snapshots -> {

                    ArrayList<Shift> shifts =
                            new ArrayList<>();

                    for (DocumentSnapshot document :
                            snapshots.getDocuments()) {

                        Shift shift =
                                document.toObject(Shift.class);

                        if (shift != null) {
                            shifts.add(shift);
                        }
                    }

                    callback.onDataReceived(shifts);
                })
                .addOnFailureListener(e -> {
                    Log.e("DAO_FIREBASE",
                            "WHERE QUERY FAILED", e);

                    callback.onDataReceived(
                            new ArrayList<>());
                });
    }

    // מחיקת משמרת לפי מזהה המסמך
    public void deleteDocument(String documentID) {

        db.collection(COLLECTION_NAME)
                .document(documentID)
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d("DAO_FIREBASE",
                                "SUCCESSFUL DELETE"))
                .addOnFailureListener(e ->
                        Log.e("DAO_FIREBASE",
                                "DELETE FAILED", e));
    }

    // מאזין בזמן אמת לכל המשמרות
    public void listenToAllShifts(
            IFirestoreCallback callback) {

        stopListening();

        listenerRegistration =
                db.collection(COLLECTION_NAME)
                        .addSnapshotListener(
                                (snapshots, error) -> {

                                    if (error != null) {
                                        Log.e(
                                                "DAO_FIREBASE",
                                                "LISTENER FAILED",
                                                error);
                                        return;
                                    }

                                    if (snapshots == null) {
                                        return;
                                    }

                                    ArrayList<Shift> shifts =
                                            new ArrayList<>();

                                    for (DocumentSnapshot document :
                                            snapshots.getDocuments()) {

                                        Shift shift =
                                                document.toObject(
                                                        Shift.class);

                                        if (shift != null) {
                                            shifts.add(shift);
                                        }
                                    }

                                    callback.onDataReceived(shifts);
                                });
    }

    // מאזין בזמן אמת לפי תאריך
    public void listenToShiftsByDate(
            String date,
            IFirestoreCallback callback) {

        stopListening();

        listenerRegistration =
                db.collection(COLLECTION_NAME)
                        .whereEqualTo("date", date)
                        .addSnapshotListener(
                                (snapshots, error) -> {

                                    if (error != null) {
                                        Log.e(
                                                "DAO_FIREBASE",
                                                "FILTER LISTENER FAILED",
                                                error);
                                        return;
                                    }

                                    if (snapshots == null) {
                                        return;
                                    }

                                    ArrayList<Shift> shifts =
                                            new ArrayList<>();

                                    for (DocumentSnapshot document :
                                            snapshots.getDocuments()) {

                                        Shift shift =
                                                document.toObject(
                                                        Shift.class);

                                        if (shift != null) {
                                            shifts.add(shift);
                                        }
                                    }

                                    callback.onDataReceived(shifts);
                                });
    }

    // מאזין למסמך מסוים
    public void listenToDocumentChange(String docID) {

        stopListening();

        listenerRegistration =
                db.collection(COLLECTION_NAME)
                        .document(docID)
                        .addSnapshotListener(
                                (document, error) -> {

                                    if (error != null) {
                                        Log.e(
                                                "DAO_FIREBASE",
                                                "DOCUMENT LISTENER FAILED",
                                                error);
                                        return;
                                    }

                                    Log.d(
                                            "DAO_FIREBASE",
                                            "DOCUMENT CHANGED");
                                });
    }

    public void listenToADocumentChange(String docID) {
        listenToDocumentChange(docID);
    }

    // עצירת המאזין
    public void stopListening() {

        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }
}
