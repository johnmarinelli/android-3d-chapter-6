package com.robsexample.chapter6;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ShortBuffer;


public class MeshEx 
{
	enum MeshType
	{
		Triangles,
		Lines
	}
	
	// Use Indexed Drawing method
	private FloatBuffer m_VertexBuffer 		= null; // Holds mesh vertex data
	private ShortBuffer m_DrawListBuffer 	= null; // Holds index values into the vertex buffer that indicate 
													// the order in which to draw the vertices.
	private int m_CoordsPerVertex = 3;
	private static final int FLOAT_SIZE_BYTES = 4;
	private int m_VertexCount = 0;
	private int m_MeshVerticesDataStrideBytes = m_CoordsPerVertex * FLOAT_SIZE_BYTES;
	private int m_MeshVerticesDataPosOffset = 0;
	private int m_MeshVerticesDataUVOffset = -1;
	private int m_MeshVerticesDataNormalOffset = -1; 
	
	private boolean m_MeshHasUV = false;
	private boolean m_MeshHasNormals = false;
	
	private Vector3 mSize = new Vector3(0, 0, 0);
	private float mRadius;
	private float mRadiusAverage; //average of x,y,z of size
	public MeshType mMeshType = MeshType.Triangles;
	
	public MeshEx(int CoordsPerVertex, 
				int MeshVerticesDataPosOffset, 
				int MeshVerticesUVOffset , 
				int MeshVerticesNormalOffset,
				float[] Vertices,
				short[] DrawOrder
				)
	{
		m_CoordsPerVertex = CoordsPerVertex;
		m_MeshVerticesDataStrideBytes	= m_CoordsPerVertex * FLOAT_SIZE_BYTES;
		m_MeshVerticesDataPosOffset 	= MeshVerticesDataPosOffset;
		m_MeshVerticesDataUVOffset 		= MeshVerticesUVOffset ; 
		m_MeshVerticesDataNormalOffset 	= MeshVerticesNormalOffset;
		
		if (m_MeshVerticesDataUVOffset >= 0)
		{
			m_MeshHasUV = true;
		}
		
		if (m_MeshVerticesDataNormalOffset >=0)
		{
			m_MeshHasNormals = true;
		}
		
		// Allocate Vertex Buffer
		ByteBuffer bb = ByteBuffer.allocateDirect(
				    // (# of coordinate values * 4 bytes per float)
					Vertices.length * FLOAT_SIZE_BYTES);
	    bb.order(ByteOrder.nativeOrder());
	    m_VertexBuffer = bb.asFloatBuffer();
		
	    if (Vertices != null)
	    {
	    	m_VertexBuffer.put(Vertices);
	    	m_VertexBuffer.position(0);
	    	    	
	    	m_VertexCount = Vertices.length / m_CoordsPerVertex;
	    }
	    
	   // Initialize DrawList Buffer  
	   m_DrawListBuffer = ShortBuffer.wrap(DrawOrder);
	  
	}
	
	void SetUpMeshArrays(int PosHandle, int TexHandle, int NormalHandle)
	{
		//glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int offset)
		//glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr)
		
		// Set up stream to position variable in shader
		m_VertexBuffer.position(m_MeshVerticesDataPosOffset);
	    GLES20.glVertexAttribPointer(PosHandle, 
	    							3, 
	    							GLES20.GL_FLOAT, 
	    							false,
	    							m_MeshVerticesDataStrideBytes, 
	    							m_VertexBuffer);
	       
	    GLES20.glEnableVertexAttribArray(PosHandle);
	     
	    
	    if (m_MeshHasUV)
	    {
	    	// Set up Vertex Texture Data stream to shader  
	    	m_VertexBuffer.position(m_MeshVerticesDataUVOffset);
	    	GLES20.glVertexAttribPointer(TexHandle, 
	    								2, 
	    								GLES20.GL_FLOAT, 
	    								false,
	    								m_MeshVerticesDataStrideBytes, 
	    								m_VertexBuffer);
	    	GLES20.glEnableVertexAttribArray(TexHandle);
	    }
	   
	    if (m_MeshHasNormals)
	    {
	    	
	    	// Set up Vertex Texture Data stream to shader
	    	m_VertexBuffer.position(m_MeshVerticesDataNormalOffset);
	    	GLES20.glVertexAttribPointer(NormalHandle, 
	    								3, 
	    								GLES20.GL_FLOAT, 
	    								false,
	    								m_MeshVerticesDataStrideBytes, 
	    								m_VertexBuffer);
	    	GLES20.glEnableVertexAttribArray(NormalHandle);
	    } 
	    
	          
	    
	}
	
