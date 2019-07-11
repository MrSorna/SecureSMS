package ap.com.securesms.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ap.com.securesms.Util.CalendarHelper;

/**
 * Created by Amirhosein on 12/31/2018.
 */

public class MessageItem {
    private Contact contact;
    private String date;
    private String latestMessage;
    private ArrayList<Message> messages;

    public MessageItem(Contact contact, String date, String latestMessage, ArrayList<Message> messages) {
        this.contact = contact;
        this.date = date;
        this.latestMessage = latestMessage;
        this.messages = messages;
    }

    public Contact getContact() {
        return contact;
    }

    public int getCount() {
        return messages.size();
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public String getDate() {
        return date;
    }


    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public ArrayList<Message> getMessages() {
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return (int) (o1.getMils() - o2.getMils());
            }
        });
        return messages;
    }


    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
