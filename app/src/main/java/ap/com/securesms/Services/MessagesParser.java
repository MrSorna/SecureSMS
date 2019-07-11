package ap.com.securesms.Services;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import ap.com.securesms.Activity.HomeActivity;
import ap.com.securesms.Activity.LoginActivity;
import ap.com.securesms.Model.MessageItem;
import ap.com.securesms.Util.UIWorker;
import ap.com.securesms.Util.Utils;
import ap.com.securesms.Database.DatabaseHandler;
import ap.com.securesms.Model.Contact;
import ap.com.securesms.Model.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


public class MessagesParser {

    private static final Uri SMS_INBOX = Uri.parse("content://sms/");
    private DatabaseHandler db;
    private ArrayList<Contact> contacts;
    private Map<String, MessageItem> ITEM_MAP;
    private Activity activity;


    public MessagesParser(Activity activity) {
        this.activity = activity;
        ITEM_MAP = new HashMap<>();
        db = new DatabaseHandler(activity);
    }


    public void reloadMessages(final OnFinished finished, final long delay) {
        if (!Utils.isKeyUp()) {
            Intent i = new Intent(activity, LoginActivity.class);
            activity.startActivity(i);
            activity.finish();
            return;
        }
        new UIWorker(activity) {
            private ArrayList<MessageItem> messageItems = new ArrayList<MessageItem>();

            @Override
            public Object construct() {
                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Cursor messageInboxCursor = activity.getContentResolver().query(SMS_INBOX, null, null, null, null);
                contacts = db.getContacts();
                ITEM_MAP = new HashMap<>();
                int totalSms = messageInboxCursor.getCount();
                String address;
                String body;
                String id;
                String date;
                int type;

                if (messageInboxCursor.moveToFirst()) {
                    for (int i = 0; i < totalSms; i++) {
                        id = messageInboxCursor.getString(messageInboxCursor.getColumnIndexOrThrow("_id"));
                        address = messageInboxCursor.getString(messageInboxCursor.getColumnIndexOrThrow("address"));
                        body = messageInboxCursor.getString(messageInboxCursor.getColumnIndexOrThrow("body"));
                        date = messageInboxCursor.getString(messageInboxCursor.getColumnIndexOrThrow("date"));
                        type = messageInboxCursor.getInt(messageInboxCursor.getColumnIndexOrThrow("type"));
                        try {
                            if (address.length() > 0) {
                                address = Utils.getPhone(address);
                                for (Contact contact : contacts) {
                                    if (address.contains(contact.getPhone())) {
                                        if (body != null) {
                                            if (Utils.isEnc(body)) {
                                                body = Utils.decrypt(body);
                                                if (body != null) {
                                                    if (ITEM_MAP.containsKey(address)) {
                                                        MessageItem messages = ITEM_MAP.get(address);
                                                        Message message = new Message(id, body, contact, date, type, false);
                                                        message.setMessage(message.getMessage());
                                                        messages.getMessages().add(message);
                                                    } else {
                                                        Message message = new Message(id, body, contact, date, type, false);
                                                        if (message.getMessage() != null) {
                                                            message.setMessage(message.getMessage());
                                                            ArrayList<Message> arrayList = new ArrayList<>();
                                                            arrayList.add(message);
                                                            MessageItem messages = new MessageItem(contact, message.getDate(), body, arrayList);
                                                            messageItems.add(messages);
                                                            ITEM_MAP.put(address, messages);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        messageInboxCursor.moveToNext();
                    }

                    for (Contact contact : contacts) {
                        ArrayList<Message> messages = HomeActivity.db.getMessages(contact);
                        for (Message message : messages) {
                            if (Utils.isEnc(message.getMessage())) {
                                message.setMessage(Utils.decrypt(message.getMessage()));
                            }
                        }
                        if (ITEM_MAP.containsKey(contact.getPhone())) {
                            MessageItem messageItem = ITEM_MAP.get(contact.getPhone());
                            for (Message dbMessage : messages) {
                                for (int i = 0; i < messageItem.getMessages().size(); i++) {
                                    if (messageItem.getMessages().get(i).getId().equals(dbMessage.getId())) {
                                        messageItem.getMessages().remove(i);
                                    }
                                }
                            }
                            messageItem.getMessages().addAll(messages);
                        } else {
                            if (!messages.isEmpty()) {
                                MessageItem messageItem = new MessageItem(contact, messages.get(0).getDate(),
                                        messages.get(0).getMessage(), messages);
                                messageItems.add(messageItem);
                                ITEM_MAP.put(contact.getPhone(), messageItem);
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            public void finished() {
                finished.finished(messageItems);
            }
        }.start();
    }

    public static interface OnFinished {
        public void finished(ArrayList<MessageItem> items);
    }

    public void close() {
        db.close();
        if (contacts != null)
            contacts.clear();
        contacts = null;
        if (ITEM_MAP != null)
            ITEM_MAP.clear();
        ITEM_MAP = null;
    }

    public MessageItem getMessageItem(String phone) {
        return ITEM_MAP.get(phone);
    }

    public ArrayList<Message> getMessages() {
        ArrayList<Message> messages = new ArrayList<>();
        if (contacts != null)
            for (Contact contact : contacts) {
                if (ITEM_MAP.containsKey(contact.getPhone())) {
                    messages.addAll(ITEM_MAP.get(contact.getPhone()).getMessages());
                }
            }
        return messages;
    }


}
