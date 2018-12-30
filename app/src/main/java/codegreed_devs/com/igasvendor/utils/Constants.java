package codegreed_devs.com.igasvendor.utils;

import codegreed_devs.com.igasvendor.BuildConfig;

public class Constants {
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    public static final long SPLASH_TIME_OUT = 3000;
    public static final String SHARED_PREF_NAME = PACKAGE_NAME + "SHARED_PREF";
    public static final String SHARED_PREF_NAME_IS_FIRST_LOGIN = "IsFirstLogin";
    public static final String SHARED_PREF_NAME_BUSINESS_NAME = "BusinessName";
    public static final String SHARED_PREF_NAME_EMAIL = "Email";

    public static final String SHARED_PREF_NAME_LOC_LAT = "LocationLatitude";
    public static final String SHARED_PREF_NAME_LOC_LONG = "LocationLongitude";
}
