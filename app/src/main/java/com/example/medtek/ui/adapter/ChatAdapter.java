package com.example.medtek.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.medtek.R;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.model.UserModel;
import com.example.medtek.model.state.ChatType;
import com.example.medtek.ui.activity.ChatRoomActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.example.medtek.utils.PropertyUtil.DATA_USER;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.Utils.dateTimeToStringHour;
import static com.example.medtek.utils.Utils.getDateTime;
import static com.example.medtek.utils.Utils.getDurationVideo;
import static com.example.medtek.utils.Utils.getFileInfo;
import static com.example.medtek.utils.Utils.getFileName;
import static com.example.medtek.utils.Utils.getPath;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>  {

    public static final int MSG_TEXT_LEFT = 100;
    public static final int MSG_TEXT_RIGHT = 101;
    public static final int MSG_IMAGE_LEFT = 200;
    public static final int MSG_IMAGE_RIGHT = 201;
    public static final int MSG_VIDEO_LEFT = 300;
    public static final int MSG_VIDEO_RIGHT = 301;
    public static final int MSG_FILE_LEFT = 400;
    public static final int MSG_FILE_RIGHT = 401;
    public static final int MSG_END = 500;
    private static final String TAG = ChatAdapter.class.getSimpleName();
    private final Context mContext;
    private final Activity activity;
    private final ArrayList<ChatsModel.Chat> mChatList;
    private ChatAdapter.OnItemClickCallback onItemClickCallback;
    private final boolean isActiveChat;


    public ChatAdapter(Context mContext, Activity activity, boolean isActiveChat) {
        this.mContext = mContext;
        this.activity = activity;
        this.isActiveChat = isActiveChat;
        mChatList = new ArrayList<>();
    }

    public void setChatList(ArrayList<ChatsModel.Chat> mChatList) {
        if (mChatList.size() > 0) {
            this.mChatList.clear();
        }
        this.mChatList.addAll(mChatList);
        notifyDataSetChanged();
    }


    public void addItem(ChatsModel.Chat item) {
        this.mChatList.add(item);
        notifyItemInserted(this.mChatList.size() - 1);
    }

    public void removeItem(int position) {
        this.mChatList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.mChatList.size());
    }

    public void updateItem(int position, ChatsModel.Chat item) {
        this.mChatList.set(position, item);
        notifyItemChanged(position, item);
    }

    public void setOnItemClickCallback(ChatAdapter.OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case MSG_TEXT_LEFT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_text_left, parent, false);
                return new ViewHolder(view);
            case MSG_IMAGE_LEFT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_image_left, parent, false);
                return new ViewHolder(view);
            case MSG_IMAGE_RIGHT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_image_right, parent, false);
                return new ViewHolder(view);
            case MSG_VIDEO_LEFT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_video_left, parent, false);
                return new ViewHolder(view);
            case MSG_VIDEO_RIGHT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_video_right, parent, false);
                return new ViewHolder(view);
            case MSG_FILE_LEFT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_file_left, parent, false);
                return new ViewHolder(view);
            case MSG_FILE_RIGHT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_file_right, parent, false);
                return new ViewHolder(view);
            case MSG_END:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_end, parent, false);
                return new ViewHolder(view);
            case MSG_TEXT_RIGHT:
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_text_right, parent, false);
                return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatsModel.Chat chat = mChatList.get(position);

        boolean isFileExist = false;
        String pathFile = chat.getMessage();
        String time = chat.getTime();

        if (!isActiveChat) {
            DateTime lastUpdate = getDateTime(chat.getTime(), DateTimeZone.UTC);
            time = dateTimeToStringHour(lastUpdate);

            if (chat.getType() != ChatType.TEXT && chat.getType() != ChatType.END) {
                File checkFile = new File(getPath(chat.getType()) +
                        getFileName(chat.getMessage()));

                if (checkFile.isFile()) {
                    isFileExist = checkFile.isFile();
                    if (chat.getType() == ChatType.FILE) {
                        pathFile = getFileInfo(checkFile);
                    } else {
                        pathFile = checkFile.getPath();
                    }
                }
            }
        }

        {
            boolean finalIsFileExist = isFileExist;
            switch (chat.getType()) {
                case TEXT:
                    holder.tvChatText.setOnClickListener(v -> {
                        if (holder.getAdapterPosition() < mChatList.size()) {
                            onItemClickCallback.onItemTextClick(chat, holder.tvChatText, holder.getAdapterPosition(), finalIsFileExist);
                        }
                    });
                    break;
                case IMAGE:
                case VIDEO:
                    holder.ivChatImage.setOnClickListener(v -> {
                        if (holder.getAdapterPosition() < mChatList.size()) {
                            if (chat.getType() == ChatType.IMAGE) {
                                onItemClickCallback.onItemImageClick(chat, holder.ivChatImage, holder.pbLoading, holder.llDownloadImage, holder.getAdapterPosition(), finalIsFileExist);
                            } else {
                                onItemClickCallback.onItemVideoClick(chat, holder.ivChatImage, holder.pbLoading, holder.llDownloadVideo, holder.llDownloadVideo, holder.getAdapterPosition(), finalIsFileExist);
                            }
                        }
                    });
                    break;
                case FILE:
                    holder.llBgChat.setOnClickListener(v -> {
                        if (holder.getAdapterPosition() < mChatList.size()) {
                            onItemClickCallback.onItemFileClick(chat, holder.llBgChat, holder.pbLoading, holder.ivDownloadFile, holder.tvFileExt, holder.getAdapterPosition(), finalIsFileExist);
                        }
                    });
                    break;
            }
        }
        if (chat.getType() == ChatType.TEXT) {
            holder.tvChatTime.setText(time);
            holder.tvChatText.setText(chat.getMessage());
        } else if (chat.getType() == ChatType.IMAGE) {
            holder.tvChatTime.setText(time);

            if (!isFileExist && !isActiveChat) {
                holder.pbLoading.setVisibility(View.GONE);
                holder.llDownloadImage.setVisibility(View.VISIBLE);

                Drawable bgGray;
                if (chat.getIdSender() != ((UserModel) getData(DATA_USER)).getIdUser()) {
                    bgGray = holder.itemView.getContext().getResources().getDrawable(R.drawable.bg_gray_left);
                } else {
                    bgGray = holder.itemView.getContext().getResources().getDrawable(R.drawable.bg_gray_right);
                }
                Glide.with(holder.itemView.getContext())
                        .load(bgGray)
                        .into(holder.ivChatImage);
            } else {
                holder.pbLoading.setVisibility(View.GONE);
                holder.llDownloadImage.setVisibility(View.GONE);

                RoundedCornersTransformation transformation;
                if (chat.getIdReceiver() != ((UserModel) getData(DATA_USER)).getIdUser()) {
                    transformation = new RoundedCornersTransformation(32, 0, RoundedCornersTransformation.CornerType.OTHER_TOP_RIGHT);
                } else {
                    transformation = new RoundedCornersTransformation(32, 0, RoundedCornersTransformation.CornerType.OTHER_TOP_LEFT);
                }

                RequestOptions options = new RequestOptions().transform(new CenterCrop(), transformation);
                Glide.with(holder.itemView.getContext())
                        .load(pathFile)
                        .apply(options)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                ((ChatRoomActivity) activity).moveToBottomImage(((ChatRoomActivity) activity).getRunnable(), 500);
                                return false;
                            }
                        })
                        .into(holder.ivChatImage);
            }


        } else if (chat.getType() == ChatType.VIDEO) {
            holder.tvChatTime.setText(time);

            if (!isFileExist && !isActiveChat) {
                holder.llOpenVid.setVisibility(View.GONE);
                holder.pbLoading.setVisibility(View.GONE);
                holder.llDownloadVideo.setVisibility(View.VISIBLE);

                Drawable bgGray;
                if (chat.getIdSender() != ((UserModel) getData(DATA_USER)).getIdUser()) {
                    bgGray = holder.itemView.getContext().getResources().getDrawable(R.drawable.bg_gray_left);
                } else {
                    bgGray = holder.itemView.getContext().getResources().getDrawable(R.drawable.bg_gray_right);
                }
                Glide.with(holder.itemView.getContext())
                        .load(bgGray)
                        .into(holder.ivChatImage);
            } else {
                holder.llOpenVid.setVisibility(View.VISIBLE);
                holder.pbLoading.setVisibility(View.GONE);
                holder.llDownloadVideo.setVisibility(View.GONE);

                holder.tvDurationVideo.setText(getDurationVideo(pathFile));

                RoundedCornersTransformation transformation;
                if (chat.getIdReceiver() != ((UserModel) getData(DATA_USER)).getIdUser()) {
                    transformation = new RoundedCornersTransformation(32, 0, RoundedCornersTransformation.CornerType.OTHER_TOP_RIGHT);
                } else {
                    transformation = new RoundedCornersTransformation(32, 0, RoundedCornersTransformation.CornerType.OTHER_TOP_LEFT);
                }
                RequestOptions options = new RequestOptions().transform(new CenterCrop(), transformation);
                Glide.with(holder.itemView.getContext())
                        .load(pathFile)
                        .apply(options)
                        .into(holder.ivChatImage);
            }
        } else if (chat.getType() == ChatType.FILE) {
            holder.tvChatTime.setText(time);

            if (!isFileExist && !isActiveChat) {
                holder.tvFileExt.setVisibility(View.GONE);
                holder.ivDownloadFile.setVisibility(View.VISIBLE);
                holder.pbLoading.setVisibility(View.GONE);

                holder.tvFileSize.setText(holder.itemView.getContext().getResources().getString(R.string.click_download));
                holder.tvFileName.setText(getFileName(chat.getMessage()));
            } else {
                Log.d(TAG, pathFile);
                holder.tvFileExt.setVisibility(View.VISIBLE);
                holder.ivDownloadFile.setVisibility(View.GONE);
                holder.pbLoading.setVisibility(View.GONE);

                String[] arrFileInfo = pathFile.split(",");
                holder.tvFileName.setText(arrFileInfo[0]);
                holder.tvFileExt.setText(arrFileInfo[1].toUpperCase());
                holder.tvFileSize.setText(arrFileInfo[2]);
                Log.d(TAG, arrFileInfo[3]);
            }
        } else {
            if (chat.getType() == ChatType.END) {
                holder.tvEndSession.setText(mContext.getResources().getString(R.string.end_session_time)
                        .replace("__waktu__", chat.getTime()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatsModel.Chat chat = mChatList.get(position);
        if (chat.getIdReceiver() == ((UserModel) getData(DATA_USER)).getIdUser()) {
            switch (chat.getType()) {
                case IMAGE:
                    return MSG_IMAGE_LEFT;
                case VIDEO:
                    return MSG_VIDEO_LEFT;
                case FILE:
                    return MSG_FILE_LEFT;
                case END:
                    return MSG_END;
                case TEXT:
                default:
                    return MSG_TEXT_LEFT;
            }
        } else {
            switch (chat.getType()) {
                case IMAGE:
                    return MSG_IMAGE_RIGHT;
                case VIDEO:
                    return MSG_VIDEO_RIGHT;
                case FILE:
                    return MSG_FILE_RIGHT;
                case END:
                    return MSG_END;
                case TEXT:
                default:
                    return MSG_TEXT_RIGHT;
            }
        }
    }

    public interface OnItemClickCallback {
        void onItemTextClick(ChatsModel.Chat model, View view, int position, boolean isFileExist);
        void onItemImageClick(ChatsModel.Chat model, View view, ProgressBar loading, LinearLayout llDownloadImage, int position, boolean isFileExist);
        void onItemVideoClick(ChatsModel.Chat model, View view, ProgressBar loading, LinearLayout llDownloadVideo, LinearLayout llOpenVid, int position, boolean isFileExist);
        void onItemFileClick(ChatsModel.Chat model, View view, ProgressBar loading, ImageView ivDownloadFile, TextView tvFileExt, int position, boolean isFileExist);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llBgChat, llDownloadImage, llDownloadVideo, llOpenVid;
        TextView tvChatText, tvChatTime, tvDurationVideo, tvFileName, tvFileExt, tvFileSize, tvEndSession;
        ImageView ivChatImage, ivDownloadFile;
        ProgressBar pbLoading;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            llBgChat = itemView.findViewById(R.id.ll_bg_chat);
            tvChatText = itemView.findViewById(R.id.tv_chat_text);
            tvChatTime = itemView.findViewById(R.id.tv_chat_time);
            tvDurationVideo = itemView.findViewById(R.id.tv_duration_video);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvFileExt = itemView.findViewById(R.id.tv_file_ext);
            tvFileSize = itemView.findViewById(R.id.tv_file_size);
            tvEndSession = itemView.findViewById(R.id.tv_end_session);
            ivChatImage = itemView.findViewById(R.id.iv_chat_image);
            llDownloadImage = itemView.findViewById(R.id.ll_download_image);
            llDownloadVideo = itemView.findViewById(R.id.ll_download_vid);
            llOpenVid = itemView.findViewById(R.id.ll_open_vid);
            ivDownloadFile = itemView.findViewById(R.id.iv_download_file);
            pbLoading = itemView.findViewById(R.id.loading);
        }
    }

    public void setImageLoading(ProgressBar loading, LinearLayout llDownloadImage) {
        llDownloadImage.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    public void setVideoLoading(ProgressBar loading, LinearLayout llDownloadVideo, LinearLayout llOpenVid) {
        llDownloadVideo.setVisibility(View.GONE);
        llOpenVid.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    public void setFileLoading(ProgressBar loading, ImageView ivDownloadFile, TextView tvFileExt) {
        ivDownloadFile.setVisibility(View.GONE);
        tvFileExt.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }
}

