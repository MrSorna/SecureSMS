package ap.com.securesms.Fragment;

/**
 * Created by Amirhosein on 1/11/2019.
 */

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ap.com.securesms.Activity.HomeActivity;
import ap.com.securesms.Adaptor.ContactAdapter;
import ap.com.securesms.Dialog.DialogAddContact;
import ap.com.securesms.Dialog.DialogContact;
import ap.com.securesms.Dialog.DialogSetContact;
import ap.com.securesms.Model.Contact;
import ap.com.securesms.Util.RecyclerItemClickListener;
import ap.com.securesms.R;
import ap.com.securesms.Util.CircleButton;
import ap.com.securesms.Util.Utils;

public class ContactsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private CircleButton circleButton;
    private static ContactAdapter contactAdapter;
    private static ArrayList<Contact> contacts = new ArrayList<>();
    public static final int CONTACT_PICKER_RESULT = 1001;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        circleButton = (CircleButton) view.findViewById(R.id.add);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        contactAdapter = new ContactAdapter(contacts, true, getContext());
        mRecyclerView.setAdapter(contactAdapter);

        refreshContacts();
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                DialogContact fragment
                        = DialogContact.newInstance(ContactsFragment.this, contacts.get(position),
                        8,
                        2,
                        true,
                        false
                );
                fragment.show(getActivity().getFragmentManager(), "Contact");
            }

            @Override
            public void onLongClick(View view, int position) {
                final Contact contact = contacts.get(position);
                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("هشدار");
                alertDialog.setMessage("آیا میخواهید " + contact.getName() + " با شماره موبایل " + contact.getPhone() + " را حذف کنید؟");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "خیر", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                        refreshContacts();
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "حذف کن", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HomeActivity.db.deleteContact(contact);
                        HomeActivity.db.deleteDraft(contact.getPhone());
                        HomeActivity.db.deleteMessages(contact);
                        alertDialog.dismiss();
                        refreshContacts();
                    }
                });
                alertDialog.show();
            }
        }));

        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogAddContact fragment
                        = DialogAddContact.newInstance(ContactsFragment.this, 8,
                        2,
                        true,
                        false
                );
                fragment.show(getActivity().getFragmentManager(), "AddContact");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null && requestCode == CONTACT_PICKER_RESULT) {
                Uri contactData = data.getData();
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                Cursor cursor = getActivity().getContentResolver().query(contactData, projection, null, null, null);
                if (cursor.moveToFirst()) {
                    HomeActivity.db.saveContact(new Contact(cursor).setSelect(true));
                    refreshContacts();
                }
            }
        }
    }

    public static void refreshContacts() {
        contacts = HomeActivity.db.getContacts();
        contactAdapter.setContacts(contacts);
        contactAdapter.notifyDataSetChanged();
    }

    public static void filter(String newText) {
        contactAdapter.getFilter().filter(newText);
    }

    @Override
    public void onResume() {
        super.onResume();
        contacts = HomeActivity.db.getContacts();
        contactAdapter.setContacts(contacts);
        contactAdapter.notifyDataSetChanged();
    }

    public void saveContact(String name, String phone) {
        if (HomeActivity.db.saveContact(new Contact(name, phone, false).setSelect(true))) {
            Utils.toast(getContext(), "مخاطب با موفقیت ذخیره شد.");
            refreshContacts();
        } else {
            Utils.toast(getContext(), "خطا در ذخیره سازی مخاطب !");
        }
    }


    public static void onFocus() {
        refreshContacts();
    }
}