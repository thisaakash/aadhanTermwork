package com.aakash.aadhan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import cn.pedant.SweetAlert.SweetAlertDialog;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddCompanyActivity extends AppCompatActivity {

    private static final String TAG = "AddCompanyActivity";

    private EditText etCompanyName, etCity, etTech, etLatitude, etLongitude;
    private RadioGroup rgOption;
    private Button btnSubmit;
    private FirebaseFirestore db;
    private LinearLayout layoutImageUpload;
    private ImageView ivCompanyImage;
    private Button btnUploadImage;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private ImageCapture imageCapture;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
//    private Uri imageUri;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Void> takePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    // Save the bitmap to a file and get its Uri
                    imageUri = saveBitmapToFile(bitmap);
                    if (imageUri != null) {
                        ivCompanyImage.setImageURI(imageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_company);

        Log.d(TAG, "onCreate called");

        initializeFirebaseStorage();

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        etCompanyName = findViewById(R.id.etCompanyName);
        etCity = findViewById(R.id.etCity);
        etTech = findViewById(R.id.etTech);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        rgOption = findViewById(R.id.rgOption);
        btnSubmit = findViewById(R.id.btnSubmit);
        layoutImageUpload = findViewById(R.id.layoutImageUpload);
        ivCompanyImage = findViewById(R.id.ivCompanyImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);

        rgOption.setOnCheckedChangeListener((group, checkedId) -> {
            layoutImageUpload.setVisibility(View.VISIBLE);
        });

        btnUploadImage.setOnClickListener(v -> checkPermissionAndOpenChooser());

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCompany();
            }
        });

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        findViewById(R.id.captureButton).setOnClickListener(v -> takePicture.launch(null));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void checkPermissionAndOpenChooser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                openFileChooser();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 2296);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openFileChooser();
            }
        } else {
            openFileChooser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser();
            } else {
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Permission Denied")
                        .setContentText("You need to grant storage permission to upload an image")
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Log.d(TAG, "Image selected: " + imageUri.toString());
            ivCompanyImage.setImageURI(imageUri);
        } else if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Log.d(TAG, "External storage permission granted");
                    openFileChooser();
                } else {
                    Log.e(TAG, "External storage permission denied");
                    new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Permission Denied")
                            .setContentText("You need to grant storage permission to upload an image")
                            .show();
                }
            }
        }
    }

    private void addCompany() {
        String name = etCompanyName.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String tech = etTech.getText().toString().trim();
        String latitudeStr = etLatitude.getText().toString().trim();
        String longitudeStr = etLongitude.getText().toString().trim();
        String option = ((RadioButton)findViewById(rgOption.getCheckedRadioButtonId())).getText().toString();

        Log.d(TAG, "Starting addCompany method");

        if (storage == null || storageRef == null) {
            Log.e(TAG, "Firebase Storage not initialized. Attempting to reinitialize.");
            initializeFirebaseStorage();
        }

        if (name.isEmpty() || city.isEmpty() || tech.isEmpty() || latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Please fill all fields")
                    .show();
            return;
        }

        double latitude, longitude;
        try {
            latitude = Double.parseDouble(latitudeStr);
            longitude = Double.parseDouble(longitudeStr);
        } catch (NumberFormatException e) {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Invalid Input")
                    .setContentText("Please enter valid latitude and longitude")
                    .show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please capture an image", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> company = new HashMap<>();
        company.put("name", name);
        company.put("city", city);
        company.put("tech", tech);
        company.put("latitude", latitude);
        company.put("longitude", longitude);
        company.put("option", option);

        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.primary));
        pDialog.setTitleText("Adding Company");
        pDialog.setCancelable(false);
        pDialog.show();

        String imageName = "company_images/" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(imageName);

        Log.d(TAG, "Attempting to upload image: " + imageName);
        Log.d(TAG, "Image URI: " + imageUri.toString());
        Log.d(TAG, "Storage Reference: " + imageRef.getPath());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Image uploaded successfully. Getting download URL.");
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                Log.d(TAG, "Download URL obtained: " + imageUrl);
                                company.put("imageUrl", imageUrl);
                                saveCompanyToFirestore(company, pDialog);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to get download URL", e);
                                pDialog.dismissWithAnimation();
                                handleUploadError(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to upload image", e);
                    pDialog.dismissWithAnimation();
                    handleUploadError(e);
                });
    }

    private void handleUploadError(Exception e) {
        String errorMessage = "Error uploading image: " + e.getMessage();
        Log.e(TAG, errorMessage, e);
        if (e instanceof StorageException) {
            StorageException storageException = (StorageException) e;
            Log.e(TAG, "Storage Exception Error Code: " + storageException.getErrorCode());
            Log.e(TAG, "Storage Exception HTTP Result: " + storageException.getHttpResultCode());
        }
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText(errorMessage)
                .setConfirmText("OK")
                .setConfirmClickListener(dialog -> {
                    dialog.dismissWithAnimation();
                    if (storage == null || storageRef == null) {
                        Log.e(TAG, "Firebase Storage not initialized. Attempting to reinitialize.");
                        initializeFirebaseStorage();
                    }
                })
                .show();
    }

    private void initializeFirebaseStorage() {
        try {
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
            Log.d(TAG, "Firebase Storage reinitialized successfully");
            Log.d(TAG, "Storage Bucket: " + storage.getReference().getBucket());
        } catch (Exception e) {
            Log.e(TAG, "Failed to reinitialize Firebase Storage", e);
        }
    }

    private void saveCompanyToFirestore(Map<String, Object> company, SweetAlertDialog pDialog) {
        db.collection("companies")
                .add(company)
                .addOnSuccessListener(documentReference -> {
                    pDialog.dismissWithAnimation();
                    new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Success!")
                            .setContentText("Company added successfully")
                            .setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                finish();
                            })
                            .show();
                })
                .addOnFailureListener(e -> {
                    pDialog.dismissWithAnimation();
                    new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Error adding company: " + e.getMessage())
                            .show();
                    Log.e("TAG", "Error adding company: " + e.getMessage());
                });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    private Uri saveBitmapToFile(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "company_image_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                    return uri;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error saving bitmap: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
