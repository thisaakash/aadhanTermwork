package com.aakash.aadhan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.aakash.aadhan.model.Company;

import java.util.HashMap;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private Map<String, LatLng> companiesMap = new HashMap<>();
    private Map<String, String> markerIdToDocumentId = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        db = FirebaseFirestore.getInstance();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float density = metrics.density;

        int iconSize = (int) (24 * density);

        Bitmap customIcon = BitmapHelper.getBitmapFromVectorDrawable(requireContext(), R.drawable.ic_building, iconSize, iconSize);

        db.collection("companies")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Company company = document.toObject(Company.class);
                        if (company != null && company.getLatitude() != 0 && company.getLongitude() != 0) {
                            LatLng location = new LatLng(company.getLatitude(), company.getLongitude());
                            if (!companiesMap.containsKey(company.getName())) {
                                companiesMap.put(company.getName(), location);
                                
                                // Create custom marker
                                BitmapDescriptor customMarker = createCustomMarker(company.getName());
                                
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .icon(customMarker));
                                
                                if (marker != null) {
                                    markerIdToDocumentId.put(marker.getId(), document.getId());
                                }
                            }
                        }
                    }

                    LatLng ahmedabadCenter = new LatLng(23.0225, 72.5714);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ahmedabadCenter, 12.0f));
                } else {
                    // Handle possible errors.
                }
            });

        mMap.setOnMarkerClickListener(this);
    }

    private BitmapDescriptor createCustomMarker(String companyName) {
        View markerView = LayoutInflater.from(getContext()).inflate(R.layout.custom_marker, null);
        
        ImageView markerIcon = markerView.findViewById(R.id.marker_icon);
        TextView markerText = markerView.findViewById(R.id.marker_text);
        
        markerIcon.setImageResource(R.drawable.ic_building);
        markerText.setText(companyName);
        
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        
        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);
        
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String documentId = markerIdToDocumentId.get(marker.getId());
        if (documentId != null) {
            fetchCompanyAndStartActivity(documentId);
        }
        return true;
    }

    private void fetchCompanyAndStartActivity(String documentId) {
        db.collection("companies").document(documentId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Company company = documentSnapshot.toObject(Company.class);
                    if (company != null) {
                        Intent intent = new Intent(getActivity(), CompanyDetailActivity.class);
                        intent.putExtra("company", company); // Pass the Company object directly
                        startActivity(intent);
                    }
                }
            })
            .addOnFailureListener(e -> {
                // Handle any errors
            });
    }
}
