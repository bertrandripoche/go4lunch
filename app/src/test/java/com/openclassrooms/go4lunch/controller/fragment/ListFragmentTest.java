package com.openclassrooms.go4lunch.controller.fragment;

import android.location.Location;


import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListFragmentTest {

    ListFragment fragment;

    @Before
    public void setUp() throws Exception {
        fragment = new ListFragment();
    }

    @Test
    public void gettingDistanceShouldReturn250m() throws Exception {
        ListFragment listFragment = mock(ListFragment.class);

        Location lastKnownLocation = new Location("");

        when(FirebaseFirestore.getInstance()).thenReturn(null);
        when(lastKnownLocation.getLatitude()).thenReturn(48.878473d);
        when(lastKnownLocation.getLongitude()).thenReturn(2.353904d);

        assertEquals("250m", listFragment.getDistanceFromLastKnownLocation(48.869902,2.352717));
    }
}