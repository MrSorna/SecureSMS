package ap.com.securesms.Adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ap.com.securesms.Model.Contact;
import ap.com.securesms.R;
import ap.com.securesms.Services.SMS;
import ap.com.securesms.Util.Settings;

/**
 * Created by Amirhosein on 11/25/2018.
 */

public class SendStatusAdapter extends RecyclerView.Adapter<SendStatusAdapter.MyViewHolder> {
    private ArrayList<SMS.SMS_Item> smsItems = new ArrayList<>();
    private Context context;
    private Settings settings;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView image;
        public TextView name;
        public TextView number;
        public View view;


        public MyViewHolder(View view) {
            super(view);
            this.view = view;
            this.image = view.findViewById(R.id.image);
            this.name = view.findViewById(R.id.name);
            this.number = view.findViewById(R.id.number);
        }
    }

    public SendStatusAdapter(Context context) {
        this.context = context;
        settings = new Settings(context);
    }

    public void setSmsItems(ArrayList<SMS.SMS_Item> smsItems) {
        this.smsItems = smsItems;
        notifyDataSetChanged();
    }

    @Override
    public SendStatusAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item_no_flag, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SMS.SMS_Item item = smsItems.get(position);
        Contact contact = item.getContact();
        holder.name.setText(contact.getName());
        holder.number.setText(contact.getPhone());
        holder.name.setTextSize(settings.getFontSize());
        holder.number.setTextSize(settings.getFontSize());
        holder.image.setTextSize(settings.getFontSize() + 5);
        if (contact.getName().isEmpty()) {
            holder.image.setText(contact.getPhone().charAt(0) + "");
        } else {
            holder.image.setText(contact.getName().charAt(0) + "");
        }
        if (item.isDeliver()) {
            holder.view.setBackground(context.getResources().getDrawable(R.drawable.list_item_deliver));
        } else if (item.isSent()) {
            holder.view.setBackground(context.getResources().getDrawable(R.drawable.list_item_sent));
        }
    }

    @Override
    public int getItemCount() {
        return smsItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Contact item);
    }

    public void clear() {
        smsItems.clear();
        notifyDataSetChanged();
    }

}