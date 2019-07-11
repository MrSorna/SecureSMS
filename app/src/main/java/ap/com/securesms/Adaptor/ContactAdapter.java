package ap.com.securesms.Adaptor;

import android.content.Context;
import android.media.Image;
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

import ap.com.securesms.Model.Contact;
import ap.com.securesms.R;
import ap.com.securesms.Util.Settings;

/**
 * Created by Amirhosein on 11/25/2018.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> implements Filterable {
    private ArrayList<Contact> contacts;
    private ArrayList<Contact> contactsFiltered;
    private Context context;
    private Settings settings;
    private String name;
    private String phone;
    private boolean flag;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView image;
        public ImageView save;
        public TextView name;
        public TextView number;
        public View view;

        public MyViewHolder(View view) {
            super(view);
            this.view = view;
            this.image = view.findViewById(R.id.image);
            this.save = view.findViewById(R.id.save);
            this.name = view.findViewById(R.id.name);
            this.number = view.findViewById(R.id.number);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ContactAdapter(ArrayList<Contact> contacts, boolean flag, Context context) {
        this.contacts = contacts;
        this.contactsFiltered = contacts;
        this.flag = flag;
        this.context = context;
        settings = new Settings(context);
    }

    public void setContacts(ArrayList<Contact> contacts) {
        this.contacts = contacts;
        this.contactsFiltered = contacts;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ContactAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Contact contact = contactsFiltered.get(position);
        holder.image.setTextSize(settings.getFontSize() + 5);
        holder.name.setTextSize(settings.getFontSize());


        if (phone != null && !phone.isEmpty()) {
            String newString = contact.getPhone().replaceAll(phone, "<font color='red'>" + phone + "</font>");
            holder.number.setText(Html.fromHtml(newString));
        } else {
            holder.number.setText(contact.getPhone());
        }

        if (name != null && !name.isEmpty()) {
            String newString = contact.getName().replaceAll(name, "<font color='red'>" + name + "</font>");
            holder.name.setText(Html.fromHtml(newString));
        } else {
            holder.name.setText(contact.getName());
        }


        if (contact.getName().isEmpty()) {
            holder.image.setText(contact.getPhone().charAt(0) + "");
        } else {
            holder.image.setText(contact.getName().charAt(0) + "");
        }
        if (flag)
            if (contact.isSave()) {
                holder.save.setVisibility(View.VISIBLE);
            } else {
                holder.save.setVisibility(View.GONE);
            }
        else holder.save.setVisibility(View.GONE);

        if (contact.isSelect()) {
            holder.view.setBackground(context.getResources().getDrawable(R.drawable.list_item_selected));
        } else {
            holder.view.setBackgroundColor(context.getResources().getColor(R.color.background));
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return contactsFiltered.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Contact item);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                name = phone = "";
                if (charString.isEmpty()) {
                    contactsFiltered = contacts;
                } else {
                    ArrayList<Contact> filteredList = new ArrayList<>();
                    for (Contact row : contacts) {
                        if (row.getName().contains(charString) || row.getPhone().contains(charSequence)) {
                            filteredList.add(row);
                        }
                        if (row.getName().contains(charString)) {
                            name = charString;
                        } else if (row.getPhone().contains(charSequence)) {
                            phone = charString;
                        }
                    }
                    contactsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                contactsFiltered = (ArrayList<Contact>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}