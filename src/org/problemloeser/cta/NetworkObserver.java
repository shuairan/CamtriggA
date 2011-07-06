package org.problemloeser.cta;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

public class NetworkObserver implements Runnable {
	private static final String TAG = "NetworkObserver";
	public static int DEFAULT_PORT = 4444;
	private List<NetworkListener> _listeners = new ArrayList<NetworkListener>();
	
	private int port;
	
	public NetworkObserver() {
		this(DEFAULT_PORT);
	}
	
	public NetworkObserver(int port) {
		this.port = port;
	}

	public void run() {
		
		Log.v(TAG, "Starting Network Listener");
		
		ServerSocket serverSocket = null;
        boolean listening = true;

        try {
            serverSocket = new ServerSocket(port);
            Log.v(TAG, "Listening on port: " + port);
        } catch (IOException e) {
        	Log.e(TAG, "Could not listen on port: " + port);
            System.exit(-1);
        }
        
        try {
        	while (listening)
        		new NetworkObserverThread(serverSocket.accept(), this).start();

			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
    public void setPort(int port) {
    	this.port = port;
    }
	

	public synchronized void addNetworkListener( NetworkListener l ) {
        _listeners.add( l );
    }
    
    public synchronized void removeNetworkListener( NetworkListener l ) {
        _listeners.remove( l );
    }
    
    protected synchronized void _fireEvent(String event) {
		Log.v(TAG, "Event fired: " + event);
    	NetworkEvent netevent = new NetworkEvent(event);
        Iterator<NetworkListener> listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (NetworkListener) listeners.next() ).eventReceived( netevent );
        }
    }
}
