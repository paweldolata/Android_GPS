package com.example.pawe.inzynierkatest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by Paweł on 2015-11-08.
 */
public class MyLocationListener extends PhoneStateListener implements LocationListener {

    public TextView textView;
    public static Marker gpsMarker;
    public GsmCellLocation lastGsmLocation;
    public TextView textViewPhoneState;
    public TextView textViewSignalStrength;
    public PhoneStateListener phoneStateListener;
    public GoogleMap mMap;
    public static String gsmStrength;
    public static String cid;
    public static String lac;
    public static String latitude;
    public static String longitude;
    public HashMap<Marker, MainActivity.MyPoint> markers;
    private Context context;
    static int currentCIDColorIndex = 0;
    public HashMap<String, Integer> colors = new HashMap<>();
    public TelephonyManager helpTelManager;
    int strength;


    static int numMarkersInColor[] =
            {
                    R.drawable.map_dot_black,
                    R.drawable.map_dot_blue,
                    R.drawable.map_dot_green,
                    R.drawable.map_dot_orange,
                    R.drawable.map_dot_red,
                    R.drawable.map_dot_yellow,
                    R.drawable.map_dot_grey,
                    R.drawable.map_dot_pink,
                    R.drawable.map_dot_brown,
                    R.drawable.map_dot_violet,
            };

    HashMap<Integer, Integer> colorsDrawable = new HashMap<>();

    ///////////////////////////////////////////////////////
    public MyLocationListener(TextView textView, TextView textViewPhoneState, TextView textViewSignalStrength, PhoneStateListener phoneStateListener, GsmCellLocation gsmLocation, GoogleMap mMap2, HashMap<Marker, MainActivity.MyPoint> markers2, Context con, TelephonyManager helpTelManager2) {
        this.textView = textView;
        this.textViewPhoneState = textViewPhoneState;
        this.phoneStateListener = phoneStateListener;
        this.lastGsmLocation = gsmLocation;
        this.textViewSignalStrength = textViewSignalStrength;
        this.mMap = mMap2;
        this.markers = markers2;
        this.context = con;
        this.helpTelManager = helpTelManager2;


        colorsDrawable.put(R.drawable.map_dot_black, 0x55000000);
        colorsDrawable.put(R.drawable.map_dot_blue, 0x5500eaff);
        colorsDrawable.put(R.drawable.map_dot_brown, 0x55692f00);
        colorsDrawable.put(R.drawable.map_dot_green, 0x5500ff19);
        colorsDrawable.put(R.drawable.map_dot_grey, 0x55818181);
        colorsDrawable.put(R.drawable.map_dot_orange, 0x55eea300);
        colorsDrawable.put(R.drawable.map_dot_pink, 0x55ff00f2);
        colorsDrawable.put(R.drawable.map_dot_red, 0x55ee6464);
        colorsDrawable.put(R.drawable.map_dot_yellow, 0x55fdf559);
        colorsDrawable.put(R.drawable.map_dot_violet, 0x556200ac);
    }

    /////////////////////////////////////////////////////////////
    //wywoływane gdy zmieni się lokalizacja telefonu
    @Override
    public void onCellLocationChanged(CellLocation location) {
        super.onCellLocationChanged(location);

        if (location != null) {
            switch (helpTelManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    lac = String.valueOf(((GsmCellLocation) location).getLac());
                    cid = String.valueOf(((GsmCellLocation) location).getCid());
                    textViewPhoneState.setText(String.format("LAC: %d CID: %d", (((GsmCellLocation) location).getLac()), (((GsmCellLocation) location).getCid())));
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    lac = String.valueOf(((GsmCellLocation) location).getLac());//UMTS %65536
                    cid = String.valueOf(((GsmCellLocation) location).getCid() % 0x10000);
                    textViewPhoneState.setText(String.format("LAC: %d CID: %d", (((GsmCellLocation) location).getLac()), (((GsmCellLocation) location).getCid() % 0x10000)));
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    lac = String.valueOf(((GsmCellLocation) location).getLac());
                    cid = String.valueOf(((GsmCellLocation) location).getCid());
                    textViewPhoneState.setText(String.format("LAC: %d CID: %d", (((GsmCellLocation) location).getLac()), (((GsmCellLocation) location).getCid())));
                    break;
            }
            if (!colors.containsKey(cid)) {
                currentCIDColorIndex = (currentCIDColorIndex + 1) % numMarkersInColor.length;
                colors.put(cid, currentCIDColorIndex);
            } else {
                currentCIDColorIndex = colors.get(cid);
            }
            lastGsmLocation = (GsmCellLocation) location;
        }
    }

