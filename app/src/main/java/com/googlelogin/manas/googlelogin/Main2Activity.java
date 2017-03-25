package com.googlelogin.manas.googlelogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.PlusShare;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.Image;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.POST;

public class Main2Activity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    Button share;
    EditText editText;
    GoogleApiClient googleApiClient;
    Button googlebtn,fbbtn,twitterbtn,linkedinbtn;
    ImageView ivgoogle,ivfb,ivtwitter,ivlinkedin;
    SharedPreferences sharedPreferences;
    String content;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        share=(Button)findViewById(R.id.m2a_sharebtn);
        editText=(EditText)findViewById(R.id.sa_et);

        sharedPreferences=getSharedPreferences("social",MODE_PRIVATE);
//        Toast.makeText(this,""+sharedPreferences.getBoolean("GOOGLE",false)+sharedPreferences.getBoolean("FACEBOOK",false)+sharedPreferences.getBoolean("TWITTER",false)+sharedPreferences.getBoolean("LINKEDIN",false),Toast.LENGTH_SHORT).show();

        googlebtn=(Button)findViewById(R.id.sign_in_button);
        fbbtn=(Button)findViewById(R.id.fb_btn);
        twitterbtn=(Button)findViewById(R.id.twitter_login_button);
        linkedinbtn=(Button)findViewById(R.id.li_btn);

        ivgoogle=(ImageView)findViewById(R.id.iv_google);
        ivfb=(ImageView)findViewById(R.id.iv_fb);
        ivtwitter=(ImageView)findViewById(R.id.iv_twitter);
        ivlinkedin=(ImageView)findViewById(R.id.iv_linkedin);

        if(sharedPreferences.getBoolean("GOOGLE",false))
            googlebtn.setVisibility(View.VISIBLE);
        if(sharedPreferences.getBoolean("FACEBOOK",false))
            fbbtn.setVisibility(View.VISIBLE);
        if(sharedPreferences.getBoolean("TWITTER",false))
            twitterbtn.setVisibility(View.VISIBLE);
        if(sharedPreferences.getBoolean("LINKEDIN",false))
            linkedinbtn.setVisibility(View.VISIBLE);

        progressDialog=new ProgressDialog(Main2Activity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content=editText.getText().toString();

                if(content.equals(""))
                {
                    Toast.makeText(Main2Activity.this,"Empty Field",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    shareEverywhereText();
//                finish();

                }
            }
        });

        googlebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content=editText.getText().toString();
                if(content.equals(""))
                {
                    Toast.makeText(Main2Activity.this,"Empty Field",Toast.LENGTH_SHORT).show();
                }
                else
                shareGoogle();
            }
        });

        fbbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content=editText.getText().toString();
                if(content.equals(""))
                {
                    Toast.makeText(Main2Activity.this,"Empty Field",Toast.LENGTH_SHORT).show();
                }
                else
                    shareFacebook();
            }
        });

        twitterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content=editText.getText().toString();
                if(content.equals(""))
                {
                    Toast.makeText(Main2Activity.this,"Empty Field",Toast.LENGTH_SHORT).show();
                }
                else
                    shareTwitter();
            }
        });

        linkedinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content=editText.getText().toString();
                if(content.equals(""))
                {
                    Toast.makeText(Main2Activity.this,"Empty Field",Toast.LENGTH_SHORT).show();
                }
                else
                    shareLinkedIn();
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void shareEverywhereText() {

        if(sharedPreferences.getBoolean("FACEBOOK",false))
            shareFacebook();
        if(sharedPreferences.getBoolean("TWITTER",false))
            shareTwitter();
        if(sharedPreferences.getBoolean("LINKEDIN",false))
            shareLinkedIn();
        if(sharedPreferences.getBoolean("GOOGLE",false))
            shareGoogle();
    }

    private void shareGoogle() {

        Intent shareIntent = new PlusShare.Builder(this)
                .setType("text/plain")
                .setText(content)
                .setContentUrl(Uri.parse("https://developers.google.com/+/"))
                .getIntent();

        startActivityForResult(shareIntent, 0);
    }

    public void shareTwitter()
    {
        progressDialog.setMessage("Posting on Twitter");
        progressDialog.show();

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<Tweet> call=statusesService.update(content,null,null,null,null,null,null,null,null);
        call.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                Toast.makeText(Main2Activity.this,"Post Shared on Twitter Successfully",Toast.LENGTH_SHORT).show();
                ivtwitter.setVisibility(View.VISIBLE);
                progressDialog.dismiss();
            }

            public void failure(TwitterException exception) {
                Toast.makeText(Main2Activity.this,"Post Share on Twitter Failed",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    public void shareFacebook()
    {
        progressDialog.setMessage("Posting on Facebook");
        progressDialog.show();

        Bundle params = new Bundle();
        params.putString("message",content);
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
//                        Toast.makeText(Main2Activity.this,response.toString(),Toast.LENGTH_SHORT).show();
                        Toast.makeText(Main2Activity.this,"Post Shared on Facebook Successfully",Toast.LENGTH_SHORT).show();

                        ivfb.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                    }
                }
        ).executeAsync();
    }

    public void shareLinkedIn()
    {
        try {
            progressDialog.setMessage("Posting on LinkedIn");
            progressDialog.show();

            String url = "https://api.linkedin.com/v1/people/~/shares";

            String payload = "{" +
                    "\"comment\":\""+content+"\"," +
                    "\"visibility\":{" +
                    "    \"code\":\"anyone\"}" +
                    "}";

            String payload1 = "{" +
                    "\"comment\":\""+content+"" +
                    "http://linkd.in/1FC2PyG\"," +
                    "\"visibility\":{" +
                    "    \"code\":\"anyone\"}" +
                    "}";

            APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
            apiHelper.postRequest(this, url, payload, new ApiListener() {
                @Override
                public void onApiSuccess(ApiResponse apiResponse) {
                    Toast.makeText(Main2Activity.this,"Post Shared on LinkedIn successfully",Toast.LENGTH_SHORT).show();
                    ivlinkedin.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                }

                @Override
                public void onApiError(LIApiError liApiError) {
                    Toast.makeText(Main2Activity.this,"Post Share on LinkedIn failed",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });

        }
        catch (Exception e)
        {
            Toast.makeText(Main2Activity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==0)
        {
            if(resultCode==RESULT_OK)
            {
                Toast.makeText(Main2Activity.this,"Post Shared on Google+ Successfully",Toast.LENGTH_SHORT).show();
                ivgoogle.setVisibility(View.VISIBLE);
            }
            else
            {
                Toast.makeText(Main2Activity.this,"Post Shared on Google+ Failed",Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
