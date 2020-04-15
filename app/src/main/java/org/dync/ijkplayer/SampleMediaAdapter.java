package org.dync.ijkplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by KathLine on 2017/10/13.
 */

public class SampleMediaAdapter extends RecyclerView.Adapter<SampleMediaAdapter.ViewHolder> {
    private Context context;
    private ArrayList<SampleMediaItem> mDatas;

    final class SampleMediaItem {
        String mUrl;
        String mName;

        public SampleMediaItem(String url, String name) {
            mUrl = url;
            mName = name;
        }
    }

    public SampleMediaAdapter(Context context) {
        this.context = context;
        mDatas = new ArrayList<>();
    }

    public void addItem(String url, String name) {
        mDatas.add(new SampleMediaItem(url, name));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final SampleMediaItem item = mDatas.get(position);
        holder.mNameTextView.setText(item.mName);
        holder.mUrlTextView.setText(item.mUrl);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null) {
                    onItemClickListener.OnItemClick(v, item, position);
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mNameTextView;
        private TextView mUrlTextView;

        ViewHolder(final View itemView){
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(android.R.id.text1);
            mUrlTextView = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }

    interface OnItemClickListener {
        void OnItemClick(View view, SampleMediaItem item, int position);
    }

    OnItemClickListener onItemClickListener;

    void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }
}