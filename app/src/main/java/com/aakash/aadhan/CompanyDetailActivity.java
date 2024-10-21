package com.aakash.aadhan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.aakash.aadhan.model.Company;
import androidx.core.content.res.ResourcesCompat;
import android.graphics.Typeface;
import android.text.Html;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import android.widget.LinearLayout;
import android.graphics.Bitmap;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

public class CompanyDetailActivity extends AppCompatActivity {

    private static final String TAG = "CompanyDetailActivity";
    private PlacesClient placesClient;

    private RecyclerView recyclerViewImages;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_detail);

        Intent intent = getIntent();
        String companyId = intent.getStringExtra("companyId");
        String companyName = intent.getStringExtra("companyName");

        // Now use these values to populate your views
        TextView nameTextView = findViewById(R.id.textViewCompanyName);
        nameTextView.setText(companyName);

        // If you need more details, you can fetch them from Firestore using the companyId
        // ... implement other functionality

        Log.d(TAG, "onCreate: Starting activity");

        // Initialize Places API
        Places.initialize(getApplicationContext(), "AIzaSyAV0BwzviBTs8XACkqG1TK7ysI1nnfISSQ");
        placesClient = Places.createClient(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Company Details");
        }

        ImageView imageViewCompany = findViewById(R.id.imageViewCompany);
        TextView textViewCompanyName = findViewById(R.id.textViewCompanyName);
        TextView textViewAddress = findViewById(R.id.textViewAddress);

        // Initialize RecyclerView and Adapter
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        recyclerViewImages.setLayoutManager(new GridLayoutManager(this, 2));
        imageAdapter = new ImageAdapter();
        recyclerViewImages.setAdapter(imageAdapter);

        // Set custom font
        Typeface customFont = ResourcesCompat.getFont(this, R.font.roboto_regular);
        textViewAddress.setTypeface(customFont);

        Company company = (Company) getIntent().getSerializableExtra("company");

        if (company != null) {
            Log.d(TAG, "onCreate: Company name: " + company.getName());

            Glide.with(this)
                    .load(company.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(imageViewCompany);

            textViewCompanyName.setText(company.getName());

            // Get and display the address and images
            String searchQuery = company.getName() + ", " + company.getCity();
            getAddressAndImagesFromCompanyName(searchQuery, textViewAddress);
        } else {
            Log.e(TAG, "onCreate: Company object is null");
        }
    }

    private void getAddressAndImagesFromCompanyName(String companyName, TextView textViewAddress) {
        Log.d(TAG, "getAddressAndImagesFromCompanyName: Searching for " + companyName);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(companyName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressText = new StringBuilder();
                
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressText.append(address.getAddressLine(i)).append("<br>");
                }
                
                String city = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();
                
                addressText.append("City: ").append(city).append("<br>")
                           .append("State: ").append(state).append("<br>")
                           .append("Country: ").append(country).append("<br>")
                           .append("Postal Code: ").append(postalCode);
                
                textViewAddress.setText(Html.fromHtml("<b>Address:</b><br>" + addressText.toString()));

                Log.d(TAG, "getAddressAndImagesFromCompanyName: Address found - " + addressText.toString());

                // Fetch place details and photos
                getPlaceId(companyName);
            } else {
                textViewAddress.setText(Html.fromHtml("<b>Address:</b> Not available"));
                Log.w(TAG, "getAddressAndImagesFromCompanyName: No address found for " + companyName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            textViewAddress.setText("Address: Error fetching address");
            Log.e(TAG, "getAddressAndImagesFromCompanyName: Error fetching address", e);
        }
    }

    private void getPlaceId(String companyName) {
        Log.d(TAG, "getPlaceId: Searching for place ID for " + companyName);

        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
            .setQuery(companyName)
            .setCountries("IN") // Assuming you're searching in India, change if needed
            .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                if (prediction.getPlaceTypes().contains(Place.Type.ESTABLISHMENT)) {
                    Log.d(TAG, "getPlaceId: Found place ID: " + prediction.getPlaceId());
                    Log.d(TAG, "getPlaceId: Place full text: " + prediction.getFullText(null));
                    fetchPlaceDetails(prediction.getPlaceId());
                    return;
                }
            }
            Log.w(TAG, "getPlaceId: No matching place found for " + companyName);
        }).addOnFailureListener((exception) -> {
            Log.e(TAG, "getPlaceId: Error finding place", exception);
        });
    }

    private void fetchPlaceDetails(String placeId) {
        Log.d(TAG, "fetchPlaceDetails: Fetching details for place ID: " + placeId);

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            if (place.getPhotoMetadatas() != null) {
                Log.d(TAG, "fetchPlaceDetails: Found " + place.getPhotoMetadatas().size() + " photos");
                for (PhotoMetadata photoMetadata : place.getPhotoMetadatas()) {
                    FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .setMaxWidth(500)
                            .setMaxHeight(300)
                            .build();
                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                        Bitmap bitmap = fetchPhotoResponse.getBitmap();
                        Log.e(TAG, fetchPhotoResponse.toString());
                        runOnUiThread(() -> {
                            if (imageAdapter != null) {
                                imageAdapter.addImage(bitmap);
                            } else {
                                Log.e(TAG, "fetchPlaceDetails: ImageAdapter is null");
                            }
                        });
                    }).addOnFailureListener((exception) -> {
                        Log.e(TAG, "fetchPlaceDetails: Error fetching photo", exception);
                    });
                }
            } else {
                Log.w(TAG, "fetchPlaceDetails: No photos found for this place");
            }
        }).addOnFailureListener((exception) -> {
            Log.e(TAG, "fetchPlaceDetails: Error fetching place details", exception);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private ArrayList<Bitmap> images = new ArrayList<>();

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            holder.imageView.setImageBitmap(images.get(position));
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        public void addImage(Bitmap bitmap) {
            images.add(bitmap);
            notifyItemInserted(images.size() - 1);
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
}
