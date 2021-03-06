# Project 3 - *Twitter Client*

**Simple Tweets** is an android app that allows a user to view his Twitter timeline and post a new tweet. The app utilizes [Twitter REST API](https://dev.twitter.com/rest/public).

Time spent: **29** hours spent in total

## User Stories

The following **required** functionality is completed:

* [x]	User can **sign in to Twitter** using OAuth login
* [x]	User can **view tweets from their home timeline**
  * [x] User is displayed the username, name, and body for each tweet
  * [x] User is displayed the [relative timestamp](https://gist.github.com/nesquena/f786232f5ef72f6e10a7) for each tweet "8m", "7h"
  * [x] User can view more tweets as they scroll with [infinite pagination](http://guides.codepath.com/android/Endless-Scrolling-with-AdapterViews-and-RecyclerView). Number of tweets is unlimited.
    However there are [Twitter Api Rate Limits](https://dev.twitter.com/rest/public/rate-limiting) in place.
* [x] User can **compose and post a new tweet**
  * [x] User can click a “Compose” icon in the Action Bar on the top right (Use FAB for this)
  * [x] User can then enter a new tweet and post this to twitter
  * [x] User is taken back to home timeline with **new tweet visible** in timeline

The following **optional** features are implemented:

* [x] User can **see a counter with total number of characters left for tweet** on compose tweet page
* [x] User can **click a link within a tweet body** on tweet details view. The click will launch the web browser with relevant page opened.
* [x] User can **pull down to refresh tweets timeline**
* [x] User can **open the twitter app offline and see last loaded tweets**. Persisted in SQLite tweets are refreshed on every application launch. While "live data" is displayed when app can get it from Twitter API, it is also saved for use in offline mode.
* [x] User can tap a tweet to **open a detailed tweet view**
* [x] User can **select "reply" from detail view to respond to a tweet**
* [x] Improve the user interface and theme the app to feel "twitter branded" (i tried...)

The following **bonus** features are implemented:

* [x] User can see embedded image media within the tweet detail view
* [x] User can watch embedded video within the tweet
* [x] Compose tweet functionality is build using modal overlay
* [x] Use Parcelable instead of Serializable using the popular [Parceler library](http://guides.codepath.com/android/Using-Parceler). (Use android studio plugin)
* [x] Apply the popular [Butterknife annotation library](http://guides.codepath.com/android/Reducing-View-Boilerplate-with-Butterknife) to reduce view boilerplate.
* [x] Leverage the popular [GSON library](http://guides.codepath.com/android/Using-Android-Async-Http-Client#decoding-with-gson-library) to streamline the parsing of JSON data.
* [x]  [Leverage RecyclerView](http://guides.codepath.com/android/Using-the-RecyclerView) as a replacement for the ListView and ArrayAdapter for all lists of tweets.
* [x] Move the "Compose" action to a [FloatingActionButton](https://github.com/codepath/android_guides/wiki/Floating-Action-Buttons) instead of on the AppBar.
* [x] Replace Picasso with [Glide](http://inthecheesefactory.com/blog/get-to-know-glide-recommended-by-google/en) for more efficient image rendering.

The following **additional** features are implemented:

* [x] List anything else that you can get done to improve the app functionality!

  * Scroll to top when tapping on the toolbar
  * A snackar is shown for network error and the user can reload (either
    fetching newer or older items)
  * Tap on relative timestamp to toggle between relative and absolute timestamps
  * Re-enable loading more tweets if the network is recovered at some point after a load more failed.
  * Use cursor for RecyclerView, so the app does not use memory to store the tweets any more.
  * A user can like/unlike a tweet on detail view.
  * A user can retweet/unretweet a tweet that does not belong to me on detail view.
  * A user can press the "show more" button to fill the gap exists in the
    timeline. The gap exists because when we fetch new tweets, the oldest of
    them may still be much newer than the tweets we have before.
  * Show my replies to tweets (not sure how to show replies from others)
  * Progress bar when loading older items

## Video Walkthrough 

Here's a walkthrough of implemented user stories:

<img src='tweet-client-overall.gif' title='Video Walkthrough' width='' alt='Video Walkthrough' />
<img src='tweet-client-time-gap-solved-by-show-more-items-click-links-reply.gif' title='Video Walkthrough' width='' alt='Video Walkthrough' />
<img src='tweet-client-retweet-like.gif' title='Video Walkthrough' width='' alt='Video Walkthrough' />
<img src='tweet-client-new-items-and-offline.gif' title='Video Walkthrough' width='' alt='Video Walkthrough' />

GIF created with [LiceCap](http://www.cockos.com/licecap/).

## Notes

Describe any challenges encountered while building the app.

## Open-source libraries used

- [Active Android](https://github.com/pardom/ActiveAndroid/wiki/Getting-started) ActiveAndroid is an active record style ORM (object relational mapper).
- [Android Async HTTP](https://github.com/loopj/android-async-http) - Simple asynchronous HTTP requests with JSON parsing
- [Butter Knife](http://jakewharton.github.io/butterknife/) - Field and method binding for Android views
- [Glide](https://github.com/bumptech/glide) - An image loading and caching library for Android focused on smooth scrolling
- [Gson](https://github.com/google/gson) - A Java serialization library that can convert Java Objects into JSON and back.

## License

    Copyright [2016] [Heung Ming Tai]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
