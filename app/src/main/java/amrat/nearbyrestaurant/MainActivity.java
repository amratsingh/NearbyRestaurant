package amrat.nearbyrestaurant;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.TimeUnit;

import amrat.nearbyrestaurant.api.RetrofitAPI;
import amrat.nearbyrestaurant.api.model.RestaurantListResponse;
import amrat.nearbyrestaurant.api.model.Result;
import amrat.nearbyrestaurant.custom.RestaurantInfoWindow;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnCompleteListener<LocationSettingsResponse>, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private MapView mapView;
    private GoogleMap googleMap;

    private Retrofit retrofit;

    private FusedLocationProviderClient client;
    private LocationRequest mLocationRequest;

    private final int REQUEST_CHECK_SETTINGS = 100;
    private final int REQUEST_PERMISSION_LOCATION = 200;

    private long UPDATE_INTERVAL_IN_MILLISECONDS = 60 * 1000;

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {

                if (location != null) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17);
                    googleMap.animateCamera(cameraUpdate);
                }

                Log.d("Location", ">>" + location);
                Log.d("Location Lat", ">>" + location.getLatitude());
                Log.d("Location Long", ">>" + location.getLongitude());

                client.removeLocationUpdates(mLocationCallback);

                String currentLocation, radius, type, apiKey;

                currentLocation = location.getLatitude() + "," + location.getLongitude();
                radius = "500";
                type = "restaurant";
                apiKey = getString(R.string.google_api_key1);

                retrofit.create(RetrofitAPI.class)
                        .nearPlaceListAPI(currentLocation, radius, type, apiKey)
                        .enqueue(new Callback<RestaurantListResponse>() {
                            @Override
                            public void onResponse(Call<RestaurantListResponse> call, Response<RestaurantListResponse> response) {

                                RestaurantListResponse restaurantListResponse = response.body();

                                googleMap.clear();
                                googleMap.setInfoWindowAdapter(new RestaurantInfoWindow(MainActivity.this, restaurantListResponse));

                                for (int i = 0; i < restaurantListResponse.getResults().size(); i++) {

                                    Result result = restaurantListResponse.getResults().get(i);

                                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng()))
                                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_orange))
                                            .anchor(0.5f, 0.5f)
                                            .title(String.valueOf(i));

                                    googleMap.addMarker(markerOptions);
                                }
                            }

                            @Override
                            public void onFailure(Call<RestaurantListResponse> call, Throwable t) {

                            }
                        });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = builder.connectTimeout(RetrofitAPI.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(RetrofitAPI.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(RetrofitAPI.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .addInterceptor(logging)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(RetrofitAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();


        mapView = (MapView) findViewById(R.id.main_mapview);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(MainActivity.this);

        client = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setMaxWaitTime(UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);

        } else {

            checkAndRequestLocationUpdates();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);

                } else {

                    checkAndRequestLocationUpdates();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    public void checkAndRequestLocationUpdates() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(this);
    }

    @Override
    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

        try {

            LocationSettingsResponse response = task.getResult(ApiException.class);

            // All location settings are satisfied. The client can initialize location
            // requests here.
            client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        } catch (ApiException exception) {
            switch (exception.getStatusCode()) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the
                    // user a dialog.
                    try {
                        // Cast to a resolvable exception.
                        ResolvableApiException resolvable = (ResolvableApiException) exception;
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        resolvable.startResolutionForResult(
                                MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    } catch (ClassCastException e) {
                        // Ignore, should be an impossible error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHECK_SETTINGS) {

            if (resultCode == RESULT_OK) {

                client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

            } else {

                Toast.makeText(getApplicationContext(), "Enabled GPS to proceed", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        } else {

            Toast.makeText(this, "Permission required to proceed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        this.googleMap.setMyLocationEnabled(true);

        this.googleMap.setBuildingsEnabled(false);
        this.googleMap.setTrafficEnabled(false);

        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        this.googleMap.getUiSettings().setCompassEnabled(false);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.googleMap.getUiSettings().setTiltGesturesEnabled(false);
        this.googleMap.getUiSettings().setZoomControlsEnabled(false);
        this.googleMap.getUiSettings().setRotateGesturesEnabled(false);

        this.googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        marker.showInfoWindow();

        return false;
    }
}