package com.robsexample.chapter6;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;

public class MyGLRenderer implements GLSurfaceView.Renderer 
{
	private Context m_Context;
	
	private PointLight m_PointLight;
	private Camera m_Camera;
	
	private int m_ViewPortWidth;
    private int m_ViewPortHeight;
	
	private Cube m_Cube;
	private Cube mCube2;
	
	private GravityGridEx mGrid;
	
	private Vector3 mForce1 = new Vector3(0, 20, 0);
	private float mRotationalForce = 3;
	
	private SoundPool mSoundPool;
	private int mSoundIndex1;
	private int mSoundIndex2;
	private boolean mSFXOn = true;
	
	private Texture[] mCharacterSetTextures = new Texture[BillboardCharacterSet.MAX_CHARACTERS];
	
	/* character set for HUD */
	private BillboardCharacterSet mCharacterSet = null;
	
	/* texture used for an HUD item */
	private Texture mHUDTexture = null;
	
	/* holds reference to billboard to be used for HUD item */
	private Billboard mHUDComposite = null;
	
	private HUD mHUD = null;
	
	private int mHealth = 100;
	
	private int mScore = 0;
	
	public MyGLRenderer(Context context) 
	{
	   m_Context = context; 
	}
	
	 void SetupLights()
	 {
		 // Set Light Characteristics
	     Vector3 LightPosition = new Vector3(0,125,125);
	     
	     float[] AmbientColor = new float [3];
	     AmbientColor[0] = 0.0f;
	     AmbientColor[1] = 0.0f;
	     AmbientColor[2] = 0.0f;  
	        
	     float[] DiffuseColor = new float[3];
	     DiffuseColor[0] = 1.0f;
	     DiffuseColor[1] = 1.0f;
	     DiffuseColor[2] = 1.0f;
	        
	     float[] SpecularColor = new float[3];
	     SpecularColor[0] = 1.0f;
	     SpecularColor[1] = 1.0f;
	     SpecularColor[2] = 1.0f;
	          
	     m_PointLight.SetPosition(LightPosition);
	     m_PointLight.SetAmbientColor(AmbientColor);
	     m_PointLight.SetDiffuseColor(DiffuseColor);
	     m_PointLight.SetSpecularColor(SpecularColor);
	 }
	   
	 void SetupCamera()
	 {	
		// Set Camera View
		 Vector3 Eye = new Vector3(0,0,8);
	     Vector3 Center = new Vector3(0, 0,-1);
	     Vector3 Up = new Vector3(0,1,0);  
	        
	     float ratio = (float) m_ViewPortWidth / m_ViewPortHeight;
	     float Projleft	= -ratio;
	     float Projright = ratio;
	     float Projbottom= -1;
	     float Projtop	= 1;
	     float Projnear	= 3;
	     float Projfar	= 50; //100;
	    	
	     m_Camera = new Camera(m_Context,
	        				   Eye,
	        				   Center,
	        				   Up,
	        				   Projleft, Projright, 
	        				   Projbottom,Projtop, 
	        				   Projnear, Projfar);
	  }
	 
	 void createGrid(Context ctx) {
		 Vector3 gridColor = new Vector3(0, 0, 0.3f);
		 float gridHeight = -0.5f;
		 float gridStartZValue = -15;
		 float gridStartXValue = -15;
		 float gridSpacing = 1.f;
		 int gridSizeZ = 33;
		 int gridSizeX = 33;
		 
		 Shader shader = new Shader(ctx, R.raw.vsgrid, R.raw.fslocalaxis);
		 
		 mGrid = new GravityGridEx(ctx, gridColor, gridHeight, gridStartZValue, gridStartXValue, gridSpacing, gridSizeZ, gridSizeX, shader);
	 }
	    
