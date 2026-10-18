
package com.katza.myfirebaseapplication;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreDAO {

    private static final String COLLECTION_NAME = "shift"; //Data Access Object

    // הוספת משמרת
    public void createDocument(String date, String startHour, String finishHour, int payment) {

        Map<String, Object> shiftRecord = new HashMap<>();
        shiftRecord.put("date", date);
        shiftRecord.put("startHour", startHour);
        shiftRecord.put("finishHour", finishHour);
        shiftRecord.put("payment", payment);

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME).document(date);

        docRef.set(shiftRecord)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("DAO_FIREBASE", "SUCCESSFUL WRITE!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("DAO_FIREBASE", "WRITE FAILED", e);
                    }
                });
    }

    // עדכון משמרת קיימת
    public void updateDocument(String date, String startHour, String finishHour, int payment) {

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME).document(date);

        Map<String, Object> shiftRecord = new HashMap<>();
        shiftRecord.put("startHour", startHour);
        shiftRecord.put("finishHour", finishHour);
        shiftRecord.put("payment", payment);

        docRef.update(shiftRecord)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("DAO_FIREBASE", "SUCCESSFUL UPDATE!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("DAO_FIREBASE", "UPDATE FAILED", e);
                    }
                });
    }

    // קריאת משמרת לפי תאריך
    public void readDocumentById(String docID, IFirestoreCallback iFirestoreCallback) {

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME).document(docID);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                ArrayList<Shift> shifts = new ArrayList<>();

                if (documentSnapshot.exists()) {
                    Shift shift = documentSnapshot.toObject(Shift.class);

                    if (shift != null) {
                        shifts.add(shift);
                        Log.d("DAO_FIREBASE", "SUCCESSFUL READ! " + shift);
                    }
                }

                iFirestoreCallback.onDataReceived(shifts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("DAO_FIREBASE", "READ FAILED", e);
                iFirestoreCallback.onDataReceived(new ArrayList<>());
            }
        });
    }

    // הצגת כל המשמרות
    public void readAllDocuments(IFirestoreCallback iFirestoreCallback) {

        CollectionReference colRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME);

        colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                ArrayList<Shift> shifts = new ArrayList<>();

                for (DocumentSnapshot docSS : queryDocumentSnapshots.getDocuments()) {

                    Shift shift = docSS.toObject(Shift.class);

                    if (shift != null) {
                        Log.d("DAO_FIREBASE", "SUCCESSFUL READ!! " + shift);
                        shifts.add(shift);
                    }
                }

                iFirestoreCallback.onDataReceived(shifts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("DAO_FIREBASE", "READ ALL FAILED", e);
                iFirestoreCallback.onDataReceived(new ArrayList<>());
            }
        });
    }
    public void deleteDocument(String documentID)
    {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(documentID);
        docRef.delete().addOnCanceledListener(new OnSuccessListener<void>()
        {
            @Override
            public void onSuccess(void unused) {
                Log.d("DAO_FIREBASE", "SUCESSFUL DELETE!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DAO_FIREBASE", e.getMessage());
            }
        });
    }
}
