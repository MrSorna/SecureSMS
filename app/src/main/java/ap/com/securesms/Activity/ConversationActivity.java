package ap.com.securesms.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ap.com.securesms.Adaptor.ConversationAdapter;
import ap.com.securesms.Fragment.ContactsFragment;
import ap.com.securesms.Model.Contact;
import ap.com.securesms.Model.Message;
import ap.com.securesms.Services.MessagesParser;
import ap.com.securesms.Model.MessageItem;
import ap.com.securesms.R;
import ap.com.securesms.Services.SMS;
import ap.com.securesms.Util.Constants;
import ap.com.securesms.Util.RecyclerItemClickListener;
import ap.com.securesms.Util.Utils;


public class ConversationActivity extends AppCompatActivity {

    private EditText message;
    private static RecyclerView recyclerView;
    private static ConversationAdapter contactMessagesAdapter;
    public static boolean resume = false;
    private static MessageItem messageItem = null;
    private static Contact contact;
    private RelativeLayout loader;
    private static final int INITIAL_REQUEST = 1336;
    private static String id = null;
    private static String search = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_contact_messages);
        loader = (RelativeLayout) findViewById(R.id.loader);
        String phone = null;
        try {
            phone = getIntent().getExtras().getString(Constants.KEY_NUMBER);
        } catch (Exception e) {
        }
        contact = HomeActivity.db.getContact(phone);
        contactMessagesAdapter = new ConversationAdapter(ConversationActivity.this);
        message = (EditText) findViewById(R.id.message);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(contactMessagesAdapter);
        message.setFocusable(true);
        if (contact != null) {
            String msg = HomeActivity.db.getDraft(contact.getPhone());
            if (msg != null) {
                message.setText(msg);
            }
        }
        try {
            id = getIntent().getExtras().getString(Constants.KEY_ID);
            search = getIntent().getExtras().getString(Constants.KEY_SEARCHING);
        } catch (Exception e) {
        }

    }

    public void save(int position, boolean save) {
        messageItem.getMessages().get(position).setSave(save);
        contactMessagesAdapter.notifyDataSetChanged();
        Message imessage = messageItem.getMessages().get(position);
        if (imessage.isSave()) {
            String enc = Utils.encrypt(imessage.getMessage());
            if (enc != null) {
                Message message = new Message(imessage.getId(), enc, imessage.getContact(), imessage.getMils(), imessage.getiType(), imessage.isSave());
                HomeActivity.db.saveMessage(message);
                Utils.toast(getApplicationContext(), " پیام ذخیره شد");
            } else {
                Utils.toast(getApplicationContext(), "خطا در رمز کردن پیام");
            }
        } else {
            HomeActivity.db.deleteMessage(imessage);
            Utils.toast(getApplicationContext(), " پیام حذف شد");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_coversation, menu);
        MenuItem mSearch = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactMessagesAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.db:
                Intent intent = new Intent(ConversationActivity.this, DBCActivity.class);
                intent.putExtra(Constants.KEY_NUMBER, contact.getPhone());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void onClickSendSMS(View view) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (Utils.hasPermission(getApplicationContext(), Manifest.permission.SEND_SMS)) {
                doFinal();
            } else {
                ActivityCompat.requestPermissions(ConversationActivity.this, new String[]{Manifest.permission.SEND_SMS}, INITIAL_REQUEST);
            }
        } else {
            doFinal();
        }
    }

    private void doFinal() {
        if (message.getText().toString().length() > 0) {
            String body = message.getText().toString();
            body = Utils.encrypt(body);
            if (body == null) {
                Utils.toast(getApplicationContext(), "خطا در رمز کردن پیام");
                return;
            }
            contact.setSelect(true);
            SMS sms = new SMS(getApplicationContext(), contact, body);
            if (sms.send()) {
                message.setText("");
                Utils.toast(getApplicationContext(), "پیام با موفقیت ارسال شد.");
            } else {
                Utils.toast(getApplicationContext(), "خطا در ارسال پیام");
            }
            loader.setVisibility(View.VISIBLE);
            HomeActivity.refresh(new MessagesParser.OnFinished() {
                @Override
                public void finished(ArrayList<MessageItem> items) {
                    loader.setVisibility(View.GONE);
                }
            }, 2000);
        } else {
            Utils.toast(getApplicationContext(), "لطفا متن پیام را بنویسید");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume = true;
        loader.setVisibility(View.VISIBLE);
        HomeActivity.refresh(new MessagesParser.OnFinished() {
            @Override
            public void finished(ArrayList<MessageItem> items) {
                loader.setVisibility(View.GONE);
                if (contact != null)
                    setTitle(contact.getName());
            }
        }, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        resume = false;
    }

    public static void refresh() {
        if (ConversationActivity.resume) {
            messageItem = HomeActivity.messagesParser.getMessageItem(contact.getPhone());
            if (messageItem != null) {
                contactMessagesAdapter.setValues(messageItem.getMessages());
                contactMessagesAdapter.notifyDataSetChanged();
                if (id != null) {
                    try {
                        int position = 0;
                        for (position = 0; position < messageItem.getMessages().size(); position++) {
                            if (messageItem.getMessages().get(position).getId().equals(id)) {
                                break;
                            }
                        }
                        recyclerView.scrollToPosition(position);
                        contactMessagesAdapter.setContent(search);
                    } catch (Exception e) {
                    }
                    id = search = null;
                } else {
                    recyclerView.scrollToPosition(messageItem.getMessages().size() - 1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Utils.hasPermission(ConversationActivity.this, Manifest.permission.SEND_SMS)) {
            doFinal();
        } else {
            Utils.toast(getApplicationContext(), "مجوز ارسال پیام ندارید");
        }
    }

    @Override
    protected void onDestroy() {
        if (contact != null) {
            String msg = message.getText().toString();
            if (!msg.isEmpty()) {
                HomeActivity.db.saveDraft(contact.getPhone(), msg);
                Utils.toast(getApplicationContext(), "پیام در پیش نویس ذخیره شد...");
            } else {
                HomeActivity.db.deleteDraft(contact.getPhone());
            }
        }
        super.onDestroy();
    }
}