	 void CreateCube(Context iContext)
	 {
		 //Create Cube Shader
		 Shader Shader = new Shader(iContext, R.raw.vsonelight, R.raw.fsonelight);	// ok
    	         
		 //MeshEx(int CoordsPerVertex, 
		 //		int MeshVerticesDataPosOffset, 
		 //		int MeshVerticesUVOffset , 
		 //		int MeshVerticesNormalOffset,
		 //		float[] Vertices,
		 //		short[] DrawOrder
		 MeshEx CubeMesh = new MeshEx(8,0,3,5,Cube.CubeData, Cube.CubeDrawOrder);
        
		 // Create Material for this object
		 Material Material1 = new Material();
		 //Material1.SetEmissive(0.0f, 0, 0.25f);
    
       
		 // Create Texture
		 Texture TexAndroid = new Texture(iContext,R.drawable.ic_launcher);		
        
		 Texture[] CubeTex = new Texture[1];
		 CubeTex[0] = TexAndroid;
           
        
		 m_Cube = new Cube(iContext, 
				 		   CubeMesh, 
        				   CubeTex, 
        				   Material1, 
        				   Shader);
         m_Cube.getPhysics().setGravity(true);
         
		 // Set Intial Position and Orientation
		 Vector3 Axis = new Vector3(0,1,0);
		 Vector3 Position = new Vector3(0.0f, 4.0f, 0.0f);
		 Vector3 Scale = new Vector3(1.0f,1.0f,1.0f);
        
		 m_Cube.m_Orientation.SetPosition(Position);
		 m_Cube.m_Orientation.SetRotationAxis(Axis);
		 m_Cube.m_Orientation.SetScale(Scale);
		 
		 /* setting spotlight variables */
		 float[] gridColor = new float[3];
		 gridColor[0] = 1;
		 gridColor[1] = 0;
		 gridColor[2] = 0;
		 
		 m_Cube.mSpotlightColor = gridColor;
		 m_Cube.getPhysics().mMassEffectiveRadius = 6;
		 
		 //m_Cube.m_Orientation.AddRotation(45);
     	
	 }
	 
	 void createCube2(Context ctx) {
		//Create Cube Shader
		 Shader shader = new Shader(ctx, R.raw.vsonelight, R.raw.fsonelight);	// ok
    	         
		 //MeshEx(int CoordsPerVertex, 
		 //		int MeshVerticesDataPosOffset, 
		 //		int MeshVerticesUVOffset , 
		 //		int MeshVerticesNormalOffset,
		 //		float[] Vertices,
		 //		short[] DrawOrder
		 MeshEx cubeMesh = new MeshEx(8,0,3,5,Cube.CubeData, Cube.CubeDrawOrder);
        
		 // Create Material for this object
		 Material material = new Material();
		 //Material1.SetEmissive(0.0f, 0, 0.25f);
    
       
		 // Create Texture
		 Texture texAndroid = new Texture(ctx,R.drawable.ic_launcher);		
        
		 Texture[] cubeTex = new Texture[1];
		 cubeTex[0] = texAndroid;
		 
		 mCube2 = new Cube(ctx, cubeMesh, cubeTex, material, shader);
		 
		 Vector3 axis = new Vector3(0, 1, 0);
		 Vector3 position = new Vector3(0, 8, 0);
		 Vector3 scale = new Vector3(1, 1, 1);
		 
		 mCube2.m_Orientation.SetRotationAxis(axis);
		 mCube2.m_Orientation.SetPosition(position);
		 mCube2.m_Orientation.SetScale(scale);
		 
		 mCube2.getPhysics().setGravity(true);

		 /* setting spotlight variables */
		 float[] gridColor = new float[3];
		 gridColor[0] = 0;
		 gridColor[1] = 1;
		 gridColor[2] = 0;
		 
		 m_Cube.mSpotlightColor = gridColor;
		 m_Cube.getPhysics().mMassEffectiveRadius = 6;
	 }
    
	
    	@Override
    	public void onSurfaceCreated(GL10 unused, EGLConfig config) 
    	{
    		m_PointLight = new PointLight(m_Context);
    		SetupLights();
    		
    		CreateCube(m_Context);
    		createCube2(m_Context);
    		    		
    		createGrid(m_Context);
    		
    		createSoundPool();
    		createSound(m_Context);
    		
    		m_ViewPortHeight = m_Context.getResources().getDisplayMetrics().heightPixels;
    		m_ViewPortWidth = m_Context.getResources().getDisplayMetrics().widthPixels;
    		SetupCamera();
    		createCharacterSet(m_Context);
    		createHUD();
    		
    		loadGameState();
    	}

    	@Override
    	public void onSurfaceChanged(GL10 unused, int width, int height) 
    	{
    		// Ignore the passed-in GL10 interface, and use the GLES20
            // class's static methods instead.
            GLES20.glViewport(0, 0, width, height);
           
            m_ViewPortWidth = width;
            m_ViewPortHeight = height;
            
            SetupCamera();
    	}
    	
    	void updateGravityGrid()
    	{
    		mGrid.resetGrid();
    		
    		/* add cubes to grid */
    		mGrid.addMass(m_Cube);
    	}

