package com.example.pawe.inzynierkatest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    GoogleMap mMap;
    MapFragment mapfragment;
    GpsStatus.Listener gpsStatusListener;


    //HashMap is an implementation of Map. All optional operations are supported. All elements are permitted as keys or values, including null.
    public HashMap<Marker, MyPoint> markers = new HashMap<>();
    TextView textView;
    TextView textViewPhoneState;
    TextView textViewSignalStrength;
    MyLocationListener locationListener;
    LocationManager locationManager;
    TelephonyManager telephonyManager;
    PhoneStateListener phoneStateListener;
    GsmCellLocation gsmCellLocation;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapfragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mapfragment.getMapAsync(this);

        textView = (TextView) findViewById(R.id.location);
        textViewPhoneState = (TextView) findViewById(R.id.phoneState);
        textViewSignalStrength = (TextView) findViewById(R.id.signalStrength);


        context = this.getApplicationContext();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        locationListener = new MyLocationListener(textView, textViewPhoneState, textViewSignalStrength, phoneStateListener, gsmCellLocation, mMap, markers, context, telephonyManager);
        telephonyManager.listen(locationListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        telephonyManager.listen(locationListener, PhoneStateListener.LISTEN_CELL_LOCATION);


//stworzenie gpsStatusListenera

        gpsStatusListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                Location location = null;
                switch (event) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        if (location != null) {
                            try {
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 30, locationListener);
                                telephonyManager.listen(locationListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

                            } catch (SecurityException e) {
                                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
                            }

                        } else {
                            try {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                pointToPosition(location);//przyblizenie do punktu
                            } catch (SecurityException e) {
                                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
                            }
                        }
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        try {

                            locationManager.removeUpdates(locationListener);
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 30, locationListener);
                            telephonyManager.listen(locationListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

                        } catch (SecurityException e) {
                            Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
                        }


                }

            }
        };


    }

    // Funkcja do przyblizania widoku
    public void zoomInClick(View v) {
        // Zoom in the map by 1
        if (mMap != null)
            mMap.moveCamera(CameraUpdateFactory.zoomIn());

    }

    //////////////
    //funkcja do przyblizania automatycznego do lokalizacji
    private void pointToPosition(Location location) {
        //Build camera position
        LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate myLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 14);
        mMap.animateCamera(myLocation);
    }

    ////////////////////////////////////////////////////////
    ///przycisk start
    public void start(View v) {
        onMapLoaded();
    }

    ///////////////////////////////////////////////////////
    //przycisk stop
    public void stop(View v) {

        try {
            locationManager.removeUpdates(locationListener);
            locationManager.removeGpsStatusListener(gpsStatusListener);
            telephonyManager.listen(locationListener, PhoneStateListener.LISTEN_NONE);
        } catch (SecurityException e) {
            Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
        }

    }

    /////////////////////////////////////////////////////////////////
    // ręczne oddalenie kamery
    public void zoomOutClick(View v) {
        if (mMap != null)
            mMap.moveCamera(CameraUpdateFactory.zoomOut());
    }

    ////////////////////////////////////////////////////
    @Override
    public void onMapReady(GoogleMap map) {
        locationListener.setMap(map);
        //odpowiada za "chmurke" nad punktem z jego danymi
        map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));

        mMap = map;
        //ustawienie mapy na terenową
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        // wywoluje OnMapLoaded jak mapa gotowa
        map.setOnMapLoadedCallback(this);

    }

    //////////////////////////////////////////////////////////////
    @Override
    public void onMapLoaded() {
        //   markers = MyLocationListener.randomPoints(mMap, locationListener, locationManager);


        Location loc = null;
        try {
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
        }


        if (loc != null && mMap != null) {
            try {
                // ustawia aktualizacje gps z minimalnym czasem i odlegloscia
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 30, locationListener);

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        // przybliza kamere do pozycji
                        CameraPosition cameraPos = mMap.getCameraPosition();
                        if (cameraPos.zoom < 14f)
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(14f));

                        return false;
                    }


                });
                telephonyManager.listen(locationListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                telephonyManager.listen(locationListener, PhoneStateListener.LISTEN_CELL_LOCATION);
                locationManager.addGpsStatusListener(gpsStatusListener);


            } catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }

        }
    }

    ////////////////////////////////////////////////////////////////////////
    @Override
    protected void onResume() {
        super.onResume();

        if (mMap != null) {
            try {
                //jak wczytana mapa to ustawia aktualizacje lokalizacji i dodaje obiekt nasłuchujący GPS
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 30, locationListener);
                locationManager.addGpsStatusListener(gpsStatusListener);
                telephonyManager.listen(locationListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                telephonyManager.listen(locationListener, PhoneStateListener.LISTEN_CELL_LOCATION);

            } catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }

        }

    }

    ///////////////////////////////////////////////////////////////
    @Override
    protected void onPause() {
        super.onPause();
//aplikacja pracuje w tle poki nie wcisniemy Stop
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 30, locationListener);
        } catch (SecurityException e) {
            Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
        }

    }

    /////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //////////////////////////////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /////////////////////////////////////////////////
    public GoogleMap getMap() {
        return mMap;
    }

    ////////////////////////////////////////////////
