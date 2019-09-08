package com.openclassrooms.go4lunch.controller.fragment;

import android.location.Location;


import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListFragmentTest {

    @Test
    public void gettingDistanceShouldReturn250m() throws Exception {
        ListFragment listFragment = new ListFragment();

//        String distance = listFragment.getDistanceFromLastKnownLocation(48.869902,2.352717);

        Location lastKnownLocation = mock(Location.class);
//        lastKnownLocation.setLatitude(48.878473);
//        lastKnownLocation.setLongitude(2.353904d);

        when(listFragment.mDb).thenReturn(null);
        when(FirebaseFirestore.getInstance()).thenReturn(null);
        when(lastKnownLocation.getLatitude()).thenReturn(48.878473d);
        when(lastKnownLocation.getLongitude()).thenReturn(2.353904d);

        assertEquals("250m", listFragment.getDistanceFromLastKnownLocation(48.869902,2.352717));
    }
}