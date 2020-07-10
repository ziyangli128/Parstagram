package com.example.parstagram.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parstagram.adapters.CommentsAdapter;
import com.example.parstagram.EndlessRecyclerViewScrollListener;
import com.example.parstagram.R;
import com.example.parstagram.models.Comment;
import com.example.parstagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    public static final String TAG = "DetailFragment";

    private TextView tvUsername;
    private TextView tvUsername2;
    private TextView tvDescription;
    private TextView tvCreatedAt;
    private ImageView ivImage;
    private ImageView ivProfile;
    private ImageView ivLike;
    private ImageView ivComment;
    private TextView tvLikesCount;
    private TextView tvCommentsCount;
    private EditText etComment;
    private Button btnComment;

    Post post;

    private RecyclerView rvComments;
    private CommentsAdapter commentsAdapter;
    private List<Comment> allcomments;
    private Date oldestCommentCreatedAt;
    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;

    public DetailFragment() {
        // Required empty public constructor
    }

    public DetailFragment(Post post) {
        this.post = post;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvUsername2 = view.findViewById(R.id.tvUsername2);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvCreatedAt = view.findViewById(R.id.tvCreatedAt);
        ivImage = view.findViewById(R.id.ivImage);
        ivProfile = view.findViewById(R.id.ivProfile);
        ivLike = view.findViewById(R.id.ivLike);
        ivComment = view.findViewById(R.id.ivComment);
        tvLikesCount = view.findViewById(R.id.tvLikesCount);
        tvCommentsCount = view.findViewById(R.id.tvCommentsCount);
        etComment = view.findViewById(R.id.etComment);
        btnComment = view.findViewById(R.id.btnComment);
        rvComments = view.findViewById(R.id.rvComments);

        tvUsername.setText(post.getUser().getUsername());
        tvUsername2.setText(post.getUser().getUsername());
        tvDescription.setText(post.getDescription());
        tvCreatedAt.setText(post.getCreatedAt().toString());
        ParseFile image = post.getImage();
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(ivImage);
        }

        ParseFile profileImage = post.getUser().getParseFile("profileImage");
        if (image != null) {
            Glide.with(this).load(profileImage.getUrl()).into(ivProfile);
        }

        if (post.getLikes().contains(ParseUser.getCurrentUser().getObjectId())) {
            ivLike.setSelected(true);
        } else {
            ivLike.setSelected(false);
        }

        allcomments = new ArrayList<>();

        commentsAdapter = new CommentsAdapter(getContext(), allcomments);
        rvComments.setAdapter(commentsAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvComments.setLayoutManager(layoutManager);

        queryComments();

        // Retain an instance to call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(long page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.i(TAG, "onLoadMore Comments!");
                //loadNextComments(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvComments.addOnScrollListener(scrollListener);

        setIvProfileClickListener(post);
        setIvLikeClickListener(post);
        tvLikesCount.setText(Integer.toString(post.getLikes().size() - 1));

        setBtnCommentClickListener(post);
        if (post.getComments() == null) {
            tvCommentsCount.setText("0");
        } else {
            tvCommentsCount.setText(Integer.toString(post.getComments().size()));
        }

    }

    public void setIvProfileClickListener(final Post post) {
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ProfileFragment(post.getUser());
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.flContainer, fragment);
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

    public void setBtnCommentClickListener(final Post post) {
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentString = etComment.getText().toString();
                if (commentString.isEmpty()) {
                    Toast.makeText(getContext(),
                            R.string.comment_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                Comment comment = new Comment();
                comment.setDescription(commentString);
                comment.setAuthor(ParseUser.getCurrentUser());
                comment.setCommentedPost(post);
                comment.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Issue with commenting on post.", e);
                            Toast.makeText(getContext(),
                                    R.string.comment_error, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(getContext(), R.string.comment_success, Toast.LENGTH_LONG).show();
                        Log.i(TAG, "Comment was successful!");
                        etComment.setText("");
                    }
                });
                post.setComments(comment.getObjectId());
                post.saveInBackground();
                // Update the adapter
                allcomments.add(0, comment);
                commentsAdapter.notifyItemInserted(0);
                rvComments.smoothScrollToPosition(0);
            }
        });
    }

    protected void queryComments() {
        // specify the class to query
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        query.include(Comment.KEY_AUTHOR);
        query.setLimit(20);
        query.whereEqualTo(Comment.KEY_COMMENTED_POST, post);
        query.addDescendingOrder(Comment.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> comments, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting comments.", e);
                    return;
                }
                for (int i = 0; i < comments.size(); i++) {
                    Log.i(TAG, "Comment: " + comments.get(i).getComment()
                            + ", username: " + comments.get(i).getAuthor().getUsername());
                    if (i == comments.size() - 1) {
                        oldestCommentCreatedAt = comments.get(i).getCreatedAt();
                    }
                }
                commentsAdapter.clear();
                commentsAdapter.addAll(comments);
            }
        });
    }
}