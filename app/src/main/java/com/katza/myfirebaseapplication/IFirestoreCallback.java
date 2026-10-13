package com.katza.myfirebaseapplication;

import java.util.ArrayList;

public interface IFirestoreCallback {
    void onDataReceived(ArrayList<Student> students);
}