    	@Override
    	public void onDrawFrame(GL10 unused) 
    	{
    		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
    		
    		m_Camera.UpdateCamera();
    		 	 
    		// Add Rotation to Cube
    		m_Cube.m_Orientation.AddRotation(1);
    		 
    		//update physics
    		m_Cube.updateObject3d();
    		boolean hitGround = m_Cube.getPhysics().getHitGroundStatus();
    		if(hitGround) {
    		    m_Cube.getPhysics().applyTranslationalForce(mForce1);
    			m_Cube.getPhysics().applyRotationalForce(mRotationalForce, 10.0f);
    			m_Cube.getPhysics().clearHitGroundStatus();
    		}
    		 
    		//cube 2
    		mCube2.updateObject3d();
    		
    		Physics.CollisionStatus col = m_Cube.getPhysics()
    			 .checkForCollisionSphereBounding(m_Cube, mCube2);
    		
    		if(col == Physics.CollisionStatus.COLLISION || 
    			col == Physics.CollisionStatus.PENETRATING_COLLISION) {
    		    m_Cube.getPhysics().applyLinearImpulse(m_Cube, mCube2);
    		    
    		    //m_Cube.playSound(mSoundIndex1);
    		    //m_Cube.playSound(mSoundIndex2);
    		    
    		    mHealth = mHealth < 1 ? 100 : mHealth-1;
    		    mScore = mScore > 99 ? 0 : mScore+1;
    		}
    		  
    		m_Cube.DrawObject(m_Camera, m_PointLight);
    		mCube2.DrawObject(m_Camera, m_PointLight);
    		 
    		updateGravityGrid();
    	    mGrid.drawGrid(m_Camera);
    	    
    	    updateHUD();
    	    mHUD.updateHUD(m_Camera);
    	    mHUD.renderHUD(m_Camera, m_PointLight);
    	    
    	}
    	
    	void createSoundPool()
    	{
    		int maxStreams = 10;
    		int streamType = AudioManager.STREAM_MUSIC;
    		int srcQuality = 0;
    		
    		mSoundPool = new SoundPool(maxStreams, streamType, srcQuality);
    		
    		if(mSoundPool == null) {
    			Log.e("RENDERER", "mSoundPool creating failure!");
    		}
    	}
    	
    	void createSound(Context ctx) {
    		mSoundIndex1 = m_Cube.addSound(mSoundPool, R.raw.boink);
    		m_Cube.setSFXOnOff(mSFXOn);
    		mSoundIndex2 = mCube2.addSound(mSoundPool, R.raw.boink2);
    		mCube2.setSFXOnOff(mSFXOn);
    	}
    	
    	void createCharacterSetTextures(Context iContext) {
    		/* numeric */
    		mCharacterSetTextures[0] = new Texture(iContext, R.drawable.charset0);
    		mCharacterSetTextures[1] = new Texture(iContext, R.drawable.charset1);
    		mCharacterSetTextures[2] = new Texture(iContext, R.drawable.charset2);
    		mCharacterSetTextures[3] = new Texture(iContext, R.drawable.charset3);
    		mCharacterSetTextures[4] = new Texture(iContext, R.drawable.charset4);
    		mCharacterSetTextures[5] = new Texture(iContext, R.drawable.charset5);
    		mCharacterSetTextures[6] = new Texture(iContext, R.drawable.charset6);
    		mCharacterSetTextures[7] = new Texture(iContext, R.drawable.charset7);
    		mCharacterSetTextures[8] = new Texture(iContext, R.drawable.charset8);
    		mCharacterSetTextures[9] = new Texture(iContext, R.drawable.charset9);
    		 
    		// Alphabet
    		mCharacterSetTextures[10] = new Texture(iContext, R.drawable.charseta);
    		mCharacterSetTextures[11] = new Texture(iContext, R.drawable.charsetb);
    		mCharacterSetTextures[12] = new Texture(iContext, R.drawable.charsetc);
    		mCharacterSetTextures[13] = new Texture(iContext, R.drawable.charsetd);
    		mCharacterSetTextures[14] = new Texture(iContext, R.drawable.charsete);
    		mCharacterSetTextures[15] = new Texture(iContext, R.drawable.charsetf);
    		mCharacterSetTextures[16] = new Texture(iContext, R.drawable.charsetg);
    		mCharacterSetTextures[17] = new Texture(iContext, R.drawable.charseth);
    		mCharacterSetTextures[18] = new Texture(iContext, R.drawable.charseti);
    		mCharacterSetTextures[19] = new Texture(iContext, R.drawable.charsetj);
    		mCharacterSetTextures[20] = new Texture(iContext, R.drawable.charsetk);
    		mCharacterSetTextures[21] = new Texture(iContext, R.drawable.charsetl);
    		mCharacterSetTextures[22] = new Texture(iContext, R.drawable.charsetm);
    		mCharacterSetTextures[23] = new Texture(iContext, R.drawable.charsetn);
    		mCharacterSetTextures[24] = new Texture(iContext, R.drawable.charseto);
    		mCharacterSetTextures[25] = new Texture(iContext, R.drawable.charsetp);
    		mCharacterSetTextures[26] = new Texture(iContext, R.drawable.charsetq);
    		mCharacterSetTextures[27] = new Texture(iContext, R.drawable.charsetr);
    		mCharacterSetTextures[28] = new Texture(iContext, R.drawable.charsets);
    		mCharacterSetTextures[29] = new Texture(iContext, R.drawable.charsett);
    		mCharacterSetTextures[30] = new Texture(iContext, R.drawable.charsetu);
    		mCharacterSetTextures[31] = new Texture(iContext, R.drawable.charsetv);
    		mCharacterSetTextures[32] = new Texture(iContext, R.drawable.charsetw);
    		mCharacterSetTextures[33] = new Texture(iContext, R.drawable.charsetx);
    		mCharacterSetTextures[34] = new Texture(iContext, R.drawable.charsety);
    		mCharacterSetTextures[35] = new Texture(iContext, R.drawable.charsetz);
    		
    		// debug symbols
    		mCharacterSetTextures[36] = new Texture(iContext, R.drawable.charsetcolon);
    		mCharacterSetTextures[37] = new Texture(iContext, R.drawable.charsetsemicolon);
    		mCharacterSetTextures[38] = new Texture(iContext, R.drawable.charsetcomma);
    		mCharacterSetTextures[39] = new Texture(iContext, R.drawable.charsetequals);
    		mCharacterSetTextures[40] = new Texture(iContext, R.drawable.charsetleftparen);
    		mCharacterSetTextures[41] = new Texture(iContext, R.drawable.charsetrightparen);
    		mCharacterSetTextures[42] = new Texture(iContext, R.drawable.charsetdot);
    	}
    	
