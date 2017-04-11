package com.example.weather.weatherexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ftoslab.openweatherretrieverz.CurrentWeatherInfo;
import com.ftoslab.openweatherretrieverz.DailyForecastCallback;
import com.ftoslab.openweatherretrieverz.DailyForecastInfo;
import com.ftoslab.openweatherretrieverz.OpenWeatherRetrieverZ;
import com.ftoslab.openweatherretrieverz.WeatherCallback;
import com.ftoslab.openweatherretrieverz.WeatherUnitConverter;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final String API_KEY = "ea6ab1ca55169a961c105dd860a3d79f"; // Your API Key here
    private TextView cityNameTextView;
    private TextView day1TemperatureTextView;
    private TextView day2DateTextView;
    private TextView day2HighTextView;
    private TextView day2LowTextView;
    private TextView degreeCIndicatorTextView;
    private TextView degreeFIndicatorTextView;
    private ImageView day1WeatherIconImageView;
    private ImageView day2WeatherIconImageView;
    private boolean inDegreeC;

    CurrentWeatherInfo currentWeatherInfoC;
    CurrentWeatherInfo currentWeatherInfoF;
    List<DailyForecastInfo> dailyForecastInfoListC;
    List<DailyForecastInfo> dailyForecastInfoListF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cityNameTextView = (TextView)findViewById(R.id.cityNameTextView);
        day1TemperatureTextView = (TextView)findViewById(R.id.day1TemperatureTextView);
        day2DateTextView = (TextView)findViewById(R.id.day2DateTextView);
        day2HighTextView = (TextView)findViewById(R.id.day2HighTextView);
        day2LowTextView = (TextView)findViewById(R.id.day2LowTextView);
        degreeCIndicatorTextView = (TextView)findViewById(R.id.degreeCIndicatorTextView);
        degreeFIndicatorTextView = (TextView)findViewById(R.id.degreeFIndicatorTextView);
        day1WeatherIconImageView = (ImageView)findViewById(R.id.day1WeatherIconImageView);
        day2WeatherIconImageView = (ImageView)findViewById(R.id.day2WeatherIconImageView);
        OpenWeatherRetrieverZ retriever = new OpenWeatherRetrieverZ(API_KEY);
        retriever.updateCurrentWeatherInfo("6167865", new WeatherCallback() {
            @Override
            public void onReceiveWeatherInfo(CurrentWeatherInfo currentWeatherInfo) {
                // Switch the weather unit to Metric
                currentWeatherInfoC = WeatherUnitConverter.convertToMetric(currentWeatherInfo);
                currentWeatherInfoF = WeatherUnitConverter.convertToImperial(currentWeatherInfo);
            }
            @Override
            public void onFailure(String error) {
                Log.e("error", error);
            }
        });
        retriever.updateDailyForecastInfo("6167865", TimeZone.getDefault(), new DailyForecastCallback() {
            @Override
            public void onReceiveDailyForecastInfoList(List<DailyForecastInfo> dailyForecastInfoList) {
                // Switch the weather unit to Metric
                dailyForecastInfoListC = WeatherUnitConverter.convertToMetric(dailyForecastInfoList);
                dailyForecastInfoListF = WeatherUnitConverter.convertToImperial(dailyForecastInfoList);

            }
            @Override
            public void onFailure(String error) {
                Log.e("error", error);
            }
        });

        // Set up the Unit Indicator
        inDegreeC = true;
        degreeCIndicatorTextView.setTypeface(null, Typeface.BOLD);
        degreeFIndicatorTextView.setAlpha(Float.parseFloat("0.7"));
        degreeCIndicatorTextView.setClickable(true);
        degreeFIndicatorTextView.setClickable(true);
    }

    private void updateCurrentWeatherView(CurrentWeatherInfo currentWeatherInfo){
        cityNameTextView.setText(currentWeatherInfo.getCityName());
        day1TemperatureTextView.setText(currentWeatherInfo.getCurrentTemperature());
        new DownloadImageTask(day1WeatherIconImageView).execute(currentWeatherInfo.getWeatherIconLink());
    }

    private void updateDay2ForecastView(List<DailyForecastInfo> dailyForecastInfoList){
        // DailyForecastInfoList start from today, so to get day 2, we get index 1
        DailyForecastInfo day2Forecast = dailyForecastInfoList.get(1);
        day2DateTextView.setText(day2Forecast.getDateCalendar().get(Calendar.MONTH) + "." + day2Forecast.getDateCalendar().get(Calendar.DAY_OF_MONTH));
        day2HighTextView.setText(String.format(getString(R.string.degree), day2Forecast.getDailyMaxTemperature()));
        day2LowTextView.setText(String.format(getString(R.string.degree), day2Forecast.getDailyMinTemperature()));
        if (day2Forecast.getWeatherIconIdLinkList().get(0) != null && !day2Forecast.getWeatherIconIdLinkList().get(0).equals("")){
            new DownloadImageTask(day2WeatherIconImageView).execute(day2Forecast.getWeatherIconIdLinkList().get(0));
        }
    }

    @Override
    public void onClick(View v) {
        if (v == degreeCIndicatorTextView){
            if (!inDegreeC){
                // Change unit to C
                updateCurrentWeatherView(currentWeatherInfoC);
                updateDay2ForecastView(dailyForecastInfoListC);
                inDegreeC = true;
                degreeCIndicatorTextView.setTypeface(null, Typeface.BOLD);
                degreeFIndicatorTextView.setTypeface(null, Typeface.NORMAL);
                degreeCIndicatorTextView.setAlpha(Float.parseFloat("1.0"));
                degreeFIndicatorTextView.setAlpha(Float.parseFloat("0.7"));

            }
        }else if (v == degreeFIndicatorTextView){
            if (inDegreeC){
                // Change unit to F
                updateCurrentWeatherView(currentWeatherInfoF);
                updateDay2ForecastView(dailyForecastInfoListF);
                inDegreeC = false;
                degreeFIndicatorTextView.setTypeface(null, Typeface.BOLD);
                degreeCIndicatorTextView.setTypeface(null, Typeface.NORMAL);
                degreeFIndicatorTextView.setAlpha(Float.parseFloat("1.0"));
                degreeCIndicatorTextView.setAlpha(Float.parseFloat("0.7"));
            }
        }
    }


    // ASYNC TASK
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
