package com.atilsamancioglu.myplace;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    static SQLiteDatabase myDatabase;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,locationListener);

                Intent intent = getIntent();
                String info = intent.getStringExtra("info");
                if (info.equalsIgnoreCase("new")) {
                    mMap.clear();
                    //center the camera around user location
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));

                } else {

                    mMap.clear();
                    Integer pos = intent.getIntExtra("position", 0);
                    LatLng oldLocation = new LatLng(MainActivity.locations.get(pos).latitude, MainActivity.locations.get(pos).longitude);
                    mMap.addMarker(new MarkerOptions().position(oldLocation).title(MainActivity.names.get(pos)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oldLocation, 10));

                }


            }
        }



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener((GoogleMap.OnMapLongClickListener) this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);

        } else {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            Intent intent = getIntent();
            String info = intent.getStringExtra("info");
            System.out.println(info);
            if (info.equalsIgnoreCase("new")) {
                mMap.clear();
                //center the camera around user location
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));

            } else {
                mMap.clear();
                Integer pos = intent.getIntExtra("position", 0);
               LatLng oldLocation = new LatLng(MainActivity.locations.get(pos).latitude, MainActivity.locations.get(pos).longitude);
                mMap.addMarker(new MarkerOptions().position(oldLocation).title(MainActivity.names.get(pos)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oldLocation, 10));

            }



        }



    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String name = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses != null & addresses.size() > 0 ) {

                if (addresses.get(0).getThoroughfare() != null) {
                    name += addresses.get(0).getThoroughfare();
                }
            } else {
                name = "New Place";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        mMap.addMarker(new MarkerOptions().position(latLng).title(name));
        MainActivity.names.add(name);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(),"Place Created", Toast.LENGTH_LONG);

        try {

            Double l1 = latLng.latitude;
            Double l2 = latLng.longitude;

            String coordl1 = l1.toString();
            String coordl2 = l2.toString();

            myDatabase = this.openOrCreateDatabase("Places", MODE_PRIVATE, null);
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS places (name VARCHAR, latitude VARCHAR, longitude VARCHAR)");

            String toCompile = "INSERT INTO places (name, latitude, longitude) VALUES(? , ?, ?)";

            SQLiteStatement sqLiteStatement = myDatabase.compileStatement(toCompile);

            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,coordl1);
            sqLiteStatement.bindString(3,coordl2);

            sqLiteStatement.execute();



        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
