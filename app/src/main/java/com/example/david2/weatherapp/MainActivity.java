package com.example.david2.weatherapp;

// David Cui B00788648 Nov 2018

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

//this is the controller of the weather app
// it binds UI elements
// uses Volley to get weather data from API as JSONObject
// extracts data from JSONObject and sets the view
public class MainActivity extends AppCompatActivity {

    //constant strings used for API URL
    private final String URL_BASE = "http://api.openweathermap.org/data/2.5/weather?q=";
    private final String UNITS = "metric";
    private final String APPID = "e6e90aa53a240528ca840bacf6352221";

    //15-25 is defined as the comfortable temperature
    private final double TEMP_COLD = 15;
    private final double TEMP_HOT = 25;

    //UI elements
    private EditText etEnterCity;
    private TextView txtCityName;
    private TextView txtCountryName;
    private TextView txtTemperature;
    private TextView txtMinTemp;
    private TextView txtMaxTemp;
    private TextView txtWeather;
    private TextView txtWeatherDescrip;
    private TextView txtHumidity;
    private TextView txtClouds;

    //stores value of EditText
    private String cityName;

    //listener checks user input, hides keyboard, and calls getCurrentWeather method
    // which does the work of Json request, data parsing, and view setting
    private View.OnClickListener getWeatherListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cityName = etEnterCity.getText().toString();

            //hides keyboard
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

            //checks user input
            // no need to check if city name is valid, because API will do that
            if(TextUtils.isEmpty(cityName)) {
                Toast.makeText(getApplicationContext(),
                        "Please enter a city.",
                        Toast.LENGTH_SHORT).show();
            } else {
                getCurrentWeather();
            }
        }
    };

    //binds UI elements, and set click listener
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEnterCity = findViewById(R.id.etEnterCity);
        txtCountryName = findViewById(R.id.txtCountryName);
        txtCityName = findViewById(R.id.txtCityName);
        txtTemperature = findViewById(R.id.txtTemperature);
        txtMinTemp = findViewById(R.id.txtMinTemp);
        txtMaxTemp = findViewById(R.id.txtMaxTemp);
        txtWeather = findViewById(R.id.txtWeather);
        txtWeatherDescrip = findViewById(R.id.txtWeatherDescrip);
        txtHumidity = findViewById(R.id.txtHumidity);
        txtClouds = findViewById(R.id.txtClouds);

        Button btnGetWeather = findViewById(R.id.btnGetWeather);
        btnGetWeather.setOnClickListener(getWeatherListener);
    }

    //uses Volley to get JSONObject from the API, extract data from the JSONObject,
    // and sets view appropriately.
    // if API returns error, display error message to users, reset the views
    private void getCurrentWeather() {
        //URL for API
        String urlWithBase = URL_BASE + cityName + "&units=" + UNITS + "&APPID=" + APPID;

        //JsonObjectRequest is used to get data from API
        // on success, processes data and displays them on view
        // on error, informs users
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, urlWithBase, null,
                new Response.Listener<JSONObject>() {
                    //for success
                    @Override
                    public void onResponse(JSONObject response) {
                        //informs user
                        Toast.makeText(getApplicationContext(), "Success!",
                                Toast.LENGTH_SHORT).show();
                        //extract data and sets view
                        try {
                            display(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    //for error
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        //informs user
                        Toast.makeText(getApplicationContext(),
                                "Sorry, error getting the current weather.",
                                Toast.LENGTH_SHORT).show();
                        //reset views to empty, also shows error
                        displayError();
                    }
                }
        );

        //adds request to queue
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    //gets value from the JSONObject and set views
    private void display(JSONObject response) throws JSONException {
        //city name
        txtCityName.setText(response.getString("name"));
        txtCityName.setTextColor(getResources().getColor(R.color.colorBlack));
        //country name, firstly convert country code to name with Java.util.Locale
        String countryCode = response.getJSONObject("sys").getString("country");
        Locale loc = new Locale("", countryCode);
        String countryName = loc.getDisplayCountry();
        txtCountryName.setText(countryName);

        //temp, min temp, max temp
        // changes color of current temp depending on value
        // 15-25 is comfortable temperature
        JSONObject main = response.getJSONObject("main");
        Double temp = main.getDouble("temp");
        if(temp < TEMP_COLD) {
            txtTemperature.setTextColor(getResources().getColor(R.color.colorBlueCold));
        } else if(temp < TEMP_HOT) {
            txtTemperature.setTextColor(getResources().getColor(R.color.colorGreenComfy));
        } else {
            txtTemperature.setTextColor(getResources().getColor(R.color.colorRedHot));
        }
        String tempS = getString(R.string.txt_temp, main.getString("temp"));
        txtTemperature.setText(tempS);
        String minTemp = getString(R.string.txt_min_temp, main.getString("temp_min"));
        txtMinTemp.setText(minTemp);
        String maxTemp = getString(R.string.txt_max_temp, main.getString("temp_max"));
        txtMaxTemp.setText(maxTemp);

        //weather and description
        JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
        txtWeather.setText(weather.getString("main"));
        txtWeatherDescrip.setText(weather.getString("description"));

        //humidity and clouds
        String humidity = getString(R.string.txt_humidity, main.getString("humidity"));
        txtHumidity.setText(humidity);
        String clouds = getString(R.string.txt_clouds, response.getJSONObject("clouds").getString("all"));
        txtClouds.setText(clouds);
    }

    //resets views to empty, also shows error
    // either city not found or network error
    private void displayError() {
        txtCityName.setText(getResources().getString(R.string.error_message));
        txtCityName.setTextColor(getResources().getColor(R.color.colorRedError));
        txtCountryName.setText("");
        txtTemperature.setText("");
        txtMinTemp.setText("");
        txtMaxTemp.setText("");
        txtWeather.setText("");
        txtWeatherDescrip.setText("");
        txtHumidity.setText("");
        txtClouds.setText("");
    }
}