    //funkcja zwraca location area code
    public static String getMyLac() {
        return lac;
    }

    /////////////////////////////////////////////////////////
//funkcja zwraca aktualny cell ID
    public static String getMyCid() {
        return cid;
    }
    /////////////////////////////////////////////////////////


    //funkcja uaktywniana przy zmianie mocy sygnału odbieranego
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        switch (helpTelManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_EDGE:
                textViewSignalStrength.setText(String.format("Gsm Signal Strength: %d ", signalStrength.getGsmSignalStrength() * 2 - 113));
                gsmStrength = String.valueOf(signalStrength.getGsmSignalStrength() * 2 - 113);
                strength = signalStrength.getGsmSignalStrength() * 2 - 113;
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                CellInfoWcdma cellinfoWcdma = (CellInfoWcdma) helpTelManager.getAllCellInfo().get(0);
                CellSignalStrengthWcdma cellSignalStrengthWcdma = cellinfoWcdma.getCellSignalStrength();

                textViewSignalStrength.setText(String.format("UMTS Signal Strength: %d ", cellSignalStrengthWcdma.getDbm()));
                gsmStrength = String.valueOf(cellSignalStrengthWcdma.getDbm());
                strength = cellSignalStrengthWcdma.getDbm();

                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                try {
                    Method[] methods = android.telephony.SignalStrength.class.getMethods();
                    for (Method method : methods) {
                        if (method.getName().equals("getLteRssi") || method.getName().equals("getLteSignalStrength") || method.getName().equals("getLteRsrp")) {
                            textViewSignalStrength.setText(String.format("LTE Signal Strength: %d ", method.invoke(signalStrength)));
                            gsmStrength = String.valueOf(method.invoke(signalStrength));
                            strength = (Integer) method.invoke(signalStrength);
                            break;
                        }
                    }
                } catch (SecurityException e) {

                    e.printStackTrace();
                } catch (IllegalArgumentException e) {

                    e.printStackTrace();
                } catch (IllegalAccessException e) {

                    e.printStackTrace();
                } catch (InvocationTargetException e) {

                    e.printStackTrace();
                }
                break;
        }
    }


    /////////////////////////////////////////////////
    public void setMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    //////////////////////////////////////////////////////////
