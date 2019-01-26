package codegreed_devs.com.igasvendor.utils;

import codegreed_devs.com.igasvendor.BuildConfig;

public class Constants {
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    public static final long SPLASH_TIME_OUT = 3000;

    public static final String SHARED_PREF_NAME = PACKAGE_NAME + "SHARED_PREF";
    public static final String SHARED_PREF_NAME_IS_FIRST_LOGIN = "IsFirstLogin";
    public static final String SHARED_PREF_NAME_IS_ONLINE = "IsOnline";
    public static final String SHARED_PREF_NAME_BUSINESS_NAME = "BusinessName";
    public static final String SHARED_PREF_NAME_BUSINESS_EMAIL = "BusinessEmail";
    public static final String SHARED_PREF_NAME_BUSINESS_PHONE = "PhoneNumber";
    public static final String SHARED_PREF_NAME_BUSINESS_ADDRESS = "BusinessAddress";
    public static final String SHARED_PREF_NAME_BUSINESS_ID = "BusinessId";
    public static final String SHARED_PREF_NAME_LOC_LAT = "LocationLatitude";
    public static final String SHARED_PREF_NAME_LOC_LONG = "LocationLongitude";
    public static final String SHARED_PREF_NAME_FCM_TOKEN = "FCMToken";

    public static final String SHARED_PREF_NAME_COMPLETE_SIX_KG_PRICE = "CompleteSixKgPrice";
    public static final String SHARED_PREF_NAME_COMPLETE_THIRTEEN_KG_PRICE = "CompleteThirteenKgPrice";
    public static final String SHARED_PREF_NAME_SIX_KG_PRICE = "SixKgPrice";
    public static final String SHARED_PREF_NAME_THIRTEEN_KG_PRICE = "ThirteenKgPrice";

    public static final String CONFIRM_EMAIL_URL = "https://scriptests.000webhostapp.com/igas/vendor/send_email.php";
    public static final String FCM_LEGACY_URL = "https://fcm.googleapis.com/fcm/send";
    public static final int HTTP_RESPONSE_OK = 200;

    public static final int LOCATION_PERMISSIONS_REQUEST_CODE = 1;
    public static final int CALL_PHONE_PERMISSION_REQUEST_CODE = 2;

    public static final long A_MINUTE = 1000 * 60;

    public static final String FCM_LEGACY_API_TOKEN = "AIzaSyAaslzBTkkRgiPqNIriqewsvZhqZ64lL2Q";
    public static final String FCM_SENDER_ID = "981860619165";

}
