package ap.com.securesms.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;


/**
 * Created by Amirhosein on 11/25/2018.
 */

public class Settings {
    private SharedPreferences prefs = null;
    private final String ACTIVE = "active";
//    private final String PASSWORD = "pass";
    private final String FONT_SIZE = "fontsize";
    private final String SOUND = "sound";

    public Settings(Context context) {
        prefs = context.getSharedPreferences("SecureSMS", context.MODE_PRIVATE);
    }


    public boolean setActivation(String activation) {
        return prefs.edit().putString(ACTIVE, activation).commit();
    }

    public String getActivation() {
        return prefs.getString(ACTIVE, "").trim();
    }

//    public String getPassword() {
//        return prefs.getString(PASSWORD, "").trim();
//    }
//
//    public boolean setPassword(String pass) {
//        if (pass == null)
//            return false;
//        pass = pass.trim();
//        return prefs.edit().putString(PASSWORD, pass).commit();
//    }

    public Uri getSoundUri() {
        String uri = prefs.getString(SOUND, null);
        if (uri != null)
            return Uri.parse(uri);
        return null;
    }

    public int getFontSize() {
        return prefs.getInt(FONT_SIZE, 15);
    }

    public boolean setFontSize(int i) {
        return prefs.edit().putInt(FONT_SIZE, i).commit();
    }

    public boolean setSoundUri(Uri uri) {
        return prefs.edit().putString(SOUND, uri.toString()).commit();
    }
}