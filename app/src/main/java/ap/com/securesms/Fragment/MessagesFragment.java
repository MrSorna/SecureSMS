package ap.com.securesms.Fragment;

/**
 * Created by Amirhosein on 1/11/2019.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ap.com.securesms.Activity.ConversationActivity;
import ap.com.securesms.Adaptor.AllMessagesAdapter;
import ap.com.securesms.Activity.HomeActivity;
import ap.com.securesms.Services.MessagesParser;
import ap.com.securesms.Model.MessageItem;
import ap.com.securesms.Util.Constants;
import ap.com.securesms.Util.RecyclerItemClickListener;
import ap.com.securesms.R;
import ap.com.securesms.Util.Settings;

public class MessagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private static AllMessagesAdapter allMessagesAdapter;
    private SwipeRefreshLayout refresh;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_messages, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.list);
        refresh = view.findViewById(R.id.refresh);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allMessagesAdapter = new AllMessagesAdapter(new Settings(getContext()));
        recyclerView.setAdapter(allMessagesAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MessageItem item = allMessagesAdapter.getItem(position);
                Intent intent = new Intent(getContext(), ConversationActivity.class);
                intent.putExtra(Constants.KEY_NUMBER, item.getContact().getPhone());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));


        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                HomeActivity.refresh(new MessagesParser.OnFinished() {
                    @Override
                    public void finished(ArrayList<MessageItem> items) {
                        refresh.setRefreshing(false);
                    }
                }, 500);
            }
        });
    }

    public static void filter(String newText) {
        allMessagesAdapter.getFilter().filter(newText);
    }

    public static void setAdapter(ArrayList<MessageItem> items) {
        allMessagesAdapter.setValues(items);
        allMessagesAdapter.notifyDataSetChanged();
    }

    public static void onFocus() {
        HomeActivity.refresh(new MessagesParser.OnFinished() {
            @Override
            public void finished(ArrayList<MessageItem> items) {
                setAdapter(items);
            }
        }, 0);
    }
}