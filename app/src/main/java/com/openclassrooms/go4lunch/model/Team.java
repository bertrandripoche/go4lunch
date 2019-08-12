package com.openclassrooms.go4lunch.model;
import androidx.annotation.Nullable;
import java.util.HashMap;

public class Team {

    private HashMap<String, String> lunchPlaceByEmployee;

    public Team() { }

    public Team(@Nullable HashMap<String, String> lunchPlaceByEmployee) {
        this.lunchPlaceByEmployee = lunchPlaceByEmployee;
    }

    // --- GETTERS ---
    public  HashMap<String, String>  getLunchPlaceByEmployee() {return lunchPlaceByEmployee;}

    // --- SETTERS ---
    public void setLunchPlaceByEmployee(HashMap<String, String> lunchPlaceByEmployee) {this.lunchPlaceByEmployee = lunchPlaceByEmployee;}

}
