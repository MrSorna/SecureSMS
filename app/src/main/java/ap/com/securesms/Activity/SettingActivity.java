package ap.com.securesms.Activity;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import ap.com.securesms.Adaptor.SettingAdapter;
import ap.com.securesms.Dialog.DialogChangeFont;
import ap.com.securesms.Fragment.LoginFragment;
import ap.com.securesms.Fragment.SetPasswordFragment;
import ap.com.securesms.R;
import ap.com.securesms.Util.Settings;
import ap.com.securesms.Util.Utils;


public class SettingActivity extends AppCompatActivity implements SettingAdapter.ItemClickListener {
    public SettingAdapter adapter;
    private Settings settings;
    private SetPasswordFragment.OnSetKeyListener setKeyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ArrayList<String> setting = new ArrayList<>();
        settings = new Settings(getApplicationContext());
        setting.add("تغییر رمزعبور");
        setting.add("تغییر صدای اعلان");
        setting.add("تغییر اندازه قلم");
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SettingAdapter(getApplicationContext(), setting);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        setKeyListener = new SetPasswordFragment.OnSetKeyListener() {
            @Override
            public void success(int type) {
                if (Utils.isKeyExists()) {
                    startActivity(new Intent(SettingActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    ActivityCompat.finishAffinity(SettingActivity.this);
                } else {
                    replaceFragment(SetPasswordFragment.newInstance(settings, SetPasswordFragment.TYPE_NEW_KEY).setKeyListener(setKeyListener));
                    Utils.toast(getApplicationContext(), "کلید یافت نشد.");
                }
            }
        };
    }


    @Override
    public void onItemClick(View view, int position) {
        switch (position) {
            case 0:
                replaceFragment(SetPasswordFragment.newInstance(settings, SetPasswordFragment.TYPE_CHANGE_KEY).setKeyListener(setKeyListener));
//                startActivity(new Intent(SettingActivity.this, SetPassActivity.class));
                break;
            case 1:
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                this.startActivityForResult(intent, 5);
                break;
            case 2:
                DialogChangeFont fragment
                        = DialogChangeFont.newInstance(SettingActivity.this, 8,
                        2,
                        true,
                        false
                );
                fragment.show(getFragmentManager(), "ChangeFontSize");
                break;
            default:
                Toast.makeText(this, "not create yet", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                settings.setSoundUri(uri);
                Log.d("dbg", "URI: " + uri);
                Toast.makeText(this, "sound ok", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void replaceFragment(Fragment destFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, destFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
