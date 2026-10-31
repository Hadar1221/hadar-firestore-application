package com.katza.myfirebaseapplication;

public interface IFirestoreImageCallback<T> {

    void onSuccess(T result);

    void onFailure(Exception error);
}
