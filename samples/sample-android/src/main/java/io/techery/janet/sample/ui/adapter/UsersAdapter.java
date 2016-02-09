package io.techery.janet.sample.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.techery.janet.sample.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.techery.janet.sample.model.User;

public class UsersAdapter extends RecyclerViewAdapter<UsersAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final Context context;
    private List<User> users = new ArrayList<>();

    public UsersAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(inflater.inflate(R.layout.user_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = users.get(position);
        holder.name.setText(user.getLogin());
        holder.url.setText(user.getUrl());
        Picasso.with(context)
                .load(user.getAvatar())
                .placeholder(R.drawable.avatar_placeholder)
                .into(holder.photo);
    }

    public void setData(List<User> data) {
        users = data;
        notifyDataSetChanged();
    }

    public User getItem(int position) {
        return users.get(position);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.photo)
        public ImageView photo;
        @Bind(R.id.name)
        public TextView name;
        @Bind(R.id.url)
        public TextView url;

        public ViewHolder(View item) {
            super(item);
            ButterKnife.bind(this, item);
        }
    }

}
