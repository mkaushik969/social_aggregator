package com.googlelogin.manas.googlelogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class SettingActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;

    LoginButton loginButton;
    CallbackManager callbackManager;

    private TwitterLoginButton twitterLoginButton;

    Button liBtn,fblobtn,twlobtn,lilobtn;

    static int REQ_CODE=100;
    static int GOOGLE_REQ_CODE=101;

    FloatingActionButton iv1,iv2,iv3,iv4;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTwitter();
        initFacebook();

        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");

        sharedPreferences=getSharedPreferences("social",MODE_PRIVATE);
        editor=sharedPreferences.edit();

        iv1=(FloatingActionButton) findViewById(R.id.iv_google);
        iv2=(FloatingActionButton) findViewById(R.id.iv_fb);
        iv3=(FloatingActionButton) findViewById(R.id.iv_twitter);
        iv4=(FloatingActionButton) findViewById(R.id.iv_linkedin);

        liBtn=(Button)findViewById(R.id.li_btn);
        fblobtn=(Button)findViewById(R.id.fb_btn1);
        twlobtn=(Button)findViewById(R.id.twitter_login_button1);
        lilobtn=(Button)findViewById(R.id.li_btn1);

        initGoogle();
        signInButton=(SignInButton)findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sharedPreferences.getBoolean("GOOGLE",false))
                    loginGoogle();
                else
                    logoutGoogle();
            }
        });

        loginButton=(LoginButton)findViewById(R.id.fb_btn);
        loginButton.setReadPermissions("email","public_profile","user_posts");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                editor.putBoolean("FACEBOOK",true);
                editor.apply();

                iv2.setVisibility(View.VISIBLE);
                fblobtn.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.INVISIBLE);

                Toast.makeText(SettingActivity.this,"Facebook login successful",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(SettingActivity.this,"Facebook login failed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("LOGIN",error.getMessage());
                Toast.makeText(SettingActivity.this,"Facebook login failed",Toast.LENGTH_SHORT).show();
            }
        });

        fblobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                Toast.makeText(SettingActivity.this,"Signed out of Facebook successfully",Toast.LENGTH_SHORT).show();
                iv2.setVisibility(View.INVISIBLE);
                fblobtn.setVisibility(View.INVISIBLE);
                loginButton.setVisibility(View.VISIBLE);
                editor.putBoolean("FACEBOOK",false);
                editor.apply();
            }
        });

        twitterLoginButton= (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;
                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                editor.putBoolean("TWITTER",true);
                editor.apply();
                iv3.setVisibility(View.VISIBLE);
                twlobtn.setVisibility(View.VISIBLE);
                twitterLoginButton.setVisibility(View.INVISIBLE);

                Toast.makeText(getApplicationContext(),"Twitter login successful", Toast.LENGTH_LONG).show();
            }
            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "Twitter login failed", Toast.LENGTH_LONG).show();
            }
        });

        twlobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Twitter.getSessionManager().clearActiveSession();
                Twitter.logOut();
                Toast.makeText(SettingActivity.this,"Signed out of Twitter successfully",Toast.LENGTH_SHORT).show();
                iv3.setVisibility(View.INVISIBLE);
                twlobtn.setVisibility(View.INVISIBLE);
                twitterLoginButton.setVisibility(View.VISIBLE);
                editor.putBoolean("TWITTER",false);
                editor.apply();

            }
        });

        liBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginLinkedIn();
            }
        });

        lilobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LISessionManager.getInstance(SettingActivity.this).clearSession();

                Toast.makeText(SettingActivity.this,"Signed out of LinkedIn successfully",Toast.LENGTH_SHORT).show();
                liBtn.setVisibility(View.VISIBLE);
                lilobtn.setVisibility(View.INVISIBLE);
                iv4.setVisibility(View.INVISIBLE);
                editor.putBoolean("LINKEDIN",false);
                editor.apply();
            }
        });

        if(sharedPreferences.getBoolean("GOOGLE",false))
            iv1.setVisibility(View.VISIBLE);
        if(sharedPreferences.getBoolean("FACEBOOK",false))
        {
            iv2.setVisibility(View.VISIBLE);
            fblobtn.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
        }
        if(sharedPreferences.getBoolean("TWITTER",false))
        {
            iv3.setVisibility(View.VISIBLE);
            twlobtn.setVisibility(View.VISIBLE);
            twitterLoginButton.setVisibility(View.INVISIBLE);

        }
        if(sharedPreferences.getBoolean("LINKEDIN",false))
        {
            iv4.setVisibility(View.VISIBLE);
            lilobtn.setVisibility(View.VISIBLE);
            liBtn.setVisibility(View.INVISIBLE);
        }


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
//                    Toast.makeText(SettingActivity.this,"already Signed In",Toast.LENGTH_SHORT).show();
                } else {
                    // User is signed out
//                    Toast.makeText(SettingActivity.this,"already Signed out",Toast.LENGTH_SHORT).show();
                }
                // ...
            }
        };

    }

    private void logoutGoogle() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess())
                {
                    Toast.makeText(SettingActivity.this,"Signed out of Google successfully",Toast.LENGTH_SHORT).show();
                    iv1.setVisibility(View.INVISIBLE);
                    editor.putBoolean("GOOGLE",false);
                    editor.apply();
                }
                else
                {
                    Toast.makeText(SettingActivity.this,"Google sign out failed",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void initGoogle()
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void loginGoogle()
    {
        REQ_CODE=GOOGLE_REQ_CODE;
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_REQ_CODE);
    }

    public void initFacebook()
    {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }

    public void initTwitter()
    {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
    }

    public void loginLinkedIn()
    {
        LISessionManager.getInstance(getApplicationContext()).init(SettingActivity.this, buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {

                editor.putBoolean("LINKEDIN",true);
                editor.apply();

                iv4.setVisibility(View.VISIBLE);
                lilobtn.setVisibility(View.VISIBLE);
                liBtn.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "LinkedIn login successful",Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAuthError(LIAuthError error) {

                Toast.makeText(getApplicationContext(), "LinkedIn login failed ",Toast.LENGTH_LONG).show();
            }
        }, true);
    }

    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE,  Scope.R_EMAILADDRESS, Scope.W_SHARE);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
//                            Toast.makeText(SettingActivity.this, "Google login failed",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
//                            Toast.makeText(SettingActivity.this, "Google login successful",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else
        {
//            Toast.makeText(this,"Reqcode:"+requestCode+":"+resultCode,Toast.LENGTH_SHORT).show();

            LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
            editor.putBoolean("GOOGLE",true);
            editor.apply();

            iv1.setVisibility(View.VISIBLE);

            Toast.makeText(this,"Google login successful",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"Google login failed",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,NewsFeedActivity.class));
        finish();
    }
}
