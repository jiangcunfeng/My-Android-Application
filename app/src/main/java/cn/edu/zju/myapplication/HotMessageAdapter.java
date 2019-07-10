package cn.edu.zju.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HotMessageAdapter extends RecyclerView.Adapter<HotMessageAdapter.ViewHolder> {
    private List<HotMessage> mHotMessageList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView no;
        TextView title;
        TextView value;

        public ViewHolder(View view) {
            super(view);
            no = (TextView) view.findViewById(R.id.text_hot_no);
            title = (TextView) view.findViewById(R.id.text_hot_title);
            value = (TextView) view.findViewById(R.id.text_hot_value);
        }
    }

    public HotMessageAdapter(List<HotMessage> hotMessageList) {
        mHotMessageList = hotMessageList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hot_message_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HotMessage hot_msg = mHotMessageList.get(position);
        holder.no.setText(hot_msg.getNo());
        holder.title.setText(hot_msg.getTitle());
        holder.value.setText(hot_msg.getValue());
    }

    @Override
    public int getItemCount() {
        return mHotMessageList.size();
    }
}