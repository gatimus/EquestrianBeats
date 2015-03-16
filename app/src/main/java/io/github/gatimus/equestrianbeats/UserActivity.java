package io.github.gatimus.equestrianbeats;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.github.gatimus.equestrianbeats.eqb.EQBeats;
import io.github.gatimus.equestrianbeats.eqb.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class UserActivity extends Activity implements Callback<User> {

    public static final String EXTRA_USER_ID = "io.github.gatimus.equestrianbeats.EXTRA_USER_ID";

    private int userID;
    private ActionBar actionBar;
    private ImageView avatar;
    private TextView name;
    private WebView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            userID = savedInstanceState.getInt(EXTRA_USER_ID);
        }
        else if(getIntent().getData() != null) {
            String uri = getIntent().getData().toString();
            userID = Integer.parseInt(uri.substring(uri.lastIndexOf("/")+1));
        } else {
            userID = getIntent().getIntExtra(EXTRA_USER_ID,0);
        }

        setContentView(R.layout.activity_user);

        actionBar = getActionBar();
        avatar = (ImageView) findViewById(R.id.avatar);
        name = (TextView) findViewById(R.id.name);
        description = (WebView)this.findViewById(R.id.description);
        description.getSettings().setJavaScriptEnabled(true);

        EQBeats.getEQBInterface().getUser(userID, this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(EXTRA_USER_ID, userID);
    }


/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/

    @Override
    public void success(User user, Response response) {
        actionBar.setTitle(user.toString());

        if(!user.avatar.isEmpty()){
            Picasso picasso = Picasso.with(getApplicationContext());
            if(BuildConfig.DEBUG) picasso.setIndicatorsEnabled(true);
            picasso.load(user.avatar)
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .resize(80,80)
                    .into(avatar);
        }

        name.setText(user.toString());

        description.loadDataWithBaseURL("", user.htmlDescription, "text/html", "UTF-8", "");
    }

    @Override
    public void failure(RetrofitError error) {
        Log.e(getClass().getSimpleName(), error.toString());
    }

}
