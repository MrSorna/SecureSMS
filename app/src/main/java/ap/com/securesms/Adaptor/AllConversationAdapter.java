package ap.com.securesms.Adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import ap.com.securesms.Model.Message;
import ap.com.securesms.R;
import ap.com.securesms.Util.Settings;


public class AllConversationAdapter extends RecyclerView.Adapter<AllConversationAdapter.ViewHolder> implements Filterable {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private String content;
    private String name;
    private String phone;
    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<Message> messagesFiltered = new ArrayList<>();
    private Settings settings;

    public AllConversationAdapter(Context context) {
        this.settings = new Settings(context);
    }

    public void setValues(ArrayList<Message> messages) {
        this.messages = messages;
        this.messagesFiltered = messages;
        Collections.reverse(this.messagesFiltered);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_message_sent, parent, false);
            return new ViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_message_received, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messagesFiltered.get(position);
        if (message.isInbox()) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    public Message getItem(int position) {
        return messagesFiltered.get(position);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Message message = getItem(position);
        if (content != null && !content.isEmpty()) {
            String newString = message.getMessage().replaceAll(content, "<font color='red'>" + content + "</font>");
            holder.message.setText(Html.fromHtml(newString));
        } else {
            holder.message.setText(message.getMessage());
        }
        if (phone != null && !phone.isEmpty()) {
            String newString = message.getContact().getPhone().replaceAll(phone, "<font color='red'>" + phone + "</font>");
            holder.contact.setText(Html.fromHtml(newString + " - " + message.getContact().getName()));
        } else if (name != null && !name.isEmpty()) {
            String newString = message.getContact().getName().replaceAll(name, "<font color='red'>" + name + "</font>");
            holder.contact.setText(Html.fromHtml(newString));
        } else {
            holder.contact.setText(message.getContact().getName());
        }

        holder.time.setText(message.getDate());
        holder.message.setTextSize(settings.getFontSize());
        holder.time.setTextSize(settings.getFontSize() - 5);
        if (message.isInbox()) {
            holder.status.setTextSize(settings.getFontSize() - 5);
            switch (message.getType()) {
                case SENT:
                    holder.status.setText("ارسال شد");
                    break;
                case FAILED:
                    holder.status.setText("ارسال ناموفق");
                    break;
                case QUEUED:
                    holder.status.setText("درحال ارسال");
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return messagesFiltered.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView message;
        public final TextView time;
        public final TextView status;
        public final TextView contact;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            message = view.findViewById(R.id.message);
            time = view.findViewById(R.id.time);
            status = view.findViewById(R.id.status);
            contact = view.findViewById(R.id.contact);
        }
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                content = name = phone = "";
                if (charString.isEmpty()) {
                    messagesFiltered = messages;
                } else {
                    ArrayList<Message> filteredList = new ArrayList<>();
                    for (Message row : messages) {
                        if (row.getMessage().contains(charSequence) || row.getContact().getName().contains(charString) || row.getContact().getPhone().contains(charSequence)) {
                            filteredList.add(row);
                        }
                        if (row.getMessage().contains(charSequence)) {
                            content = charString;
                        } else if (row.getContact().getName().contains(charString)) {
                            name = charString;
                        } else if (row.getContact().getPhone().contains(charSequence)) {
                            phone = charString;
                        }
                    }
                    messagesFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = messagesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                messagesFiltered = (ArrayList<Message>) results.values;
                notifyDataSetChanged();
            }
        };
    }


}
