package oss.abnd.volumiospotifyhelper;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by abhinandh on 12/28/14.
 */
public class SpotifyUri {

    private static final String TAG = "SpotifyUri";

    /* Type of this spotify uri. It can be one of
               1. playlist
               2. track
               3. album
               4. artist
               6. local
               7. search
         */
    String type;
    boolean isStarred; // True if this is a starred playlist.

    String artist;
    String album;
    String track;
    int seconds;
    String id;
    String user;

    SpotifyUri() {
        isStarred = false;
        seconds = -1;
    }

    String toUri() {
        String out = null;
        if (type.equals("playlist")) {
            out = "spotify:user:" + user;
            if ( isStarred ) {
                out += ":starred";
            } else {
                out += ":playlist:" + id;
            }
        } else if (type.equals("artist") || type.equals("album") || type.equals("track")) {
            out = "spotify:" + type + ":" + id;
        } else {
           Log.w( TAG, "toUri(): !Implemented" );
        }
        return out;
    }

    private void parseSpotifyParts(String[] parts) {
        int len = parts.length;
        if (parts[0].equals("search")) {
            Log.w( TAG, "parseParts(): !Implemented" );
        } else if (len >= 3 && parts[1].equals("local")) {
            Log.w( TAG, "parseparts(): !Implemented" );
        } else if (len >= 5) {
            // Playlist
            type = parts[3];
            id = parts[4];
            user = parts[2];
        } else if (len >= 4 && parts[3].equals("starred")) {
            // Starred
            type = "playlist";
            isStarred = true;
            user = parts[2];
        } else if (len >= 3) {
            // track, artist, album
            type = parts[1];
            id = parts[2];
        }
    }

    void fromOpenUrl(String str) throws MalformedURLException {
        URL url;
        url = new URL(str);
        if (!url.getHost().equals("open.spotify.com")) {
            throw new MalformedURLException("Not a spotify open http url");
        }
        String[] parts = url.getPath().split("/");
        parseSpotifyParts(parts);
    }

}
