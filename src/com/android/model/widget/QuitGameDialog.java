package com.android.model.widget;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.android.model.BaseApplication;
import com.android.model.PoxiaoGameRes;
import com.poxiao.tbuandroidgamemodel.R;

public class QuitGameDialog extends Dialog{

    private ImageButton closeImgBtn;
    private ImageButton okImgBtn;
    private ImageButton cancelImgBtn;
    private ImageView quitBg;
    private ImageView quitTxt;
    private ImageView quitTitle;

    private Activity activity;
    private PoxiaoGameRes gameRes;
    
    public QuitGameDialog(Activity activity) {
        super(activity, R.style.dialog_game_style);
        this.activity = activity;
        gameRes = PoxiaoGameRes.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_quit_game);
        initView();
    }

    private void initView() {
    	quitBg = (ImageView)findViewById(R.id.quit_game_bg);
    	quitBg.setBackgroundResource(gameRes.getQuitGameBgResId());
    	
    	quitTitle = (ImageView)findViewById(R.id.quit_game_title);
    	quitTitle.setBackgroundResource(gameRes.getQuitGameTitleResId());
    	
    	quitTxt = (ImageView)findViewById(R.id.quit_game_txt);
    	quitTxt.setBackgroundResource(gameRes.getQuitGameTxtResId());
    	
        okImgBtn = (ImageButton) findViewById(R.id.quit_game_confirm);
        okImgBtn.setBackgroundResource(gameRes.getQuitGameConfirmResId());
        okImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                okImgBtn.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                    	BaseApplication.getInstance().updatePlayerInfo();
                    	activity.finish();
                    	BaseApplication.getInstance().fullExitApplication();
                    }
                }, 250L);
            }
        });
        
        

        cancelImgBtn = (ImageButton) findViewById(R.id.quit_game_cancel);
        cancelImgBtn.setBackgroundResource(gameRes.getQuitGameCancelResId());
        cancelImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        closeImgBtn = (ImageButton) findViewById(R.id.quit_game_close);
        closeImgBtn.setBackgroundResource(gameRes.getQuitGameCloseResId());
        closeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
    
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    }
}
