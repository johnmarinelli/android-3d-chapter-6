package com.robsexample.chapter6;

public class HUDItem {
	public boolean mItemValid;
	public String mItemName;
	public int mNumericalValue;
	public String mTextValue;
	public Vector3 mScreenPosition;
	public BillboardCharacterSet mText;
	public Texture mIcon;
	public Billboard mHUDImage;
	public boolean mDirty = false;
	public boolean mVisible = true;
	
	HUDItem(String itemName,
			int numericalVal,
			Vector3 screenPos,
			BillboardCharacterSet text,
			Texture icon,
			Billboard HUDImage)
	{
		mItemName = itemName;
		mNumericalValue = numericalVal;
		mScreenPosition = screenPos;
		mText = text;
		mIcon = icon;
		mHUDImage = HUDImage;
	}
}
