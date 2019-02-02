package codegreed_devs.com.igasvendor.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Utils {

    public static void setPrefString(Context context, String key, String value){
        context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
    }

    public static void setPrefFloat(Context context, String key, float value){
        context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putFloat(key, value)
                .apply();
    }

    public static void setPrefBoolean(Context context, String key, boolean value) {
        context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    public static boolean isFirstLogin(Context context){
        return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(Constants.SHARED_PREF_NAME_IS_FIRST_LOGIN, true);
    }

    public static boolean businessPricesSet(Context context){
        return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(Constants.SHARED_PREF_NAME_BUSINESS_PRICES_SET, false);
    }

    public static String getPrefString(Context context, String key){
        return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getString(key, "");
    }

    public static float getPrefFloat(Context context, String key){
        return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getFloat(key, 0);
    }

    public static boolean getPrefBoolean(Context context, String key) {
        return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(key, false);
    }

    public static void clearSP(Context context) {
        context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    //gets an address from LatLng using Goecoder
    public static String getAddressFromLocation(Context context, double latitude, double longitude){
        String address = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            if (addresses != null && addresses.size() > 0)
            {
                if (addresses.get(0).getFeatureName() != null)
                {
                    address = addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality();
                }
                else
                {
                    address = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    public static void sendFCMNotification(String jsonData, Callback callback){

        OkHttpClient okHttpClient = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody =  RequestBody.create(JSON, jsonData);

        Request request = new Request.Builder()
                .url(Constants.FCM_LEGACY_URL)
                .addHeader("Content-Type","application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "key=" + Constants.FCM_LEGACY_API_TOKEN)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(callback);

    }

}
