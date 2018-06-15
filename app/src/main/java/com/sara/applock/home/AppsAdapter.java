package com.sara.applock.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.sara.applock.R;

import java.util.List;

/**
 * Created by Hariharan on 09-06-2017.
 */

class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {

    private ItemClickListener clickListener;
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public ImageView icon;
        TextView appName;
        CheckBox checkBox;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        ViewHolder(final View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            appName = (TextView) itemView.findViewById(R.id.app_name);
            checkBox =(CheckBox) itemView.findViewById(R.id.checkbox);
            itemView.setTag(checkBox);
            checkBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

    private List<App> mApps;
    // Store the context for easy access
    private Context mContext;
    private DatabaseHelper dbHelper;

    // Pass in the contact array into the constructor
    public AppsAdapter(Context context, List<App> apps) {
        mApps = apps;
        mContext = context;
        dbHelper=DatabaseHelper.getInstance(context);
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View appsView = inflater.inflate(R.layout.apps_row, parent, false);
        // Return a new holder instance
        return new ViewHolder(appsView);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        App app = mApps.get(position);
        // Set item views based on your views and data model
        ImageView icon = holder.icon;
        icon.setImageDrawable(app.getIcon());
        TextView textView = holder.appName;
        textView.setText(app.getAppName());
        CheckBox checkBox=holder.checkBox;
        checkBox.setChecked(app.isChecked());
    }

    @Override
    public int getItemCount() {
        return mApps.size();
    }
}
