package sr.listener;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

public class TheListenerActivity extends Activity implements NetworkListener, OnSharedPreferenceChangeListener {
	CustomMediaPlayer mp;
	
	private static final String TAG = "TheListenerActivity";
	private static final String VERSION = "0.0.7";
	
	protected static final int START_VIDEO = 0;
	SharedPreferences preferences;
    private static final int MENU_QUIT = 1;
    
    private NetworkObserver listener;
    private MjpegView mv;
	
    Handler mHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    	switch(msg.what){
    	     case START_VIDEO:
    	    	 /*Refresh UI*/
    	    	 //showVideo();
    	    	 showMjpegVideo();
    	    	 break;
    	   }
    	}
    };

	private Button button_video;

    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {    
	    menu.add(0, MENU_QUIT, 0, "Quit");
	    return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {    
        switch (item.getItemId()) {
            case MENU_QUIT:
                finish();
                return true;    
            }
        return false;
    }
    
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {   	
    	if (key.equals("port")) {
    		startNetworkListener();
    	}
	}    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        
        mp = new CustomMediaPlayer(this, R.raw.door);
        mp.setInterval(0);
        mv = (MjpegView) findViewById(R.id.mjpegView1);
        
        Button button_audio = (Button) findViewById(R.id.button_audio);
        button_audio.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mp.play(5);
			}
		});
        
        button_video = (Button) findViewById(R.id.button_video);
        button_video.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//showVideo();
				if (mv.isPlaying()) {
					Log.v(TAG, "is playing");
					button_video.setText(R.string.video_start);
					mv.stopPlayback();
				}
				else {
					Log.v(TAG, "is not playing");
					showMjpegVideo();
				}
			}
		});

        Button button_pref = (Button) findViewById(R.id.button_pref);
        button_pref.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
				startActivity(settingsActivity);
			}
		});
        
        Button finish = (Button) findViewById(R.id.button2);
        finish.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
				//String str = getLocalIpAddress();
				//Log.e(TAG, str);
			}
		});
        
        startNetworkListener();

        Thread NeObTh = new Thread(listener);
        NeObTh.start();
    }
    
    private void startNetworkListener() {
    	int port = getIntPrefs("port", NetworkObserver.DEFAULT_PORT);
        Log.v(TAG, "starting Network Observer on port: " + port);
        listener = new NetworkObserver(port);
        listener.addNetworkListener(this);
	}

	public int getIntPrefs(String key, int defValue) {
    	String value = preferences.getString(key, null);
        return value == null ? defValue : Integer.valueOf(value);
    }
    
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }
    
    
    private void showVideo() {
    	VideoView videoView = (VideoView) findViewById(R.id.videoView1);
		MediaController mediaController = new MediaController(this);
		mediaController.setAnchorView(videoView);
		// Set video link (mp4 format )
		
		//String url = "http://192.168.25.23:8081/file.avi";
		String url = preferences.getString("video_url", "");
		Log.v(TAG, "using URL: " + url);
		Uri video = Uri.parse(url);
		videoView.setMediaController(mediaController);
		videoView.setVideoURI(video);
		videoView.start();
    }
    
    
    private void showMjpegVideo() {
    	//VideoView videoView = (VideoView) findViewById(R.id.videoView1);
    	String url = preferences.getString("video_url", "");
    	Log.v(TAG, "starting video playback: " + url);
    	button_video.setText(R.string.video_stop);
    	
        //mv = new MjpegView(this);
        //setContentView(mv);
        mv.setSource(MjpegInputStream.read(url));
        mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        mv.showFps(false);
    }
    
	public void eventReceived(NetworkEvent event) {
		String type = event.eventtype;
		Log.v(TAG, "event received: " + type);
		
		if (type.equals("audio")) {
			mp.play(5);
		}
		else if (type.equals("video")) {
			//mp.play(1);
			
			mHandler.sendEmptyMessage(START_VIDEO);

			/*
			VideoView videoView = (VideoView) findViewById(R.id.videoView1);
			MediaController mediaController = new MediaController(this);
			mediaController.setAnchorView(videoView);
			// Set video link (mp4 format )
			String url = "rtsp://stream.zoovision.com/podcasts/HorizonChannelVideo.3gp";
			Uri video = Uri.parse(url);
			videoView.setMediaController(mediaController);
			videoView.setVideoURI(video);
			videoView.start();
			
			//String url = "http://192.168.1.126:81/videostream.cgi?user=admin&pwd=";
			//String url = "rtsp://stream.zoovision.com/podcasts/HorizonChannelVideo.3gp";
			
			/*
			MediaPlayer vid = new MediaPlayer();
			try {
				mp.setDataSource(url);
				mp.setDisplay(sh);
				mp.prepare();
				mp.start();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		
	}
}