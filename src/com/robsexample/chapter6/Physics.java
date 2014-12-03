package com.robsexample.chapter6;

import android.content.Context;

public class Physics {
	enum CollisionStatus
	{
		COLLISION,
		NOCOLLISION,
		PENETRATING,
		PENETRATING_COLLISION
	}
	
	private final float COLLISION_TOLERANCE = 0.1f;
	private final float COEFFICIENTOFRESTITUTION= 0.5f;
	private float mCollisionTolerance = COLLISION_TOLERANCE;
	private float mCoefficientOfRestitution = COEFFICIENTOFRESTITUTION;
	
	private Vector3 mCollisionNormal = new Vector3(0, 0, 0);
	private Vector3 mRelativeVelocity = new Vector3(0, 0, 0);

	static float PI = (float)(3.14159);
	static float TWO_PI = (float)(2*PI);
	static float HALF_PI = (float)(PI/2.0);
	static float QUARTER_PI = (float)(PI/4.0);
	
	private Vector3 mVelocity = new Vector3(0.f, 0.f, 0.f);
	private Vector3 mAcceleration = new Vector3(0, 0, 0);
	private Vector3 mMaxVelocity = new Vector3(1.25f, 1.25f, 1.25f);
	private Vector3 mMaxAcceleration = new Vector3(1.f, 1.f, 1.f);
	
	private float mAngularVelocity = 0.f;
	private float mAngularAcceleration = 0.f;
	private float mMaxAngularVelocity = 4 * PI;
	private float mMaxAngularAcceleration = HALF_PI;
	
	private boolean mApplyGravity = false;
	private float mGravity = .01f;
	private float mGroundLevel = -1f;
	private boolean mJustHitGround = false;
	public float mMass = 100.f;
	
	/* radius for mass effect on gravity grid */
	public float mMassEffectiveRadius = 10;
		
	private Context mContext;
	
	Physics(Context ctx) {
		mContext = ctx;
	}
	
	public void applyTranslationalForce(Vector3 force) {
		// f = m*a
		// f / m = a
		Vector3 a = new Vector3(force);
		
		if(mMass != 0.f) {
			a.Divide(mMass);
		}
		
		mAcceleration.Add(a);
	}
	
	public void applyRotationalForce(float force, float rot) {
		//torque = force * rot
		// torque = i * angularacceleration
		//torque / inertia = angular acceleration
		
		float torque = rot * force;
		float aangular = 0.f;
		float i = mMass;
		
		if(i != 0) {
			aangular = torque / i;
		}
		
		mAngularAcceleration += aangular;
	}
	
	private float updateValueWithinLimit(float val, float inc, float lim) {
		float tempv = val + inc, retval = 0;
		
		if(tempv > lim) retval = lim;
		else if (tempv < -lim) retval = -lim;
		else retval += inc;
		
		return retval;
	}
	
	private float testSetLimitValue(float val, float lim) {
		float retval = val;
		
		if(val > lim) retval = lim;
		else if(val < -lim) retval = -lim;
		return retval;
	}
	
	private void applyGravity() {
		mAcceleration.y -= mGravity;
	}
	
	public void updatePhysics(Orientation o) {
		if(mApplyGravity) applyGravity();
		
		/*
		 * linear bullshit
		 */
		mAcceleration.x = testSetLimitValue(mAcceleration.x, mMaxAcceleration.x);
		mAcceleration.y = testSetLimitValue(mAcceleration.y, mMaxAcceleration.y);
		mAcceleration.z = testSetLimitValue(mAcceleration.z, mMaxAcceleration.z);
		
		mVelocity.Add(mAcceleration);
		mVelocity.x = testSetLimitValue(mVelocity.x, mMaxVelocity.x);
		mVelocity.y = testSetLimitValue(mVelocity.y, mMaxVelocity.y);
		mVelocity.z = testSetLimitValue(mVelocity.z, mMaxVelocity.z);
		
		/*
		 * angular bullshit
		 */
		mAngularAcceleration = testSetLimitValue(mAngularAcceleration, mMaxAngularAcceleration);
		
		mAngularVelocity += mAngularAcceleration;
		mAngularVelocity = testSetLimitValue(mAngularVelocity, mMaxAngularVelocity);
		
		/*
		 * reset forces
		 */
		mAcceleration.Clear();
		mAngularAcceleration = 0;
		
		/*
		 * update linear position
		 */
		Vector3 pos = o.GetPosition();
		pos.Add(mVelocity);
		
		if(mApplyGravity) {
			if((pos.y < mGroundLevel) && mVelocity.y < 0) {
				if(Math.abs(mVelocity.y) > Math.abs(mGravity)) {
					mJustHitGround = true;
				}
				pos.y = mGroundLevel;
				mVelocity.y = 0;
			}
		}
		
		o.AddRotation(mAngularVelocity);
	}
	
	public void setGravity(boolean g) {
		mApplyGravity = g;
	}
	
	public boolean getHitGroundStatus() {
		return mJustHitGround;
	}
	
	public void clearHitGroundStatus() {
		mJustHitGround = false;
	}
	
	CollisionStatus checkForCollisionSphereBounding(Object3d body1, Object3d body2) {
		float impactRadiusSum = 0;
		float relativeVelocityNormal = 0;
		float collisionDistance = 0;
		Vector3 body1Vel, body2Vel;
		CollisionStatus status;
		
		//calculate collision distance
		impactRadiusSum = body1.getScaledRadius() + body2.getScaledRadius();
		Vector3 pos1 = body1.m_Orientation.GetPosition();
		Vector3 pos2 = body2.m_Orientation.GetPosition();
		
		Vector3 distanceVector = Vector3.Subtract(pos1, pos2);
		collisionDistance = distanceVector.Length() - impactRadiusSum;
		
		// set collision normal vector
		distanceVector.Normalize();
		mCollisionNormal = distanceVector;
		
		// calculate relative normal velocity
		body1Vel = body1.getPhysics().getVelocity();
		body2Vel = body2.getPhysics().getVelocity();
		
		mRelativeVelocity = Vector3.Subtract(body1Vel, body2Vel);
		relativeVelocityNormal = mRelativeVelocity.DotProduct(mCollisionNormal);
		
		// test for collision
		if((Math.abs(collisionDistance) <= mCollisionTolerance) &&
				(relativeVelocityNormal < 0.0)) {
			status = CollisionStatus.COLLISION;
		}
		
		else if((collisionDistance < -mCollisionTolerance) &&
				(relativeVelocityNormal < 0.0)) {
			status = CollisionStatus.PENETRATING_COLLISION;
		}
		
		else if((collisionDistance < -mCollisionTolerance)) {
			status = CollisionStatus.PENETRATING;
		}
		
		else
			status = CollisionStatus.NOCOLLISION;
		
		return status;
	}
	
	Vector3 getVelocity() {
		return mVelocity;
	}
	
	void applyLinearImpulse(Object3d body1, Object3d body2) {
		float impulse;
		
		/* calculate impulse along collision normal */
		impulse = (-(1+mCoefficientOfRestitution) * (mRelativeVelocity.DotProduct(mCollisionNormal))) /
				(( 1/body1.getPhysics().mMass + 1/body2.getPhysics().mMass));
		
		/* apply translational force to bodies */
		Vector3 force1 = Vector3.Multiply(impulse, mCollisionNormal);
		Vector3 force2 = Vector3.Multiply(-impulse, mCollisionNormal);
		
		body1.getPhysics().applyTranslationalForce(force1);
		body2.getPhysics().applyTranslationalForce(force2);
	}
}
