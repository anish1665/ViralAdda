package com.ronak.viral.adda.providers.maps;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ronak.viral.adda.inherit.CollapseControllingFragment;
import com.ronak.viral.adda.MainActivity;
import com.ronak.viral.adda.inherit.PermissionsFragment;
import com.ronak.viral.adda.R;
import com.ronak.viral.adda.util.Helper;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;

import com.ronak.viral.adda.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MapsFragment extends Fragment implements PermissionsFragment, CollapseControllingFragment {

	private LinearLayout ll;
	private TextView text;

	private MapView mMapView;
	private GoogleMap googleMap;

	private LocationManager locationManager;
	private Location loc;

	private ProgressDialog locationDialog;

	Activity mAct;
	Double lat;
	Double lon;
	String query;
	String[] maps;
	String[] places;

	int mode;
	int PLACES = 2;
	int ARRAY = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		ll = (LinearLayout) inflater.inflate(R.layout.fragment_maps, container,
				false);

		setHasOptionsMenu(true);

		mMapView = (MapView) ll.findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);
		mMapView.onResume();

		return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();
		Helper.isOnlineShowDialog(mAct);

		String[] data = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA);
		if (data.length > 1) {
			maps = data;
			mode = ARRAY;
		} else {
			mode = PLACES;
			query = data[0];
		}

		MapsInitializer.initialize(mAct);

		mMapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				MapsFragment.this.googleMap = googleMap;
				if (mode == ARRAY) {

					lat = Double.parseDouble(maps[3]);
					lon = Double.parseDouble(maps[4]);

					LatLng loc = new LatLng(lat, lon);

					googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,
							Integer.parseInt(maps[5])));

					//Marker marker = googleMap.addMarker(new MarkerOptions().title(maps[1])
					//		.snippet(maps[2]).position(loc));
				//	marker.showInfoWindow();

					text = (TextView) ll.findViewById(R.id.textViewInfo);
					text.setText(Html.fromHtml(maps[0]));
				} else {
					text = (TextView) ll.findViewById(R.id.textViewInfo);
					text.setVisibility(View.GONE);
					currentLocation();

					googleMap.clear();
				}
			}
		});


	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}


	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (mode == ARRAY) {
			inflater.inflate(R.menu.maps_menu, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.navigate:
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
						Uri.parse("http://maps.google.com/maps?daddr=" + lat + "," + lon));
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public String[] requiredPermissions() {
		return new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
	}

	@Override
	public boolean supportsCollapse() {
		return false;
	}

	private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {

		private ProgressDialog dialog;
		private Context context;
		private String places;

		public GetPlaces(Context context, String places) {
			this.context = context;
			this.places = places;
		}

		@Override
		protected void onPostExecute(ArrayList<Place> result) {
			super.onPostExecute(result);
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			if (null == result || result.size() < 1) {
				Helper.noConnection(mAct);
			} else {
				//for (int i = 0; i < result.size(); i++) {
				//	googleMap.addMarker(new MarkerOptions()
		//					.title(result.get(i).getName())
		//					.position(
			//						new LatLng(result.get(i).getLatitude(),
			//								result.get(i).getLongitude()))
							// .icon(BitmapDescriptorFactory
							// .fromResource(R.drawable.pin))
					//		.snippet(result.get(i).getVicinity()));
				//}
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(result.get(0).getLatitude(), result
								.get(0).getLongitude())) // Sets the center of
						// the map to
						// Mountain View
						.zoom(14) // Sets the zoom
						.tilt(30) // Sets the tilt of the camera to 30 degrees
						.build(); // Creates a CameraPosition from the builder
				googleMap.animateCamera(CameraUpdateFactory
						.newCameraPosition(cameraPosition));
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(context);
			dialog.setCancelable(true);
			dialog.setMessage(getResources().getString(R.string.loading));
			dialog.isIndeterminate();
			dialog.show();
		}

		@Override
		protected ArrayList<Place> doInBackground(Void... arg0) {
			PlacesService service = new PlacesService(
					getResources().getString(R.string.google_server_key));

			ArrayList<Place> findPlaces = service.findPlaces(loc.getLatitude(), // 28.632808
					loc.getLongitude(), places); // 77.218276

			for (int i = 0; i < findPlaces.size(); i++) {

				Place placeDetail = findPlaces.get(i);
				Log.e("INFO", "places : " + placeDetail.getName());
			}
			return findPlaces;
		}

	}


	private void currentLocation() throws SecurityException {
		locationManager = (LocationManager) mAct.getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);

		String provider = locationManager
				.getBestProvider(criteria, false);

		Location location = locationManager.getLastKnownLocation(provider);

		if (location == null) {
			locationManager.requestLocationUpdates(provider, 0, 0, listener);

			locationDialog = new ProgressDialog(mAct);
			locationDialog.setCancelable(true);
			locationDialog.setTitle(getResources().getString(R.string.maps_location_title));
			locationDialog.setMessage(getResources().getString(R.string.maps_location_subtitle));
			locationDialog.isIndeterminate();
			locationDialog.show();
		} else {
			loc = location;
			new GetPlaces(mAct, query).execute();
			Log.e("INFO", "location : " + location);
		}

	}

	private LocationListener listener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onLocationChanged(Location location) {
			Log.e("INFO", "location update : " + location);
			loc = location;
			new GetPlaces(getActivity(), query).execute();

			if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				return;
			}
			locationManager.removeUpdates(listener);
			
			if (locationDialog.isShowing()) {
				locationDialog.dismiss();
			}
		}
	};
}