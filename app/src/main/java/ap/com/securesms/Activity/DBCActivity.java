package ap.com.securesms.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ap.com.securesms.Adaptor.DBCAdapter;
import ap.com.securesms.Fragment.ContactsFragment;
import ap.com.securesms.Fragment.MessagesFragment;
import ap.com.securesms.Model.Contact;
import ap.com.securesms.Model.Message;
import ap.com.securesms.R;
import ap.com.securesms.Util.Constants;
import ap.com.securesms.Util.RecyclerItemClickListener;
import ap.com.securesms.Util.Utils;


public class DBCActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView text;
    private DBCAdapter dbcAdapter;
    private ArrayList<Message> messages;
    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_messages);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        text = (TextView) findViewById(R.id.text);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        String phone = null;
        try {
            phone = getIntent().getExtras().getString(Constants.KEY_NUMBER);
        } catch (Exception e) {
        }
        contact = HomeActivity.db.getContact(phone);
        setMessages();
        dbcAdapter = new DBCAdapter(DBCActivity.this);
        dbcAdapter.setValues(messages);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(dbcAdapter);
    }

    public void delete(int position) {
        Message message = messages.get(position);
        HomeActivity.db.deleteMessage(message);
        Utils.toast(getApplicationContext(), " پیام حذف شد");
        messages.remove(position);
        dbcAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        if (contact != null) setTitle(contact.getName());
        else setTitle("ناشناس");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_db, menu);
        MenuItem mSearch = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                dbcAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    private void setMessages() {
        if (!Utils.isKeyUp()) {
            Intent i = new Intent(DBCActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        } else {
            messages = HomeActivity.db.getMessages(contact);
            for (Message message : messages) {
                if (Utils.isEnc(message.getMessage())) {
                    message.setMessage(Utils.decrypt(message.getMessage()));
                }
            }
            if (messages.isEmpty()) {
                text.setVisibility(View.VISIBLE);
            } else {
                text.setVisibility(View.GONE);
                Collections.sort(messages, new Comparator<Message>() {
                    @Override
                    public int compare(Message m1, Message m2) {
                        return (int) (m2.getMils() - m1.getMils());
                    }
                });
            }

        }
    }


}
