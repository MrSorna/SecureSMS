package ap.com.securesms.Services;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;

import java.util.ArrayList;

import ap.com.securesms.Model.Contact;
import ap.com.securesms.Util.OnReceiveSMStatus;
import ap.com.securesms.Util.Utils;

/**
 * Created by Amirhosein on 1/10/2019.
 */

public class SMS {
    private Context context;
    private ArrayList<SMS_Item> smsItems = new ArrayList<>();
    private String message;
    private OnReceiveSMStatus onReceiveSMStatus;
    private SmsManager smsManager = SmsManager.getDefault();

    public SMS(Context context, ArrayList<Contact> contacts, String message) {
        this.context = context;
        this.message = message;
        for (Contact contact : contacts) {
            if (contact.isSelect())
                smsItems.add(new SMS_Item(contact));
        }
    }


    public SMS(Context context, Contact contact, String message) {
        this.context = context;
        this.message = message;
        if (contact.isSelect())
            smsItems.add(new SMS_Item(contact));
    }


    public void addContact(Contact contact) {
        if (contact.isSelect())
            smsItems.add(new SMS_Item(contact));
    }

    public void setOnReceiveSMStatus(OnReceiveSMStatus onReceiveSMStatus) {
        this.onReceiveSMStatus = onReceiveSMStatus;

    }

    public ArrayList<SMS_Item> getSmsItems() {
        return smsItems;
    }

    public boolean send() {
        if (smsItems.isEmpty()) {
            isFin();
            return false;
        }
        for (SMS_Item item : smsItems) {
            final Contact contact = item.getContact();
            if (contact.isSelect()) {
                int len = 1;
                if (message.length() > 160) {
                    ArrayList<String> parts = smsManager.divideMessage(message);
                    len = parts.size();
                    ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();
                    ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                    for (int j = 0; j < len; j++) {
                        sentIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(contact.getPhone()), 0));
                        deliveryIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(contact.getPhone()), 0));
                    }
                    smsManager.sendMultipartTextMessage(contact.getPhone(), null, parts, sentIntents, deliveryIntents);
                } else {
                    PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(contact.getPhone()), 0);
                    PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(contact.getPhone()), 0);
                    smsManager.sendTextMessage(contact.getPhone(), null, message, sentPI, deliveredPI);
                }
                if (onReceiveSMStatus != null) {
                    item.setDeliverReceiver(new DeliverReceiver(len, contact.getPhone()));
                    item.setSentReceiver(new SentReceiver(len, contact.getPhone()));
                    context.registerReceiver(item.getSentReceiver(), new IntentFilter(contact.getPhone()));
                    context.registerReceiver(item.getDeliverReceiver(), new IntentFilter(contact.getPhone()));
                }
            }
        }
        return true;
    }


    public void close() {
        for (SMS_Item item : smsItems) {
            if (item.getDeliverReceiver() != null && item.getDeliverReceiver().isOrderedBroadcast())
                context.unregisterReceiver(item.getDeliverReceiver());
            if (item.getSentReceiver() != null && item.getSentReceiver().isOrderedBroadcast())
                context.unregisterReceiver(item.getSentReceiver());
        }
        smsItems.clear();
    }


    private void isFin() {
        boolean b = true;
        for (SMS_Item item : smsItems) {
            b = b && item.isSetSent() && item.isSetDeliver();
            if (!b)
                break;
        }
        if (b)
            if (onReceiveSMStatus != null)
                onReceiveSMStatus.finish(smsItems);
    }

    private int getIndex(String phone) {
        for (int i = 0; i < smsItems.size(); i++) {
            if (smsItems.get(i).getContact().getPhone().equals(phone)) {
                return i;
            }
        }
        return -1;
    }

    private class DeliverReceiver extends BroadcastReceiver {
        private String phone = "";
        private int len;

        public DeliverReceiver(int len, String phone) {
            this.phone = phone;
            this.len = len;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Activity.RESULT_OK == getResultCode()) {
                len--;
                if (len == 0) {
                    if (phone != null) {
                        int index = getIndex(phone);
                        if (index != -1) {
                            smsItems.get(index).setSetDeliver(true);
                            smsItems.get(index).setDeliver(Activity.RESULT_OK == getResultCode());
                            if (onReceiveSMStatus != null)
                                onReceiveSMStatus.deliver(smsItems, smsItems.get(index));
                        }
                    }
                    isFin();
                    context.unregisterReceiver(this);
                }
            }
        }
    }

    private class SentReceiver extends BroadcastReceiver {
        private String phone = "";
        private int len;

        public SentReceiver(int len, String phone) {
            this.phone = phone;
            this.len = len;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Activity.RESULT_OK == getResultCode()) {
                len--;
                if (len == 0) {
                    if (phone != null) {
                        int index = getIndex(phone);
                        if (index != -1) {
                            smsItems.get(index).setSetSent(true);
                            smsItems.get(index).setSent(Activity.RESULT_OK == getResultCode());
                            if (onReceiveSMStatus != null)
                                onReceiveSMStatus.sent(smsItems, smsItems.get(index));
                        }
                    }
                    isFin();
                    context.unregisterReceiver(this);

                }
            }
        }
    }

    public static class SMS_Item {
        private Contact contact;
        private boolean sent, deliver;
        private boolean isSetSent, isSetDeliver;
        private SentReceiver sentReceiver;
        private DeliverReceiver deliverReceiver;

        public SMS_Item(Contact contact) {
            this.contact = contact;
        }

        private void setSetDeliver(boolean setDeliver) {
            isSetDeliver = setDeliver;
        }

        private boolean isSetDeliver() {
            return isSetDeliver;
        }

        private boolean isSetSent() {
            return isSetSent;
        }

        private void setSetSent(boolean setSent) {
            isSetSent = setSent;
        }

        private void setDeliverReceiver(DeliverReceiver deliverReceiver) {
            this.deliverReceiver = deliverReceiver;
        }

        private void setSentReceiver(SentReceiver sentReceiver) {
            this.sentReceiver = sentReceiver;
        }

        private DeliverReceiver getDeliverReceiver() {
            return deliverReceiver;
        }

        private SentReceiver getSentReceiver() {
            return sentReceiver;
        }

        public boolean isDeliver() {
            return deliver;
        }

        public boolean isSent() {
            return sent;
        }

        public Contact getContact() {
            return contact;
        }

        public void setContact(Contact contact) {
            this.contact = contact;
        }

        private void setSent(boolean sent) {
            this.sent = sent;
        }

        private void setDeliver(boolean deliver) {
            this.deliver = deliver;
        }

        public SMS_Item(Contact contact, boolean sent, boolean deliver) {
            this.contact = contact;
            this.sent = sent;
            this.deliver = deliver;
        }

    }
}
