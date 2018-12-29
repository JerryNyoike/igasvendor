package codegreed_devs.com.igasvendor.utils;

import android.content.Context;

public class Utils {

    public static void setPrefString(Context context, String key, String value){
        context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
    }

    public static boolean isFirstLogin(Context context){
        return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(Constants.SHARED_PREF_NAME_IS_FIRST_LOGIN, true);
    }

    public static String getPrefString(Context context, String key){
        return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getString(key, "");
    }

    public static void clearSP(Context context) {
        context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
