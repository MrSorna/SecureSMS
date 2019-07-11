package ap.com.securesms.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;

import ap.com.securesms.Fragment.SelectContactFragment;
import ap.com.securesms.R;
import ap.com.securesms.Fragment.SendMessageFragment;
import ap.com.securesms.Fragment.SendStatusFragment;
import ap.com.securesms.Database.DatabaseHandler;
import ap.com.securesms.Model.Contact;
import ap.com.securesms.Util.Utils;


public class MultiSendMessage extends AppCompatActivity {


    public static ArrayList<Contact> contacts = new ArrayList<>();
    public static ViewPager viewPager;
    private static final int INITIAL_REQUEST = 1336;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        contacts = HomeActivity.db.getContacts();
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        final LayoutInflater inflater = LayoutInflater.from(viewPagerTab.getContext());
        final Resources res = viewPagerTab.getContext().getResources();
        viewPagerTab.setCustomTabView(new SmartTabLayout.TabProvider() {
            @Override
            public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
                ImageView icon = (ImageView) inflater.inflate(R.layout.tab_icon, container,
                        false);
                switch (position) {
                    case 0:
                        icon.setImageDrawable(res.getDrawable(R.drawable.contact_tab));
                        break;
                    case 1:
                        icon.setImageDrawable(res.getDrawable(R.drawable.write_tab));
                        break;
                    case 2:
                        icon.setImageDrawable(res.getDrawable(R.drawable.send_tab));
                        break;
                    default:
                        throw new IllegalStateException("Invalid position: " + position);
                }
                return icon;
            }
        });

        FragmentPagerItems pages = new FragmentPagerItems(this);
        pages.add(FragmentPagerItem.of("انتخاب مخاطب", SelectContactFragment.class));
        pages.add(FragmentPagerItem.of("ارسال پیام", SendMessageFragment.class));
        pages.add(FragmentPagerItem.of("وضعیت ارسال", SendStatusFragment.class));
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), pages);
        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        SelectContactFragment.onFocus();
                        break;
                    case 1:
                        SendMessageFragment.onFocus();
                        break;
                    case 2:
                        SendStatusFragment.onFocus();
                        break;
                }
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!Utils.hasPermission(getApplicationContext(), Manifest.permission.SEND_SMS)) {
                ActivityCompat.requestPermissions(MultiSendMessage.this, new String[]{Manifest.permission.SEND_SMS}, INITIAL_REQUEST);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SendMessageFragment.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (!Utils.hasPermission(MultiSendMessage.this, Manifest.permission.SEND_SMS)) {
            Utils.toast(getApplicationContext(), "مجوز ارسال پیام ندارید");
            finish();
        }
    }
}
