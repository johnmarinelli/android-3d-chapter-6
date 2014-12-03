package com.robsexample.chapter6;

import android.util.FloatMath;
import java.lang.Math;

class Vector3
{
	 public float x;
     public float y;
     public float z;
    
     public Vector3(float _x, float _y, float _z)
     {
    	 x = _x;
    	 y = _y;
    	 z = _z;
     }
     
     public Vector3(Vector3 v) 
     {
    	 x = v.x;
    	 y = v.y;
    	 z = v.z;
     }
     
      //////////////////// Vector Operators ////////////////////////////////////
    
      void Multiply(float v)
      {
    	  x *= v;
    	  y *= v;
    	  z *= v;
      }
   
      ///////////// Static Vector Operations /////////////////////////////////// 
   
      static Vector3 Add(Vector3 vec1, Vector3 vec2)
      {
    	  Vector3 result = new Vector3(0,0,0);
    	  
    	  result.x = vec1.x + vec2.x;
    	  result.y = vec1.y + vec2.y;
    	  result.z = vec1.z + vec2.z;
    	  
    	  return result;
      }

      static Vector3 Subtract(Vector3 vec1, Vector3 vec2) {
    	  Vector3 r = new Vector3(0, 0, 0);
    	  r.x = vec1.x - vec2.x;
    	  r.y = vec1.y - vec2.y;
    	  r.z = vec1.z - vec2.z;
    	  
    	  return r;
      }
     
      /////////////////////////////////////////////////////////////////////////
      
     void Set(float _x, float _y, float _z)
     {
	      x = _x;
	      y = _y;
	      z = _z;
     }
      
     void Normalize()
     {
	      float l = Length();

	      x = x/l;
	      y = y/l;
	      z = z/l;
     }

     float Length()
     {
	     return FloatMath.sqrt(x*x + y*y + z*z);
    	 //return java.lang.Math.sqrt(x*x + y*y + z*z);
     }

     static Vector3 CrossProduct(Vector3 a, Vector3 b)
     {
    	 Vector3 result = new Vector3(0,0,0);
    	 
    	 result.x= (a.y*b.z) - (a.z*b.y);
    	 result.y= (a.z*b.x) - (a.x*b.z);
    	 result.z= (a.x*b.y) - (a.y*b.x);
    	 
    	 return result;
     }
     
     
      
     void Negate()
     {
	      x = -x;
	      y = -y;
	      z = -z;
     }

     void Divide (float v)
     {
   	  //debug_assert(v != 0.0f, "divide by zero error");

   	  x /= v;
   	  y /= v;
   	  z /= v;
     }
     
     void Add (Vector3 vec)
     {
   	  x = x + vec.x;
   	  y = y + vec.y;
   	  z = z + vec.z;
     }
          
     // Additional Functions
     void Clear()
     {
	      x = 0.0f;
	      y = 0.0f;
	      z = 0.0f;
     }
     void Divide (Vector3 vec)
     {
   	  //debug_assert(vec.x != 0.0f, "divide by zero error");
   	  //debug_assert(vec.y != 0.0f, "divide by zero error");
   	  //debug_assert(vec.z != 0.0f, "divide by zero error");

   	  x /= vec.x;
   	  y /= vec.y;
   	  z /= vec.z;
     }

	public float DotProduct(Vector3 vec) {
	     return (x * vec.x) + (y * vec.y) + (z * vec.z);
	}

	public static Vector3 Multiply(float impulse, Vector3 v) {
		Vector3 vec= new Vector3(v.x*impulse, v.y*impulse, v.z*impulse);
		return vec;
	}
       
}
