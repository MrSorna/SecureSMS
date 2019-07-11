package ap.com.securesms.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ap.com.securesms.R;
import ap.com.securesms.Util.Utils;

public class ActivationFragment extends Fragment {
    private String sn, imei, currentActivation;
    private OnActivationListener activationListener;
    private static final String SERIAL_NUMBER = "sn";
    private static final String IMEI = "imei";
    private static final String CURRENT_ACTIVATION_KEY = "currentActivation";

    public static ActivationFragment newInstance(String currentActivation, String imei, String sn) {
        ActivationFragment activationFragment = new ActivationFragment();
        Bundle args = new Bundle();
        args.putString(SERIAL_NUMBER, sn);
        args.putString(IMEI, imei);
        args.putString(CURRENT_ACTIVATION_KEY, currentActivation);
        activationFragment.setArguments(args);

        return activationFragment;
    }

    public interface OnActivationListener {
        void success(String key);

        void failed();
    }

    public ActivationFragment setActivationListener(OnActivationListener activationListener) {
        this.activationListener = activationListener;
        return this;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            sn = getArguments().getString(SERIAL_NUMBER, "");
            imei = getArguments().getString(IMEI, "");
            currentActivation = getArguments().getString(CURRENT_ACTIVATION_KEY, "");
        } catch (Exception e) {
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View retView = inflater.inflate(R.layout.activation_fragment, container, false);
        ((TextView) retView.findViewById(R.id.imei)).setText("IMEI: " + imei);
        ((TextView) retView.findViewById(R.id.sn)).setText("SN: " + sn);
        (retView.findViewById(R.id.active)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String activation = ((EditText) retView.findViewById(R.id.et_activation)).getText().toString();
                if (currentActivation != null && currentActivation.equals(activation)) {
                    if (activationListener != null) activationListener.success(activation);
                } else {
                    if (activationListener != null) activationListener.failed();
                }
            }
        });
        return retView;
    }


}
