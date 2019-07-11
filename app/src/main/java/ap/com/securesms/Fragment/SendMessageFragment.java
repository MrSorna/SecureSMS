package ap.com.securesms.Fragment;

/**
 * Created by Amirhosein on 1/11/2019.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import ap.com.securesms.Activity.MultiSendMessage;
import ap.com.securesms.Model.Contact;
import ap.com.securesms.R;
import ap.com.securesms.Services.SMS;
import ap.com.securesms.Util.OnReceiveSMStatus;
import ap.com.securesms.Util.Utils;

public class SendMessageFragment extends Fragment {
    private EditText message;
    public static TextView tv_count;
    private Button send;
    public static SMS sms;


    public static void close() {
        if (sms != null)
            sms.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send_message, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        message = (EditText) view.findViewById(R.id.message);
        send = (Button) view.findViewById(R.id.sendsms);
        tv_count = (TextView) view.findViewById(R.id.count);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = 0;
                for (Contact contact : MultiSendMessage.contacts) {
                    if (contact.isSelect())
                        count++;
                }
                if (count == 0) {
                    Utils.toast(getContext(), "حداقل باید یک مخاطب انتخاب شود");
                    return;
                }
                if (message.getText().toString().length() < 0) {
                    Utils.toast(getContext(), "لطفا متن پیام را بنویسید");
                    return;
                }
                Utils.showDialog(getActivity(), "", "آیا می خواهید این پیام را برای " + count + " مخاطب انتخاب شده ارسال کنید؟", true, "بله", new Runnable() {
                    @Override
                    public void run() {
                        String body = message.getText().toString();
                        body = Utils.encrypt(body);
                        if (body == null) {
                            Utils.toast(getContext(), "خطا در رمز کردن پیام");
                            return;
                        }
                        sms = new SMS(getContext(), MultiSendMessage.contacts, body);
                        sms.setOnReceiveSMStatus(new OnReceiveSMStatus() {
                            @Override
                            public void sent(ArrayList<SMS.SMS_Item> smsItems, SMS.SMS_Item item) {
                                SendStatusFragment.adapter.setSmsItems(smsItems);
                            }

                            @Override
                            public void deliver(ArrayList<SMS.SMS_Item> smsItems, SMS.SMS_Item item) {
                                SendStatusFragment.adapter.setSmsItems(smsItems);
                            }

                            @Override
                            public void finish(ArrayList<SMS.SMS_Item> smsItems) {
                                SendStatusFragment.adapter.setSmsItems(smsItems);
                            }
                        });
                        if (sms.send()) {
                            SendStatusFragment.adapter.setSmsItems(sms.getSmsItems());
                            Utils.toast(getContext(), "پیام با موفقیت ارسال شد.");
                            MultiSendMessage.viewPager.setCurrentItem(2);
                        } else {
                            Utils.toast(getContext(), "خطا در ارسال پیام");
                        }
                    }
                }, "خیر", new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });
    }

    public static void onFocus() {
        int count = 0;
        for (Contact contact : MultiSendMessage.contacts) {
            if (contact.isSelect())
                count++;
        }
        if (count == 0)
            SendMessageFragment.tv_count.setText("هیچ مخاطبی انتخاب نشده");
        else
            SendMessageFragment.tv_count.setText(count + " مخاطب انتخاب شد");
    }

}