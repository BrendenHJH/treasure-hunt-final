package org.mixare.data.convert;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.MixView;
import org.mixare.POIMarker;
import org.mixare.TreasureHuntItem;
import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.lib.HtmlUnescape;
import org.mixare.lib.marker.Marker;

import java.util.ArrayList;
import java.util.List;


public class TreasureDataProcessor extends DataHandler implements DataProcessor{

    public static final int MAX_JSON_OBJECTS = 1000;

    // Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    public String[] getUrlMatch() {
        String[] str = {"treasure"};
        return str;
    }

    @Override
    public String[] getDataMatch() {
        String[] str = {"treasure"};
        return str;
    }

    @Override
    public boolean matchesRequiredType(String type) {
        if(type.equals(DataSource.TYPE.TREASURE.name())){
            return true;
        }
        return false;
    }

    @Override
    public List<Marker> load(String rawData, int taskId, int colour) throws JSONException {
        List<Marker> markers = new ArrayList<Marker>();
        JSONObject root = convertToJSON(rawData);
        JSONArray dataArray = root.getJSONArray("geonames");
        int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

        for (int i = 0; i < top; i++) {
            JSONObject jo = dataArray.getJSONObject(i);

            Marker ma = null;
            if (jo.has("title") && jo.has("lat") && jo.has("lng")
                    && jo.has("elevation") && jo.has("wikipediaUrl")) {

                Log.v(MixView.TAG, "processing Wikipedia JSON object");

                //no unique ID is provided by the web service according to http://www.geonames.org/export/wikipedia-webservice.html
                ma = new POIMarker(
                        "",
                        HtmlUnescape.unescapeHTML(jo.getString("title"), 0),
                        jo.getDouble("lat"),
                        jo.getDouble("lng"),
                        jo.getDouble("elevation"),
                        "http://"+jo.getString("wikipediaUrl"),
                        taskId, colour);
                markers.add(ma);
            }
        }
        return markers;
    }

    //Hong
    private void initFirebaseDatabase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("item");
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TreasureHuntItem TreasureHuntItem = dataSnapshot.getValue(TreasureHuntItem.class);
                TreasureHuntItem.setFirebaseKey(dataSnapshot.getKey());

                Log.i("junho_dataSnapshot", dataSnapshot.toString());
                Log.i("junho!", TreasureHuntItem.getItemName());

                /*POIMarker poiMarker = new POIMarker("", TreasureHuntItem.getItemName(),TreasureHuntItem.getLatitude(),TreasureHuntItem.getLongitude(), 0,"",0,Color.YELLOW);
                List<Marker> markers = new ArrayList<Marker>();
                markers.add(poiMarker);
                dataView.getDataHandler().addMarkers(markers);*/
                //mAdapter.add(TreasureHuntItem);
                //mListView.smoothScrollToPosition(mAdapter.getCount());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                /*String firebaseKey = dataSnapshot.getKey();
                int count = mAdapter.getCount();
                for (int i = 0; i < count; i++) {
                    if (mAdapter.getItem(i).firebaseKey.equals(firebaseKey)) {
                        mAdapter.remove(mAdapter.getItem(i));
                        break;
                    }
                }*/
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    private JSONObject convertToJSON(String rawData){
        try {
            return new JSONObject(rawData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }



}
