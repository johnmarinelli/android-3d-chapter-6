package com.robsexample.chapter6;

import android.graphics.Bitmap;
import android.util.Log;

public class BillboardCharacterSet {
	static int MAX_CHARACTERS = 50;
	
	public int mNumberCharacters = 0;
	
	private BillboardFont[] mCharacterSet = new BillboardFont[MAX_CHARACTERS];
	
	private int MAX_CHARACTERS_TEXT = 100;
	private char[] mText = new char[MAX_CHARACTERS_TEXT];
	
	private BillboardFont[] mTextBillboard = new BillboardFont[MAX_CHARACTERS_TEXT];
	
	BillboardFont getCharacter(int index) {
		BillboardFont font = null;
		
		if(index < mNumberCharacters && index >= 0) {
			font = mCharacterSet[index];
		}
		
		return font;
	}
	
	int getFontWidth() {
		int width = 0;
		
		if(mNumberCharacters > 0) {
			BillboardFont character = mCharacterSet[0];
			Texture tex = character.GetTexture(0);
			Bitmap img = tex.GetTextureBitMap();
			width = img.getWidth();
		}
		
		return width;
	}
	
	int getFontHeight() {
		int height = 0;
		
		if(mNumberCharacters > 0) {
			BillboardFont character = mCharacterSet[0];
			Texture tex = character.GetTexture(0);
			Bitmap img = tex.GetTextureBitMap();
			height = img.getHeight();
		}
		
		return height;
	}
	
	boolean addToCharacterSet(BillboardFont character) {
		if(mNumberCharacters < MAX_CHARACTERS) {
			mCharacterSet[mNumberCharacters++] = character;
			return true;
		}
		else {
			Log.e("BILLBOARD CHARACTER SET", "NOT ENOUGH ROOM FOR ANOTHER CHARACTER");
			return false;
		}
	}
	
	BillboardFont findBillboardCharacter(char character) {
		BillboardFont font = null;
		
		for(int i = 0; i < mNumberCharacters; ++i) {
			if(mCharacterSet[i].isFontCharacter(character)) {
				font = mCharacterSet[i];
			}
		}
		
		return font;
	}
	
	void setText(char[] text) {
		String textStr = new String(text);
		textStr = textStr.toLowerCase();
		mText = textStr.toCharArray();
		
		for(int i = 0; i < mText.length; ++i) {
			BillboardFont character = findBillboardCharacter(mText[i]);
			if(character != null) {
				mTextBillboard[i] = character;
			}
			else {
				Log.e("CHARACTER SET ERROR", "SET TEXT ERROR: " + mText[i] + " NOT FOUND");
			}
		}
	}
	
	void drawFontToComposite(BillboardFont obj, int x, int y, Billboard composite) {
		Texture texSource = obj.GetTexture(0);
		Bitmap bmpSource = texSource.GetTextureBitMap();
		int bmpSourceWidth = bmpSource.getWidth();
		
		Texture texDest = composite.GetTexture(0);
		Bitmap bmpDest = texDest.GetTextureBitMap();
		int bmpDestWidth = bmpDest.getWidth();
		
		/* put sub image on composite */
		int xEndTexture = x + bmpSourceWidth;
		if(xEndTexture >= bmpDestWidth) {
			Log.e("BillboardCharacterSet::DrawFontToComposite", "ERROR Overwriting Dest Texture, Last X Position To Write = " + xEndTexture + ", Max Destination Width = " + bmpDestWidth);
		}
		else {
			texDest.copySubTextureToTexture(0, xEndTexture, y, bmpSource);
		}
	}
	
	void renderToBillboard(Billboard composite, int x, int y) {
		int length = mText.length;
		for(int i = 0; i < length; ++i) {
			BillboardFont character = mTextBillboard[i];
			
			if(character != null) {
				/* draw this font to composite by copying bmp image data */
				Texture tex = character.GetTexture(0);
				Bitmap img = tex.GetTextureBitMap();
				int width = img.getWidth();
				int xCompositeOffset = x + (width*i);
				
				drawFontToComposite(character, x, y, composite);
			}
		}
	}
}
