package com.android.model;

public abstract class PoxiaoGameRes {
	
	private static PoxiaoGameRes gameRes;
	
	public static PoxiaoGameRes getInstance(){
		return gameRes;
	}
	
	public static void init(PoxiaoGameRes gameResImpl){
		PoxiaoGameRes.gameRes = gameResImpl;
	}
	
	//背景音乐资源id
	public abstract int getBgMusicResId();
	//退出确认框背景资源id
	public abstract int getQuitGameBgResId();
	//退出确认框标题资源id
	public abstract int getQuitGameTitleResId();
	//退出确认框叉资源id
	public abstract int getQuitGameCloseResId();
	//退出确认框确定按钮资源id
	public abstract int getQuitGameConfirmResId();
	//退出确认框取消按钮资源id
	public abstract int getQuitGameCancelResId();
	//退出确认框中文字资源id
	public abstract int getQuitGameTxtResId();
	
}
