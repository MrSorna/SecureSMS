package ap.com.securesms.Activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;

import ap.com.securesms.Adaptor.AllConversationAdapter;
import ap.com.securesms.Model.Message;
import ap.com.securesms.Services.MessagesParser;
import ap.com.securesms.Fragment.MessagesFragment;
import ap.com.securesms.Fragment.ContactsFragment;
import ap.com.securesms.Model.MessageItem;
import ap.com.securesms.R;
import ap.com.securesms.Util.Constants;
import ap.com.securesms.Util.RecyclerItemClickListener;
import ap.com.securesms.Util.Settings;
import ap.com.securesms.Util.Utils;
import ap.com.securesms.Database.DatabaseHandler;

import android.support.v7.widget.SearchView;

public class HomeActivity extends AppCompatActivity {

    public static boolean resume = false;
    public static MessagesParser messagesParser;
    public static ViewPager viewPager;
    public static DatabaseHandler db;
    private RelativeLayout loader;
    private AllConversationAdapter allConversationAdapter;
    private RelativeLayout searching;
    public static Settings settings;
    public RecyclerView rv_searching;
    private Animation slideUp;
    private static final int INITIAL_REQUEST = 1338;
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
    };
    private SearchView mSearchView;
    private static HomeActivity homeActivity;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_messages);
        db = new DatabaseHandler(getApplicationContext());
        loader = (RelativeLayout) findViewById(R.id.loader);
        searching = (RelativeLayout) findViewById(R.id.searching);
        rv_searching = (RecyclerView) findViewById(R.id.list);
        homeActivity = HomeActivity.this;
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        FragmentPagerItems pages = new FragmentPagerItems(this);
        pages.add(FragmentPagerItem.of("پیام ها", MessagesFragment.class));
        pages.add(FragmentPagerItem.of("مخاطب ها", ContactsFragment.class));
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), pages);
        settings = new Settings(getApplicationContext());
        viewPager.setAdapter(adapter);
        allConversationAdapter = new AllConversationAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        rv_searching.setLayoutManager(linearLayoutManager);
        rv_searching.setAdapter(allConversationAdapter);
        rv_searching.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), rv_searching, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Message message = allConversationAdapter.getItem(position);
                if (message != null) {
                    Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                    intent.putExtra(Constants.KEY_NUMBER, message.getContact().getPhone());
                    intent.putExtra(Constants.KEY_ID, message.getId());
                    intent.putExtra(Constants.KEY_SEARCHING, mSearchView.getQuery().toString());
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        viewPagerTab.setViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        MessagesFragment.onFocus();
                        break;
                    case 1:
                        ContactsFragment.onFocus();
                        break;
                }
            }
        });
        messagesParser = new MessagesParser(HomeActivity.this);
        String number = null;
        try {
            number = getIntent().getExtras().getString(Constants.KEY_NUMBER);
        } catch (Exception e) {
        }
        if (number != null) {
            Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
            intent.putExtra(Constants.KEY_NUMBER, number);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        messagesParser.close();
        resume = false;
        Utils.killKey();
//        expireHandler.removeCallbacks(expire);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        MenuItem mSearch = menu.findItem(R.id.search);
        mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        if (newText != null && !newText.isEmpty()) {
                            if (searching.getVisibility() == View.GONE) {
                                searching.setVisibility(View.VISIBLE);
                                searching.startAnimation(slideUp);
                            }
                            allConversationAdapter.getFilter().filter(newText);
                        } else {
                            searching.setVisibility(View.GONE);
                        }
                        break;
                    case 1:
                        ContactsFragment.filter(newText);
                        break;
                }
                return false;
            }
        });
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allConversationAdapter.setValues(messagesParser.getMessages());
                allConversationAdapter.notifyDataSetChanged();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                startActivity(new Intent(HomeActivity.this, SettingActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume = true;
        if (!Utils.isKeyUp()) {
            Intent i = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        access();
//        expireHandler.removeCallbacks(expire);
    }

    private void access() {
        loader.setVisibility(View.VISIBLE);
        refresh(new MessagesParser.OnFinished() {
            @Override
            public void finished(ArrayList<MessageItem> items) {
                loader.setVisibility(View.GONE);
            }
        }, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        resume = false;
//        expireHandler.postDelayed(expire, 2 * 60000);
    }

    public static void refresh(final MessagesParser.OnFinished finished, long delay) {
        if (Utils.hasPermission(homeActivity, PERMISSIONS)) {
            messagesParser.reloadMessages(new MessagesParser.OnFinished() {
                @Override
                public void finished(ArrayList<MessageItem> items) {
                    if (resume)
                        MessagesFragment.setAdapter(items);
                    if (ConversationActivity.resume)
                        ConversationActivity.refresh();
                    finished.finished(items);
                }
            }, delay);
        } else {
            ActivityCompat.requestPermissions(homeActivity, PERMISSIONS, INITIAL_REQUEST);
        }
    }

    public void onClickNewSMS(View view) {
        Intent intent = new Intent(this, MultiSendMessage.class);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Utils.hasPermission(HomeActivity.this, PERMISSIONS)) {
            access();
        } else {
            Utils.toast(getApplicationContext(), "مجوز خواندن مخاطبین یا خواندن پیام را ندارید");
        }
    }

}
