package oss.abnd.volumiospotifyhelper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class ShareActivity extends Activity {

    SpotifyUri sUri_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    MaterialDialog dialog =
            new  MaterialDialog.Builder(this)
                .title("Share link with Volumio")
                .customView(R.layout.sharelayout, true)
                .positiveText("Share")
                .negativeText("Cancel")
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        dialog.getActionButton( DialogAction.POSITIVE ).setEnabled( false );

                        View customView = dialog.getCustomView();
                        EditText hostname = (EditText) customView.findViewById(R.id.hostname);
                        VolumioConnection vConn = new VolumioConnection(hostname.getText().toString());

                        Context context = getApplicationContext();
                        SharedPreferences sharedPref = context.getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.hostname), hostname.getText().toString() );
                        editor.commit();

                        RadioGroup radioGrp = (RadioGroup) customView.findViewById(R.id.radioGroup);
                        int checkedButtonId = radioGrp.getCheckedRadioButtonId();

                        AsyncTask task = null;
                        if (checkedButtonId == R.id.radioButtonAdd) {
                            task = vConn.addSpotifyPlaylist(sUri_);
                        } else if (checkedButtonId == R.id.radioButtonReplace) {
                            task = vConn.replaceSpotifyPlaylist(sUri_);
                        } else {
                            showErrorToast("Fatal error occurred.");
                            closeAct(dialog);
                            return;
                        }

                        long result = -1;
                        try {
                            result = (long) task.get( 10, TimeUnit.SECONDS );
                            task.cancel(true);
                        } catch (TimeoutException te) {
                            showErrorToast("Timeout while sharing link.");
                            closeAct(dialog);
                            return;
                        } catch (Exception e) {
                            showErrorToast("Unknown error sharing link.");
                            closeAct(dialog);
                            return;
                        }

                        if (result == 0) {
                            showToast("Success sending link to Volumio.");
                        } else {
                            showToast("Error sending link to Volumio.");
                        }
                        closeAct(dialog);
                    }

                    private void closeAct(MaterialDialog dialog) {
                        dialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        closeAct(dialog);
                    }
                })
                .dismissListener(new MaterialDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();

        EditText hostEditText = (EditText) dialog.getCustomView().findViewById(R.id.hostname);
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String hostname = sharedPref.getString(getString(R.string.hostname), "" );
        hostEditText.setText( hostname );

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        } else {
            finish();
        }
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