//funkcja uaktywniana po zmianie lokalizacji
    @Override
    public void onLocationChanged(final Location location) {

        int pixel = this.context.getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
        Bitmap markerBitmap = Bitmap.createBitmap(pixel, pixel, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(markerBitmap);
        Drawable shape = this.context.getResources().getDrawable(numMarkersInColor[currentCIDColorIndex]);
        shape.setBounds(0, 0, markerBitmap.getWidth(), markerBitmap.getHeight());
        shape.draw(canvas);

        gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(markerBitmap)).title(String.format("Latitude/Longitude: " + location.getLatitude() + "/" + location.getLongitude() + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + gsmStrength)));
        int color = colorsDrawable.get(numMarkersInColor[currentCIDColorIndex]);
        checkpower(color,gpsMarker);


        longitude = String.valueOf(location.getLongitude());
        latitude = String.valueOf(location.getLatitude());
        MainActivity.MyPoint myPoint = new MainActivity.MyPoint(gpsMarker, cid, lac, strength);
        markers.put(gpsMarker, myPoint);
        this.textView.setText(String.format("Latitude: %f Longitude: %f", location.getLatitude(), location.getLongitude()));

    }

    //funkcja tworząca okrąg wokół punktu pomiarowego o odpowiednim promieniu
    public void checkpower(int color, Marker gpsMarker) {
        int power = strength;

        if (-121 < power && power < -110) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(10).strokeWidth(2).fillColor(color));
        } else if (-110 < power && power < -105) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(15).strokeWidth(2).fillColor(color));
        } else if (-105 < power && power < -100) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(20).strokeWidth(2).fillColor(color));
        } else if (-100 < power && power < -95) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(25).strokeWidth(2).fillColor(color));
        } else if (-95 < power && power < -90) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(30).strokeWidth(2).fillColor(color));
        } else if (-90 < power && power < -85) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(35).strokeWidth(2).fillColor(color));
        } else if (-85 < power && power < -80) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(40).strokeWidth(2).fillColor(color));
        } else if (-80 < power && power < -75) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(45).strokeWidth(2).fillColor(color));
        } else if (-75 < power && power < -70) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(50).strokeWidth(2).fillColor(color));
        } else if (-70 < power && power < -65) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(55).strokeWidth(2).fillColor(color));
        } else if (-65 < power && power < -60) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(60).strokeWidth(2).fillColor(color));
        } else if (-60 < power && power < -55) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(65).strokeWidth(2).fillColor(color));
        } else if (-55 < power && power < -49) {
            mMap.addCircle(new CircleOptions().center(gpsMarker.getPosition()).radius(70).strokeWidth(2).fillColor(color));
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    //////////////////////////////////////////
    @Override
    public void onProviderEnabled(String s) {

    }

    ///////////////////////////////////////////
    @Override
    public void onProviderDisabled(String s) {

    }


    ///////////////////////////////////////
//funkcje do losowania cell id z obszaru
    public static double randomLongitude(double longitudemin, double longitudemax) {
        return longitudemin + (Math.random() * ((longitudemax - longitudemin)));
    }

    public static double randomLatitude(double latitudemin, double latitudemax) {
        return latitudemin + (Math.random() * ((latitudemax - latitudemin)));
    }

    //funkcja generująca 9 obszarów o różnym Cell ID
    //////////////////////
  /*   public static HashMap<Marker, MainActivity.MyPoint> randomPoints(GoogleMap mMap, MyLocationListener myLocationListener, LocationManager locationManager) {
        Location location = null;
        try {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        } catch (SecurityException e) {
            Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
        }
        HashMap<Marker, MainActivity.MyPoint> hashmapa = new HashMap<>();
        int ilosc = 50;
       for (int i = 0; i < ilosc; i++) {
            double latitude = randomLatitude(16.7870, 16.7874);
            double longitude = randomLongitude(52.485, 52.486);
            int px = myLocationListener.context.getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
            Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mDotMarkerBitmap);
            Drawable shape = myLocationListener.context.getResources().getDrawable(numMarkersInColor[currentCIDColorIndex]);
            shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
            shape.draw(canvas);

            // gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + location.getLatitude() + "/" + location.getLongitude() + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + getMyStrength())));
            cid = String.valueOf(1);
            lac = String.valueOf(11);
            gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + latitude + "/" + longitude + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + gsmStrength)));


            MainActivity.MyPoint myPoint = new MainActivity.MyPoint(gpsMarker, cid, lac, strength);
            hashmapa.put(gpsMarker, myPoint);
            myLocationListener.textView.setText(String.format("Latitude: %f Longitude: %f", location.getLatitude(), location.getLongitude()));

        }
        for (int i = 0; i < ilosc; i++) {
            double latitude = randomLatitude(16.7875, 16.7879);
            double longitude = randomLongitude(52.485, 52.486);
            int px = myLocationListener.context.getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
            Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mDotMarkerBitmap);
            Drawable shape = myLocationListener.context.getResources().getDrawable(numMarkersInColor[currentCIDColorIndex + 1]);
            shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
            shape.draw(canvas);

            // gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + location.getLatitude() + "/" + location.getLongitude() + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + getMyStrength())));
            cid = String.valueOf(2);
            lac = String.valueOf(12);
            gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + latitude + "/" + longitude + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + gsmStrength)));


            MainActivity.MyPoint myPoint = new MainActivity.MyPoint(gpsMarker, cid, lac, strength);
            hashmapa.put(gpsMarker, myPoint);
            myLocationListener.textView.setText(String.format("Latitude: %f Longitude: %f", location.getLatitude(), location.getLongitude()));

        }
        for (int i = 0; i < ilosc; i++) {
            double latitude = randomLatitude(16.7870, 16.7879);
            double longitude = randomLongitude(52.4861, 52.4865);
            int px = myLocationListener.context.getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
            Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mDotMarkerBitmap);
            Drawable shape = myLocationListener.context.getResources().getDrawable(numMarkersInColor[currentCIDColorIndex + 2]);
            shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
            shape.draw(canvas);

            // gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + location.getLatitude() + "/" + location.getLongitude() + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + getMyStrength())));
            cid = String.valueOf(3);
            lac = String.valueOf(13);
            gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + latitude + "/" + longitude + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + gsmStrength)));


            MainActivity.MyPoint myPoint = new MainActivity.MyPoint(gpsMarker, cid, lac, strength);
            hashmapa.put(gpsMarker, myPoint);
            myLocationListener.textView.setText(String.format("Latitude: %f Longitude: %f", location.getLatitude(), location.getLongitude()));

        }

        for (int i = 0; i < ilosc; i++) {
            double latitude = randomLatitude(16.7870, 16.7874);
            double longitude = randomLongitude(52.4815, 52.4819);
            int px = myLocationListener.context.getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
            Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mDotMarkerBitmap);
            Drawable shape = myLocationListener.context.getResources().getDrawable(numMarkersInColor[currentCIDColorIndex + 4]);
            shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
            shape.draw(canvas);

            // gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + location.getLatitude() + "/" + location.getLongitude() + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + getMyStrength())));
            cid = String.valueOf(5);
            lac = String.valueOf(15);
            gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + latitude + "/" + longitude + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + gsmStrength)));


            MainActivity.MyPoint myPoint = new MainActivity.MyPoint(gpsMarker, cid, lac, strength);
            hashmapa.put(gpsMarker, myPoint);
            myLocationListener.textView.setText(String.format("Latitude: %f Longitude: %f", location.getLatitude(), location.getLongitude()));

        }
        for (int i = 0; i < ilosc; i++) {
            double latitude = randomLatitude(16.7865, 16.7869);
            double longitude = randomLongitude(52.4820, 52.4824);
            int px = myLocationListener.context.getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
            Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mDotMarkerBitmap);
            Drawable shape = myLocationListener.context.getResources().getDrawable(numMarkersInColor[currentCIDColorIndex + 3]);
            shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
            shape.draw(canvas);

            // gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + location.getLatitude() + "/" + location.getLongitude() + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + getMyStrength())));
            cid = String.valueOf(4);
            lac = String.valueOf(14);
            gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + latitude + "/" + longitude + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + gsmStrength)));


            MainActivity.MyPoint myPoint = new MainActivity.MyPoint(gpsMarker, cid, lac, strength);
            hashmapa.put(gpsMarker, myPoint);
            myLocationListener.textView.setText(String.format("Latitude: %f Longitude: %f", location.getLatitude(), location.getLongitude()));

        }

        for (int i = 0; i < ilosc; i++) {
            double latitude = randomLatitude(16.7865, 16.7869);
            double longitude = randomLongitude(52.4815, 52.4819);
            int px = myLocationListener.context.getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
            Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mDotMarkerBitmap);
            Drawable shape = myLocationListener.context.getResources().getDrawable(numMarkersInColor[currentCIDColorIndex + 5]);
            shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
            shape.draw(canvas);

            // gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + location.getLatitude() + "/" + location.getLongitude() + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + getMyStrength())));
            cid = String.valueOf(6);
            lac = String.valueOf(16);
            gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + latitude + "/" + longitude + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + gsmStrength)));


            MainActivity.MyPoint myPoint = new MainActivity.MyPoint(gpsMarker, cid, lac, strength);
            hashmapa.put(gpsMarker, myPoint);
            myLocationListener.textView.setText(String.format("Latitude: %f Longitude: %f", location.getLatitude(), location.getLongitude()));

        }
        for (int i = 0; i < ilosc; i++) {
            double latitude = randomLatitude(16.7875, 16.7879);
            double longitude = randomLongitude(52.4815, 52.4819);
            int px = myLocationListener.context.getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
            Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mDotMarkerBitmap);
            Drawable shape = myLocationListener.context.getResources().getDrawable(numMarkersInColor[currentCIDColorIndex + 6]);
            shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
            shape.draw(canvas);

            // gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + location.getLatitude() + "/" + location.getLongitude() + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + getMyStrength())));
            cid = String.valueOf(7);
            lac = String.valueOf(17);
            gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + latitude + "/" + longitude + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + gsmStrength)));


            MainActivity.MyPoint myPoint = new MainActivity.MyPoint(gpsMarker, cid, lac, strength);
            hashmapa.put(gpsMarker, myPoint);
            myLocationListener.textView.setText(String.format("Latitude: %f Longitude: %f", location.getLatitude(), location.getLongitude()));

        }
        for (int i = 0; i < ilosc; i++) {
            double latitude = randomLatitude(16.7875, 16.7879);
            double longitude = randomLongitude(52.4825, 52.4829);
            int px = myLocationListener.context.getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
            Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mDotMarkerBitmap);
            Drawable shape = myLocationListener.context.getResources().getDrawable(numMarkersInColor[currentCIDColorIndex + 7]);
            shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
            shape.draw(canvas);

            // gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + location.getLatitude() + "/" + location.getLongitude() + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + getMyStrength())));
            cid = String.valueOf(8);
            lac = String.valueOf(18);
            gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + latitude + "/" + longitude + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + gsmStrength)));


            MainActivity.MyPoint myPoint = new MainActivity.MyPoint(gpsMarker, cid, lac, strength);
            hashmapa.put(gpsMarker, myPoint);
            myLocationListener.textView.setText(String.format("Latitude: %f Longitude: %f", location.getLatitude(), location.getLongitude()));

        }
        for (int i = 0; i < ilosc; i++) {
            double latitude = randomLatitude(16.7865, 16.7869);
            double longitude = randomLongitude(52.4825, 52.4829);
            int px = myLocationListener.context.getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
            Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mDotMarkerBitmap);
            Drawable shape = myLocationListener.context.getResources().getDrawable(numMarkersInColor[currentCIDColorIndex+8]);
            shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
            shape.draw(canvas);

            // gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + location.getLatitude() + "/" + location.getLongitude() + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + getMyStrength())));
            cid = String.valueOf(9);
            lac = String.valueOf(19);
            gpsMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)).title(String.format("Latitude/Longitude: " + latitude + "/" + longitude + "\n" + "LAC/CID: " + getMyLac() + "/" + getMyCid() + "\n" + "Signal Strength: " + gsmStrength)));


            MainActivity.MyPoint myPoint = new MainActivity.MyPoint(gpsMarker, cid, lac, strength);
            hashmapa.put(gpsMarker, myPoint);
            myLocationListener.textView.setText(String.format("Latitude: %f Longitude: %f", location.getLatitude(), location.getLongitude()));

        }
        return hashmapa;
    }*/


}