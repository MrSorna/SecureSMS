package ap.com.securesms.Dialog;


/**
 * Created by H on 11/12/2017.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import ap.com.securesms.Fragment.ContactsFragment;
import ap.com.securesms.Model.Contact;
import ap.com.securesms.R;
import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;


/**
 * Simple fragment with blur effect behind.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DialogSetContact extends BlurDialogFragment {

    public static enum Type {
        ADD, UPDATE
    }

    /**
     * Bundle key used to start the blur dialog with a given scale factor (float).
     */
    private static final String BUNDLE_KEY_DOWN_SCALE_FACTOR = "bundle_key_down_scale_factor";

    /**
     * Bundle key used to start the blur dialog with a given blur radius (int).
     */
    private static final String BUNDLE_KEY_BLUR_RADIUS = "bundle_key_blur_radius";

    /**
     * Bundle key used to start the blur dialog with a given dimming effect policy.
     */
    private static final String BUNDLE_KEY_DIMMING = "bundle_key_dimming_effect";

    /**
     * Bundle key used to start the blur dialog with a given debug policy.
     */
    private static final String BUNDLE_KEY_DEBUG = "bundle_key_debug_effect";

    private int mRadius;
    private float mDownScaleFactor;
    private boolean mDimming;
    private boolean mDebug;
    private Contact contact;
    private static ContactsFragment activity;
    private static Type type = Type.ADD;

    /**
     * Retrieve a new instance of the sample fragment.
     *
     * @param radius          blur radius.
     * @param downScaleFactor down scale factor.
     * @param dimming         dimming effect.
     * @param debug           debug policy.
     * @return well instantiated fragment.
     */
    public static DialogSetContact newInstance(ContactsFragment activity, Type type, int radius,
                                               float downScaleFactor,
                                               boolean dimming,
                                               boolean debug) {
        DialogSetContact.activity = activity;
        DialogSetContact.type = type;
        DialogSetContact fragment = new DialogSetContact();
        Bundle args = new Bundle();
        args.putInt(
                BUNDLE_KEY_BLUR_RADIUS,
                radius
        );
        args.putFloat(
                BUNDLE_KEY_DOWN_SCALE_FACTOR,
                downScaleFactor
        );
        args.putBoolean(
                BUNDLE_KEY_DIMMING,
                dimming
        );
        args.putBoolean(
                BUNDLE_KEY_DEBUG,
                debug
        );

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Bundle args = getArguments();
        mRadius = args.getInt(BUNDLE_KEY_BLUR_RADIUS);
        mDownScaleFactor = args.getFloat(BUNDLE_KEY_DOWN_SCALE_FACTOR);
        mDimming = args.getBoolean(BUNDLE_KEY_DIMMING);
        mDebug = args.getBoolean(BUNDLE_KEY_DEBUG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog builder = new Dialog(getActivity());
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_set_contact, null);
        view.setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Button button = ((Button) view.findViewById(R.id.button));
        final EditText name = ((EditText) view.findViewById(R.id.name));
        final EditText phone = ((EditText) view.findViewById(R.id.phone));
        if (type == Type.UPDATE) {
            button.setText("ذخیره");
            if (contact != null) {
                name.setText(contact.getName());
                phone.setText(contact.getPhone());
            }
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == Type.ADD) {
                    activity.saveContact(name.getText().toString().trim(), phone.getText().toString().trim());
                } else {
                    activity.saveContact(name.getText().toString().trim(), phone.getText().toString().trim());
                }
                dismiss();
            }
        });

        builder.setContentView(view);
        builder.getWindow().getAttributes().windowAnimations = R.style.PopupWindow;
        return builder;
    }


    public void setContact(Contact contact) {
        this.contact = contact;
    }

    @Override
    protected boolean isDebugEnable() {
        return mDebug;
    }

    @Override
    protected boolean isDimmingEnable() {
        return mDimming;
    }

    @Override
    protected boolean isActionBarBlurred() {
        return true;
    }

    @Override
    protected float getDownScaleFactor() {
        return mDownScaleFactor;
    }

    @Override
    protected int getBlurRadius() {
        return mRadius;
    }


}
