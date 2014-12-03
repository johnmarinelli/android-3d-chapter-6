package com.robsexample.chapter6;

import com.robsexample.chapter6.MeshEx.MeshType;

import android.content.Context;
import android.opengl.Matrix;
import android.util.Log;

public class GravityGridEx {
	private Context mContext;
	
	public MeshEx mLineMeshGrid;
	
	/* x, y, z coords for each vertex */
	private int mCoordsPerVertex = 3;
	private int mMeshVerticesDataPosOffset = 0;
	
	/* there is no UV texture or vertex normals, so the offset is -1 */
	private int mMeshVerticesUVOffset = -1;
	private int mMeshVerticesNormalOffset = -1;
	
	/* holds vertex data for grid */
	private float[] mVertices;
	
	/* draw order for mVertices */
	private short[] mDrawOrder;
	
	/* # of masses on grid */
	public int mNumberMasses = 0;
	
	/* index into the array data for masses */
	private int  massesIndex = 0;
	
	/* maximum number of masses */
	public final int MAX_MASSES = 30;
	
	/* values of each mass on grid */
	private float[] mMassValues = new float[MAX_MASSES];
	
	/* values of locations of masses: x,y,z for each mass */
	private float[] mMassLocations = new float[MAX_MASSES*3];
	
	/* values for the radius in which to draw spotlight */
	private float[] mMassEffectiveRadius = new float[MAX_MASSES];
	
	/* spotlight color to place on grid for each mass in x,y,z */
	private float[] mMassSpotlightColor = new float[MAX_MASSES*3];
	
	/* shader for grid */
	private Shader mShader;
	
	/* link to vertex position in shader */
	private int mPositionHandle;
	
	/* color of grid */
	private Vector3 mGridColor;
	
	/* value to send to MVP matrix in shader */
	private float[] mMVPMatrix = new float[16];
	
	/* grid location boundaries along x,y,z axes */
	public float mXMinBoundary, mXMaxBoundary;
	private float mYMinBoundary, mYMaxBoundary;
	public float mZMinBoundary, mZMaxBoundary;
	
	/* create a grid of lines on XZ plane at gridHeight height
	 * of size gridSizeZ X gridSizeX in # of grid points */
	GravityGridEx(Context ctx, 
			Vector3 gridColor,
			float gridHeight,
			float gridStartZValue,
			float gridStartXValue,
			float gridSpacing,
			int gridSizeZ, 
			int gridSizeX,
			Shader shader) 
	{
		mContext = ctx;
		mShader = shader;
		mGridColor = gridColor;
			
		/* set grid bounds */
		float numberCellsX = gridSizeX - 1;
		float numberCellsZ = gridSizeZ - 1;
			
		mXMinBoundary = gridStartXValue;
		mXMaxBoundary = mXMinBoundary + (numberCellsX * gridSpacing);
			
		mZMinBoundary = gridStartZValue;
		mZMaxBoundary = mZMinBoundary + (numberCellsZ * gridSpacing);
			
		int numberVertices = gridSizeZ * gridSizeX;
		int totalNumberCoords = mCoordsPerVertex * numberVertices;
		
		Log.e("GRAVITYGRIDEX", "totalNumberCoords = " + totalNumberCoords);
		mVertices = new float[totalNumberCoords];
			
		/* create vertices */
		int index = 0;
			
		for(float z = 0; z < gridSizeZ; ++z) {
			for(float x = 0; x < gridSizeX; ++x) {
				/* determine world position of vertex */
				float xPos = gridStartXValue + (x*gridSpacing);
				float zPos = gridStartZValue + (z*gridSpacing);
				
				if(index >= totalNumberCoords) {
					Log.e("GRAVITYGRIDEX", "array out of bounds error, index >= totalNumberCoords");
				}
					
				/* assign vertex to array */
				mVertices[index] = xPos;
				mVertices[index+1] = gridHeight; //y coord
				mVertices[index+2] = zPos;
					
				index += 3;
			}
		}
		
		/* create drawlist for grid */
		int drawListEntriesX = (gridSizeX-1) * 2;
		int totalDrawListEntriesX = gridSizeZ * drawListEntriesX;
		
		int drawListEntriesZ = (gridSizeZ-1) * 2;
		int totalDrawListEntriesZ = gridSizeX * drawListEntriesZ;
		
		int totalDrawListEntries = totalDrawListEntriesX + totalDrawListEntriesZ;
		
		Log.e("GRAVITYGRIDEX", "TotalDrawListEntries = " + Integer.toString(totalDrawListEntries));
		mDrawOrder = new short[totalDrawListEntries];
		
		index = 0;
		/* create draw list for horizontal lines */
		for(int z = 0; z < gridSizeZ; ++z) {
			for(int x = 0; x < (gridSizeX-1); ++x) {
				if(index >= totalDrawListEntries) {
					Log.e("GRAVITYGRIDEX", "array out of bounds error - horizontal, index >= totalDrawListEntries");
				}
				
				int currentVertexIndex = (z*gridSizeX)+x;
				mDrawOrder[index] = (short)currentVertexIndex;
				mDrawOrder[index+1] = (short)(currentVertexIndex+1);
				
				index += 2;
			}
		}
		
		/* create draw list for vertical lines */
		for(int z = 0; z < (gridSizeZ - 1); ++z) {
			for(int x = 0; x < gridSizeX; ++x) {
				if(index >= totalDrawListEntries) {
					Log.e("GRAVITYGRIDEX", "array out of bounds error - vertical, index >= totalDrawListEntries");
				}
				
				int currentVertexIndex = (z*gridSizeX) + x;
				int vertexIndexBelowCurrent = currentVertexIndex + gridSizeX;
				mDrawOrder[index] = (short)currentVertexIndex;
				mDrawOrder[index+1] = (short)vertexIndexBelowCurrent;
				
				index += 2;
			}
		}
		
		/* create mesh */
		mLineMeshGrid = new MeshEx(mCoordsPerVertex, mMeshVerticesDataPosOffset, 
				mMeshVerticesUVOffset, mMeshVerticesNormalOffset, mVertices, mDrawOrder);
		mLineMeshGrid.mMeshType = MeshType.Lines;
		
		/* clear value of masses */
		clearMasses();
	}
	
