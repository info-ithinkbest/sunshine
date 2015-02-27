package com.ithinkbest.android.sunshine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        // 1.05 Create ArrayAdapter
        // Mark: 宣告要往上放
        ArrayAdapter<String> mForecastAdapter;

        public PlaceholderFragment() {
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

            // 2.01 add_network_code
            // 取自於 https://gist.github.com/udacityandroid/d6a7bb21904046a91695
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

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
                Log.e("PlaceholderFragment", "Error ", e);
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
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            return rootView;
        }
    }
}
