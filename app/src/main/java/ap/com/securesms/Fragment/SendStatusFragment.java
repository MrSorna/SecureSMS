package ap.com.securesms.Fragment;

/**
 * Created by Amirhosein on 1/11/2019.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ap.com.securesms.Adaptor.SendStatusAdapter;
import ap.com.securesms.Fragment.SendMessageFragment;
import ap.com.securesms.R;

public class SendStatusFragment extends Fragment {

    public static SendStatusAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send_status, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter = new SendStatusAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);

    }

    public static void onFocus() {
        if (SendMessageFragment.sms != null)
            adapter.setSmsItems(SendMessageFragment.sms.getSmsItems());
    }
    public static void close(){
        adapter.clear();
    }
}