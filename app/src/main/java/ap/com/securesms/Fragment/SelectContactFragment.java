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
import android.widget.Button;

import ap.com.securesms.Activity.MultiSendMessage;
import ap.com.securesms.Adaptor.ContactAdapter;
import ap.com.securesms.R;
import ap.com.securesms.Util.RecyclerItemClickListener;

public class SelectContactFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_contact, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        final ContactAdapter contactAdapter = new ContactAdapter(MultiSendMessage.contacts,false,getActivity());
        mRecyclerView.setAdapter(contactAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MultiSendMessage.contacts.get(position).setSelect(!MultiSendMessage.contacts.get(position).isSelect());
                contactAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
            ((Button) view.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MultiSendMessage.viewPager.setCurrentItem(1);
                }
            });
    }
    public static void onFocus(){

    }
}