package com.codepath.apps.mysimpletweets.twitter;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "NzmGcz6PldHC4Dvzy6JucH7TQ";       // Change this
	public static final String REST_CONSUMER_SECRET = "t6YPY8DAxeRfEWhNHKOM3NU1TEAnjjO1g4uKnmc5bMFWrsYxhD"; // Change this
	public static final String REST_CALLBACK_URL =
			"oauth://cpsimpletweets"; // Change this (here and in manifest)

	public TwitterClient(Context context) {
		super(
                context,
                REST_API_CLASS,
                REST_URL,
                REST_CONSUMER_KEY,
                REST_CONSUMER_SECRET,
                REST_CALLBACK_URL);
	}


    // METHOD == ENDPOINT

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */

    /**
     * HomeTimeline - Gets us the home timeline
     * Get statuses/home_timeline.json
     * The json lists the tweets in timestamp descending order.
     *
     * @param count
     * @param since_id The ID of the oldest tweet to retrieve, exclusive. Use 0 if just want to get
     *                 the newest tweets.
     * @param max_id The ID of the newest tweet to retrieve, exclusive. Use 0 if not setting a
     *               limit.
     * @param handler
     */
    public void getHomeTimeline(
            int count,
            long since_id,
            long max_id,
            AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");

        // Specify the params
        RequestParams params = new RequestParams();
        params.put("count", count);
        if (since_id != 0) {
            params.put("since_id", since_id);
        }
        if (max_id != 0) {
            // The max_id in the API is actually inclusive, so we adjust it here to make our API
            // cleaner.
            params.put("max_id", max_id - 1);
        }

        getClient().get(apiUrl, params, handler);
    }

    public void updateStatus(CharSequence status, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");

        // Specify the params
        RequestParams params = new RequestParams();
        params.put("status", status);

        getClient().post(apiUrl, params, handler);
    }
}