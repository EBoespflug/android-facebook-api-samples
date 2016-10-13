/* MainActivity.java
 * 2016 - Etienne Boespflug
 * This file is dedicated to the public domain and is free to use (https://creativecommons.org/publicdomain/zero/1.0/).
 */
package fr.etienne_boespflug.samples.facebook_api_sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The MainActivity class represent the main activity of this example.
 * <p>
 * This activity allow the user to authenticate with Facebook and shows
 * his name, its e-mail address and its profile picture.
 * <p>
 * <p>
 * This project has been created for the blog http://etienne-boespflug.fr/blog/ (FR).
 *
 * @author Etienne Boespflug
 */
public class MainActivity extends AppCompatActivity {

    /** The Facebook login button. Allows the user to authenticate/disconnect. */
    LoginButton facebookLoginButton;
    /** Callback manager for the Facebook login button. */
    CallbackManager callbackManager;
    /** Tracker for the user's Facebook profile.*/
    ProfileTracker profileTracker;
    /** Tracker for the user's Facebook access token. */
    AccessTokenTracker accessTokenTracker;

    /**
     * Initialize the Facebook SDK and create the needed objects.
     * <p>
     * The ProfileTracker and the AccessTokenTracker are created, as well
     * as the CallbackManager.
     * <p>
     * Starts the traking of the profile and the access-token.
     * <p>
     * Set LoginButton permission and register it's callback.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Facebook SDK.
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        setContentView(R.layout.activity_main);

        callbackManager = CallbackManager.Factory.create();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                displayProfileData(newProfile);
            }
        };
        profileTracker.startTracking();

        displayGraphAPIResult(AccessToken.getCurrentAccessToken()); // /!\ unlike the ProfileTracker, the AccessTokenTracker is not always called on application start.
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                displayGraphAPIResult(newToken);
            }
        };
        accessTokenTracker.startTracking();

        facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions("email");  // See https://developers.facebook.com/docs/graph-api/reference/user.
        // facebookLoginButton.setFragment(this); // If using in a fragment.


        // Callback registration
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                displayProfileData(Profile.getCurrentProfile());
                displayGraphAPIResult(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() { /* NOP */ }

            @Override
            public void onError(FacebookException e) {
                Log.e("facebook_login", e.getMessage());
            }
        });
    }

    /**
     * This function display the profile data about the currently logged user.
     * <p>
     * If {@code profile} is {@code null}, the information are erased.
     *
     * @param profile the profile of the currently logged user.
     */
    private void displayProfileData(Profile profile) {
        TextView nameView = (TextView) findViewById(R.id.name_view);
        ImageView picView = (ImageView) findViewById(R.id.profile_picture_view);

        if(profile != null) {
            nameView.setText(profile.getName());
            new FetchProfilePictureTask(picView).execute(profile.getProfilePictureUri(300, 300));
        } else {
            nameView.setText("");
            picView.setImageResource(0);
        }
    }

    /**
     * Display the result of the GraphRequest to the user.
     * <p>
     * Here, the only requested field if the user email address which
     * is displayed in the corresponding TextView.
     * <p>
     * If the access token is null (user disconnection) or expired, the
     * data are removed.
     *
     * @param accessToken the AccessToken of the currently logged user.
     */
    private void displayGraphAPIResult(AccessToken accessToken) {
        final TextView emailView = (TextView) findViewById(R.id.email_view);

        // If access token is invalid, reset the content of the email TextView.
        if(accessToken == null || accessToken.isExpired())
            emailView.setText("");
        else {

            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            if(object == null) return;

                            String email = null;
                            try {
                                email = object.getString("email");
                            } catch (JSONException e) {
                                Log.e("graph_api_result", "Null email" + Log.getStackTraceString(e));
                            }
                            if(email != null) emailView.setText(email);
                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "email"); // See https://developers.facebook.com/docs/graph-api/reference/user.
            request.setParameters(parameters);
            request.executeAsync();
        }
    }

    /**
     * Stops tracking profile and access-token when the activity is stopped.
     */
    @Override
    protected void onStop() {
        super.onStop();
        profileTracker.stopTracking();
        accessTokenTracker.stopTracking();
    }

    /**
     * Refresh the user data when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        displayProfileData(Profile.getCurrentProfile());
        displayGraphAPIResult(AccessToken.getCurrentAccessToken());
    }

    /**
     * Passes the result arguments to the CallbackManager on activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        callbackManager.onActivityResult(requestCode, responseCode, intent);
    }
}
