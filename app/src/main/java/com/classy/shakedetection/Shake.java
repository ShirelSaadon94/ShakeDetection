package com.classy.shakedetection;

import java.util.Date;

public class Shake {
    private float speed = 0.00f;
    private Date date;
    private double forGraph;


    public Shake(float speed, Date date, double forGraph) {
        this.speed = speed;
        this.date = date;
        this.forGraph = forGraph;
    }

    public Shake() {
    }

    @Override
    public String toString() {
        return "Shake{" +
                "speed=" + speed +
                ", date=" + date +
                ", forGraph=" + forGraph +
                '}';
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getForGraph() {
        return forGraph;
    }

    public void setForGraph(double forGraph) {
        this.forGraph = forGraph;
    }
}