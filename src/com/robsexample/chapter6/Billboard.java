package com.robsexample.chapter6;

import android.content.Context;

public class Billboard extends Cube {

	Billboard(Context iContext, MeshEx iMeshEx, Texture[] iTextures,
			Material iMaterial, Shader iShader) {
		super(iContext, iMeshEx, iTextures, iMaterial, iShader);
		Vector3 scale = new Vector3(1f, 1f, 1f);
		m_Orientation.SetScale(scale);
	}
	
	void setBillboardTowardCamera(Camera cam) {
		/* get front vector of billboard objected projected on xz axis */
		Vector3 frontVectorProj = new Vector3(m_Orientation.GetForwardWorldCoords().x, 0, m_Orientation.GetForwardWorldCoords().z);
		
		/* get billboard position projected on xz axis */
		Vector3 billboardPosProj = new Vector3(m_Orientation.GetPosition().x, 0, m_Orientation.GetPosition().z);
		
		/* get position of camera projected on 2d xz plane */
		Vector3 cameraPosProj = new Vector3(cam.GetCameraEye().x, 0, cam.GetCameraEye().z);
		
		/* calculate vector from billboard to camera */
		Vector3 billboardToCameraVecProj = Vector3.Subtract(cameraPosProj, billboardPosProj);
		billboardToCameraVecProj.Normalize();
		
		/* find angle between forward of billboard object and camera
		 * p = forwardxy
		 * q = vector billboard camera
		 * p & q are normalised vectors
		 * p.q = p*q*cos(theta)
		 * p.q/p*q = cos(theta)
		 * acos(p.q/p*q) = theta
		 * 
		 * p.q > 0 ? angle btn vectors is < 90 deg
		 * p.q < 0 ? angle btn vectors is > 90 deg
		 * p.q = 0 ? angle btn vectors is = 90 deg
		 * 
		 * get current theta. returns 0-PI radians
		 */
		float theta = (float)Math.acos(frontVectorProj.DotProduct(billboardToCameraVecProj));
		float degreeTheta = theta * (180.f / Physics.PI);
		
		/* cross product to form rotation axis */
		Vector3 rotationAxis = Vector3.CrossProduct(frontVectorProj, billboardToCameraVecProj);
		
		/* rotate billboard toward camera (cos in radians) */
		if((Math.cos(theta) < 0.9999) && (Math.cos(theta) > -0.9999)) {
			m_Orientation.SetRotationAxis(rotationAxis);
			m_Orientation.AddRotation(degreeTheta);
		}
	}
	
	void UpdateObject3d(Camera cam) {
		super.updateObject3d();
		setBillboardTowardCamera(cam);
	}

}
