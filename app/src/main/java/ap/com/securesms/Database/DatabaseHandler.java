package ap.com.securesms.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import ap.com.securesms.Model.Message;
import ap.com.securesms.Util.Constants;
import ap.com.securesms.Model.Contact;
import ap.com.securesms.Util.Utils;

/**
 * Created by osarvade on 21-05-2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {


    public DatabaseHandler(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACT_TABLE = "CREATE TABLE " + Constants.TABLE_CONTACT + "(" + Constants.KEY_NUMBER + " TEXT primary key, " + Constants.KEY_NAME + " TEXT " + " )";
        String CREATE_DRAFTS_TABLE = "CREATE TABLE " + Constants.TABLE_DRAFTS + "(" + Constants.KEY_NUMBER + " TEXT primary key, " + Constants.KEY_MESSAGE + " TEXT " + " )";
        String CREATE_MESSAGE_TABLE = "CREATE TABLE " + Constants.TABLE_MESSAGES + "(" + Constants.KEY_ID + " TEXT primary key, "
                + Constants.KEY_MESSAGE + " TEXT ,"
                + Constants.KEY_DATE + " TEXT ,"
                + Constants.KEY_TYPE + " INTEGER ,"
                + Constants.KEY_NUMBER + " TEXT "
                + " )";
        db.execSQL(CREATE_CONTACT_TABLE);
        db.execSQL(CREATE_DRAFTS_TABLE);
        db.execSQL(CREATE_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_DRAFTS);
        onCreate(db);
    }


    public boolean saveMessage(Message message) {
        if (message != null)
            if (message.isSave()) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(Constants.KEY_NUMBER, message.getContact().getPhone());
                values.put(Constants.KEY_ID, message.getId());
                values.put(Constants.KEY_MESSAGE, message.getMessage());
                values.put(Constants.KEY_DATE, message.getMils());
                values.put(Constants.KEY_TYPE, message.getiType());
                long l = db.replace(Constants.TABLE_MESSAGES, null, values);
                db.close();
                return l != -1;
            }

        return false;
    }

    public void saveMessages(ArrayList<Message> messages) {
        deleteMessages();
        for (Message message : messages) {
            saveMessage(message);
        }
    }

    public boolean saveContact(Contact contact) {
        if (contact != null)
            if (contact.isSelect()) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(Constants.KEY_NAME, contact.getName());
                values.put(Constants.KEY_NUMBER, contact.getPhone());
                db.replace(Constants.TABLE_CONTACT, null, values);
                db.close();
                return true;
            }
        return false;
    }


    public boolean saveDraft(String phone, String message) {
        if (phone != null)
            if (message != null) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(Constants.KEY_NUMBER, phone);
                values.put(Constants.KEY_MESSAGE, message);
                db.replace(Constants.TABLE_DRAFTS, null, values);
                db.close();
                return true;
            }
        return false;
    }


    public ArrayList<Contact> getContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) {
            Cursor c = db.rawQuery("SELECT * FROM " + Constants.TABLE_CONTACT, null);
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    contacts.add(new Contact(c.getString(c.getColumnIndex(Constants.KEY_NAME)), c.getString(c.getColumnIndex(Constants.KEY_NUMBER)), getMessagesCount(c.getString(c.getColumnIndex(Constants.KEY_NUMBER))) > 0));
                    c.moveToNext();
                }
            }
        }
        return contacts;
    }

    public Contact getContact(String number) {
        if (number != null) {
            SQLiteDatabase db = this.getReadableDatabase();
            if (db != null && db.isOpen()) {
                Cursor c = db.rawQuery("SELECT * FROM " + Constants.TABLE_CONTACT + " WHERE " + Constants.KEY_NUMBER + " = " + number, null);
                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) {
                        return new Contact(c.getString(c.getColumnIndex(Constants.KEY_NAME)), c.getString(c.getColumnIndex(Constants.KEY_NUMBER)), getMessagesCount(number) > 0);
                    }
                }
            }
        }
        return null;
    }

    public String getDraft(String phone) {
        if (phone != null) {
            SQLiteDatabase db = this.getReadableDatabase();
            if (db != null && db.isOpen()) {
                Cursor c = db.rawQuery("SELECT * FROM " + Constants.TABLE_DRAFTS + " WHERE " + Constants.KEY_NUMBER + " = " + phone, null);
                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) {
                        return c.getString(c.getColumnIndex(Constants.KEY_MESSAGE));
                    }
                }
            }
        }
        return null;
    }

    public ArrayList<Message> getMessages(Contact contact) {
        ArrayList<Message> messages = new ArrayList<>();
        if (contact != null) {
            SQLiteDatabase db = this.getReadableDatabase();
            if (db != null && db.isOpen()) {
                Cursor c = db.rawQuery("SELECT * FROM " + Constants.TABLE_MESSAGES + " WHERE " + Constants.KEY_NUMBER + " = " + contact.getPhone(), null);
                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) {
                        messages.add(new Message(
                                c.getString(c.getColumnIndex(Constants.KEY_ID)),
                                c.getString(c.getColumnIndex(Constants.KEY_MESSAGE)),
                                contact,
                                c.getString(c.getColumnIndex(Constants.KEY_DATE)),
                                c.getInt(c.getColumnIndex(Constants.KEY_TYPE)), true
                        ));
                        c.moveToNext();
                    }
                }
            }
        }
        return messages;
    }

    public int getMessagesCount(String number) {
        if (number != null) {
            SQLiteDatabase db = this.getReadableDatabase();
            if (db != null && db.isOpen()) {
                Cursor c = db.rawQuery("SELECT * FROM " + Constants.TABLE_MESSAGES + " WHERE " + Constants.KEY_NUMBER + " = " + number, null);
                return c.getCount();
            }
        }
        return 0;
    }


    public boolean deleteDraft(String phone) {
        if (phone != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(Constants.TABLE_DRAFTS, Constants.KEY_NUMBER + " = ?", new String[]{phone});
            db.close();
            return true;
        }
        return false;
    }

    public boolean deleteContact(Contact contact) {
        if (contact != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(Constants.TABLE_CONTACT, Constants.KEY_NUMBER + " = ?", new String[]{contact.getPhone()});
            db.close();
            return true;
        }
        return false;
    }


    public void deleteMessage(Message message) {
        if (message != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(Constants.TABLE_MESSAGES, Constants.KEY_ID + " = ?", new String[]{message.getId()});
            db.close();
        }
    }


    public void deleteContacts() {
        SQLiteDatabase db = this.getWritableDatabase();
        String DeleteQuery = "DELETE FROM " + Constants.TABLE_CONTACT;
        db.execSQL(DeleteQuery);
        db.close();
    }

    public void deleteDrafts() {
        SQLiteDatabase db = this.getWritableDatabase();
        String DeleteQuery = "DELETE FROM " + Constants.TABLE_DRAFTS;
        db.execSQL(DeleteQuery);
        db.close();
    }

    public void deleteMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        String DeleteQuery = "DELETE FROM " + Constants.TABLE_MESSAGES;
        db.execSQL(DeleteQuery);
        db.close();
    }

    public void deleteMessages(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_MESSAGES, Constants.KEY_NUMBER + " = ?", new String[]{contact.getPhone()});
        db.close();
    }

    public void deleteMessages(ArrayList<Message> messages) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Message message : messages) {
            if (message.isSave())
                db.delete(Constants.TABLE_MESSAGES, Constants.KEY_ID + " = ?", new String[]{message.getId()});
        }
        db.close();
    }

}
