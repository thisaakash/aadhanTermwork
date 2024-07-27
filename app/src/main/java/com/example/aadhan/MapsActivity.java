package com.example.aadhan;

import androidx.fragment.app.FragmentActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.example.aadhan.databinding.ActivityMapsBinding;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Bitmap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // List of locations with LatLng coordinates and titles for companies in Gujarat
        List<Location> gujaratLocations = new ArrayList<>();
        gujaratLocations.add(new Location(new LatLng(23.0225, 72.4850747), "Mastek"));
        gujaratLocations.add(new Location(new LatLng(23.0287902, 72.4867348), "Cimcom"));
        gujaratLocations.add(new Location(new LatLng(23.0385179, 72.5102451), "Gateway"));
        gujaratLocations.add(new Location(new LatLng(23.0328143, 72.5542604), "Infopercept"));
        gujaratLocations.add(new Location(new LatLng(23.02839, 72.4968785), "Simform"));
        gujaratLocations.add(new Location(new LatLng(22.9929611, 72.4966474), "Zobi"));
        gujaratLocations.add(new Location(new LatLng(21.1453509, 72.7541851), "La Net"));
        gujaratLocations.add(new Location(new LatLng(23.0407732, 72.5039941), "Inventyv"));

        Bitmap customIcon = BitmapHelper.getBitmapFromVectorDrawable(this, R.drawable.building);
        // Add markers for each company in Gujarat
        for (Location location : gujaratLocations) {
            mMap.addMarker(new MarkerOptions()
                    .position(location.latLng)
                    .title(location.title)
                    .icon(BitmapDescriptorFactory.fromBitmap(customIcon)));
        }

        // Move the camera to Ahmedabad with a zoom level to focus on the city
        LatLng ahmedabadCenter = new LatLng(23.0225, 72.5714); // Central point in Ahmedabad
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ahmedabadCenter, 12.0f)); // Zoom level 12

        // Set the custom info window adapter
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));

        // Set a listener for info window clicks
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // Get the title of the clicked marker
        String markerTitle = marker.getTitle();

        // Create an intent to start the InfoActivity
        Intent intent = new Intent(MapsActivity.this, InfoActivity.class);
        // Pass the marker title to the InfoActivity
        intent.putExtra("markerTitle", markerTitle);
        startActivity(intent);
    }

    // Custom class to store LatLng and title
    private static class Location {
        LatLng latLng;
        String title;

        Location(LatLng latLng, String title) {
            this.latLng = latLng;
            this.title = title;
        }
    }
}
