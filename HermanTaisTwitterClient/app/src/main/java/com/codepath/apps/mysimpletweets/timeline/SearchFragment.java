package com.codepath.apps.mysimpletweets.timeline;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.SimpleTweetsApplication;
import com.codepath.apps.mysimpletweets.helpers.LogUtil;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.TweetInterface;
import com.codepath.apps.mysimpletweets.tweetdetail.TweetDetailActivity;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.codepath.apps.mysimpletweets.widgets.SimpleProgressDialog;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchFragment extends Fragment {
    @Bind(R.id.rvSearchResult) RecyclerView mRvSearchResult;
    private TwitterClient mClient;
    private SearchResultsAdapter mAdapter;

    public static SearchFragment newInstance() {
        Bundle args = new Bundle();

        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mClient = SimpleTweetsApplication.getRestClient();
        mAdapter = new SearchResultsAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, v);

        LinearLayoutManager llManager = new LinearLayoutManager(getActivity());
        llManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvSearchResult.setLayoutManager(llManager);
        mRvSearchResult.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                // workaround to avoid issues with some emulators and keyboard devices firing
                // twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();
                searchItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        // TODO: the following does not seem to work, not sure how to request focus when the page
        // just loaded
        searchView.setFocusable(true);
        searchView.requestFocus();
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
    }

    private void search(String query) {
        final ProgressDialog progressDialog = SimpleProgressDialog.createProgressDialog(
                getActivity());
        mClient.search(query, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                LogUtil.d(Common.INFO_TAG, "search result: " + response);
                JSONArray jsonArray = response.optJSONArray("statuses");
                if (jsonArray != null) {
                    List<Tweet> newTweets = Tweet.fromJsonArray(jsonArray);
                    mAdapter.clear();
                    mAdapter.addAll(new ArrayList<TweetInterface>(newTweets));
                }
                progressDialog.dismiss();
            }
        });
    }

    class SearchResultsAdapter extends RecyclerView.Adapter<TweetViewHolder> {
        List<TweetInterface> mTweets = new ArrayList<>();

        @Override
        public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_tweet, parent, false);

            TweetViewHolder vh = new TweetViewHolder(
                    v,
                    null,
                    null,
                    new TweetViewHolder.TweetOnClickListener(){
                        @Override
                        public void onClick(int position, TweetInterface tweet) {
                            Intent i = TweetDetailActivity.newIntent(
                                    getActivity(), position, tweet, "");
                            startActivity(i);
                        }
                    });
            return vh;
        }

        @Override
        public void onBindViewHolder(TweetViewHolder holder, int position) {
            TweetInterface tweet = getItem(position);
            holder.bindTweet(getActivity(), position, tweet);
        }

        @Override
        public int getItemCount() {
            return mTweets.size();
        }

        public TweetInterface getItem(int position) {
            return mTweets.get(position);
        }

        public void addAll(List<TweetInterface> tweets) {
            mTweets.addAll(tweets);
            notifyDataSetChanged();
        }

        public void clear() {
            mTweets.clear();
        }
    }
}