//klasa center odpowiadająca za środki odcinków
    public class Center {
        LatLng latlng;
        Boolean wasUsed = false;

        Center(LatLng latlnghelp) {
            this.latlng = latlnghelp;
        }

        public void wasUsed() {
            wasUsed = true;
        }
    }

    /////////////////////////////////
    //klasa do wyznaczenia pierwszej pary punktów
    public class Pair {
        MyPoint myPoint1;
        MyPoint myPoint2;

        Pair(MyPoint x1, MyPoint y1) {
            this.myPoint1 = x1;
            this.myPoint2 = y1;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Pair)) {
                return false;
            }
            Pair p = (Pair) obj;

            return (p.myPoint1.equals(myPoint1) && p.myPoint2.equals(myPoint2)) || (p.myPoint2.equals(myPoint1) && p.myPoint1.equals(myPoint2));
        }

    }

    //////////////////////////////////
    //klasa MyPoint reprezentujaca punkt pomiarowy składająca się z Markera, Stringów odpowiadających za LAC i CID oraz Integer jako moc,
    // a także flagę logiczną do oznaczania użycia punktu
    public static class MyPoint {
        public Marker marker;
        public String lac;
        public String cid;
        public int strength;
        public boolean used = false;


        public MyPoint(Marker marker2, String cid2, String lac2, int strength2) {
            this.marker = marker2;
            this.cid = cid2;
            this.lac = lac2;
            this.strength = strength2;
        }


        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MyPoint)) {
                return false;
            }
            MyPoint mp = (MyPoint) obj;

            return marker.equals(mp.marker) && lac.equals(mp.lac) && cid.equals(mp.cid);

        }

        public void wasUsed() {
            used = true;
        }

        public void clearUse() {
            used = false;
        }
    }

    /////////////////////////////
    //Button Rysuj granice
    public void drawCoverage(View v) {

        drawCid(markers);
    }


    //////////////////////////////////////////////
    //główna funkcja odpowiadająca za algorytm wyznaczania środków, a także rysowanie granic
    public void drawCid(HashMap<Marker, MyPoint> markers) {
        List<MyPoint> points = new ArrayList<>();
        for (MyPoint mp : markers.values()) {
            points.add(mp);
        }
        for (MyPoint mp : points) {
            mp.clearUse();
        }
        List<LatLng> halfDistancePoints = new ArrayList<>();
        Pair startPair = firstPair(points);
        MyPoint first = startPair.myPoint1;
        MyPoint second = startPair.myPoint2;
        first.wasUsed();
        second.wasUsed();
        int usedPoints = 2;
        LatLng center = getCenterCoordinatesFrom2Points(first, second);
        halfDistancePoints.add(center);
        //   Log.d("first", String.format("'%s','%s'", first.marker.getPosition().latitude, first.marker.getPosition().longitude));
        //   Log.d("second", String.format("'%s','%s'", second.marker.getPosition().latitude, second.marker.getPosition().longitude));
        LatLng goodCenter = center;


        while (true) {
            MyPoint helpPoint = getNearestToCenterMyPoint(goodCenter, points);
            if (helpPoint == null)
                break;

            helpPoint.wasUsed();
            usedPoints++;
            //    Log.d("help", String.format("'%s','%s'", helpPoint.marker.getPosition().latitude, helpPoint.marker.getPosition().longitude));

            MyPoint lastPoint = getNearest(helpPoint, points);
            //   Log.d("help czy uzyty", String.format("'%s'", helpPoint.used));
            //   Log.d("czwarty czy uzyty", String.format("'%s'", lastPoint.used));
            //   Log.d("kolejne dwójki punktow", String.format("'%s', '%s'", helpPoint.cid, lastPoint.cid));
            //   Log.d("lastPoint", String.format("'%s','%s'", lastPoint.marker.getPosition().latitude, lastPoint.marker.getPosition().longitude));

            if (!lastPoint.used) {
                MyPoint checkingPoint = getNearesthelpPoint(lastPoint, helpPoint, points);
                double angle2 = getAngle(lastPoint, helpPoint, checkingPoint);
                lastPoint.wasUsed();
                usedPoints++;
                if (angle2 > 20.0) {
                    center = getCenterCoordinatesFrom2Points(helpPoint, lastPoint);
                    halfDistancePoints.add(center);
                    goodCenter = center;
                } else {
                    if (!checkingPoint.used) {
                        checkingPoint.wasUsed();
                        usedPoints++;
                    }
                }
            } else {

                double angle = getAngleCenter(lastPoint, goodCenter, helpPoint);//ten przy ktorym liczymy,srodek,trzeci punkt
                //   Log.d("kat", String.format("'%s'", angle));
                center = getCenterCoordinatesFrom2Points(helpPoint, lastPoint);
                if (angle > 30.0) {
                    MyPoint checkingPoint = getNearesthelpPoint(lastPoint, helpPoint, points);
                    double angle2 = getAngle(helpPoint, lastPoint, checkingPoint);
                    //    Log.d("kat2", String.format("'%s'", angle2));
                    if (angle2 < 20.0) {
                        helpPoint.wasUsed();
                        usedPoints++;
                    } else {
                        halfDistancePoints.add(center);
                        goodCenter = center;
                    }

                } else {
                    helpPoint.wasUsed();
                    usedPoints++;
                }

            }


            //   Log.d("center", String.format("'%s','%s'", center.latitude, center.longitude));
            //   Log.d("rozmiar listy", String.format("'%s'", halfDistancePoints.size()));
            //   Log.d("rozmiar listy", String.format("'%s'", points.size()));
            //  Log.d("rozmiar listy", String.format("'%s'", usedPoints));

        }


        PolylineOptions poly = new PolylineOptions().color(Color.BLUE).width(6);

        List<Center> listCenter = new ArrayList<>();
        for (LatLng location : halfDistancePoints) {
            listCenter.add(new Center(location));
        }
        List<Center> fixedList = fixedCenterList(listCenter, points);
        Center start = minimalLatitude(listCenter);
        start.wasUsed();
        poly.add(start.latlng);
        Center actual;
        actual = nearestCenter(start, listCenter);
        double length = getLengthBetweenLatLng(start.latlng, actual.latlng);
        Log.d("length", String.format("'%s'", length));
        poly.add(actual.latlng);
        actual.wasUsed();
        Center help;
        while (true) {
            help = nearestCenter(actual, fixedList);
            if (help == null) {
                break;
            }

            double lengthhelpPoint = getLengthBetweenLatLng(actual.latlng, help.latlng);
            if (lengthhelpPoint > 1.5E-4) {
                Center nearestUsedFinish = findNearestCenter(actual, listCenter);
                poly.add(nearestUsedFinish.latlng);
                mMap.addPolyline(poly);
                actual = help;
                Center nearestUsed = findNearestCenter(actual, listCenter);
                poly = new PolylineOptions().color(Color.BLUE).width(6);
                poly.add(nearestUsed.latlng);
                poly.add(actual.latlng);

            } else {
                poly.add(help.latlng);
                help.wasUsed();
                actual = help;
            }
        }


        mMap.addPolyline(poly);
    }


    ///////////////////////////////////////////////////////////////////////////////
    //funkcja liczy wspolrzedne srodka
    public LatLng getCenterCoordinatesFrom2Points(MyPoint x, MyPoint y) {
        double x1 = x.marker.getPosition().latitude;
        double y1 = x.marker.getPosition().longitude;
        double x2 = y.marker.getPosition().latitude;
        double y2 = y.marker.getPosition().longitude;

        return new LatLng((x1 + x2) / 2, (y1 + y2) / 2);
    }

    /////////////////////////////////////////////////////////////////////////////////
    //funkcja zwracajaca pierwsza pare punktow, startowa o najmniejszej odleglosci miedzy punktami
    public Pair firstPair(List<MyPoint> list) {
        Pair best = null;
        double nearest = -1;
        for (MyPoint mp1 : list) {
            for (MyPoint mp2 : list) {
                if (!(mp1.cid.equals(mp2.cid))) {
                    double actual = getLengthBetweenPoints(mp1, mp2);
                    if (nearest == -1) {
                        nearest = actual;
                    } else {
                        if (actual < nearest) {
                            nearest = actual;
                            best = new Pair(mp1, mp2);
                        }
                    }
                }
            }
        }
        return best;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //funkcja wyznacza punkt do drugiej weryfikacji
    public MyPoint getNearesthelpPoint(MyPoint lastPoint, MyPoint previousPoint, List<MyPoint> list) {
        double nearest = -1;
        MyPoint best = null;
        for (MyPoint mp : list) {
            if (!(lastPoint.cid.equals(mp.cid))) {
                if (mp.marker.getPosition().latitude != previousPoint.marker.getPosition().latitude && mp.marker.getPosition().longitude != previousPoint.marker.getPosition().longitude) {
                    double length = getLengthBetweenPoints(mp, lastPoint);
                    if (length > 0) {
                        if (nearest == -1) {
                            nearest = length;
                            best = mp;
                        } else {
                            if (length < nearest) {
                                nearest = length;
                                best = mp;
                            }
                        }
                    }
                }
            }
        }
        return best;
    }

    ////////////////////////////////////////////////////////////////////////////////
    ///funkcja wyznacza punkt najbliższy trzeciemu o różnym od niego Cell ID, czyli punkt lastpoint
    public MyPoint getNearest(MyPoint help, List<MyPoint> list) {
        double nearest = -1;
        MyPoint best = null;
        for (MyPoint mp : list) {
            if (!(help.cid.equals(mp.cid))) {
                double actual = getLengthBetweenPoints(mp, help);
                if (actual > 0) {
                    if (nearest == -1) {
                        nearest = actual;
                        best = mp;
                    } else {
                        if (actual < nearest) {
                            nearest = actual;
                            best = mp;
                        }
                    }
                }
            }
        }
        return best;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ///funkcja wylicza odległość między punktami, ale w argumencie korzystamy z Pair
    public double getLengthBetweenLatLng(LatLng point1, LatLng point2) {
        double x1 = point1.latitude;
        double y1 = point1.longitude;
        double x2 = point2.latitude;
        double y2 = point2.longitude;

        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    //funkcja wylicza odległość pomiędzy dwoma punktami
    public double getLengthBetweenPoints(MyPoint x, MyPoint y) {
        double x1 = x.marker.getPosition().latitude;
        double y1 = x.marker.getPosition().longitude;
        double x2 = y.marker.getPosition().latitude;
        double y2 = y.marker.getPosition().longitude;

        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //funkcja wylicza odleglosc miedzy punktem a wspolrzednymi LatLng
    public double getLengthBetweenPointsandLatLng(MyPoint x, LatLng y) {
        double x1 = x.marker.getPosition().latitude;
        double y1 = x.marker.getPosition().longitude;
        double x2 = y.latitude;
        double y2 = y.longitude;

        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //funkcja wylicza najbliższy nieużywany punkt w stosunku do środka pomiędzy dwoma punktami
    public MyPoint getNearestToCenterMyPoint(LatLng y, List<MyPoint> list) {
        double x2 = y.latitude;
        double y2 = y.longitude;
        double nearest = -1;
        MyPoint best = null;
        for (MyPoint p : list) {
            if (!p.used) {
                double length = Math.sqrt((x2 - p.marker.getPosition().latitude) * (x2 - p.marker.getPosition().latitude) + (y2 - p.marker.getPosition().longitude) * (y2 - p.marker.getPosition().longitude));
                if (nearest == -1) {
                    nearest = length;
                    best = p;
                } else {
                    if (length < nearest) {
                        nearest = length;
                        best = p;
                    }
                }
            }
        }


        return best;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //zamiana z radianów na stopnie
    public static double radiansToDegrees(double radians) {
        return 180 * radians / Math.PI;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //wyliczanie kąta z twierdzenia cosinusów
    public double getAngle(MyPoint first, MyPoint second, MyPoint help) {
        double a, b, c, radians;
        a = getLengthBetweenPoints(help, second);
        //  Log.d("a", String.format("'%s'", a));
        b = getLengthBetweenPoints(first, help);
        //   Log.d("b", String.format("'%s'", b));
        c = getLengthBetweenPoints(first, second);
        //  Log.d("c", String.format("'%s'", c));
        // twierdzenie cosinusów
        radians = Math.acos((a * a + c * c - b * b) / (2 * a * c));
        return radiansToDegrees(radians);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //twierdzenie cosinusów wyliczanie kąta w drugiej weryfikacji
    public double getAngleCenter(MyPoint fourth, LatLng center, MyPoint help) {
        double a, b, c, radians;
        a = getLengthBetweenPoints(help, fourth);
        //  Log.d("a", String.format("'%s'", a));
        b = getLengthBetweenPointsandLatLng(help, center);
        //     Log.d("b", String.format("'%s'", b));
        c = getLengthBetweenPointsandLatLng(fourth, center);
        //     Log.d("c", String.format("'%s'", c));
        // twierdzenie cosinusów
        radians = Math.acos((a * a + c * c - b * b) / (2 * a * c));
        return radiansToDegrees(radians);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //funkcja wyznacza punkt o najmniejszej szerokości geograficznej z środków żeby zacząć algorytm łączenia
    public Center minimalLatitude(List<Center> halfDistancePoints) {
        double minimum = -1;
        Center best = null;
        for (Center center : halfDistancePoints) {
            double actualLatitude = center.latlng.longitude;
            if (minimum == -1) {
                minimum = actualLatitude;
                best = center;
            } else {
                if (actualLatitude < minimum) {
                    minimum = actualLatitude;
                    best = center;
                }
            }
        }
        return best;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Szukanie najbliższego center w stosunku do podanego center
    public Center nearestCenter(Center start, List<Center> halfDistancePoints) {
        Center best = null;
        double nearest = -1;
        for (Center center : halfDistancePoints) {
            if (!center.wasUsed) {
                double length = getLengthBetweenLatLng(start.latlng, center.latlng);
                if (nearest == -1) {
                    nearest = length;
                    best = center;
                } else {
                    if (length < nearest) {
                        nearest = length;
                        best = center;
                    }
                }
            }
        }
        return best;
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //funkcja szukająca najbliższego środka wykorzystywana przy rysowaniu granic
    public Center findNearestCenter(Center actual, List<Center> halfDistancePoints) {
        Center newCenter = null;
        double nearest1 = -1;
        for (Center center : halfDistancePoints) {
            if (center.wasUsed) {
                double length = getLengthBetweenLatLng(center.latlng, actual.latlng);
                if (nearest1 == -1) {
                    nearest1 = length;
                    newCenter = center;
                } else {
                    if (length < nearest1) {
                        nearest1 = length;
                        newCenter = center;
                    }
                }

            }
        }
        return newCenter;
    }


    ///////////////////////////////////////////////////////////////////////////////////
    //funkcja do szukania punktu najblizszego srodkowi ale roznego od 2 z argumentu
    public MyPoint nearestPoint(Center center, MyPoint first, MyPoint second, List<MyPoint> list) {
        double nearest = -1;
        MyPoint best = null;
        for (MyPoint mp : list) {
            if (!mp.equals(first) && !mp.equals(second)) {
                double actual = getLengthBetweenPointsandLatLng(mp, center.latlng);
                if (actual > 0) {
                    if (nearest == -1) {
                        nearest = actual;
                        best = mp;
                    } else {
                        if (actual < nearest) {
                            nearest = actual;
                            best = mp;
                        }
                    }
                }
            }
        }
        return best;
    }


    ///////////////////////////////////////////////////////////////////////////////////
    //funkcja zwracająca poprawioną listę środków
    public List<Center> fixedCenterList(List<Center> centers, List<MyPoint> points) {
        List<Center> fixedList = new ArrayList<>();
        MyPoint first;
        MyPoint second;
        MyPoint third;
        for (Center center : centers) {
            first = nearestPoint(center, null, null, points);
            second = nearestPoint(center, first, null, points);
            third = nearestPoint(center, first, second, points);
            if (first.cid.equals(second.cid) && first.cid.equals(third.cid) && second.cid.equals(third.cid)) {
                continue;
            } else {
                fixedList.add(center);
            }
        }
        return fixedList;
    }
}

