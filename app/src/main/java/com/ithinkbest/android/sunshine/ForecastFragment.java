package com.ithinkbest.android.sunshine;

/**
 * Created by MARK on 2015/2/27.
 */

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    // 1.05 Create ArrayAdapter
    // Mark: 宣告要往上放
    ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {

            // 2.05_execute_fetchweathertask
            // Mark, 2015-2-27,
            // 以下兩行,是可以合併為一行
            // 免去了 weatherTask 這個 variable
//            FetchWeatherTask weatherTask = new FetchWeatherTask();
//            weatherTask.execute();

//            new FetchWeatherTask().execute();
//            weatherTask.execute("94043")
            new FetchWeatherTask().execute("94043");


            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Mark:
        // 這一個Fragment, http://developer.android.com/guide/components/fragments.html
        // 是 embedded 在 MainActivity 裡, 在27行裡, 整個入在activity_main 裡
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // 1.04 Add dummy data
        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        // Mark, 2015-2-27, Taichung:
        // dummy data 是常用的開發方法,先有個東西可以運行,看得到,再一個一個替換
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        // Mark, 2015-2-27, Taichung:
        // ArrayAdapter 要的是List, 通常是使用 ArrayList
        // List 是 interface, ArrayList 是 class
        // data 在這裡是一個 String Array, 在Java裡, Array 是 Object
        //      http://docs.oracle.com/javase/specs/jls/se7/html/jls-10.html
        // Array 可以很方便入到同泛型的 List
        //      http://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.1.2
        //      http://docs.oracle.com/javase/7/docs/api/java/util/Arrays.html#asList(T...)
        // 以下這個 weekForecast 只用一次,經過理解,可以採匿名方式,直接以Arrays.asList(data) 供 ArrayAdapter 使用
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

        // 1.05 Create ArrayAdapter
        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        weekForecast);
        // ArrayAdpater 有多種 constructor,
        // http://developer.android.com/reference/android/widget/ArrayAdapter.html
        // 這種是: (1)Context (2) Layout (3) TextView (4) List<String>


        // 1.06 attach_adapter
        // 這個 listView 的任務就是要 setAdapter, 一次性的任務
//            ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
//            listView.setAdapter(mForecastAdapter);
        // 是可以合併成一句
        ((ListView) rootView.findViewById(R.id.listview_forecast)).setAdapter(mForecastAdapter);


        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }
            Log.d("debug","params[0]="+params[0]);
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }
    }
}