	private void clearMasses() {
		for(int i = 0; i < MAX_MASSES; ++i) {
			mMassValues[i] = 0;
		}
	}
	
	void resetGrid() {
		/* clear grid of all masses */
		massesIndex = 0;
		mNumberMasses = 0;
		clearMasses();
	}
	
	boolean addMass(Object3d mass) {
		boolean result = true;
		
		/* mass & spotlights have 3 components */
		int massLocationIndex = massesIndex * 3; 
		int spotlightLocationIndex = massesIndex * 3;
		
		if(mNumberMasses >= MAX_MASSES) {
			result = false;
			return result;
		}
		
		float[] color;
		mMassValues[massesIndex] = mass.getPhysics().mMass;
		
		/* add x,y,z location of mass */
		mMassLocations[massLocationIndex] = mass.m_Orientation.GetPosition().x;
		mMassLocations[massLocationIndex+1] = mass.m_Orientation.GetPosition().y;
		mMassLocations[massLocationIndex+2] = mass.m_Orientation.GetPosition().z;
		
		massLocationIndex += 3;
		
		/* add radius of spotlight for mass */
		mMassEffectiveRadius[massesIndex] = mass.getPhysics().mMassEffectiveRadius;
		
		/* add spotlight color for mass */
		color = mass.mSpotlightColor;
		mMassSpotlightColor[spotlightLocationIndex] = color[0];
		mMassSpotlightColor[spotlightLocationIndex+1] = color[1];
		mMassSpotlightColor[spotlightLocationIndex+2] = color[2];
		
		spotlightLocationIndex += 3;
		
		massesIndex++;
		mNumberMasses++;
		
		return result;
	}
	
	boolean addMasses(int numberMasses, Object3d[] masses) {
		boolean result = true;
		
		int massLocationIndex = massesIndex * 3;
		int spotlightLocationIndex = massesIndex * 3;
		
		float[] color;
		
		for(int i = 0; i < numberMasses; ++i) {
			if(mNumberMasses >= MAX_MASSES) {
				return false;
			}
			
			mMassValues[massesIndex] = masses[i].getPhysics().mMass;
			
			mMassLocations[massLocationIndex] = masses[i].m_Orientation.GetPosition().x;
			mMassLocations[massLocationIndex+1] = masses[i].m_Orientation.GetPosition().y;
			mMassLocations[massLocationIndex+2] = masses[i].m_Orientation.GetPosition().z;
			
			massLocationIndex += 3;
			
			/* add radius of spotlight for mass */
			mMassEffectiveRadius[massesIndex] = masses[i].getPhysics().mMassEffectiveRadius;
			
			/* add spotlight color for each mass */
			color = masses[i].mSpotlightColor;
			mMassSpotlightColor[spotlightLocationIndex] = color[0];
			mMassSpotlightColor[spotlightLocationIndex+1] = color[1];
			mMassSpotlightColor[spotlightLocationIndex+2] = color[2];
			
			massesIndex++;
			mNumberMasses++;
		}
		
		return result;
	}
	
	void setupShader() {
		/* add program to OpenGL environment */
		mShader.ActivateShader();
		
		/* get handle to vertex shaders aPosition member */
		mPositionHandle = mShader.GetShaderVertexAttributeVariableLocation("aPosition");
		
		/* set gravity line variables */
		mShader.SetShaderUniformVariableValueInt("NumberMasses", mNumberMasses);
		mShader.SetShaderVariableValueFloatVector1Array("MassValues", MAX_MASSES, mMassValues, 0);
		mShader.SetShaderVariableValueFloatVector3Array("MassLocations", MAX_MASSES, mMassLocations, 0);
		mShader.SetShaderVariableValueFloatVector1Array("MassEffectiveRadius", MAX_MASSES, mMassEffectiveRadius, 0);
		mShader.SetShaderVariableValueFloatVector3Array("SpotLightColor", MAX_MASSES, mMassSpotlightColor, 0);
		
		/* set color of line */
		mShader.SetShaderUniformVariableValue("vColor", mGridColor);
		
		/* set view projection matrix */
		mShader.SetShaderVariableValueFloatMatrix4Array("uMVPMatrix", 1, false, mMVPMatrix, 0);
	}
	
	/* build MVP matrix */
	void generateMatrices(Camera c) {
		Matrix.multiplyMM(mMVPMatrix, 0, c.GetProjectionMatrix(), 0, c.GetViewMatrix(), 0);
	}
	
	/* create matrices, set up vertex shader, draws mesh */
	void drawGrid(Camera c) {
		/* set up shader */
		generateMatrices(c);
		setupShader();
		
		/* draw mesh */
		mLineMeshGrid.DrawMesh(mPositionHandle, -1, -1);
	}
}
