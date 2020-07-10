package com.example.parstagram.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.parstagram.EndlessRecyclerViewScrollListener;
import com.example.parstagram.adapters.PostsAdapter;
import com.example.parstagram.R;
import com.example.parstagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostsFragment extends Fragment {

    public static final String TAG = "PostsFragment";


    protected RecyclerView rvPosts;
    private PostsAdapter adapter;
    protected List<Post> allposts;
    protected SwipeRefreshLayout swipeContainer;

    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;
    private Date oldestCreatedAt;

    public PostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPosts = view.findViewById(R.id.rvPosts);
        allposts = new ArrayList<>();

        adapter = new PostsAdapter(getContext(), allposts);
        rvPosts.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(layoutManager);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "onRefresh: fetching new data!");
                queryPosts();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        queryPosts();

        // Retain an instance to call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(long page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.i(TAG, "onLoadMore!");
                loadNextData(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);
    }

    protected void queryPosts() {
        // specify the class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts.", e);
                    return;
                }
                for (int i = 0; i < posts.size(); i++) {
                    Log.i(TAG, "Post: " + posts.get(i).getDescription()
                            + ", username: " + posts.get(i).getUser().getUsername());
                    if (i == posts.size() - 1) {
                        oldestCreatedAt = posts.get(i).getCreatedAt();
                    }
                }
                adapter.clear();
                adapter.addAll(posts);
                swipeContainer.setRefreshing(false);
            }
        });
    }

    // Append the next page of data into the adapter
    protected void loadNextData(long page) {
        // specify the class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(20);
        query.whereLessThan(Post.KEY_CREATED_AT, oldestCreatedAt);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts.", e);
                    return;
                }
                for (int i = 0; i < posts.size(); i++) {
                    Log.i(TAG, "Post: " + posts.get(i).getDescription()
                            + ", username: " + posts.get(i).getUser().getUsername());
                    if (i == posts.size() - 1) {
                        oldestCreatedAt = posts.get(i).getCreatedAt();
                    }
                }
                adapter.addAll(posts);
                swipeContainer.setRefreshing(false);
            }
        });
    }
}