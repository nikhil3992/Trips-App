package com.example.nikhil.group22_hw09;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Message> messages;
    private Context mContext;
    private String currentUid;
    static private OnItemClickListener listener;


    public ChatAdapter(List<Message> messages, Context mContext, String currentUid) {
        this.messages = messages;
        this.mContext = mContext;
        this.currentUid = currentUid;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public interface OnItemClickListener {
        void onInternalItemClick(int position, int id);
        void onRowClick(int position, int id);
        boolean onLongClick(int position, int id);
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getUid().equals(currentUid)) {
            return 1;
        }

        return 0;
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View day = null;
        if (viewType == 1) {
            day = inflater.inflate(R.layout.row_layout_right, parent, false);
        } else {
            day = inflater.inflate(R.layout.row_layout, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(day);
        return viewHolder;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView text;
        public TextView fullname;
        public TextView time;
        public ImageView imgMessage;
        public ImageView delete;
        public LinearLayout linearLayout;
        View itemView;


        public ViewHolder(View itemView) {

            super(itemView);
            text = (TextView) itemView.findViewById(R.id.textMessage);
            fullname = (TextView) itemView.findViewById(R.id.full);
            imgMessage = (ImageView) itemView.findViewById(R.id.imageMessage);
            time = (TextView) itemView.findViewById(R.id.time);
            delete = (ImageView) itemView.findViewById(R.id.delete);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.relativeLayout);
            this.itemView = itemView;
        }
    }

    @Override
    public void onBindViewHolder(ChatAdapter.ViewHolder holder, final int position) {
        Message message = messages.get(position);
        View itemView = holder.itemView;
        if(!message.getImageUrl().equals(""))
        {
            ImageView imgMessage = holder.imgMessage;
            Picasso.with(mContext).load(message.getImageUrl()).into(imgMessage);
        }
        else
        {
            TextView text = holder.text;
            text.setText(message.getText());
        }
        ImageView delete = holder.delete;
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onInternalItemClick(position, view.getId());
            }
        });
        if(message.getUid().equals(currentUid)) {
            holder.linearLayout.setGravity(Gravity.END);

        }
        TextView fullname = holder.fullname;
        fullname.setText(message.getFullName());
        TextView time = holder.time;
        PrettyTime p = new PrettyTime();
        Date d = new Date(message.getDate());
        time.setText(p.format(d));

    }


    @Override
    public int getItemCount() {
        return messages.size();
    }
}
