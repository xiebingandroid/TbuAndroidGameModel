
package com.android.model.sound;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.android.model.PoxiaoConstants;

/**
 * 设置功能接口参数
 * 
 * @author bluestome.zhang
 */
public class Setting {
	
	public static final String SP_NAME = "setting_music_info";
	
	//背景音乐开关
	public static final String BG_MUSIC_SWITCH = "setting_background_music_switch";

    // 背景音乐音量
    public static final String BG_MUSIC_VOLUMN = "setting_background_music_volumn";

    // 音效开关
    public static final String SOUND_EFFECT_SWITCH = "setting_sound_effect_switch";
    
    //音效音量
    public static final String SOUND_EFFECT_VOLUMN = "setting_sound_effect_volumn";

    /**
     * AIDL的意图过滤名
     */
    public static final String SERVICE_INTENT_FILTER_NAME = "com.hifreshday.android.setting.service.MusicPlayService.AIDLAction";

   
    private static Setting setting;
    private static SharedPreferences settingPreference;
    private boolean firstUse;
    private boolean bgMusicOpen;
    private boolean soundEffectOpen;
    private float bgMusicVolumn;
    private float soundEffectVolumn;
    private Editor editor;
    
    private Context context;
    
    private Setting(){
    	
    }
    
    public static Setting getInstance(){
    	if(setting == null){
        	setting = new Setting();
    	}
    	return setting;
    }
    
    public void init(Context context){
    	this.context = context;
    	settingPreference = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    	editor = settingPreference.edit();
    	firstUse = settingPreference.getBoolean(PoxiaoConstants.FIRST_USE, true);
    	bgMusicOpen = settingPreference.getBoolean(Setting.BG_MUSIC_SWITCH, true);
    	bgMusicVolumn = settingPreference.getFloat(Setting.BG_MUSIC_VOLUMN, 0.3f);
    	soundEffectOpen = settingPreference.getBoolean(Setting.SOUND_EFFECT_SWITCH, true);
    	soundEffectVolumn = settingPreference.getFloat(Setting.SOUND_EFFECT_VOLUMN, 0.5f);
    }

	public boolean isBgMusicOpen() {
		return bgMusicOpen;
	}

	public void setBgMusicOpen(boolean bgMusicOpen) {
		if(bgMusicOpen){
			context.sendBroadcast(new Intent(PoxiaoConstants.MUSIC_ENABLED));
		}else{
			context.sendBroadcast(new Intent(PoxiaoConstants.MUSIC_DISABLED));
		}
		editor.putBoolean(Setting.BG_MUSIC_SWITCH, bgMusicOpen);
		editor.commit();
		this.bgMusicOpen = bgMusicOpen;
	}

	public boolean isSoundEffectOpen() {
		return soundEffectOpen;
	}

	public void setSoundEffectOpen(boolean soundEffectOpen) {
		editor.putBoolean(Setting.SOUND_EFFECT_SWITCH, soundEffectOpen);
		editor.commit();
		this.soundEffectOpen = soundEffectOpen;
	}

	public float getBgMusicVolumn() {
		return bgMusicVolumn;
	}

	public void setBgMusicVolumn(float bgMusicVolumn) {
		editor.putFloat(Setting.BG_MUSIC_VOLUMN, bgMusicVolumn);
		editor.commit();
		this.bgMusicVolumn = bgMusicVolumn;
	}

	public float getSoundEffectVolumn() {
		return soundEffectVolumn;
	}

	public void setSoundEffectVolumn(float soundEffectVolumn) {
		editor.putFloat(Setting.SOUND_EFFECT_VOLUMN, soundEffectVolumn);
		editor.commit();
		this.soundEffectVolumn = soundEffectVolumn;
	}
	
	public boolean isFirstUse(){
		return firstUse;
	}
	
	public void setFirstUse(boolean firstUse){
		editor.putBoolean(PoxiaoConstants.FIRST_USE, firstUse);
		editor.commit();
		this.firstUse = firstUse;
	}
}
