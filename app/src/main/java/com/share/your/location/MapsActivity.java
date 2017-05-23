package com.share.your.location;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private boolean isGranted = false;
    private Context context;
    private RelativeLayout shareGrid;
    private ImageView shareButton;

    private Location currentLocation;
    private String address;
    //    private ProgressDialog dialog;

    private void executeUserPermissionTree() {
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, android.Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("GPS");
        if (!addPermission(permissionsList, android.Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("Access coarse location");
        if (!addPermission(permissionsList, android.Manifest.permission.ACCESS_NETWORK_STATE))
            permissionsNeeded.add("Access network state");
        if (!addPermission(permissionsList, Manifest.permission.INTERNET))
            permissionsNeeded.add("Access Internet");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_WIFI_STATE))
            permissionsNeeded.add("Access WiFi state");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Access WiFi state");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Access WiFi state");


        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                                }
                            }
                        });
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
            return;
        }
        isGranted = true;

    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
                // Check for Rationale Option
                if (!shouldShowRequestPermissionRationale(permission))
                    return false;
            }
            return true;
        }
        return false;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MapsActivity.this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_NETWORK_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.INTERNET, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_WIFI_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        ) {
                    // All Permissions Granted
                    isGranted = true;
                } else {
                    isGranted = false;
                    finish();

                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isGranted) executeUserPermissionTree();
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        init();
        setListners();
        mapFragment.getMapAsync(this);
    }

    public void init() {
        context = MapsActivity.this;
        shareGrid = (RelativeLayout) findViewById(R.id.shareGrid);
        shareButton = (ImageView) findViewById(R.id.shareButton);
        if (!isNetworkAvailable()) {
          Toast.makeText(context, "Please check your Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    public void setListners() {
        shareGrid.setOnClickListener(this);
        shareButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Log.d(this.getClass().getSimpleName(), "Inside onClick");
        switch (view.getId()) {
            case R.id.shareButton:
                shareit();
                break;

        }
    }


    public void shareit() {


        final Dialog alertGroup = new Dialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog);
        alertGroup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = alertGroup.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        alertGroup.setCancelable(true);
        alertGroup.setContentView(R.layout.dialouge_layout);
        alertGroup.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        final RadioButton image_btn = (RadioButton) alertGroup.findViewById(R.id.image_btn);
        final RadioButton address_btn = (RadioButton) alertGroup.findViewById(R.id.address_btn);
        Button share_btn = (Button) alertGroup.findViewById(R.id.share_btn);

        final String dialogeTitle = "Share Location";

        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!image_btn.isChecked() && !address_btn.isChecked()) {
                    Toast.makeText(context, "Please select a type", Toast.LENGTH_SHORT).show();
                } else if (image_btn.isChecked()) {

                    File picFile;
                    String state = Environment.getExternalStorageState();
                    String formattedDate = new SimpleDateFormat("dd_MM_yyyy_HH:mm:ss").format(new Date());
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        File picDir = new File(Environment.getExternalStorageDirectory() + "/ShareYourLocation");
                        if (!picDir.exists()) {
                            picDir.mkdir();
                        }
                        picFile = new File(picDir + "/" + "syl_" + formattedDate + ".png");
                        try {
                            picFile.createNewFile();
                            final File snapShotPic = picFile;

                            SnapshotReadyCallback callback = new SnapshotReadyCallback() {
                                Bitmap bitmap;

                                @Override
                                public void onSnapshotReady(Bitmap snapshot) {
                                    bitmap = snapshot;
                                    try {
                                        File picDir = new File(Environment.getExternalStorageDirectory() + "/ShareYourLocation");
                                        if (!picDir.exists()) {
                                            picDir.mkdir();
                                        }
                                        FileOutputStream out = new FileOutputStream(snapShotPic);
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            mMap.snapshot(callback);

                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(snapShotPic));
                            shareIntent.setType("image/png");
                            startActivity(Intent.createChooser(shareIntent, isNetworkAvailable() ? dialogeTitle : dialogeTitle + " [Offline Mode]"));

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Unable to share", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Unable to access storage", Toast.LENGTH_SHORT).show();
                    }

                } else if (address_btn.isChecked()) {

                    Intent shareIntent2 = new Intent();
                    shareIntent2.setAction(Intent.ACTION_SEND);
                    shareIntent2.putExtra(Intent.EXTRA_TEXT, address + " | Lat : " + currentLocation.getLatitude() + " | Lon : " + currentLocation.getLongitude());
                    shareIntent2.setType("text/plain");
                    startActivity(Intent.createChooser(shareIntent2,  isNetworkAvailable() ? dialogeTitle : dialogeTitle + " [Offline Mode]"));

                }
            }
        });
        alertGroup.show();


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(this.getClass().getSimpleName(), "Inside onMapReady");
//        if(null != dialog && dialog.isShowing()) dialog.hide();
        mMap = googleMap;
        GPSTracker gpsTracker = new GPSTracker(context);
        if (gpsTracker.canGetLocation) {
            currentLocation = gpsTracker.getLocation();
            address = gpsTracker.getAddressSet(null, null);
            LatLng cc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

//            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

            mMap.addMarker(new MarkerOptions().position(cc).title(address));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(cc)      // Sets the center of the map to location user
                    .zoom(16)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        } else {
            Log.d(this.getClass().getSimpleName(), "Cant get Location");
            Toast.makeText(context, "Unable to get Location", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
        }else{
            Toast.makeText(context, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        }

        this.doubleBackToExitPressedOnce = true;


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
