package ap.com.securesms.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import ap.com.securesms.Fragment.ActivationFragment;
import ap.com.securesms.Fragment.LoginFragment;
import ap.com.securesms.Fragment.SetPasswordFragment;
import ap.com.securesms.R;
import ap.com.securesms.Util.Constants;
import ap.com.securesms.Util.Settings;
import ap.com.securesms.Util.Utils;


public class LoginActivity extends AppCompatActivity {

    private Settings settings;
    private ActivationFragment.OnActivationListener activationListener;
    private LoginFragment.OnLoginListener loginListener;
    private SetPasswordFragment.OnSetKeyListener setKeyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        settings = new Settings(getApplicationContext());
        loginListener = new LoginFragment.OnLoginListener() {
            @Override
            public void success() {
                String number = null;
                try {
                    number = getIntent().getExtras().getString(Constants.KEY_NUMBER);
                } catch (Exception e) {
                }
                Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                if (number != null) {
                    i.putExtra(Constants.KEY_NUMBER, number);
                }
                startActivity(i);
                finish();
            }
        };
        setKeyListener = new SetPasswordFragment.OnSetKeyListener() {
            @Override
            public void success(int type) {
                if (Utils.isKeyExists()) {
                    replaceFragment(LoginFragment.newInstance().setLoginListener(loginListener));
                } else {
                    replaceFragment(SetPasswordFragment.newInstance(settings, SetPasswordFragment.TYPE_NEW_KEY).setKeyListener(setKeyListener));
                    Utils.toast(getApplicationContext(), "کلید یافت نشد.");
                }
            }
        };
        activationListener = new ActivationFragment.OnActivationListener() {
            @Override
            public void success(String activation) {
                Utils.toast(getApplicationContext(), "نرم افزار فعال شد.");
                settings.setActivation(activation);
                replaceFragment(SetPasswordFragment.newInstance(settings, SetPasswordFragment.TYPE_NEW_KEY).setKeyListener(setKeyListener));
            }

            @Override
            public void failed() {
                Utils.toast(getApplicationContext(), "کدفعال سازی اشتباه است");
                settings.setActivation("");
            }
        };

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_PHONE_STATE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        String sn, imei;
                        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            imei = telephonyManager.getDeviceId();
                            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                                sn = Build.SERIAL;
                            } else {
                                sn = Build.getSerial();
                            }
                            checkActive(imei, sn);
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Utils.toast(getApplicationContext(), "مجوز خواندن وضعیت دستگاه را ندارید");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void checkActive(String imei, String sn) {
        String currentActivation = null;
        try {
            currentActivation = Utils.gen(imei, sn);
        } catch (Exception e) {
        }
        if (settings.getActivation().isEmpty()) {
            replaceFragment(ActivationFragment.newInstance(currentActivation, imei, sn).setActivationListener(activationListener));
        } else {
            if (currentActivation != null && currentActivation.equals(settings.getActivation())) {
                if (Utils.isKeyExists()) {
                    replaceFragment(LoginFragment.newInstance().setLoginListener(loginListener));
                } else {
                    replaceFragment(SetPasswordFragment.newInstance(settings, SetPasswordFragment.TYPE_NEW_KEY).setKeyListener(setKeyListener));
                    Utils.toast(getApplicationContext(), "کلید یافت نشد.");
                }
            } else {
                Utils.toast(getApplicationContext(), "کدفعال سازی اشتباه است");
                settings.setActivation("");
                replaceFragment(ActivationFragment.newInstance(currentActivation, imei, sn).setActivationListener(activationListener));
            }
        }
    }

    public void replaceFragment(Fragment destFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, destFragment);
//        transaction.disallowAddToBackStack();
        transaction.commit();
    }
}