    	void setupHUDComposite(Context ctx) {
    		mHUDTexture = new Texture(ctx, R.drawable.hud);
    		
    		Shader shader = new Shader(ctx, R.raw.vsonelight, R.raw.fsonelight);
    		MeshEx mesh = new MeshEx(8, 0, 3, 5, Cube.CubeData, Cube.CubeDrawOrder);
    		
    		Material material = new Material();
    		material.SetEmissive(1f, 1f, 1f);
    		
    		Texture[] tex = new Texture[1];
    		tex[0] = mHUDTexture;
    		
    		mHUDComposite = new Billboard(ctx, mesh, tex, material, shader);
    		
    		Vector3 pos = new Vector3(0, 3f, 0);
    		Vector3 scale = new Vector3(1f, .1f, .01f);
    		
    		mHUDComposite.m_Orientation.SetPosition(pos);
    		mHUDComposite.m_Orientation.SetScale(scale);
    		
    		mHUDComposite.getPhysics().setGravity(false);
    		
    		mHUDComposite.getMaterial().SetAlpha(1f);
    		mHUDComposite.mBlend = true;
    	}
    	
    	/* creates new BillboardCharacterSet to use for HUD */
    	void createCharacterSet(Context ctx) {
    		Shader shader = new Shader(ctx, R.raw.vsonelight, R.raw.fsonelightnodiffuse);
    		
    		MeshEx mesh = new MeshEx(8, 0, 3, 5, Cube.CubeData, Cube.CubeDrawOrder);
    		
    		Material material = new Material();
    		material.SetEmissive(1, 1, 1);
    		
    		createCharacterSetTextures(ctx);
    		
    		setupHUDComposite(ctx);
    		
    		mCharacterSet = new BillboardCharacterSet();
    		
    		int numberChars = 43;
    		
    		char[] Characters = new char[BillboardCharacterSet.MAX_CHARACTERS];
    		Characters[0] = '1';
    		Characters[1] = '2';
    		Characters[2] = '3';
    		Characters[3] = '4';
    		Characters[4] = '5';
    		Characters[5] = '6';
    		Characters[6] = '7';
    		Characters[7] = '8';
    		Characters[8] = '9';
    		Characters[9] = '0';
    		Characters[10] = 'a';
    		Characters[11] = 'b';
    		Characters[12] = 'c';
    		Characters[13] = 'd';
    		Characters[14] = 'e';
    		Characters[15] = 'f';
    		Characters[16] = 'g';
    		Characters[17] = 'h';
    		Characters[18] = 'i';
    		Characters[19] = 'j';
    		Characters[20] = 'k';
    		Characters[21] = 'l';
    		Characters[22] = 'm';
    		Characters[23] = 'n';
    		Characters[24] = 'o';
    		Characters[25] = 'p';
    		Characters[26] = 'q';
    		Characters[27] = 'r';
    		Characters[28] = 's';
    		Characters[29] = 't';
    		Characters[30] = 'u';
    		Characters[31] = 'v';
    		Characters[32] = 'w';
    		Characters[33] = 'x';
    		Characters[34] = 'y';
    		Characters[35] = 'z';
    		Characters[36] = ':';
    		Characters[37] = ';';
    		Characters[38] = ',';
    		Characters[39] = '=';
    		Characters[40] = '(';
    		Characters[41] = ')';
    		Characters[42] = '.';
    		
    		for(int i = 0; i < numberChars; ++i) {
    			Texture[] tex = new Texture[1];
    			tex[0] = mCharacterSetTextures[i];
    			
    			BillboardFont font = new BillboardFont(ctx, mesh, tex, material, shader, Characters[i]);
    			font.getPhysics().setGravity(false);
    			mCharacterSet.addToCharacterSet(font);
    		}
    	}
    	
