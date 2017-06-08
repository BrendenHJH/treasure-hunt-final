/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare;

import static android.view.KeyEvent.KEYCODE_CAMERA;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.gui.RadarPoints;
import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenLine;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.render.Camera;
import org.mixare.mgr.downloader.DownloadManager;
import org.mixare.mgr.downloader.DownloadRequest;
import org.mixare.mgr.downloader.DownloadResult;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * This class is able to update the markers and the radar. It also handles some
 * user events
 * 
 * @author daniele
 * 
 */
public class DataView {

	// Firebase junho 추가
	private FirebaseDatabase mFirebaseDatabase;
	private DatabaseReference mDatabaseReference;
	private ChildEventListener mChildEventListener;
    private DatabaseReference mLoginReference;
    private ChildEventListener mLoginEventListener;

    public DatabaseReference getmLoginReference() {
        return mLoginReference;
    }

    //junho 추가.
	private void initFirebaseDatabase() {
		mFirebaseDatabase = FirebaseDatabase.getInstance();
		mDatabaseReference = mFirebaseDatabase.getReference("item");
		mChildEventListener = new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				TreasureHuntItem TreasureHuntItem = dataSnapshot.getValue(TreasureHuntItem.class);
				TreasureHuntItem.setFirebaseKey(dataSnapshot.getKey());

				Log.i("junho_dataSnapshot", dataSnapshot.toString());
				Log.i("junho!", TreasureHuntItem.getFirebaseKey());

				POIMarker poiMarker = new POIMarker(TreasureHuntItem.getFirebaseKey(),
						TreasureHuntItem.getItemName(),
						TreasureHuntItem.getLatitude(),
						TreasureHuntItem.getLongitude(),
						0,
						"https://www.naver.com/",
						0,
						Color.WHITE);
				markers.add(poiMarker);
				Log.i("junho!!", poiMarker.getID());
				//dataView.getDataHandler().addMarkers(markers);
				//mAdapter.add(TreasureHuntItem);
				//mListView.smoothScrollToPosition(mAdapter.getCount());

			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s) {
			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot) {
				String firebaseKey = dataSnapshot.getKey();
				int count = markers.size();
				for (int i = 0; i < count; i++) {
					if (markers.get(i).getID().equals(firebaseKey)) {
						Log.i("junho_delete",markers.get(i).getID());
						markers.remove(i);
						break;
					}
				}
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
    private void initLoginFirebaseDatabase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mLoginReference = mFirebaseDatabase.getReference("onUser");
        mLoginEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				TreasureHuntUser treasureHuntUser = dataSnapshot.getValue(TreasureHuntUser.class);
				//treasureHuntUser.setFirebaseKey(dataSnapshot.getKey());

                Log.i("junho_user", dataSnapshot.toString());

                POIMarker poiMarker = new POIMarker(treasureHuntUser.getFirebaseKey(),
                        treasureHuntUser.getUserName(),
                        treasureHuntUser.getUserLatitude(),
                        treasureHuntUser.getUserLongitude(),
                        0,
                        "https://www.naver.com/",
                        0,
                        Color.WHITE);
                userMarkers.add(poiMarker);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("junho_login_changed","junho_login_changed");
                String firebaseKey = dataSnapshot.getKey();
                int count = userMarkers.size();
                for (int i = 0; i < count; i++) {
                    if (userMarkers.get(i).getID().equals(firebaseKey)) {
                        Log.i("junho_update",userMarkers.get(i).getID());
                        userMarkers.get(i).update(curFix);
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                /*String firebaseKey = dataSnapshot.getKey();
                int count = markers.size();
                for (int i = 0; i < count; i++) {
                    if (markers.get(i).getID().equals(firebaseKey)) {
                        Log.i("junho_delete",markers.get(i).getID());
                        markers.remove(i);
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
        mLoginReference.addChildEventListener(mLoginEventListener);
    }

	/** current context */
	private MixContext mixContext;
	/** is the view Inited? */
	private boolean isInit;

	/** width and height of the view */
	private int width, height;

	/**
	 * _NOT_ the android camera, the class that takes care of the transformation
	 */
	private Camera cam;

	private MixState state = new MixState();

	/** The view can be "frozen" for debug purposes */
	private boolean frozen;

	/** how many times to re-attempt download */
	private int retry;

	private Location curFix;
	private DataHandler dataHandler = new DataHandler();
	private float radius = 20;

    //junho 추가
    private DataHandler userDataHandler = new DataHandler();

	/** timer to refresh the browser */
	private Timer refresh = null;
	private final long refreshDelay = 45 * 1000; // refresh every 45 seconds

	private boolean isLauncherStarted;

	private ArrayList<UIEvent> uiEvents = new ArrayList<UIEvent>();

	private RadarPoints radarPoints = new RadarPoints();
    //junho 추가
    private RadarPoints userRadarPoints = new RadarPoints();

	private ScreenLine lrl = new ScreenLine();
	private ScreenLine rrl = new ScreenLine();
	private float rx = 10, ry = 20;
	private float addX = 0, addY = 0;
	
	private List<Marker> markers;
    //junho 추가.
    private List<Marker> userMarkers;

	/**
	 * Constructor
	 */
	public DataView(MixContext ctx) {
		this.mixContext = ctx;
	}

	public MixContext getContext() {
		return mixContext;
	}

	public boolean isLauncherStarted() {
		return isLauncherStarted;
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public DataHandler getDataHandler() {
		return dataHandler;
	}

    //junho 추가
    public DataHandler getUserDataHandler() {
        return userDataHandler;
    }

	public boolean isDetailsView() {
		return state.isDetailsView();
	}

	public void setDetailsView(boolean detailsView) {
		state.setDetailsView(detailsView);
	}

	public void doStart() {
		state.nextLStatus = MixState.NOT_STARTED;
		mixContext.getLocationFinder().setLocationAtLastDownload(curFix);
	}

	public boolean isInited() {
		return isInit;
	}

	public void init(int widthInit, int heightInit) {
		try {
			width = widthInit;
			height = heightInit;

			cam = new Camera(width, height, true);
			cam.setViewAngle(Camera.DEFAULT_VIEW_ANGLE);

			lrl.set(0, -RadarPoints.RADIUS);
			lrl.rotate(Camera.DEFAULT_VIEW_ANGLE / 2);
			lrl.add(rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS);
			rrl.set(0, -RadarPoints.RADIUS);
			rrl.rotate(-Camera.DEFAULT_VIEW_ANGLE / 2);
			rrl.add(rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		frozen = false;
		isInit = true;
	}

	public void requestData(String url) {
		DownloadRequest request = new DownloadRequest(new DataSource(
				"LAUNCHER", url, DataSource.TYPE.MIXARE,
				DataSource.DISPLAY.CIRCLE_MARKER, true));
		mixContext.getDataSourceManager().setAllDataSourcesforLauncher(
				request.getSource());
		mixContext.getDownloadManager().submitJob(request);
		state.nextLStatus = MixState.PROCESSING;
		}


//	public void requestData(DataSource datasource, double lat, double lon, double alt, float radius, String locale) {
//		DownloadRequest request = new DownloadRequest();
//		request.params = datasource.createRequestParams(lat, lon, alt, radius, locale);
//		request.source = datasource;
//		
//		mixContext.getDownloadManager().submitJob(request);
//		state.nextLStatus = MixState.PROCESSING;
//	}

	public void draw(PaintScreen dw) {
		mixContext.getRM(cam.transform);
		curFix = mixContext.getLocationFinder().getCurrentLocation();

        //junho 추가
        Log.d("junho_curFix", curFix.getLatitude() +  "," + curFix.getLongitude());

		state.calcPitchBearing(cam.transform);

		// Load Layer
		if (state.nextLStatus == MixState.NOT_STARTED && !frozen) {
			//junho 주석처리
			loadDrawLayer();

			Log.i("junho","junho0");

			markers = new ArrayList<Marker>();
			initFirebaseDatabase();

            userMarkers = new ArrayList<Marker>();
            initLoginFirebaseDatabase();
            FirebaseUser userInfo = LoginApplication.mAuth.getCurrentUser();
            TreasureHuntUser treasureHuntUser = new TreasureHuntUser(userInfo.getUid(), userInfo.getEmail(), curFix.getLatitude(), curFix.getLongitude());
            mLoginReference.child(userInfo.getUid()).setValue(treasureHuntUser);
		}
		else if (state.nextLStatus == MixState.PROCESSING) {
			DownloadManager dm = mixContext.getDownloadManager();
			DownloadResult dRes = null;

			//junho 주석
			//markers.addAll(downloadDrawResults(dm, dRes));

			Log.i("junho","junho1");

			if (dm.isDone()) {

				Log.i("junho","junho2");

				retry = 0;
				state.nextLStatus = MixState.DONE;

				dataHandler = new DataHandler();

				//junho
				Log.i("junho_array", markers.toString());

				dataHandler.addMarkers(markers);
				dataHandler.onLocationChanged(curFix);


                //junho  추가.
                userDataHandler = new DataHandler();
                userDataHandler.addMarkers(userMarkers);
                userDataHandler.onLocationChanged(curFix);

				//junho 주석 - refresh 필요없기 때문에
				/*if (refresh == null) { // start the refresh timer if it is null
					refresh = new Timer(false);
					Date date = new Date(System.currentTimeMillis()
							+ refreshDelay);
					refresh.schedule(new TimerTask() {

						@Override
						public void run() {
							callRefreshToast();
							refresh();
						}
					}, date, refreshDelay);
				}*/
			}
		}

		// Update markers
		dataHandler.updateActivationStatus(mixContext);
		for (int i = dataHandler.getMarkerCount() - 1; i >= 0; i--) {
			Marker ma = dataHandler.getMarker(i);
			// if (ma.isActive() && (ma.getDistance() / 1000f < radius || ma
			// instanceof NavigationMarker || ma instanceof SocialMarker)) {
			if (ma.isActive() && (ma.getDistance() / 1000f < radius)) {

				// To increase performance don't recalculate position vector
				// for every marker on every draw call, instead do this only
				// after onLocationChanged and after downloading new marker
				// if (!frozen)
				// ma.update(curFix);
				if (!frozen)
					ma.calcPaint(cam, addX, addY);
				ma.draw(dw);
			}
		}

		// Draw Radar
		drawRadar(dw);
        //junho 추가 User Radar
        drawUserRadar(dw);

		// Get next event
		UIEvent evt = null;
		synchronized (uiEvents) {
			if (uiEvents.size() > 0) {
				evt = uiEvents.get(0);
				uiEvents.remove(0);
			}
		}
		if (evt != null) {
			switch (evt.type) {
			case UIEvent.KEY:
				handleKeyEvent((KeyEvent) evt);
				break;
			case UIEvent.CLICK:
				handleClickEvent((ClickEvent) evt);
				break;
			}
		}
		state.nextLStatus = MixState.PROCESSING;
	}

	/**
	 * Part of draw function, loads the layer.
	 */
	private void loadDrawLayer(){
		if (mixContext.getStartUrl().length() > 0) {
			requestData(mixContext.getStartUrl());
			isLauncherStarted = true;
		}

		else {
			double lat = curFix.getLatitude(), lon = curFix.getLongitude(), alt = curFix
					.getAltitude();
			state.nextLStatus = MixState.PROCESSING;
			//junho 주석
			//mixContext.getDataSourceManager().requestDataFromAllActiveDataSource(lat, lon, alt,	radius);
		}

		// if no datasources are activated
		if (state.nextLStatus == MixState.NOT_STARTED)
			state.nextLStatus = MixState.DONE;
	}
	
	private List<Marker> downloadDrawResults(DownloadManager dm, DownloadResult dRes){
		List<Marker> markers = new ArrayList<Marker>();
		while ((dRes = dm.getNextResult()) != null) {
			if (dRes.isError() && retry < 3) {
				retry++;
				mixContext.getDownloadManager().submitJob(
						dRes.getErrorRequest());
				// Notification
				// Toast.makeText(mixContext, dRes.errorMsg,
				// Toast.LENGTH_SHORT).show();
			}
			
			if(!dRes.isError()) {
				if(dRes.getMarkers() != null){
					//jLayer = (DataHandler) dRes.obj;
					Log.i(MixView.TAG,"Adding Markers");
					markers.addAll(dRes.getMarkers());

					// Notification
					Toast.makeText(
							mixContext,
							mixContext.getResources().getString(
									R.string.download_received)
									+ " " + dRes.getDataSource().getName(),
							Toast.LENGTH_SHORT).show();
				}
			}
		}
		return markers;
	}
	

	/**
	 * Handles drawing radar and direction.
	 * @param PaintScreen screen that radar will be drawn to
	 */
	private void drawRadar(PaintScreen dw) {
		String dirTxt = "";
		int bearing = (int) state.getCurBearing();
		int range = (int) (state.getCurBearing() / (360f / 16f));
		// TODO: get strings from the values xml file
		if (range == 15 || range == 0)
			dirTxt = getContext().getString(R.string.N);
		else if (range == 1 || range == 2)
			dirTxt = getContext().getString(R.string.NE);
		else if (range == 3 || range == 4)
			dirTxt = getContext().getString(R.string.E);
		else if (range == 5 || range == 6)
			dirTxt = getContext().getString(R.string.SE);
		else if (range == 7 || range == 8)
			dirTxt = getContext().getString(R.string.S);
		else if (range == 9 || range == 10)
			dirTxt = getContext().getString(R.string.SW);
		else if (range == 11 || range == 12)
			dirTxt = getContext().getString(R.string.W);
		else if (range == 13 || range == 14)
			dirTxt = getContext().getString(R.string.NW);

		radarPoints.view = this;
        //junho 추가.
        radarPoints.setDataHandler(getDataHandler());
		dw.paintObj(radarPoints, rx, ry, -state.getCurBearing(), 1);
		dw.setFill(false);
		dw.setColor(Color.argb(150, 0, 0, 220));
		dw.paintLine(lrl.x, lrl.y, rx + RadarPoints.RADIUS, ry
				+ RadarPoints.RADIUS);
		dw.paintLine(rrl.x, rrl.y, rx + RadarPoints.RADIUS, ry
				+ RadarPoints.RADIUS);
		dw.setColor(Color.rgb(255, 255, 255));
		dw.setFontSize(12);

		//junho 수정 MixUtils.formatDist(radius * 1000) -> MixUtils.formatDist(radius * 25)
		radarText(dw, MixUtils.formatDist(radius * 25), rx
				+ RadarPoints.RADIUS, ry + RadarPoints.RADIUS * 2 - 10, false);
		radarText(dw, "" + bearing + ((char) 176) + " " + dirTxt, rx
				+ RadarPoints.RADIUS, ry - 5, true);
	}

    private void drawUserRadar(PaintScreen dw) {
        String dirTxt = "";
        int bearing = (int) state.getCurBearing();
        int range = (int) (state.getCurBearing() / (360f / 16f));
        // TODO: get strings from the values xml file
        if (range == 15 || range == 0)
            dirTxt = getContext().getString(R.string.N);
        else if (range == 1 || range == 2)
            dirTxt = getContext().getString(R.string.NE);
        else if (range == 3 || range == 4)
            dirTxt = getContext().getString(R.string.E);
        else if (range == 5 || range == 6)
            dirTxt = getContext().getString(R.string.SE);
        else if (range == 7 || range == 8)
            dirTxt = getContext().getString(R.string.S);
        else if (range == 9 || range == 10)
            dirTxt = getContext().getString(R.string.SW);
        else if (range == 11 || range == 12)
            dirTxt = getContext().getString(R.string.W);
        else if (range == 13 || range == 14)
            dirTxt = getContext().getString(R.string.NW);

        radarPoints.view = this;
        //junho 추가.
        radarPoints.setDataHandler(getUserDataHandler());
        dw.paintObj(radarPoints, rx, 20*ry, -state.getCurBearing(), 1);
        dw.setFill(false);
        dw.setColor(Color.argb(150, 0, 0, 220));
        dw.paintLine(lrl.x, lrl.y, rx + RadarPoints.RADIUS, ry
                + RadarPoints.RADIUS);
        dw.paintLine(rrl.x, rrl.y, rx + RadarPoints.RADIUS, ry
                + RadarPoints.RADIUS);
        dw.setColor(Color.rgb(255, 255, 255));
        dw.setFontSize(12);

        //junho 수정 MixUtils.formatDist(radius * 1000) -> MixUtils.formatDist(radius * 25)
        radarText(dw, MixUtils.formatDist(radius * 25), rx
                + RadarPoints.RADIUS, ry + RadarPoints.RADIUS * 2 - 10, false);
        radarText(dw, "" + bearing + ((char) 176) + " " + dirTxt, rx
                + RadarPoints.RADIUS, ry - 5, true);
    }

	private void handleKeyEvent(KeyEvent evt) {
		/** Adjust marker position with keypad */
		final float CONST = 10f;
		switch (evt.keyCode) {
		case KEYCODE_DPAD_LEFT:
			addX -= CONST;
			break;
		case KEYCODE_DPAD_RIGHT:
			addX += CONST;
			break;
		case KEYCODE_DPAD_DOWN:
			addY += CONST;
			break;
		case KEYCODE_DPAD_UP:
			addY -= CONST;
			break;
		case KEYCODE_DPAD_CENTER:
			frozen = !frozen;
			break;
		case KEYCODE_CAMERA:
			frozen = !frozen;
			break; // freeze the overlay with the camera button
		default: //if key is set, then ignore event
				break;
		}
	}

	boolean handleClickEvent(ClickEvent evt) {
		boolean evtHandled = false;

		Log.i("junho_UI_click", "handleClickEvent");
		Log.i("junho_UIEvents", uiEvents.toString());

		// Handle event
		if (state.nextLStatus == MixState.DONE) {
			// the following will traverse the markers in ascending order (by
			// distance) the first marker that
			// matches triggers the event.
			//TODO handle collection of markers. (what if user wants the one at the back)
			for (int i = 0; i < dataHandler.getMarkerCount() && !evtHandled; i++) {
				Marker pm = dataHandler.getMarker(i);

				//junho 주석
				evtHandled = pm.fClick(evt.x, evt.y, mixContext, state);

				if(evtHandled){
					Log.i("junho_FB_Data_삭제",pm.getID());
					mDatabaseReference.child(pm.getID()).removeValue();
				}

				/*//junho 추가 - 거리가 10m 이내일 때
                if(pm.getDistance() < 10f) {

                    evtHandled = pm.fClick(evt.x, evt.y, mixContext, state);

                    //db상에 없는 id를 삭제하는 일을 막기 위해 동기화?
                    if(evtHandled && markers.contains(pm)){
                        Log.i("junho_FB_Data_삭제",pm.getID());
                        mDatabaseReference.child(pm.getID()).removeValue();
                    }
                }*/

			}
		}
		return evtHandled;
	}

	private void radarText(PaintScreen dw, String txt, float x, float y, boolean bg) {
		float padw = 4, padh = 2;
		float w = dw.getTextWidth(txt) + padw * 2;
		float h = dw.getTextAsc() + dw.getTextDesc() + padh * 2;
		if (bg) {
			dw.setColor(Color.rgb(0, 0, 0));
			dw.setFill(true);
			dw.paintRect(x - w / 2, y - h / 2, w, h);
			dw.setColor(Color.rgb(255, 255, 255));
			dw.setFill(false);
			dw.paintRect(x - w / 2, y - h / 2, w, h);
		}
		dw.paintText(padw + x - w / 2, padh + dw.getTextAsc() + y - h / 2, txt,
				false);
	}

	public void clickEvent(float x, float y) {
		synchronized (uiEvents) {
			uiEvents.add(new ClickEvent(x, y));
			Log.i("junho_sync_list",uiEvents.toString());
		}
	}

	public void keyEvent(int keyCode) {
		synchronized (uiEvents) {
			uiEvents.add(new KeyEvent(keyCode));
		}
	}

	public void clearEvents() {
		synchronized (uiEvents) {
			uiEvents.clear();
		}
	}

	public void cancelRefreshTimer() {
		if (refresh != null) {
			refresh.cancel();
		}
	}
	
	/**
	 * Re-downloads the markers, and draw them on the map.
	 */
	public void refresh(){
		state.nextLStatus = MixState.NOT_STARTED;
	}
	
	private void callRefreshToast(){
		mixContext.getActualMixView().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(
						mixContext,
						mixContext.getResources()
								.getString(R.string.refreshing),
						Toast.LENGTH_SHORT).show();
			}
		});
	}

}

class UIEvent {
	public static final int CLICK = 0;
	public static final int KEY = 1;

	public int type;
}

class ClickEvent extends UIEvent {
	public float x, y;

	public ClickEvent(float x, float y) {
		this.type = CLICK;
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}

class KeyEvent extends UIEvent {
	public int keyCode;

	public KeyEvent(int keyCode) {
		this.type = KEY;
		this.keyCode = keyCode;
	}

	@Override
	public String toString() {
		return "(" + keyCode + ")";
	}
}
