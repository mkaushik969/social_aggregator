package com.googlelogin.manas.googlelogin;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.NewsFeedHolder> {

    Context context;
    List<NewsFeedItem> list;

    public NewsFeedAdapter(Context context, List<NewsFeedItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public NewsFeedHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.newsfeeditem_layout,parent,false);
        return new NewsFeedHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsFeedHolder holder, int position) {
        final NewsFeedItem NewsFeedItem=list.get(position);
        holder.t1.setText(NewsFeedItem.getName());
        holder.t2.setText(NewsFeedItem.getContent());
        holder.imageView.setImageDrawable(context.getResources().getDrawable(NewsFeedItem.getImageId()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class NewsFeedHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        TextView t1,t2;

        public NewsFeedHolder(View itemView) {
            super(itemView);

            imageView=(ImageView) itemView.findViewById(R.id.nfi_iv);
            t1=(TextView)itemView.findViewById(R.id.nfi_name);
            t2=(TextView)itemView.findViewById(R.id.nfi_content);
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<NewsFeedItem> list) {
        list.addAll(list);
        notifyDataSetChanged();
    }
}
