package oss.abnd.volumiospotifyhelper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import oss.abnd.volumiospotifyhelper.ViewAndUpdatePreferencesActivity;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class ShareActivity extends Activity {

    SpotifyUri sUri_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String hostname = sharedPref.getString(getString(R.string.hostname), "" );

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String ipAddress = prefs.getString(ViewAndUpdatePreferencesActivity.IP_VOLUMIO,"");
                Boolean replacePlaylist = prefs.getBoolean(ViewAndUpdatePreferencesActivity.REPLACE_PLAYLIST, false);
                handleVolumioConnection(ipAddress, replacePlaylist);
            }
        } else {
            finish();
        }
    }
    
    void handleVolumioConnection(String ipAddress, Boolean replacePlaylist) {
    	VolumioConnection vConn = new VolumioConnection(ipAddress);
    	
    	AsyncTask task =  null;
    	if (replacePlaylist) {
    		task = vConn.replaceSpotifyPlaylist(sUri_);
    	} else {
    		task = vConn.addSpotifyPlaylist(sUri_);
    	}
  
        long result = -1;
        try {
        	result = (long) task.get( 10, TimeUnit.SECONDS );
	        task.cancel(true);
	    } catch (TimeoutException te) {
	        showErrorToast("Timeout while sharing link.");
	        finish();
	        return;
	    } catch (Exception e) {
	        showErrorToast("Unknown error sharing link.");
	        finish();
	        return;
	    }
	
	    if (result == 0) {
	        showToast("Success sending link to Volumio.");
	    } else {
	        showToast("Error sending link to Volumio.");
	    }
	    finish();
    }
    
    void handleSendText( Intent intent ) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // showToast( sharedText );
            if (sharedText.contains("open.spotify.com")) {
                SpotifyUri sUri = new SpotifyUri();
                try {
                    sUri.fromOpenUrl(sharedText);
                } catch (MalformedURLException e) {
                    showErrorToast("Malformed URL");
                }

                sUri_ = sUri;
            } else {
                // Not a spotify url, ignore.
                finish();
            }
        }
    }

    void showErrorToast( String str ) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, "Error: " + str , duration);
        toast.show();
    }

    void showToast( String str ) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, "Info: " + str , duration);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
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
}
