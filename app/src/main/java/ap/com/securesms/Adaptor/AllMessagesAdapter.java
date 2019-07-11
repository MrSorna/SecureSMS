package ap.com.securesms.Adaptor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ap.com.securesms.Model.Contact;
import ap.com.securesms.Model.MessageItem;
import ap.com.securesms.R;
import ap.com.securesms.Util.Settings;


public class AllMessagesAdapter extends RecyclerView.Adapter<AllMessagesAdapter.ViewHolder> implements Filterable{

    private ArrayList<MessageItem> items = new ArrayList<>();
    private ArrayList<MessageItem> itemsFiltered  = new ArrayList<>();

    public void setValues(ArrayList<MessageItem> items) {
        this.items = items;
        this.itemsFiltered = items;
    }

    public AllMessagesAdapter(Settings settings) {
        this.settings = settings;
    }

    private Settings settings;

    public MessageItem getItem(int position) {
        return itemsFiltered.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MessageItem item = getItem(position);
        if (item.getContact().getName().isEmpty()) {
            holder.name.setText(item.getContact().getPhone());
            holder.list_image.setText(item.getContact().getPhone().charAt(0) + "");
        } else {
            holder.name.setText(item.getContact().getName());
            holder.list_image.setText(item.getContact().getName().charAt(0) + "");
        }
        holder.latest_message.setText(item.getLatestMessage());
        holder.time.setText(item.getDate());

        holder.name.setTextSize(settings.getFontSize());
        holder.list_image.setTextSize(settings.getFontSize() + 5);
        holder.latest_message.setTextSize(settings.getFontSize());
        holder.time.setTextSize(settings.getFontSize() - 5);

    }

    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView name;
        public final TextView list_image;
        public final TextView latest_message;
        public final TextView time;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            name = (TextView) view.findViewById(R.id.name);
            list_image = (TextView) view.findViewById(R.id.list_image);
            latest_message = (TextView) view.findViewById(R.id.latest_message);
            time = (TextView) view.findViewById(R.id.time);
        }

    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    itemsFiltered = items;
                } else {
                    ArrayList<MessageItem> filteredList = new ArrayList<>();
                    for (MessageItem row : items) {

                        if (row.getLatestMessage().contains(charSequence) ||  row.getContact().getName().contains(charString) || row.getContact().getPhone().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }
                    itemsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = itemsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                itemsFiltered = (ArrayList<MessageItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
