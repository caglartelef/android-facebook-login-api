package fikirlaboratuvari.com.fblogin;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity {

    private AccessTokenTracker accessTokenTracker;
    TextView profilName, profilEmail, profilLink, profilGender, profilLocale, profilTimezone, profilFriendsCount, profilId;
    LinearLayout lytbilgi, lytface;

    ProfilePictureView profilImg;

    String sprofilImg = "", sprofilName = "", sprofilEmail = "", sprofilLink = "", sprofilGender = "", sprofilLocale = "", sprofilTimezone = "", sprofilFriendsCount = "", sprofilId = "";

    LoginButton loginButton;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region Keyhash Oluşturma
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "fikirlaboratuvari.com.fblogin",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();

        }
        //endregion

        //region Komponent Tanımlamaları
        lytbilgi = (LinearLayout) findViewById(R.id.lytbilgi);
        lytface = (LinearLayout) findViewById(R.id.lytface);
        profilImg = (ProfilePictureView) findViewById(R.id.imgUrl);
        profilName = (TextView) findViewById(R.id.tvName);
        profilEmail = (TextView) findViewById(R.id.tvEmail);
        profilGender = (TextView) findViewById(R.id.tvGender);
        profilLocale = (TextView) findViewById(R.id.tvLocale);
        profilFriendsCount = (TextView) findViewById(R.id.tvCount);
        profilLink = (TextView) findViewById(R.id.tvLink);
        profilTimezone = (TextView) findViewById(R.id.tvTimezone);
        profilId = (TextView) findViewById(R.id.tvId);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        //endregion

        loginButton.setReadPermissions("email");
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.i("FaceLog", "Success");
                    }

                    @Override
                    public void onCancel() {
                        Log.i("FaceLog", "Cancelled");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.i("FaceLog", "Error");
                    }
                });

        if (AccessToken.getCurrentAccessToken() != null) {
            updateWithToken(AccessToken.getCurrentAccessToken());/*Erişim token yoksa bu bloga girer ve updateWithToken metodu çağırılır.*/
        }

        accessTokenTracker = new AccessTokenTracker() {/*Token yoksa yeni bir token oluşturulur.*/
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                updateWithToken(currentAccessToken);
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    private void updateWithToken(AccessToken currentAccessToken) {
        if (currentAccessToken != null) {
            Log.i("FaceLog", "Already Logged.");
            GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                    Log.i("FaceLog", jsonObject.toString());
                    Profile profile = Profile.getCurrentProfile();/*Kişi profil bilgileri çekilir.*/
                    if (profile != null) {
                        sprofilImg = profile.getProfilePictureUri(200, 200).toString();/*Kişinin profil resmi çekilir.*/
                        sprofilName = profile.getName();/*Kişinin adı çekilir.*/
                        sprofilLink = profile.getLinkUri().toString();/*Kişinin profil linki çekilir.*/
                        sprofilId = profile.getId();/*Kişinin facebook id si çekilir.*/
                        try {
                            sprofilLocale = jsonObject.getString("locale");/*Kişinin lokasyon bilgisi çekilir.*/
                            sprofilGender = jsonObject.getString("gender");/*Kişinin cinsiyet bilgisi çekilir.*/
                            sprofilTimezone = jsonObject.getString("timezone");/*Kişinin saat dilim bilgisi çekilir.*/
                            if (null == jsonObject.getString("email")) {
                                sprofilEmail = "";
                            } else {
                                sprofilEmail = jsonObject.getString("email");/*Kişi email adresinin çekilmesine izin verilirse mail bilgisi çekilir.*/
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + "me" + "/friends", null, HttpMethod.GET, new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {
                            Log.i("FaceLog", graphResponse.getJSONObject().toString());
                            try {
                                if (null == graphResponse.getJSONObject().getJSONObject("summary").getString("total_count")) {
                                    sprofilFriendsCount = "";
                                } else {
                                    sprofilFriendsCount = graphResponse.getJSONObject().getJSONObject("summary").getString("total_count");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            yazdir();/*Yazdır metoduna gidilerek alınan bilgiler ekranda gösterilir.*/

                        }
                    }).executeAsync();
                }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,locale,timezone");
            graphRequest.setParameters(parameters);
            graphRequest.executeAsync();

        } else {
            Log.i("FaceLog", "Not Logged.");

        }
    }

    protected void yazdir() {

        profilImg.setProfileId(sprofilId);
        profilName.setText(sprofilName);
        profilEmail.setText(sprofilEmail);
        profilLink.setText(sprofilLink);
        profilGender.setText(sprofilGender);
        profilLocale.setText(sprofilLocale);
        profilTimezone.setText(sprofilTimezone);
        //profilFriendsCount.setText(sprofilFriendsCount.toString());
        profilId.setText(sprofilId);
        lytbilgi.setVisibility(View.VISIBLE);
    }

}
