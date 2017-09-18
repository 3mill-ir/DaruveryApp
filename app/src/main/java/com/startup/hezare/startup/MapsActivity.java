package com.startup.hezare.startup;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.startup.hezare.startup.UtilClasses.BottomNavigationViewHelper;
import com.startup.hezare.startup.UtilClasses.CustomTypefaceSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,AsyncResponse {

    private GoogleMap mMap;
    private String TAG = MapsActivity.class.getSimpleName();
    SupportMapFragment mapFragment;
    SendPostRequest sendPostRequest;
    SessionManagment sessionManagment;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sessionManagment.set_splash(false);
    }
    private static long back_pressed = 0L;
    @Override
    public void onBackPressed()
    {
        if (back_pressed + 2000 > System.currentTimeMillis()) super.onBackPressed();
        else Toast.makeText(getBaseContext(), "برای خروج از برنامه دوباره فشار دهید", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }

    Typeface BYekan;
    Typeface BHoma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sessionManagment=new SessionManagment(getApplicationContext());
        sendPostRequest=new SendPostRequest(getApplicationContext());
        sendPostRequest.delegate=MapsActivity.this;
        sendPostRequest.execute("http://delivery.3mill.ir/api/AndroidServices/AndroidListStations");

           //new GetLocation().execute();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //check for google play services
        if (!isGooglePlayServicesAvailable(this)) {
            Toast.makeText(getApplicationContext(), "تنظیمات GooglePlay Services را بررسی کنید", Toast.LENGTH_LONG).show();
        }
        BYekan = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/BYekan.ttf");
        BHoma = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/BHoma.ttf");

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        Menu m = bottomNavigationView.getMenu();
        for (int i=0;i<m.size();i++) {
            MenuItem mi = m.getItem(i);
            //the method we have create in activity
            CustomTypefaceSpan.applyFontToMenuItem(mi,getApplicationContext());
        }
        bottomNavigationView.setSelectedItemId(R.id.tab_map);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId())
                {
                    case R.id.tab_list:
                        intent = new Intent(getBaseContext(), ProfileActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                    case R.id.tab_home:
                        intent = new Intent(getBaseContext(), HomeActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                    case R.id.tab_share:
                        Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + ""));
                        intent1.putExtra("sms_body", "می توانید این برنامه را از بازار دریافت کنید \n لینک دریافت برنامه در کافه بازار:");
                        startActivity(intent1);
                        break;
                    case R.id.tab_about:
                        intent = new Intent(getBaseContext(), More2Activity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                }
                return false;
            }
        });


    }
    public ArrayList<HashMap<String, String>>  Parsing()
    {
        ArrayList<HashMap<String, String>> Locations=new ArrayList<>();

        try {
            String MSG=sendPostRequest.Result;
            //replacing backslash with null and removing first and end quotation marks
            MSG = MSG.replace("\\", "");
            MSG = MSG.replaceAll("^\"|\"$", "");

            JSONObject jsonObj = new JSONObject(MSG);

            // Getting JSON Array node
            JSONArray array = jsonObj.getJSONArray("Root");

            // looping through All locations
            for (int i = 0; i < array.length(); i++) {
                JSONObject c = array.getJSONObject(i);
                String Name = c.getString("Name");
                String Lat = c.getString("Lat");
                String Long = c.getString("Long");
                String Tell = c.getString("Tell");
                String Address=c.getString("Address");

                // tmp hash map for single location

                HashMap<String, String> locations = new HashMap<>();
                // adding each child node to HashMap key => value
                locations.put("name", Name);
                locations.put("lat", Lat);
                locations.put("long", Long);
                locations.put("tell", Tell);
                locations.put("address", Address);

                // adding contact to contact list
                Locations.add(locations);
            }
        } catch (final JSONException e) {
            Log.e(TAG, "Json parsing error: " + e.getMessage());
        }

        finally {

            return Locations;
        }

    }

    @Override
    public void processFinish() {
        mapFragment.getMapAsync(MapsActivity.this);
    }


    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }

        final Map<Marker, String> markersMap = new HashMap<>();

        //for storing addresses
        ArrayList<String> Addresses = new ArrayList<>();
        //LatLng store
        ArrayList<LatLng> locations = new ArrayList<LatLng>();
        for (int i = 0; i < Parsing().size(); i++) {
            Addresses.add(Parsing().get(i).get("address"));
            locations.add(new LatLng(Double.valueOf(Parsing().get(i).get("lat")), Double.valueOf(Parsing().get(i).get("long"))));
        }

        // Add a marker in Sydney and move the camera
        for (int i = 0; i < Parsing().size(); i++) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(locations.get(i))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.daruvery_marker))
                    .title(Parsing().get(i).get("name"))
                    .snippet(Parsing().get(i).get("tell")));
            //adding extra info
            markersMap.put(marker, Addresses.get(i));

        }


        LatLng urmia = new LatLng(37.54023, 45.069767);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("ورزشگاه آزادی"));
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(urmia, 13);
        mMap.moveCamera(update);


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {

                View v = getLayoutInflater().inflate(R.layout.infowindowlayout, null);

                //LatLng latLng = marker.getPosition();
                String title = marker.getTitle();
                String information = marker.getSnippet();
                String address=markersMap.get(marker);

                ImageView Icon = (ImageView) v.findViewById(R.id.center_icon);
                TextView Title = (TextView) v.findViewById(R.id.center_Title);
                TextView Address = (TextView) v.findViewById(R.id.center_Address);
                TextView Phone = (TextView) v.findViewById(R.id.center_Phone);

                Title.setText(title);
                Title.setTypeface(BHoma);
                Address.setText(address);
                Address.setTypeface(BYekan);
                Phone.setText(information);
                Phone.setTypeface(BYekan);

                //constant icon
                Icon.setImageResource(R.mipmap.mapicon);
                return v;
            }

            @Override
            public View getInfoContents(Marker marker) {

                View v = getLayoutInflater().inflate(R.layout.infowindowlayout, null);

                //LatLng latLng = marker.getPosition();
                String title = marker.getTitle();
                String information = marker.getSnippet();
                String address=markersMap.get(marker);

                ImageView Icon = (ImageView) v.findViewById(R.id.center_icon);
                TextView Title = (TextView) v.findViewById(R.id.center_Title);
                TextView Address = (TextView) v.findViewById(R.id.center_Address);
                TextView Phone = (TextView) v.findViewById(R.id.center_Phone);

                Title.setText(title);
                Title.setTypeface(BHoma);
                Address.setText(address);
                Address.setTypeface(BYekan);
                Phone.setText(information);
                Phone.setTypeface(BYekan);

                //constant icon
                Icon.setImageResource(R.mipmap.mapicon);
                return v;
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                marker.showInfoWindow();
                return true;
            }
        });

    }


}