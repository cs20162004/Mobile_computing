package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static android.hardware.SensorManager.AXIS_X;
import static android.hardware.SensorManager.AXIS_Z;
import static android.hardware.SensorManager.remapCoordinateSystem;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Camera camera;
    FrameLayout frameLayout;
    ShowCamera showCamera;

    TextView txt;
    TextView desc;
    ListView listview;
    TextView ulsan_logo;

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;

    float[] mGravity;
    float[] mGeomagnetic;
    float azimut;
    ArrayList<String> names = new ArrayList<String>(9);
    ArrayList<String> desc1 = new ArrayList<String>(9);
    ArrayList<String> desc2 = new ArrayList<String>(9);
    ArrayList<Double> lats = new ArrayList<Double>(9);
    ArrayList<Double> lons = new ArrayList<Double>(9);

    ArrayList<Location> locs = new ArrayList<Location>(9);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = (TextView) findViewById(R.id.text_id);
        //txt.setBackgroundColor(Color.WHITE);
        //desc = (TextView) findViewById(R.id.description_id);
        listview = findViewById(R.id.listveiw_id);
        //ulsan_logo = findViewById(R.id.ulsan_id);
        //ulsan_logo.setVisibility(View.VISIBLE);

        HashMap<String, String> list;
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        list = new HashMap<>();
        list.put("name", "City name");
        list.put("desc1", "Description1");
        list.put("desc2", "Description2");
        list.put("dist", "Location");
        arrayList.add(list);

        ListAdapter adapter = new SimpleAdapter(this, arrayList, R.layout.list_view, new String[]{"name", "desc1", "desc2", "dist"}, new int[]{R.id.name_id, R.id.desc1_id, R.id.desc2_id, R.id.dist_id});
        listview.setAdapter(adapter);


        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

        }

        frameLayout = (FrameLayout) findViewById(R.id.frame_layout);
        camera = Camera.open();
        showCamera = new ShowCamera(this, camera);
        frameLayout.addView(showCamera);

        //*************************Reading JSON file**************************

        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONObject m_jArry = obj.getJSONObject("seoul");
            String a = m_jArry.getString("desc1");
            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (obj.get(key) instanceof JSONObject) {
                    JSONObject innerJObject = obj.getJSONObject(key);
                    names.add(innerJObject.getString("name"));
                    desc1.add(innerJObject.getString("desc1"));
                    desc2.add(innerJObject.getString("desc2"));
                    lats.add(innerJObject.getDouble("lat"));
                    lons.add(innerJObject.getDouble("lon"));
                    Location target = new Location("");
                    target.setLongitude(innerJObject.getDouble("lon"));
                    target.setLatitude(innerJObject.getDouble("lat"));
                    locs.add(target);
                } else if (obj.get(key) instanceof String) {
                    String value = obj.getString("type");
                    Log.v("key = type", "value = " + value);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensorMagneticField = sensorManager.getDefaultSensor((Sensor.TYPE_MAGNETIC_FIELD));
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorMagneticField, sensorManager.SENSOR_DELAY_NORMAL);


    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions options = new MarkerOptions().position(latLng).title("I am here");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                            googleMap.addMarker(options);
                        }
                    });
                }
            }
        });
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("cityinfo.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    double baseAzimuth;
    String bearingText;

    @Override
    public void onSensorChanged(SensorEvent event) {
        //LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Location LocationObj = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // If we don't have a Location, we break out
        //if (LocationObj == null) return;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            float R1[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {

                // orientation contains azimut, pitch and roll
                float orientation[] = new float[3];
                //System.out.println("AAAAAA");
                //System.out.println(R[4]);
                remapCoordinateSystem(R, AXIS_X, AXIS_Z, R1);
                SensorManager.getOrientation(R1, orientation);

                azimut = orientation[0];
                baseAzimuth = (Math.toDegrees(azimut) + 360) % 360;
            }
        }
        if ((360 >= baseAzimuth && baseAzimuth >= 337.5) || (0 <= baseAzimuth && baseAzimuth <= 22.5))
            bearingText = "North";
        else if (baseAzimuth > 22.5 && baseAzimuth < 67.5) bearingText = "NorthEast";
        else if (baseAzimuth >= 67.5 && baseAzimuth <= 112.5) bearingText = "East";
        else if (baseAzimuth > 112.5 && baseAzimuth < 157.5) bearingText = "SouthEast";
        else if (baseAzimuth >= 157.5 && baseAzimuth <= 202.5) bearingText = "South";
        else if (baseAzimuth > 202.5 && baseAzimuth < 247.5) bearingText = "SouthWest";
        else if (baseAzimuth >= 247.5 && baseAzimuth <= 292.5) bearingText = "West";
        else if (baseAzimuth > 292.5 && baseAzimuth < 337.5) bearingText = "NorthWest";
        txt.setText(bearingText);
        System.out.println(baseAzimuth);


        //**********************bearing*************
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //float bearing = location.bearingTo(locs.get(4));
        //bearing = (bearing + 360) % 360;
        //System.out.println("This is bearing" + bearing);


        HashMap<String, String> list;
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        for (int i = 0; i < 9; i++){
            float bearing = location.bearingTo(locs.get(i));
            bearing = (bearing + 360) % 360;
            if (Math.abs(bearing - baseAzimuth) < 3){
                String tx = names.get(i);
                list = new HashMap<>();
                list.put("name", names.get(i));
                list.put("desc1", desc2.get(i));
                list.put("desc2", desc1.get(i));
                String s = Float.toString(location.distanceTo(locs.get(i)));
                s = s + "m (" + String.valueOf(lats.get(i));
                s = s + ", " + String.valueOf(lons.get(i)) + ")";
                list.put("dist", s);
                arrayList.add(list);

                ListAdapter adapter = new SimpleAdapter(this, arrayList, R.layout.list_view, new String[]{"name", "desc1", "desc2", "dist"}, new int[]{R.id.name_id, R.id.desc1_id, R.id.desc2_id, R.id.dist_id});
                listview.setAdapter(null);
                listview.setAdapter(adapter);

            }
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 44)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }
}