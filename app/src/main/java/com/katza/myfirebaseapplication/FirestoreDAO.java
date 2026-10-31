
package com.katza.myfirebaseapplication;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreDAO {

    private static final String COLLECTION_NAME = "shifts";
    private static final String STUDENTS_COLLECTION_NAME = "students";

    public void addShift(String date, String startHour, String endHour, int payment) {

        Map<String, Object> shiftRecord = new HashMap<>();
        shiftRecord.put("date", date);
        shiftRecord.put("startHour", startHour);
        shiftRecord.put("endHour", endHour);
        shiftRecord.put("payment", payment);

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME).document();

        docRef.set(shiftRecord);
    }

    public void createDocument(String name, String id, int grade) {
        Map<String, Object> studentRecord = new HashMap<>();
        studentRecord.put("fullName", name);
        studentRecord.put("id", id);
        studentRecord.put("grade", grade);

        //FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(STUDENTS_COLLECTION_NAME).document(id);

        docRef.set(studentRecord).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("DAO_FIREBASE", "SUCCESSFUL WRITE!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DAO_FIREBASE", e.getMessage().toString());
            }
        });
    }

    public void readQueryDocuments(String date, IFirestoreCallback iFirestoreCallback) {

        CollectionReference colRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME);

        colRef.whereEqualTo("date", date).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Shift> shifts = new ArrayList<>();

                        for (DocumentSnapshot docSS : queryDocumentSnapshots.getDocuments()) {
                            Shift shift = docSS.toObject(Shift.class);
                            shifts.add(shift);
                        }

                        iFirestoreCallback.onDataReceived(shifts);
                    }
                });
    }

    public void readDocumentById(String docID, IFirestoreCallback iFirestoreCallback) {

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME).document(docID);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Shift shift = documentSnapshot.toObject(Shift.class);
                ArrayList<Shift> shifts = new ArrayList<>();
                shifts.add(shift);
                iFirestoreCallback.onDataReceived(shifts);
            }
        });
    }

    public void readAllDocuments() {

        CollectionReference colRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME);

        colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot docSS : queryDocumentSnapshots.getDocuments()) {
                    Shift shift = docSS.toObject(Shift.class);
                    Log.d("DAO_FIREBASE", "SUCCESSFUL READ!" + shift);
                }
            }
        });
    }

    public void readAllDocuments(IFirestoreCallback iFirestoreCallback) {

        CollectionReference colRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME);

        colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<Shift> shifts = new ArrayList<>();
                for (DocumentSnapshot docSS : queryDocumentSnapshots.getDocuments()) {
                    Shift shift = docSS.toObject(Shift.class);
                    shifts.add(shift);
                }
                iFirestoreCallback.onDataReceived(shifts);
            }
        });
    }

    public void updateDocument(String documentID, String newStartHour, String newEndHour) {

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME).document(documentID);

        docRef.update("startHour", newStartHour, "endHour", newEndHour)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("DAO_FIREBASE", "SUCCESSFUL UPDATE!");
                    }
                });
    }

    public void deleteDocument(String documentID) {

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME).document(documentID);

        docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("DAO_FIREBASE", "SUCCESSFUL DELETE!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DAO_FIREBASE", e.getMessage());
            }
        });
    }

    public void listenToDocumentChange(String docID) {
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME).document(docID);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value,
                                @Nullable FirebaseFirestoreException error) {

            }
        });
    }

    public void onEvent(@Nullable DocumentSnapshot value,
                        @Nullable FirebaseFirestoreException error) {
        if (value != null && value.exists()) {
            Shift shift = value.toObject(Shift.class);
            Log.d("CHANGED_DAO", shift.toString());
        }
    }

    private ListenerRegistration listenerRegistration;

    public void listenToAllDocumentsChange() {
        CollectionReference colRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME);

        listenerRegistration = colRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException error) {

                if (error != null || value == null)
                    return;

                for (DocumentChange docC : value.getDocumentChanges()) {
                    Shift shift = docC.getDocument().toObject(Shift.class);

                    Log.d("DAO_MULTIPLE_CHANGES",
                            docC.getType() + ": " + shift.toString());
                }
            }
        });
    }

    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    // תוספת: חיפוש מסמכים למחיקה לפי תאריך
    public void findShiftsForDelete(String date,
                                    OnSuccessListener<QuerySnapshot> listener) {

        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(listener);
    }

    public void updateDocument(String documentID,
                               String newStartHour,
                               String newEndHour,
                               int newPayment) {

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME).document(documentID);

        docRef.update(
                "startHour", newStartHour,
                "endHour", newEndHour,
                "payment", newPayment
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("DAO_FIREBASE", "SUCCESSFUL UPDATE!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DAO_FIREBASE", e.getMessage());
            }
        });
    }

}