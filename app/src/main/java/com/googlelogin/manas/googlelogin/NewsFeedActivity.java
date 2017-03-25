package com.googlelogin.manas.googlelogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Response;

public class NewsFeedActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NewsFeedAdapter newsFeedAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<NewsFeedItem> newsFeedItems;
    EditText editText;
    RelativeLayout relativeLayout;
    SharedPreferences sharedPreferences;
    CallbackManager callbackManager;
    AccessToken accessToken;
    ImageView profilePic;
    CardView cardView;
    AccessTokenTracker accessTokenTracker;
    private static final String TWITTER_KEY = "tBvbCS6N23vCXrNPGSnvfrbn3";
    private static final String TWITTER_SECRET = "i0CS82IrwRfLER9A83lN1gNSzjlw5cFbNuA0QzZwoOIE5IWnYv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            sharedPreferences=getSharedPreferences("social",MODE_PRIVATE);

            if(!(sharedPreferences.getBoolean("GOOGLE",false) || sharedPreferences.getBoolean("FACEBOOK",false) ||
                    sharedPreferences.getBoolean("TWITTER",false) || sharedPreferences.getBoolean("LINKEDIN",false)))
            {
                startActivity(new Intent(this,MainActivity.class));
                finish();
            }

            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            initFacebook();
            initTwitter();
            setContentView(R.layout.activity_news_feed);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("NewsFeed");

            newsFeedItems=new ArrayList<>();
            recyclerView = (RecyclerView) findViewById(R.id.nfa_rv);
            cardView= (CardView) findViewById(R.id.nfa_cv);
            editText=(EditText)findViewById(R.id.nfa_et);
            profilePic=(ImageView)findViewById(R.id.nfa_iv);
            swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.nfa_swipelayout);

            RecyclerView.LayoutManager manager=new LinearLayoutManager(NewsFeedActivity.this,LinearLayoutManager.VERTICAL,false);
            recyclerView.setLayoutManager(manager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(NewsFeedActivity.this,Main2Activity.class));
                }
            });

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    newsFeedItems.clear();
                    newsFeedAdapter.notifyItemRangeRemoved(0,newsFeedItems.size());
                    setupView();

                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            if(sharedPreferences.getBoolean("FACEBOOK",false))
            {
                Picasso.with(this)
                        .load(Profile.getCurrentProfile().getProfilePictureUri(100,100))
                .into(profilePic);
            }

            setupView();

        }
        catch (Exception e)
        {
            Toast.makeText(NewsFeedActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void initFacebook() {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();
    }

    public void getFacebook()
    {
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try
                        {
//                            Toast.makeText(NewsFeedActivity.this,response.getJSONObject().toString(),Toast.LENGTH_SHORT).show();

                            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
                            sf.setLenient(true);

                            String story="";
                            JSONArray jsonarray=response.getJSONObject().getJSONArray("data");
                            for(int i=0;i<jsonarray.length();i++)
                            {
                                JSONObject jsonObject=jsonarray.getJSONObject(i);
                                story="";
                                if(jsonObject.has("story"))
                                {
                                    story=story.concat(jsonObject.getString("story"));
                                }
                                if(jsonObject.has("message"))
                                {
                                    story=story.concat("\n"+jsonObject.getString("message"));
                                }
                                newsFeedItems.add(new NewsFeedItem(jsonObject.getString("id"),"Facebook",story,
                                        sf.parse(jsonObject.getString("created_time")),R.drawable.facebookiconpreview));

                            }
                        }
                        catch (Exception e)
                        {
                        Toast.makeText(NewsFeedActivity.this,"ERR:"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ).executeAsync();
    }

    public void initTwitter()
    {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
    }


    public void getTwitter()
    {
        try {
            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
            // Can also use Twitter directly: Twitter.getApiClient()
            StatusesService statusesService = twitterApiClient.getStatusesService();
            final Call<List<Tweet>> timeline = statusesService.homeTimeline(10, null, null, null, null, null, null);
            timeline.enqueue(new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> result) {
                    try
                    {
                    SimpleDateFormat sf = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
                    sf.setLenient(true);

                    List<Tweet> tweets = result.data;
                    for (int i = 0; i < tweets.size(); i++) {
                        Tweet tweet = tweets.get(i);
                        newsFeedItems.add(new NewsFeedItem(tweet.idStr, "Twitter", tweet.text, sf.parse(tweet.createdAt),R.drawable.twittercircle));
                    }

                    }
                    catch (Exception e)
                    {
                        Toast.makeText(NewsFeedActivity.this, "EXCPp:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void failure(TwitterException exception) {
                    Toast.makeText(NewsFeedActivity.this, "ERROR:" + exception.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(NewsFeedActivity.this, "EXCP:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void getLinkedIn()
    {
        String url = "https://api.linkedin.com/v1/companies/1337/updates?count=10&format=json";

        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                Toast.makeText(NewsFeedActivity.this,"success:"+apiResponse.toString(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onApiError(LIApiError liApiError) {
                Toast.makeText(NewsFeedActivity.this,"fail:"+liApiError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setupView()
    {
        newsFeedItems=new ArrayList<>();
        if((sharedPreferences.getBoolean("GOOGLE",false)||sharedPreferences.getBoolean("LINKEDIN",false)))
            Toast.makeText(this,"Newsfeed is not available for Google+ and LinkedIN",Toast.LENGTH_SHORT).show();
        if(sharedPreferences.getBoolean("FACEBOOK",false))
            getFacebook();

        if(sharedPreferences.getBoolean("TWITTER",false))
            getTwitter();

        Collections.sort(newsFeedItems, new Comparator<NewsFeedItem>() {
            @Override
            public int compare(NewsFeedItem o1, NewsFeedItem o2) {
                Date d1=o1.getDate();
                Date d2=o2.getDate();
                return d1.compareTo(d2);
            }
        });


        newsFeedAdapter=new NewsFeedAdapter(this,newsFeedItems);
        recyclerView.setAdapter(newsFeedAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            startActivity(new Intent(this,SettingActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}
