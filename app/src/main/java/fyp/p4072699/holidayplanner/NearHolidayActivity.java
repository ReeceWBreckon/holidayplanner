package fyp.p4072699.holidayplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

public class NearHolidayActivity extends NavController implements AdapterView.OnItemClickListener, SeekBar.OnSeekBarChangeListener {
    private String location, radius, type, name, rating;
    private ListView nearbyLV;
    private ArrayList<String> nearbyList, placeID;
    private ArrayAdapter ad;
    private SeekBar distance;
    private TextView dis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_holiday);
        getDrawer();
        setTitle(getIntent().getStringExtra(getString(R.string.title)));
        connectDisplay();
        setupListView();
        setupURL();
        setListeners();
        getPlaces("");
    }

    //set the url paramaters
    protected void setupURL() {
        location = getIntent().getStringExtra(getString(R.string.coords));
        type = getIntent().getStringExtra(getString(R.string.type));
        radius = String.valueOf(distance.getProgress());
    }

    //Setup the list view and adapter, initialise the lists
    protected void setupListView() {
        nearbyList = new ArrayList<>();
        placeID = new ArrayList<>();
        ad = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nearbyList);
        nearbyLV.setAdapter(ad);
        dis.setText(String.valueOf(distance.getProgress()));
    }

    //Connect to the display
    protected void connectDisplay() {
        buttonReturn();
        nearbyLV = findViewById(R.id.listView_nearby);
        distance = findViewById(R.id.seekBar_distance);
        dis = findViewById(R.id.textview_distance);
    }

    //Set the click listeners
    protected void setListeners() {
        nearbyLV.setOnItemClickListener(this);
        //Setup the seek bar
        distance.setOnSeekBarChangeListener(this);
    }

    //Get a list of places, takes a string to determine next page or not.
    protected void getPlaces(String s) {
        String URL;
        if (s.equals("")) {
            URL = getString(R.string.google_near_base_url) + getString(R.string.location_equals) + location + getString(R.string.and_radius) + radius + getString(R.string.and_type) + type + getString(R.string.and_key) + getString(R.string.key);
            nearbyList.clear();
        } else {
            URL = s;
        }
        final String[] pageToken = new String[1];
        pageToken[0] = "";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                JSONObject obj, results;
                JSONArray res;
                try {
                    obj = new JSONObject(new String(responseBody));
                    if (obj.has(getString(R.string.next_page_token))) {
                        pageToken[0] = obj.getString(getString(R.string.next_page_token));
                    }
                    res = obj.getJSONArray(getString(R.string.results));
                    for (int count = 0; count < res.length(); count++) {
                        results = res.getJSONObject(count);
                        name = results.getString(getString(R.string.name_lower));
                        if (!results.has(getString(R.string.rating_lower))) {
                            rating = getString(R.string.no_ratings);
                        } else {
                            rating = results.getString(getString(R.string.rating_lower)) + getString(R.string.out_of_five);
                        }
                        nearbyList.add(getString(R.string.name_colon) + name + getString(R.string.new_line) + getString(R.string.rating_colon) + rating);
                        placeID.add(results.getString(getString(R.string.place_id)));
                        ad.notifyDataSetChanged();
                    }
                    if (!pageToken[0].equals("")) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getPlaces(getString(R.string.google_near_base_url) + getString(R.string.page_token_equals) + pageToken[0] + getString(R.string.and_key) + getString(R.string.key));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }
        });
    }

    //Direct the user back to near holiday choice
    @Override
    public void onClick(View view) {
        startActivity(new Intent(NearHolidayActivity.this, NearHolidayChoiceActivity.class).putExtra("coords", location));
    }

    //When clicked send the required details forward
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        startActivity(new Intent(NearHolidayActivity.this, NearbyDetailsActivity.class)
                .putExtra(getString(R.string.id), placeID.get(i))
                .putExtra(getString(R.string.loc), location)
                .putExtra(getString(R.string.type), type));
    }

    //When the progress bar is changed, update the values
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        radius = String.valueOf(i);
        String distan = String.valueOf(distance.getProgress()) + "Metres";
        dis.setText(distan);
        getPlaces("");
    }

    //Change the text as the progress bar is moved
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        dis.setText(String.valueOf(distance.getProgress()));
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
