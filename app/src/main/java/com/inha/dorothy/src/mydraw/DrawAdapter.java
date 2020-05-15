package com.inha.dorothy.src.mydraw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inha.dorothy.R;
import com.inha.dorothy.src.entrance.Room;

import java.util.ArrayList;

public class DrawAdapter extends RecyclerView.Adapter<DrawAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<MyDraw> mDrawsList;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public DrawAdapter(Context context, ArrayList<MyDraw> myDraws) {
        this.mContext = context;
        this.mDrawsList = myDraws;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivMyDraw;
        ImageView ivCheck;

        ViewHolder(final View itemView) {
            super(itemView);
            // 뷰 객체에 대한 참조. (hold strong reference)
            ivMyDraw = itemView.findViewById(R.id.iv_item_my_draw);
            ivCheck = itemView.findViewById(R.id.iv_item_my_draw_check);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.onItemClick(v, pos);
                        }
                    }
                }
            });

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_my_draw, parent, false);
        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyDraw draw = mDrawsList.get(position);
        Glide.with(mContext).load(draw.info.url).centerCrop().placeholder(R.drawable.ic_loading).into(holder.ivMyDraw);

        if(draw.isCheck){
            holder.ivCheck.setVisibility(View.VISIBLE);
        }else{
            holder.ivCheck.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mDrawsList.size();
    }


}
