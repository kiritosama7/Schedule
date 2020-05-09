package com.dl.schedule.DB;


public class Motion  {
    private int ID=-1;
    private String localDate;
    private double distance;
    private String flagDis;
    private String goal;

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getFlagDis() {
        return flagDis;
    }

    public void setFlagDis(String flagDis) {
        this.flagDis = flagDis;
    }


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getLocalDate() {
        return localDate;
    }

    public void setLocalDate(String localDate) {
        this.localDate = localDate;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
