
package com.katza.myfirebaseapplication;

public class Shift {
    private String date;
    private String startHour;
    private String endHour;
    private int payment;

    public Shift() {}

    public String getdate() {
        return date;
    }

    public void setdate(String date) {
        this.date = date;
    }

    public String getstartHour() {
        return startHour;
    }

    public void setstartHour(String startHour) {
        this.startHour = startHour;
    }

    public String getendHour() {
        return endHour;
    }

    public void setendHour(String endHour) {
        this.endHour = endHour;
    }

    public int getpayment() {
        return payment;
    }

    public void setpayment(int payment) {
        this.payment = payment;
    }

    @Override
    public String toString() {
        return "\n\n****************************\nתאריך: "
                + this.getdate()
                + "\nשעת התחלה: " + this.getstartHour()
                + "\nשעת סיום: " + this.getendHour()
                + "\nשכר: " + this.getpayment()
                + "\n****************************\n\n";
    }
}
