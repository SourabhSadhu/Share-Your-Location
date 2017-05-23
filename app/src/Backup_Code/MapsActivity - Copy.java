package com.share.your.location;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,View.OnClickListener {

    private GoogleMap mMap;
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private boolean isGranted = false;
    private Context context;
    private RelativeLayout shareGrid;
    private ImageView shareButton;

    private Location currentLocation;
    private String address;
    private LatLng cc;

    private void executeUserPermissionTree() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
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
        if(!isGranted) executeUserPermissionTree();
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        init();
        setListners();
        mapFragment.getMapAsync(this);
    }

    public void init(){
        context = MapsActivity.this;
        shareGrid = (RelativeLayout) findViewById(R.id.shareGrid);
        shareButton = (ImageView) findViewById(R.id.shareButton);
    }
    public void setListners(){
        shareGrid.setOnClickListener(this);
        shareButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Log.d(this.getClass().getSimpleName(),"Inside onClick");
        switch(view.getId()){
            case R.id.shareButton:
                Toast.makeText(context,"ShareButton",Toast.LENGTH_SHORT).show();
                shareit();

//                SnapshotReadyCallback callback = new SnapshotReadyCallback() {
//                    Bitmap bitmap;
//
//                    @Override
//                    public void onSnapshotReady(Bitmap snapshot) {
//                        bitmap = snapshot;
//                        try {
//                            FileOutputStream out = new FileOutputStream("/mnt/sdcard/Download/TeleSensors.png");
//                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
//                mMap.snapshot(callback);

                break;

        }
    }


    public void shareit()
    {
        File picFile;
//        View view =  findViewById(R.id.map);//your layout id
//        view.getRootView();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            File picDir  = new File(Environment.getExternalStorageDirectory()+ "/ShareYourLocation");
            if (!picDir.exists())
            {
                picDir.mkdir();
            }
//            view.setDrawingCacheEnabled(true);
//            view.buildDrawingCache(true);
//            Bitmap bitmap = view.getDrawingCache();
//            Date date = new Date();
//            String fileName = "syl" + date + ".jpg";
            picFile = new File(picDir + "/" + "syl" + new Date() + ".jpeg");
            try
            {
                picFile.createNewFile();
//                FileOutputStream picOut = new FileOutputStream(picFile);
                final File snapShotPic = picFile;

                SnapshotReadyCallback callback = new SnapshotReadyCallback() {
                    Bitmap bitmap;

                    @Override
                    public void onSnapshotReady(Bitmap snapshot) {
                        bitmap = snapshot;
                        try {
                            File picDir  = new File(Environment.getExternalStorageDirectory()+ "/ShareYourLocation");
                            if (!picDir.exists())
                            {
                                picDir.mkdir();
                            }
                            FileOutputStream out = new FileOutputStream(snapShotPic);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

//                            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//                            sharingIntent.setType("image/jpeg");
//                            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(snapShotPic.getAbsolutePath()));
//                            startActivity(Intent.createChooser(sharingIntent, "Share via"));


                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(snapShotPic));
                            shareIntent.setType("image/jpeg");
                            startActivity(Intent.createChooser(shareIntent, "Share Location"));


//                            Intent shareIntent2 = new Intent();
//                            shareIntent2.setAction(Intent.ACTION_SEND);
//                            shareIntent2.putExtra(Intent.EXTRA_TEXT, address );
//                            shareIntent2.setType("text/plain");
//                            startActivity(Intent.createChooser(shareIntent2, "Share Location"));


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                mMap.snapshot(callback);


//                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), (int)(bitmap.getHeight()/1.2));
//                boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, picOut);
//                if (saved)
//                {
//                    Toast.makeText(context, "Image saved to your device Pictures "+ "directory!", Toast.LENGTH_SHORT).show();
//                } else
//                {
//                    //Error
//                }
//                picOut.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
//            view.destroyDrawingCache();

//            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//            sharingIntent.setType("image/jpeg");
//            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(picFile.getAbsolutePath()));
//            startActivity(Intent.createChooser(sharingIntent, "Share via"));

        } else {
            //Error

        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(this.getClass().getSimpleName(),"Inside onMapReady");
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        GPSTracker gpsTracker = new GPSTracker(context);
        if(gpsTracker.canGetLocation){
            currentLocation = gpsTracker.getLocation();
            address = gpsTracker.getAddressSet(null,null);
            cc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            mMap.addMarker(new MarkerOptions().position(cc).title(address));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(cc));
//            Log.d(this.getClass().getSimpleName(),"Address - " + address);

            /**
             * Code to Zoom into
             */
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cc, 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(cc)      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));



        }else
            Log.d(this.getClass().getSimpleName(),"Cant get Location");
    }


}
