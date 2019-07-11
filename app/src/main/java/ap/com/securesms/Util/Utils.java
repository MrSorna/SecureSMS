package ap.com.securesms.Util;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ap.com.securesms.R;

/**
 * Created by Amirhosein on 11/25/2018.
 */

public class Utils {
    private static byte[] key;
    public static final File KeyFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/ap.com.securesms/key");
    private static final byte[] my = new byte[]{0x53, 0x53, 0x4D, 0x53};

    public static String SHA(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
        byte[] bytes = input.getBytes("UTF-8");
        msdDigest.update(bytes, 0, bytes.length);
        return Base64.encodeToString(msdDigest.digest(), Base64.DEFAULT).trim();
    }

    public boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }
        return false;
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static String getPhone(String string) {
        string = string.replace(" ", "");
        if (string.length() == 11) {
            if (string.startsWith("0")) {
                return string.substring(1);
            }
        } else if (string.length() == 13) {
            if (string.startsWith("+98")) {
                return string.substring(3);
            }
        }
        return string;
    }

    public static String encrypt(String txt) {
        try {
            if (key == null) {
                new Exception("Key not loaded");
            }
            if (txt == null) {
                new Exception("txt null");
            }
            byte[] clean = txt.getBytes(Charset.forName("UTF-8"));
            int point = new SecureRandom().nextInt(key.length);
            if ((point + 16) > key.length) {
                point -= 16;
            }
            byte[] k = new byte[16];
            System.arraycopy(key, point, k, 0, 16);
            int ivSize = 16;
            byte[] iv = new byte[ivSize];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(k);
            byte[] keyBytes = new byte[16];
            System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(clean);
            byte[] encryptedIVAndText = new byte[8 + ivSize + encrypted.length];
            System.arraycopy(my, 0, encryptedIVAndText, 0, my.length);
            System.arraycopy(iv, 0, encryptedIVAndText, 4, ivSize);
            System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize + 4, encrypted.length);
            byte[] bytes = ByteBuffer.allocate(4).putInt(point).array();
            System.arraycopy(bytes, 0, encryptedIVAndText, ivSize + 4 + encrypted.length, bytes.length);
            return Base64.encodeToString(encryptedIVAndText, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isMy(byte[] bytes) {
        return new String(my).equals(new String(bytes));
    }

    public static String decrypt(String enc) {
        try {
            if (key == null) {
                new Exception("Key not loaded");
            }
            if (enc == null) {
                new Exception("enc null");
            }
            byte[] encrypt = Base64.decode(enc, Base64.DEFAULT);

            int ivSize = 16;
            int keySize = 16;
            byte[] encryptedIvTextBytes = new byte[encrypt.length - 8];
            System.arraycopy(encrypt, 4, encryptedIvTextBytes, 0, encryptedIvTextBytes.length);
            byte[] bytes = new byte[4];
            System.arraycopy(encrypt, encrypt.length - 4, bytes, 0, bytes.length);
            ByteBuffer wrapped = ByteBuffer.wrap(bytes);
            int point = wrapped.getInt();
            System.arraycopy(encrypt, 0, bytes, 0, bytes.length);
            if (isMy(bytes)) {
                byte[] k = new byte[16];
                System.arraycopy(key, point, k, 0, 16);
                byte[] iv = new byte[ivSize];
                System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
                IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
                int encryptedSize = encryptedIvTextBytes.length - ivSize;
                byte[] encryptedBytes = new byte[encryptedSize];
                System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);
                byte[] keyBytes = new byte[keySize];
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(k);
                System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
                SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
                Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
                byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);
                String out = new String(decrypted, Charset.forName("UTF-8"));
                return out;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static boolean genKey(int kb, File file) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[kb * 1024];
        random.nextBytes(bytes);
        return write(bytes, file);
    }

    public static boolean exists(File file) {
        if (!file.exists()) {
            try {
                new File(file.getParent()).mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }
        return file.exists();
    }

    private static boolean write(byte[] bytes, File file) {
        boolean result = exists(file);
        if (result) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                fos.write(bytes);
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    private static byte[] read(File file) {
        FileInputStream fileInputStream = null;
        byte[] bytesArray = new byte[0];
        if (file.exists()) {
            try {
                bytesArray = new byte[(int) file.length()];
                fileInputStream = new FileInputStream(file);
                fileInputStream.read(bytesArray);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bytesArray;
    }


    private static byte[] encrypt(byte[] key, byte[] plain) throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException {
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        SecretKeySpec sks = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, sks, iv);
        return cipher.doFinal(plain);
    }

    private static byte[] decrypt(byte[] key, byte[] encrypted) throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException {
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        SecretKeySpec sks = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, sks, iv);
        return cipher.doFinal(encrypted);
    }

    private static byte[] genSecKey(String k) {
        try {
            byte[] key = k.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            return Arrays.copyOf(key, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean copyKey(File newKey, String oldPass, String newPass) {
        try {
            return write(encrypt(genSecKey(newPass), decrypt(genSecKey(oldPass), read(newKey))), KeyFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean changeKeyPass(String oldPass, String newPass) {
        try {
            return copyKey(KeyFile, oldPass, newPass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isKeyExists() {
        return KeyFile.exists();
    }

    public static boolean loadKey(String pass) {
        try {
            Utils.key = decrypt(genSecKey(pass), read(KeyFile));
            return isKeyUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkPassKey(File key, String pass) {
        try {

            return decrypt(genSecKey(pass), read(key)) != null;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean checkPassKey(String pass) {
        try {

            return decrypt(genSecKey(pass), read(KeyFile)) != null;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isKeyUp() {
        return Utils.key != null;
    }

    public static void killKey() {
        Utils.key = null;
    }

    public static void showDialog(Context context, String title, String message, boolean cancel, String ok_button_text, final Runnable ok, String nok_button_text, final Runnable nok) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(message);
        builder1.setTitle(title);
        builder1.setCancelable(cancel);
        if (ok != null) {
            builder1.setPositiveButton(
                    ok_button_text,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ok.run();
                            dialog.cancel();
                        }
                    });
        }

        if (nok != null) {
            builder1.setNegativeButton(
                    nok_button_text,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            nok.run();
                            dialog.cancel();
                        }
                    });
        }


        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public static String gen(String imei, String dsn) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (imei == null | dsn == null) {
            return null;
        }
        if (imei.isEmpty() | dsn.isEmpty()) {
            return null;
        }
        if (imei.length() < 10 | dsn.length() < 5) {
            return null;
        }
        long result = 0;
        for (int i = 0; i < imei.length(); i++) {
            if (i + 1 < imei.length()) {
                result += ((int) imei.charAt(i) * (int) imei.charAt(i + 1));
            } else {
                result += (int) imei.charAt(i);
            }
        }
        for (int i = 0; i < dsn.length(); i++) {
            if (i + 1 < dsn.length()) {
                result += ((int) dsn.charAt(i) * (int) dsn.charAt(i + 1));
            } else {
                result += (int) dsn.charAt(i);
            }
        }
        if (result <= 0) {
            return null;
        }
        return SHA("" + result).substring(0, 16).toUpperCase();
    }


    public static void OnReceiveSMS(Context context, Intent intent, OnReceiveSMS receiveSMS) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] message = (Object[]) intentExtras.get("pdus");
            String smsBody = "";
            String address = "";
            for (int i = 0; i < message.length; i++) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) message[i]);
                smsBody = smsMessage.getMessageBody().toString();
                address = smsMessage.getOriginatingAddress();
            }

            receiveSMS.OnReceive(address, smsBody);
        }
    }

    public static interface OnReceiveSMS {
        public void OnReceive(String address, String body);
    }

    public static boolean hasPermission(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isEnc(String str) {
        str = str.trim();
        str = str.replaceAll(" ", "");
        if (str.length() <= 0) {
            return false;
        }
        try {
            byte[] bytes = Base64.decode(str, Base64.DEFAULT);
            if (bytes.length > 0) {
                byte[] b = new byte[4];
                System.arraycopy(bytes, 0, b, 0, b.length);
                return isMy(b);
            }
        } catch (Exception e) {
        }
        return false;
    }

//    public synchronized static void Log(Object o) {
//        Log.d("dbg", String.valueOf(o));
//    }


    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
