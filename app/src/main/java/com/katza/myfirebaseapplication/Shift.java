package com.katza.myfirebaseapplication;

public class Shift {
    private String date;
    private String startHour;
    private String finishHour;
    private int payment;

    public void setdate(String date) {
        this.date = date;
    }
    public void setstartHour(String startHour) {
        this.startHour = startHour;
    }

    public void setfinishHour(String finishHour) {
        this.finishHour = finishHour;
    }

    public void setpayment(int payment) {
        this.payment = payment;
    }

    public String getdate() {
        return date;
    }
    public String getstartHour() {
        return startHour;
    }

    public String getfinishHour() { return finishHour; }

    public int getpayment() {
        return payment;
    }

    public Shift() {

    }
    @Override
    public String toString() {
        return
                "\n\n*************************\nתאריך:" + this.getdate() + "\nשעת התחלה : " + this.getstartHour() + "\nשעת סיום : " + this.getfinishHour() +  "\nשכר : " + this.getpayment() + "\n*************************\n\n";
    }
}
