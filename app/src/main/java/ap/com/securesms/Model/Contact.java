package ap.com.securesms.Model;

import android.database.Cursor;
import android.provider.ContactsContract;

import ap.com.securesms.Util.Utils;

/**
 * Created by Amirhosein on 11/25/2018.
 */

public class Contact {

    private String name, phone;
    private boolean select = false;
    private boolean save = false;


    public Contact(Cursor cursor) {
        phone = Utils.getPhone(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
    }

    public Contact(String name, String phone, boolean save) {
        this.name = name;
        this.phone = Utils.getPhone(phone);
        select = false;
        this.save = save;
    }

    public boolean isSelect() {
        return select;
    }

    public boolean isSave() {
        return save;
    }

    public Contact setSelect(boolean select) {
        this.select = select;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
