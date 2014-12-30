package oss.abnd.volumiospotifyhelper;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhinandh on 12/28/14.
 */
public class VolumioConnection {
    private static final String TAG = "VolumioConnection";
    private static final String VOLUMIO_PATH = "/db/index.php";

    private HttpClient httpClient_;

    public static long IoEx = -1;
    public static long ClientProtoEx = -2;

    private String hostname_;

    VolumioConnection( String hostname ) {
        hostname_ = hostname;
        httpClient_ = new DefaultHttpClient();
    }

    class NetworkTask extends AsyncTask< List<NameValuePair>, Integer, Long >
    {
        private String volumioUrl_;

        NetworkTask( String volumioUrl ) {
            volumioUrl_ = volumioUrl;
        }
        protected Long doInBackground(List<NameValuePair>... attrs) {
            return POSTData( volumioUrl_, attrs[0] );
        }

        protected void onPostExecute(Long result) {
            Log.d( TAG, "onPostExecute()" );
        }
    };

    AsyncTask replaceSpotifyPlaylist( SpotifyUri spotifyUri ) {
        final String volumioUrl = "http://" + hostname_ + VOLUMIO_PATH + "?cmd=spop-playtrackuri";
        List<NameValuePair> postAttr = new ArrayList<NameValuePair>(1);
        postAttr.add( new BasicNameValuePair( "path", spotifyUri.toUri() ) );

        NetworkTask task = new NetworkTask( volumioUrl );
        task.execute( postAttr );
        return task;
    }

    AsyncTask addSpotifyPlaylist( SpotifyUri spotifyUri ) {
        final String volumioUrl = "http://" + hostname_ + VOLUMIO_PATH + "?cmd=spop-addtrackuri";
        List<NameValuePair> postAttr = new ArrayList<NameValuePair>(1);
        postAttr.add( new BasicNameValuePair( "path", spotifyUri.toUri() ) );

        NetworkTask task = new NetworkTask( volumioUrl );
        task.execute( postAttr );
        return task;
    }

    private long POSTData( String url, List<NameValuePair> nameValuePairs ) {
        HttpPost httppost = new HttpPost( url );
        try {
            httppost.setEntity( new UrlEncodedFormEntity( nameValuePairs ) );
            // Execute HTTP Post Request
            HttpResponse response = httpClient_.execute(httppost);
        } catch (ClientProtocolException e) {
            Log.e( TAG, "ClientProtocolException" );
            return ClientProtoEx;
        } catch (IOException e) {
            Log.e( TAG, "IOException" );
            return IoEx;
        }
        return 0;
    }
}
