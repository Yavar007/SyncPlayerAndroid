package com.yavar007.syncplayer.misc;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yavar007.syncplayer.R;
import com.yavar007.syncplayer.models.UsersInRoomModel;

import java.util.List;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.UsersViewHolder> {

    private List<UsersInRoomModel> users;
    private Context context;
    public UsersListAdapter(Context context,List<UsersInRoomModel> users){
        this.users=users;
        this.context=context;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_list_view, parent, false);
        return new UsersViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        UsersInRoomModel user=users.get(position);
        String OSName=user.getOSName().toLowerCase();
        if (OSName.contains("windows")){
            holder.imgDeviceIcon.setImageResource(R.drawable.windows);
        } else if (OSName.contains("android")) {
            holder.imgDeviceIcon.setImageResource(R.drawable.android);
        }
        else if (OSName.contains("linux")){
            holder.imgDeviceIcon.setImageResource(R.drawable.linux);
        } else if (OSName.contains("mac")) {
            holder.imgDeviceIcon.setImageResource(R.drawable.macos);
        }


        holder.roomIndicator.setBackgroundColor(
                user.getClientRole().equals("server")?Color.rgb(255,215,0):Color.rgb(142,142,142));
        holder.userStatus.setBackgroundColor(
                user.isOnline()?Color.rgb(26,255,0):Color.rgb(255,0,0));
        holder.txtUserName.setText(user.getNickName());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
    // Custom method to update data
    public void updateData(List<UsersInRoomModel> newUsersList) {
        users.clear();
        users.addAll(newUsersList);
        notifyDataSetChanged();  // Notify the adapter that data has changed
    }
    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        ImageView imgDeviceIcon;
        View roomIndicator;
        View userStatus;
        TextView txtUserName;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDeviceIcon=itemView.findViewById(R.id.imgDeviceIcon);
            roomIndicator=itemView.findViewById(R.id.roomIndicator);
            userStatus=itemView.findViewById(R.id.userStatus);
            txtUserName=itemView.findViewById(R.id.txtUserName);

        }
    }
}
