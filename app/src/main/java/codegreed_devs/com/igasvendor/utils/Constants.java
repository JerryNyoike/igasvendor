package codegreed_devs.com.igasvendor.utils;

import codegreed_devs.com.igasvendor.BuildConfig;

public class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    public static final long SPLASH_TIME_OUT = 3000;
    public static final String SHARED_PREF_NAME = PACKAGE_NAME + "SHARED_PREF";
    public static final String SHARED_PREF_NAME_IS_FIRST_LOGIN = "IsFirstLogin";
    public static final String SHARED_PREF_NAME_BUSINESS_NAME = "BusinessName";
    public static final String SHARED_PREF_NAME_EMAIL = "Email";

    public static final String SHARED_PREF_NAME_LOC_LAT = "LocationLatitude";
    public static final String SHARED_PREF_NAME_LOC_LONG = "LocationLongitude";
}
