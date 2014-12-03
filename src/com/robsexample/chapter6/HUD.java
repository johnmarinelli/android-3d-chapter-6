package com.robsexample.chapter6;

import android.content.Context;
import android.graphics.Bitmap;

public class HUD {
	private int MAX_HUDITEMS = 10;
	public HUDItem[] mHUDItems = new HUDItem[MAX_HUDITEMS];
	private Texture mBlankTexture;
	
	HUD(Context ctx) {
		mBlankTexture = new Texture(ctx, R.drawable.blankhud);
		
		String itemName = "NONE";
		int numericalVal = 0;
		Vector3 screenPos = null;
		
		BillboardCharacterSet charSet = null;
		Texture icon = null;
		Billboard HUDImage = null;
		
		/* init hud items */
		for(int i = 0; i < MAX_HUDITEMS; ++i) {
			mHUDItems[i] = new HUDItem(itemName, numericalVal, screenPos, charSet, icon, HUDImage);
			mHUDItems[i].mItemValid = false;
		}
	}
	
	int findEmptyHUDItemSlot() {
		int emptySlot = -1;
		for(int i = 0; i < MAX_HUDITEMS; ++i) {
			if(!mHUDItems[i].mItemValid) {
				return i;
			}
		}
		
		return emptySlot;
	}
	
	boolean addHUDItem(HUDItem item) {
		boolean result = false;
		
		int emptySlot = findEmptyHUDItemSlot();
		if(emptySlot > -1) {
			mHUDItems[emptySlot] = item;
			mHUDItems[emptySlot].mItemValid = true;
			mHUDItems[emptySlot].mDirty = true;
			result = true;
		}
		
		return result;
	}
	
	int findHUDItem(String id) {
		int slot = -1;
		for(int i = 0; i < MAX_HUDITEMS; ++i) {
			if(mHUDItems[i].mItemName == id && mHUDItems[i].mItemValid) {
				slot = i;
			}
		}
		
		return slot;
	}
	
	HUDItem getHUDItem(String itemName) {
		HUDItem item = null;
		int slot = findHUDItem(itemName);
		
		if(slot > -1) {
			item = mHUDItems[slot];
		}
		
		return item;
	}
	
	boolean deleteHUDItem(String itemName) {
		boolean result = false;
		int slot = findHUDItem(itemName);
		if(slot > -1) {
			mHUDItems[slot].mItemValid = false;
			result = true;
		}
		return result;
	}
	
	void updateHUDItemNumericalValue(String itemName, int val) {
		int slot = findHUDItem(itemName);
		HUDItem item = mHUDItems[slot];
		if(item != null) {
			item.mNumericalValue = val;
			item.mDirty = true;
		}
	}
	
	void updateHUDItem(Camera cam, HUDItem item) {
		/* update HUDItem position & rotation to face camera */
		Vector3 localPos = item.mScreenPosition;
		Vector3 worldPos = new Vector3(0,0,0);
		
		Vector3 camPos = new Vector3(cam.GetCameraEye().x, cam.GetCameraEye().y, cam.GetCameraEye().z);
		Vector3 camForward = cam.GetOrientation().GetForwardWorldCoords();
		Vector3 camUp = cam.GetOrientation().GetUpWorldCoords();
		Vector3 camRight = cam.GetOrientation().GetRightWorldCoords();
		
		/* local camera offsets */
		Vector3 camHorizontalOffset = Vector3.Multiply(localPos.x, camRight);
		Vector3 camVerticalOffset = Vector3.Multiply(localPos.y, camUp);
		
		float zOffset = cam.GetProjNear() + localPos.z;
		Vector3 camDepthOffset = Vector3.Multiply(zOffset, camForward);
		
		/* create final world position vector */
		worldPos = Vector3.Add(camPos, camHorizontalOffset);
		worldPos = Vector3.Add(worldPos, camVerticalOffset);
		worldPos = Vector3.Add(worldPos, camDepthOffset);
		
		/* put images from icon & numerical data onto composite hud texture */
		Billboard HUDComposite = item.mHUDImage;
		Texture HUDCompositeTexture = HUDComposite.GetTexture(0);
		Bitmap HUDCompositeBitmap = HUDCompositeTexture.GetTextureBitMap();
		
		BillboardCharacterSet characterSet = item.mText;
		
		int fontWidth = characterSet.getFontWidth();
		Texture icon = item.mIcon;
		int iconWidth = 0;
		
		if(item.mDirty){
			/* clear composite texture */
			Bitmap blankBitmap = mBlankTexture.GetTextureBitMap();
			HUDCompositeTexture.copySubTextureToTexture(0, 0, 0, blankBitmap);
			
			if(icon != null) {
				/* draw icon on composite */
				Bitmap healthBitmap = icon.GetTextureBitMap();
				iconWidth = healthBitmap.getWidth();
				HUDCompositeTexture.copySubTextureToTexture(0, 0, 0, healthBitmap);
			}
			
			/* update numerical value and render to composite billboard */
			String text = String.valueOf(item.mNumericalValue);
			characterSet.setText(text.toCharArray());
			characterSet.renderToBillboard(HUDComposite, iconWidth, 0);
			
			/* update text value and render to composite billboard */
			String textValue = item.mTextValue;
			if(textValue != null) {
				int xPosText = iconWidth + (text.length() * fontWidth);
				characterSet.setText(textValue.toCharArray());
				characterSet.renderToBillboard(HUDComposite, xPosText, 0);
			}
			
			item.mDirty = false;
		}
		
		HUDComposite.m_Orientation.GetPosition().Set(worldPos.x, worldPos.y, worldPos.z);
		
		HUDComposite.UpdateObject3d(cam);
	}
	
	void updateHUD(Camera cam) {
		for(HUDItem item : mHUDItems) {
			if(item.mItemValid && item.mVisible) {
				updateHUDItem(cam, item);
			}
		}
	}
	
	void renderHUD(Camera cam, PointLight light) {
		for(HUDItem item : mHUDItems) {
			if(item.mItemValid && item.mVisible) {
				HUDItem hItem = item;
				Billboard HUDComposite = hItem.mHUDImage;
				HUDComposite.DrawObject(cam, light);
			}
		}
	}
}
