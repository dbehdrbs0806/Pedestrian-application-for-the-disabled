package com.example.connect_bridge;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Location {
    private static int idConuter = 1;

    @SerializedName("Id")
    private int Id;
    @SerializedName("StartingLocation")
    private String StartingLocation;

    @SerializedName("DepartureLocation")
    private String DepartureLocation;

    @SerializedName("Timstamp")
    private Date Timestamp;

    @SerializedName("Disability")
    private int Disability;

    public Location(String StartingLocation, String DepartureLocation, int Disability) {
        this.Id = idConuter++;
        this.StartingLocation = StartingLocation;
        this.DepartureLocation = DepartureLocation;
        this.Timestamp = new Date();
        this.Disability = Disability;
    }
    public void setStartingLocation(String startingLocation) { StartingLocation = startingLocation;}
    public void setId(int id) { Id = id; }
    public void setDepartureLocation(String departureLocation) { DepartureLocation = departureLocation;}
    public void setTimestamp(Date timestamp) { Timestamp = timestamp; }
    public void setDisability(int disability) { Disability = disability; }

    public int getId() { return Id; }
    public String getStartingLocation() { return StartingLocation; }
    public String getDepartureLocation() { return DepartureLocation; }
    public Date getTimestamp() { return Timestamp; }
    public int getDisability() { return Disability; }

}
