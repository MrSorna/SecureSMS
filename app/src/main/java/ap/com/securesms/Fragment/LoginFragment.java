package ap.com.securesms.Fragment;

import android.Manifest;
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

import ap.com.securesms.R;
import ap.com.securesms.Util.Utils;

public class LoginFragment extends Fragment {
    private OnLoginListener loginListener;

    public static LoginFragment newInstance() {
        LoginFragment loginFragment = new LoginFragment();
        return loginFragment;
    }

    public interface OnLoginListener {
        void success();
    }

    public LoginFragment setLoginListener(OnLoginListener loginListener) {
        this.loginListener = loginListener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View retView = inflater.inflate(R.layout.login_fragment, container, false);
        (retView.findViewById(R.id.login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(getActivity())
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                try {
                                    String enter_password = ((EditText) retView.findViewById(R.id.password)).getText().toString();
                                    if (Utils.loadKey(enter_password)) {
                                        if (loginListener != null)
                                            loginListener.success();
                                    } else {
                                        Utils.toast(getContext(), "رمزعبور اشتباه است");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                Utils.toast(getContext(), "مجوز خواندن فایل را ندارید");
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
}
