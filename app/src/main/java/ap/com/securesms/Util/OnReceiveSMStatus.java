package ap.com.securesms.Util;

import java.util.ArrayList;

import ap.com.securesms.Services.SMS;

/**
 * Created by Amirhosein on 1/10/2019.
 */

public interface OnReceiveSMStatus {

    void sent(ArrayList<SMS.SMS_Item> smsItems, SMS.SMS_Item item);

    void deliver(ArrayList<SMS.SMS_Item> smsItems, SMS.SMS_Item item);

    void finish(ArrayList<SMS.SMS_Item> smsItems);
}
