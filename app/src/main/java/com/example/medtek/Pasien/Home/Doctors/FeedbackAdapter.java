package com.example.medtek.Pasien.Home.Doctors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.codesgood.views.JustifiedTextView;
import com.example.medtek.Pasien.Model.FeedbackModel;
import com.example.medtek.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {

    private List<FeedbackModel> mList = new ArrayList<>();
    private Context mContext;
    private FragmentActivity mActivity;

    public FeedbackAdapter(List<FeedbackModel> mList, Context mContext, FragmentActivity mActivity){
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_feedback,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final FeedbackModel model = mList.get(position);

        holder.tv_name.setText(model.getName());
        holder.tv_message.setText(model.getMessage());

        Locale locale = new Locale("in", "ID");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", locale);
        Date date;
        try {
            date = format.parse(model.getPost_date());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            holder.tv_post_date.setText(calendar.get(Calendar.DATE)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.YEAR)+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.ratingBar.setRating(model.getRating());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //class utk menampung objek2 yang ada pada halaman item list recycler view
    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_name, tv_post_date;
        private JustifiedTextView tv_message;
        private RatingBar ratingBar;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_user_name);
            tv_message = itemView.findViewById(R.id.jtv_feedback);
            tv_post_date = itemView.findViewById(R.id.tv_post_date);
            ratingBar = itemView.findViewById(R.id.rb_dokter);
        }
    }

}
