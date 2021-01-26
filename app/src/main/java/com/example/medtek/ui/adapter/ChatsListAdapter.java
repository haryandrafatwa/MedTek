package com.example.medtek.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medtek.R;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.model.state.ChatType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.medtek.BuildConfig.BASE_URL;
import static com.example.medtek.constant.APPConstant.LOGIN_PASIEN;
import static com.example.medtek.utils.PropertyUtil.USER_TYPE;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.Utils.changeDatePatternSlash;
import static com.example.medtek.utils.Utils.dateTimeToStringDate;
import static com.example.medtek.utils.Utils.dateTimeToStringHour;
import static com.example.medtek.utils.Utils.getDateTime;

public class ChatsListAdapter extends RecyclerView.Adapter<ChatsListAdapter.ViewHolder> implements Filterable {
    private static final String TAG = ChatsListAdapter.class.getSimpleName();

    private final Context mContext;
    private final boolean isActive;
    private final ArrayList<ChatsModel> mChatsList;
    private ArrayList<ChatsModel> mChatsListFull;
    private ChatsListAdapter.OnItemClickCallback onItemClickCallback;
    private final Filter chatsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ChatsModel> filteredList = new ArrayList<>();

            if (mChatsListFull != null) {
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(mChatsListFull);
                } else {
                    String filterText = constraint.toString().toLowerCase().trim();

                    for (ChatsModel model : mChatsListFull) {
                        if (model.getSenderName().toLowerCase().contains(filterText)) {
                            filteredList.add(model);
                        }
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mChatsList.clear();
            if (results.values != null) {
                mChatsList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        }
    };

    public ChatsListAdapter(Context mContext, boolean isActive) {
        this.mContext = mContext;
        mChatsList = new ArrayList<>();
        this.isActive = isActive;
    }

    public void addItem(ChatsModel item) {
        this.mChatsList.add(item);
        notifyItemInserted(this.mChatsList.size() - 1);
    }

    public void removeItem(int position) {
        this.mChatsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.mChatsList.size());
    }

    public void updateItem(ChatsModel item, int position) {
        this.mChatsList.set(position, item);
        notifyItemChanged(position, item);
    }

    public void setOnItemClickCallback(ChatsListAdapter.OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preview_chat, parent, false);
        return new ViewHolder(view);
    }

    public void setChatsList(ArrayList<ChatsModel> mChatsList) {
        if (mChatsList.size() > 0) {
            this.mChatsList.clear();
        }
        this.mChatsList.addAll(mChatsList);
        mChatsListFull = new ArrayList<>(mChatsList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mChatsList.size();
    }

    private int getUnseenChat(ChatsModel model) {
        int unseenChat = 0;
        for (ChatsModel.Chat chatItem : model.getChats()) {
            if (!chatItem.isRead()) {
                unseenChat++;
            }
        }
        return unseenChat;
    }

    public interface OnItemClickCallback {
        void onItemClick(ChatsModel model, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivPictureSender;
        RelativeLayout rlUnseenChat, optChat;
        TextView tvNameSender, tvLastUpdate, tvLastChat, tvUnseenChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            optChat = itemView.findViewById(R.id.opt_chat);
            ivPictureSender = itemView.findViewById(R.id.iv_chat_picture);
            rlUnseenChat = itemView.findViewById(R.id.rl_unseen_chat);
            tvNameSender = itemView.findViewById(R.id.tv_name_chat);
            tvLastUpdate = itemView.findViewById(R.id.tv_time_chat);
            tvLastChat = itemView.findViewById(R.id.tv_value_chat);
            tvUnseenChat = itemView.findViewById(R.id.tv_unseen_chat);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatsModel model = mChatsList.get(position);
        {
            holder.optChat.setOnClickListener(v -> {
                if (holder.getAdapterPosition() < mChatsList.size()) {
                    onItemClickCallback.onItemClick(model, holder.getAdapterPosition());
                }
            });
        }
        if (model.getChats().size() > 0) {
            ChatsModel.Chat chat = model.getChats().get(model.getChats().size() - 1);

            if (!isActive) {
            DateTime lastUpdate = getDateTime(chat.getTime(), DateTimeZone.UTC);

                String lastUpdateText = "";

                if (lastUpdate.isBeforeNow()) {
                    Log.d(TAG, "lastUpdate.toLocalDate(): " + lastUpdate.toLocalDate().toString());
                    Log.d(TAG, "new DateTime().toLocalDate(): " + new DateTime().toLocalDate().toString());

                    if (lastUpdate.toLocalDate().isEqual(new DateTime().toLocalDate())) {
                        lastUpdateText = dateTimeToStringHour(lastUpdate);
                    } else if (lastUpdate.toLocalDate().isEqual(new DateTime().toLocalDate().minusDays(1))) {
                        lastUpdateText = "KEMARIN";
                    } else {
                        lastUpdateText = changeDatePatternSlash(dateTimeToStringDate(lastUpdate));
                    }
                } else {
                    lastUpdateText = dateTimeToStringHour(lastUpdate);
                }
                Log.d(TAG, "Last Chat Date String: " + lastUpdateText);

                holder.tvLastUpdate.setText(lastUpdateText);
            } else {
                holder.tvLastUpdate.setText(chat.getTime());
            }

            String textLastChat = "";
            if (chat.getType() == ChatType.TEXT) {
                textLastChat = chat.getMessage();
            } else {
                textLastChat = chat.getType().toString();
            }
            holder.tvLastChat.setText(textLastChat);

            int unseenChat = getUnseenChat(model);

            if (unseenChat != 0) {
                holder.rlUnseenChat.setVisibility(View.VISIBLE);
                holder.tvUnseenChat.setText(String.valueOf(unseenChat));
            } else {
                holder.rlUnseenChat.setVisibility(View.GONE);
            }

        } else {
            holder.tvLastUpdate.setText(dateTimeToStringHour(new DateTime()));
            holder.tvLastChat.setText("");
        }

        if (model.getSenderAvatar().equals("")) {
            if ((int) getData(USER_TYPE) == LOGIN_PASIEN) {
                holder.ivPictureSender.setImageResource(R.drawable.ic_dokter_square);
            } else {
                holder.ivPictureSender.setImageResource(R.drawable.ic_pasien_square);
            }
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(BASE_URL + model.getSenderAvatar())
                    .into(holder.ivPictureSender);
        }

        holder.tvNameSender.setText(model.getSenderName());
    }

    @Override
    public Filter getFilter() {
        return chatsFilter;
    }
}

