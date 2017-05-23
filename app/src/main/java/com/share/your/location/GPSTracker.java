package com.share.your.location;

/**
 * Created by Sourabh on 5/22/2017.
 */

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class GPSTracker extends Service implements LocationListener {

    private final Context context;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    boolean canGetAddress = false;

    Location location;

    double latitude;
    double longitude;
    private static int REQUEST_LOCATION = 2;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    String complete_address;
    boolean adress_set = false;
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.context = context;
        Log.d("GPS", "Calling getLocation");
        getLocation();
    }

    public Location getLocation() {

        try {
            // =================================================================================================
            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("GPS", "Permission Not granted");
                this.canGetLocation = false;
            } else {

                this.longitude = 0.0;
                this.latitude = 0.0;
                locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled) {
                    Log.d("GPS", "GPS Access Error");
                } else if (!isNetworkEnabled) {
                    Log.d("GPS", "INTERNET Access Error");
                } else {
                    this.canGetLocation = true;

                    if (isNetworkEnabled) {

                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if (location != null) {

                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }

                    }

                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                            if (locationManager != null) {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public String getAddressSet(Double lat, Double lon) {

        if(null == lat && null == lon){
            lat = this.location.getLatitude();
            lon = this.location.getLongitude();
        }

        String address_details = "", addressone = "", addresstwo = "", city = "", state = "", country = "", postalcode = "", knownname = "";


        try {
            Geocoder gcd = new Geocoder(context, Locale.ENGLISH);
            List<Address> addresses = gcd.getFromLocation(lat, lon, 5);

//            procedure one


            if (addresses.size() > 0) {
                addressone = (addresses.get(0).getAddressLine(0) == null ? "" : "" + addresses.get(0).getAddressLine(0));
                addresstwo = (addresses.get(0).getAddressLine(1) == null ? "" : "" + addresses.get(0).getAddressLine(1));
                city = (addresses.get(0).getLocality() == null ? "" : "" + addresses.get(0).getLocality());
                state = (addresses.get(0).getAdminArea() == null ? "" : "" + addresses.get(0).getAdminArea());
                country = (addresses.get(0).getCountryName() == null ? "" : "" + addresses.get(0).getCountryName());
                postalcode = (addresses.get(0).getPostalCode() == null ? "" : "" + addresses.get(0).getPostalCode());
                knownname = (addresses.get(0).getFeatureName() == null ? "" : "" + addresses.get(0).getFeatureName());

                address_details = addressone + addresstwo + city + state + country + postalcode + knownname;
                Log.d(this.getClass().getSimpleName(),"Address_details1 " + address_details);

                try {
                    ArrayList<String> addressFragments = new ArrayList<String>();
                    Address address = addresses.get(0);
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addressFragments.add(address.getAddressLine(i));
                    }
                    complete_address = TextUtils.join(System.getProperty("line.separator"),
                            addressFragments).replaceAll("\\s+", "");
                    Log.d(this.getClass().getSimpleName(),"Address_details2 " + complete_address);
                    if (canGetLocation) {

                        if (!TextUtils.isEmpty(complete_address) && complete_address.length() > 0)
                            adress_set = true;
                        if (!adress_set)
                            return complete_address;

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return "Parsing Error";
                }


            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return this.location.getLatitude() + " | " + this.location.getLongitude();

        }
        return complete_address;
//        return address_details;
    }


    public void stopUsingGPS() {
        if (locationManager != null) {
            //locationManager.removeUpdates(GPSTracker.this);
        }
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("GPS Permission");

        alertDialog.setMessage("GPS is not enabled to access High Accuracy. Do you want to go to settings menu?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location arg0) {
        if (arg0 != null) {
            this.latitude = arg0.getLatitude();
            this.longitude = arg0.getLongitude();
        }
    }

    @Override
    public void onProviderDisabled(String arg0) {

    }

    @Override
    public void onProviderEnabled(String arg0) {

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

