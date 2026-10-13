package com.katza.myfirebaseapplication;

public class Student {
    private String fullName, id;
    private int grade;

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getFullName() {
        return fullName;
    }

    public String getId() {
        return id;
    }

    public int getGrade() {
        return grade;
    }

    public Student() {

    }
    @Override
    public String toString() {
        return
                "\n\n*************************\nשם מלא:" + this.getFullName() + "\nמספר ת.ז : " + this.getId() + "\nציון סופי : " + this.getGrade() + "\n*************************\n\n";
    }
}