	public static void CheckGLError(String glOperation) 
	{
		int error;
	    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
	    	Log.e("ERROR IN MESHEX", glOperation + " IN CHECKGLERROR() : glError - " + error);
	        throw new RuntimeException(glOperation + ": glError " + error);
	    }
	}
	
	void DrawMesh(int PosHandle, int TexHandle, int NormalHandle)
	{
		SetUpMeshArrays(PosHandle, TexHandle, NormalHandle);
		
	   
		//glDrawElements (int mode, int count, int type, int offset)
		//glDrawElements (int mode, int count, int type, Buffer indices)
		if(mMeshType == MeshType.Triangles) { 
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, 
							  m_DrawListBuffer.capacity(),
							  GLES20.GL_UNSIGNED_SHORT, 
							  m_DrawListBuffer);
		}
		else if(mMeshType == MeshType.Lines) {
			GLES20.glDrawElements(GLES20.GL_LINES, 
								  m_DrawListBuffer.capacity(),
								  GLES20.GL_UNSIGNED_SHORT,
								  m_DrawListBuffer);
		}
	    // Disable vertex array
	    GLES20.glDisableVertexAttribArray(PosHandle);
	    CheckGLError("glDisableVertexAttribArray ERROR - PosHandle");
	    
	    if (m_MeshHasUV)
	    {
	    	GLES20.glDisableVertexAttribArray(TexHandle);	
	    	CheckGLError("glDisableVertexAttribArray ERROR - TexHandle");
	    }
	    if (m_MeshHasNormals)
	    {
	    	GLES20.glDisableVertexAttribArray(NormalHandle);	
	    	CheckGLError("glDisableVertexAttribArray ERROR - NormalHandle");
	    }
	}
	
	public void calculateRadius() 
	{
		float xMin = 100000000;
		float yMin = 100000000;
		float zMin = 100000000;
		float xMax = -xMin;
		float yMax = -yMin;
		float zMax = -zMin;
		
		int elementPos = m_MeshVerticesDataPosOffset;
		
		//loop through all vertices, and find max/min values of xyz
		for(int i = 0; i < m_VertexCount; ++i) {
			float x = m_VertexBuffer.get(elementPos);
			float y = m_VertexBuffer.get(elementPos+1);
			float z = m_VertexBuffer.get(elementPos+2);
			
			if(x < xMin) xMin = x;
			if(y < yMin) yMin = y;
			if(z < zMin) zMin = z;
			if(x > xMax) xMax = x;
			if(y > yMax) yMax = y;
			if(z > zMax) zMax = z;
			
			elementPos += m_CoordsPerVertex;
			
			// calculate size of mesh in xyz directions
			mSize.x = Math.abs(xMax - xMin);
			mSize.y = Math.abs(yMax - yMin);
			mSize.z = Math.abs(zMax - zMin);
			
			// calculate radius.
			// largestsize will be diameter
			float largestSize = -1;
			if(mSize.x > largestSize) largestSize = mSize.x;
			if(mSize.y > largestSize) largestSize = mSize.y;
			if(mSize.z > largestSize) largestSize = mSize.z;
			
			mRadius = largestSize / 2.0f;
			
			//calculate avg radius
			mRadiusAverage = (mSize.x + mSize.y + mSize.z) / 3.0f;
			mRadiusAverage /= 2.0f;
		}
	}
	
	public float getRadius() {
		return mRadius;
	}
}
