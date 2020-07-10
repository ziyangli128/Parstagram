package com.example.parstagram.adapters;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parstagram.R;
import com.example.parstagram.activities.MainActivity;
import com.example.parstagram.fragments.DetailFragment;
import com.example.parstagram.fragments.ProfileFragment;
import com.example.parstagram.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostsAdapter";

    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> posts) {
        this.posts.addAll(posts);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvUsername;
        private TextView tvUsername2;
        private TextView tvRelativeTime;
        private ImageView ivImage;
        private TextView tvDescription;
        private ImageView ivProfile;
        private ImageView ivLike;
        private ImageView ivComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvUsername2 = itemView.findViewById(R.id.tvUsername2);
            tvRelativeTime = itemView.findViewById(R.id.tvRelativeTime);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivLike = itemView.findViewById(R.id.ivLike);
            ivComment = itemView.findViewById(R.id.ivComment);

            itemView.setOnClickListener(this);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void bind(final Post post) {
            // Bind the post data to the view elements
            tvUsername.setText(post.getUser().getUsername());
            tvUsername2.setText(post.getUser().getUsername());
            tvRelativeTime.setText(getRelativeTimeAgo(post.getCreatedAt().toString()));
            tvDescription.setText(post.getDescription());
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }

            ParseFile profileImage = post.getUser().getParseFile("profileImage");
            if (profileImage != null) {
                Glide.with(context).load(profileImage.getUrl()).into(ivProfile);
            }

            if (post.getLikes().contains(ParseUser.getCurrentUser().getObjectId())) {
                ivLike.setSelected(true);
            } else {
                ivLike.setSelected(false);
            }

            setIvProfileClickListener(post);
            setIvLikeClickListener(post);
        }

        @Override
        public void onClick(View view) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the post clicked on
                Post post = posts.get(position);
                MainActivity activity = (MainActivity) view.getContext();
                Fragment fragment = new DetailFragment(post);
                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.flContainer, fragment);
                activity.getSupportFragmentManager().popBackStack();
                ft.addToBackStack("from home to detail");
                ft.commit();
            }
        }

        public void setIvProfileClickListener(final Post post) {
            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity activity = (MainActivity) view.getContext();
                    Fragment fragment = new ProfileFragment(post.getUser());
                    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.flContainer, fragment);
                    activity.getSupportFragmentManager().popBackStack();
                    ft.addToBackStack("to profile");
                    ft.commit();
                }
            });
        }

        public void setIvLikeClickListener(final Post post) {
            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "onClick: " + post.getLikes());
                    if (post.getLikes().contains(ParseUser.getCurrentUser().getObjectId())) {
                        post.removeLikes(ParseUser.getCurrentUser().getObjectId());
                        post.saveInBackground();
                        ivLike.setSelected(false);
                    } else {
                        post.setLikes(ParseUser.getCurrentUser().getObjectId());
                        post.saveInBackground();
                        ivLike.setSelected(true);
                    }
                }
            });
        }
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        rawJsonDate = rawJsonDate.replace("PDT", "-0700");

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }
}
