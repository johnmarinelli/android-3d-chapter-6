package com.robsexample.chapter6;

import android.content.Context;

public class BillboardFont extends Billboard {

	public char mCharacter;
	
	BillboardFont(Context iContext, MeshEx iMeshEx, Texture[] iTextures,
			Material iMaterial, Shader iShader, char character) {
		super(iContext, iMeshEx, iTextures, iMaterial, iShader);
		mCharacter = character;
	}
	
	public boolean isFontCharacter(char val) {
		return mCharacter == val;
	}

}
