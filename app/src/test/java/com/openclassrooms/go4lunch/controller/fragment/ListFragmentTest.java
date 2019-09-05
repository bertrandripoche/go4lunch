package com.openclassrooms.go4lunch.controller.fragment;

import android.location.Location;

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
        Location lastKnownLocation = mock(Location.class);

        when(lastKnownLocation.getLatitude()).thenReturn(48.878473);
        when(lastKnownLocation.getLongitude()).thenReturn(2.353904);

        assertEquals("250m", listFragment.getDistanceFromLastKnownLocation(48.869902,2.352717));
    }
}