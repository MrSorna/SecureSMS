package ap.com.securesms.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;

import ap.com.securesms.R;
import ap.com.securesms.Util.Settings;
import ap.com.securesms.Util.Utils;

public class SetPasswordFragment extends Fragment {
    private OnSetKeyListener setKeyListener;
    private static final String TYPE = "type";
    private File key;
    private int type;
    private static final int PICKFILE_REQUEST_CODE = 1;
    private Settings settings;

    public static final int TYPE_NEW_KEY = 1;
    public static final int TYPE_CHANGE_KEY = 2;


    public static SetPasswordFragment newInstance(Settings settings, int type) {
        SetPasswordFragment myFragment = new SetPasswordFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        myFragment.setArguments(args);
        myFragment.setSettings(settings);
        return myFragment;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            type = getArguments().getInt(TYPE, TYPE_CHANGE_KEY);
        } catch (Exception e) {
        }
    }

    public static interface OnSetKeyListener {
        void success(int type);
    }

    public SetPasswordFragment setKeyListener(OnSetKeyListener setKeyListener) {
        this.setKeyListener = setKeyListener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View retView = inflater.inflate(R.layout.set_password_fragment, container, false);
        Button bt_selectKey = retView.findViewById(R.id.select_key);
        final EditText et_keyPassword = retView.findViewById(R.id.key_password);
        if (type == TYPE_NEW_KEY) {
            bt_selectKey.setVisibility(View.VISIBLE);
            bt_selectKey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    startActivityForResult(intent, PICKFILE_REQUEST_CODE);
                }
            });
        } else {
            bt_selectKey.setVisibility(View.GONE);
        }
        (retView.findViewById(R.id.set)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(getActivity())
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                try {
                                    String key_password = et_keyPassword.getText().toString();
                                    String password = ((EditText) retView.findViewById(R.id.password)).getText().toString();
                                    String confirm_password = ((EditText) retView.findViewById(R.id.confirm_password)).getText().toString();
                                    if (key_password.isEmpty()) {
                                        Utils.toast(getContext(), "رمز کلید را وارد کنید");
                                        return;
                                    }
                                    if (password.isEmpty()) {
                                        Utils.toast(getContext(), "رمزعبور جدید را وارد کنید");
                                        return;
                                    }
                                    if (confirm_password.isEmpty()) {
                                        Utils.toast(getContext(), "رمزعبور جدید را مجددا وارد کنید");
                                        return;
                                    }
                                    if (!confirm_password.equals(password)) {
                                        Utils.toast(getContext(), "رمزعبور جدید یکسان نیست");
                                        return;
                                    }
                                    if (type == TYPE_NEW_KEY) {
                                        if (key == null) {
                                            Utils.toast(getContext(), "لطفا کلید را انتخاب کنید");
                                            return;
                                        }
                                        if (!key.exists() | !key.isFile()) {
                                            Utils.toast(getContext(), "کلید یافت نشد");
                                            return;
                                        }
                                        if (Utils.checkPassKey(key, key_password)) {
                                            if (Utils.copyKey(key, key_password, password)) {
                                                if (setKeyListener != null)
                                                    setKeyListener.success(type);
                                            } else {
                                                Utils.toast(getContext(), "خطا در ذخیره سازی رمزعبور");
                                            }
                                        } else {
                                            Utils.toast(getContext(), "رمزعبور کلید اشتباه است");
                                        }
                                    } else {
                                        if (Utils.checkPassKey(key_password)) {
                                            if (Utils.changeKeyPass(key_password, password)) {
                                                if (setKeyListener != null)
                                                    setKeyListener.success(type);
                                            } else {
                                                Utils.toast(getContext(), "خطا در ذخیره سازی رمزعبور");
                                            }
                                        } else {
                                            Utils.toast(getContext(), "رمزعبور کلید اشتباه است");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                Utils.toast(getContext(), "مجوز نوشتن درون فایل را ندارید");
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });
        return retView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null)
                    key = new File(uri.getPath());
            }
        }
    }

}
