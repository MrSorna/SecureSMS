package ap.com.securesms.Adaptor;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import ap.com.securesms.Activity.DBCActivity;
import ap.com.securesms.Model.Message;
import ap.com.securesms.R;
import ap.com.securesms.Util.Settings;


public class DBCAdapter extends RecyclerView.Adapter<DBCAdapter.ViewHolder> implements Filterable {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<Message> messagesFiltered = new ArrayList<>();
    private Settings settings;
    private DBCActivity dbcActivity;
    private String content;

    public DBCAdapter(DBCActivity dbcActivity) {
        this.dbcActivity = dbcActivity;
        this.settings = new Settings(dbcActivity);
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
                    .inflate(R.layout.message_sent, parent, false);
            return new ViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_received, parent, false);
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

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Message message = messagesFiltered.get(position);
        if (content != null && !content.isEmpty()) {
            String newString = message.getMessage().replaceAll(content, "<font color='red'>" + content + "</font>");
            holder.message.setText(Html.fromHtml(newString));
        } else {
            holder.message.setText(message.getMessage());
        }
        holder.time.setText(message.getDate());
        holder.message.setTextSize(settings.getFontSize());
        holder.time.setTextSize(settings.getFontSize() - 5);
        if (message.isSave()) {
            holder.save.setImageResource(R.drawable.save);
        } else {
            holder.save.setImageResource(R.drawable.nosave);
        }


        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbcActivity.delete(position);
            }
        });

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
        public final ImageView save;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            message = view.findViewById(R.id.message);
            time = view.findViewById(R.id.time);
            status = view.findViewById(R.id.status);
            save = view.findViewById(R.id.save);
        }
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                content = "";

                if (charString.isEmpty()) {
                    messagesFiltered = messages;
                } else {
                    ArrayList<Message> filteredList = new ArrayList<>();
                    for (Message row : messages) {
                        if (row.getMessage().contains(charString)) {
                            filteredList.add(row);
                        }
                        if (row.getMessage().contains(charSequence)) {
                            content = charString;
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
