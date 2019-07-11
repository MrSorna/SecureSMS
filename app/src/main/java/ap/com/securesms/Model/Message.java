package ap.com.securesms.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ap.com.securesms.Util.CalendarHelper;
import ap.com.securesms.Util.Utils;

/**
 * Created by osarvade on 21-05-2016.
 */
public class Message {
    private String message, id;
    private long mils;
    private boolean inbox = true;
    private TYPE type = TYPE.ALL;
    private int itype;
    private Contact contact;
//    private boolean select = false;
    private boolean save = false;

    public static enum TYPE {ALL, SENT, QUEUED, FAILED}

    public Message(String id, String message, Contact contact, String date, int type, boolean save) {
        this.id = id;
        this.message = message;
        this.contact = contact;
        this.save = save;
        mils = Long.parseLong(date);
        itype = type;
        setType(type);
    }

    public Message(String id, String message, Contact contact, long date, int type, boolean save) {
        this.id = id;
        this.message = message;
        this.contact = contact;
        this.save = save;
        mils = date;
        itype = type;
        setType(type);
    }

    public Contact getContact() {
        return contact;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        Date date = new Date(mils);
        return CalendarHelper.getShamsidate(date) + " - " + CalendarHelper.getTimeNoSecond(date);
    }

//    public boolean isSelect() {
//        return select;
//    }
//
//    public void setSelect(boolean select) {
//        this.select = select;
//    }

    public long getMils() {
        return mils;
    }

    private void setType(int type) {
        switch (type) {
            case 5:
                this.type = TYPE.FAILED;
                break;
            case 1:
                inbox = false;
                break;
            case 6:
                this.type = TYPE.QUEUED;
                break;
            case 2:
                this.type = TYPE.SENT;
                break;
        }
    }

    public boolean isSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    public String getId() {
        return id;
    }

    public TYPE getType() {
        return type;
    }

    public boolean isInbox() {
        return inbox;
    }

    public int getiType() {
        return itype;
    }

}