    	void createHealthItem() {
    		Texture HUDTexture = new Texture(m_Context, R.drawable.hud);
    		
    		Shader shader = new Shader(m_Context, R.raw.vsonelight, R.raw.fsonelightnodiffuse);
    		
    		MeshEx mesh = new MeshEx(8, 0, 3, 5, Cube.CubeData, Cube.CubeDrawOrder);
    		
    		Material material = new Material();
    		material.SetEmissive(1, 1, 1);
    		
    		Texture[] tex = new Texture[1];
    		tex[0] = HUDTexture;
    		
    		Billboard HUDHealthComposite = 
    				new Billboard(m_Context, mesh, tex, material, shader);
    		
    		Vector3 scale = new Vector3(1,.1f,.01f);
    		HUDHealthComposite.m_Orientation.SetScale(scale);
    		HUDHealthComposite.getPhysics().setGravity(false);
    		
    		/* set black portion of HUD to be transparent */
    		HUDHealthComposite.getMaterial().SetAlpha(1.f);
    		HUDHealthComposite.mBlend = true;
    		
    		/* create heath HUD */
    		Texture healthTexture = new Texture(m_Context, R.drawable.health);
    		Vector3 screenPosition = new Vector3(.8f, m_Camera.GetCameraViewportHeight()/2, .5f);
    		
    		HUDItem HUDHealth = new HUDItem("health", mHealth, screenPosition, mCharacterSet, healthTexture, HUDHealthComposite);
    		if(mHUD.addHUDItem(HUDHealth) == false) {
    			Log.e("ADDHUDITEM", "CANNOT ADD IN HEALTH HUD ITEM");
    		}
    	}
    	
    	void createHUD() {
    		mHUD = new HUD(m_Context);
    		
    		Vector3 screenPos = new Vector3(-m_Camera.GetCameraViewportWidth()/2 + .3f,
    				m_Camera.GetCameraViewportHeight()/2, .5f);
    		
    		HUDItem HUDscore = new HUDItem("score", 0, screenPos, mCharacterSet, null, mHUDComposite);
    		mHUD.addHUDItem(HUDscore);
    		createHealthItem();
    	}
    	
    	void updateHUD() {
    		mHUD.updateHUDItemNumericalValue("health", mHealth);
    		mHUD.updateHUDItemNumericalValue("score", mScore);
    	}
    	
    	void saveCubes() {
    		m_Cube.saveObjectState("Cube1Data");
    		mCube2.saveObjectState("Cube2Data");
    	}
    	
    	void loadCubes() {
    		m_Cube.loadObjectState("Cube1Data");
    		mCube2.loadObjectState("Cube2Data");
    	}
    	
    	void saveGameState() {
    		SharedPreferences settings = m_Context.getSharedPreferences("gamestate", 0);
    		SharedPreferences.Editor editor = settings.edit();
    		
    		editor.putInt("health", mHealth);
    		editor.putInt("score", mScore);
    		
    		saveCubes();
    		editor.putInt("previouslysaved", 1);
    		editor.commit();
    	}
    	
    	void loadGameState() {
    		SharedPreferences settings = m_Context.getSharedPreferences("gamestate", 0);
    		int previouslySaved = settings.getInt("previouslysaved", 0);
    		//SharedPreferences.Editor e = settings.edit();
    		//e.putInt("previouslysaved", 0);
    		
    		Log.d("SPS", settings.toString());
    		//previouslySaved = 0;
    		if(previouslySaved != 0) {
    			mScore = settings.getInt("score", 0);
    			mHealth = settings.getInt("health", 0);
    			loadCubes();
    		}
    	}
}

