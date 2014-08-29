package com.android.model.activity;

import com.android.model.BaseApplication;
import com.android.model.PoxiaoConstants;
import com.android.model.widget.QuitGameDialog;
import com.poxiao.pay.llk.GamePay;
import com.tallbigup.android.cloud.TbuCloud;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public abstract class AbstractActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!GamePay.isSuccessInit()){
			GamePay.getInstance().onCreateInit(this, GamePay.SKY_PAY_TYPE);
		}
		
		if(TbuCloud.markUserType(getApplicationContext()) == 0){
			   TbuCloud.markUserPay(getApplicationContext(), 0);
		}
		TbuCloud.markUserLogin(getApplicationContext(), System.currentTimeMillis());		
		TbuCloud.markAppOpened(this);
		
		BaseApplication.getInstance().startMusic();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		TbuCloud.markOpenPushInfo(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		sendBroadcast(new Intent(PoxiaoConstants.MUSIC_ENABLED));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		sendBroadcast(new Intent(PoxiaoConstants.MUSIC_DISABLED));
	}
		
	public static final int DIALOG_QUIT_GAME = 1;
	
	public Activity getContext(){
		return this;
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_QUIT_GAME:
                final QuitGameDialog d = new QuitGameDialog(getContext());
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                d.show();
                d.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                return d;
        }

        return super.onCreateDialog(id);
    }
	
	@Override
	public void onBackPressed() {
		if(BaseApplication.getInstance().isNeedShowExitPay()){
			exitPay();
		}else{
			showDialog(AbstractActivity.DIALOG_QUIT_GAME);
		}
	}
	
	/**
	 * 退出时弹出计费点
	 */
	public abstract void exitPay();
}
