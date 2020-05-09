package com.inha.dorothy.src.entrance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inha.dorothy.R;

import java.util.ArrayList;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Room> mRoomList;

    public RoomAdapter(Context context, ArrayList<Room> roomList){
        this.mContext = context;
        this.mRoomList = roomList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvRoomTitle;
        TextView tvRoomPerson;

        ViewHolder(final View itemView) {
            super(itemView);
            // 뷰 객체에 대한 참조. (hold strong reference)
            tvRoomTitle = itemView.findViewById(R.id.tv_item_entrance_room_title);
            tvRoomPerson = itemView.findViewById(R.id.tv_item_entrance_room_person);

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_entrance, parent, false);
        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Room roomItem = mRoomList.get(position);
        holder.tvRoomTitle.setText(roomItem.info.title);
        holder.tvRoomPerson.setText(String.valueOf(roomItem.info.person).concat(mContext.getString(R.string.entrance_person_limit)));
    }

    @Override
    public int getItemCount() {
        return mRoomList.size();
    }



}
