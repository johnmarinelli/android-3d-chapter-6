package com.robsexample.chapter6;


import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import android.opengl.GLSurfaceView;
import android.content.Context;

public class MainActivity extends Activity {
	
	private MyGLSurfaceView m_GLView;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

    	// Create a MyGLSurfaceView instance and set it
    	// as the ContentView for this Activity
    	m_GLView = new MyGLSurfaceView(this);
    	setContentView(m_GLView); 
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
		m_GLView.mRenderer.saveGameState();
    	m_GLView.onPause();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
    	m_GLView.onResume();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}

/////////////////////////////////////////////////////////////////////////////////////////

class MyGLSurfaceView extends GLSurfaceView 
{
	public MyGLRenderer mRenderer = null;
	
    public MyGLSurfaceView(Context context) 
    {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer(context);
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
    }
}



