package com.android.model;

import com.android.model.pay.BasePayCallback;
import com.android.model.sound.Setting;
import com.android.model.sound.aidl.MusicPlayAidlService;
import com.android.model.sound.service.MusicPlayService;
import com.poxiao.pay.llk.GamePay;
import com.poxiao.pay.llk.GamePayCallback;
import com.poxiao.pay.llk.PAY;
import com.poxiao.pay.llk.widget.MarkClickOkInterface;
import com.tallbigup.android.cloud.CreatePlayerCallback;
import com.tallbigup.android.cloud.TbuCallback;
import com.tallbigup.android.cloud.TbuCloud;
import com.tallbigup.buffett.Buffett;
import com.tallbigup.buffett.OrderResultInfo;
import com.tallbigup.buffett.plugin.configuration.Configuration;
import com.tallbigup.buffett.plugin.deviceinfo.DeviceInfo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public abstract class BaseApplication extends Application{
	
	public static final String TAG = "BaseApplication";
	private static BaseApplication instance;
	private boolean isAppLaunch;
	private Setting setting;
	// 音乐服务
    private MusicPlayAidlService playerService;
    // 音乐服务意图ACTION
    protected String MUSIC_SERVICE_FILTER_ACTION = Setting.SERVICE_INTENT_FILTER_NAME; // 绑定状态标识
    private boolean bindflag = false;
	
	private ScreenBroadReciever screenBroadReciever;
	private boolean isUnLock = false;
	private boolean isScreenOff = false;
	
	private boolean openMoreGame = false;
	private String playerId;
	
	protected int appId = 0;
	protected int gameId = 0;
	protected String appName;
	
	protected String avosAppId;
	protected String avosAppKey;
	protected Class<? extends Activity> mainClass;
	
	private boolean needShowExitPay = true;
	
	private GameInfoUtil gameInfo;
	
	protected GamePay gamePay;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		setting = Setting.getInstance();
		setting.init(this);
		setLayout();
		gameInfo = GameInfoUtil.getInstance();
		gameInfo.init(getApplicationContext());
		
		if(isAppVisible(this)){
			GamePay.getInstance().init(getApplicationContext(), GamePay.MM_POJIE_PAY_TYPE, false, GamePay.SKY_PAY_TYPE, appId, gameId, appName);
		}
		
		TbuCloud.initCloud(getApplicationContext(), new TbuCallback() {
			
			@Override
			public void result(boolean success) {
				if(success){
					if(gameInfo.getData(GameInfoUtil.CREATE_PLAYER_SUCCESS) == 1){
						Log.i(TAG,"已成功创建用户");
						playerId = gameInfo.getPlayerId();
						return;
					}
					TbuCloud.createPlayer(gameInfo.getNickName(), DeviceInfo.imsi, DeviceInfo.version,
							"1", 0,
							 Configuration.enterId, gameInfo.getData(GameInfoUtil.PAY_MONEY),
							 0, new CreatePlayerCallback() {
						
						@Override
						public void result(boolean success, String playerId) {
							if(success){
								gameInfo.setData(GameInfoUtil.CREATE_PLAYER_SUCCESS, 1);
								BaseApplication.this.playerId = playerId;
								gameInfo.setPlayerId(playerId);
							}
						}
					});
				}else{
					Log.e(TAG, "avos init fail ...");
				}
			}
		}, avosAppId, avosAppKey, DeviceInfo.version, mainClass);
		
		gamePay = GamePay.getInstance();
	}
	
	/**
	 * 初始化应用信息
	 * @param appId    应用appId
	 * @param gameId   应用gameId
	 * @param appName  应用名称
	 * @param avosAppId  avos后台申请
	 * @param avosAppKey avos后台申请
	 * @param mainClass  主Activity
	 * @param musicAction  音乐服务的action  格式：包名.service.MusicPlayService.AIDLAction
	 */
	protected void init(int appId,int gameId,String appName,String avosAppId,
					String avosAppKey,Class<? extends Activity> mainClass,String musicAction){
		this.appId = appId;
		this.gameId = gameId;
		this.appName = appName;
		this.avosAppId = avosAppId;
		this.avosAppKey = avosAppKey;
		this.mainClass = mainClass;
		setMUSIC_SERVICE_FILTER_ACTION(musicAction);
	}
	
	public void startMusic(){
		if(isAppVisible(this)){
			registerMusicReciever();
			bindPlayerService();
		}
	}
	
	public static BaseApplication getInstance(){
		return instance;
	}
	
	public void pay(final Activity activity,final int payPoint,final int levelId,final BasePayCallback callback){
		final String orderId = String.valueOf(System.currentTimeMillis());
		final int payCount = gameInfo.getData(GameInfoUtil.PAY_COUNT) + 1;
		gameInfo.setData(GameInfoUtil.PAY_COUNT, payCount);
		setPayInfo("request", payPoint, playerId, PAY.getMoney(payPoint), levelId, 
				PAY.getName(payPoint), PAY.getDesc(payPoint), payCount, orderId, "100", "请求支付");
		gamePay.pay(activity, payPoint, orderId, new GamePayCallback() {
			
			@Override
			public void result(OrderResultInfo result) {
				if(result.getResultCode() == 0){
					callback.result(result);
					TbuCloud.markUserPay(activity, 1);
					gameInfo.setData(GameInfoUtil.PAY_MONEY, gameInfo.getData(GameInfoUtil.PAY_MONEY)+PAY.getMoney(payPoint));
					setPayInfo("success", payPoint, playerId, PAY.getMoney(payPoint), levelId, 
							PAY.getName(payPoint), PAY.getDesc(payPoint), payCount, orderId, "0", "支付成功");
				}else if(result.getResultCode() == -3){
					callback.result(result);
					setPayInfo("cancel", payPoint, playerId, PAY.getMoney(payPoint), levelId, 
							PAY.getName(payPoint), PAY.getDesc(payPoint), payCount, orderId, "-3", "取消支付");
				}else{
					callback.result(result);
					setPayInfo("fail", payPoint, playerId, PAY.getMoney(payPoint), levelId, 
							PAY.getName(payPoint), PAY.getDesc(payPoint), payCount, orderId, result.getErrorCode(), result.getErrorMsg());
				}
			}
		}, new MarkClickOkInterface() {
			
			@Override
			public void clickOk() {
				setPayInfo("clickOk", payPoint, playerId, PAY.getMoney(payPoint), levelId, 
						PAY.getName(payPoint), PAY.getDesc(payPoint), payCount, orderId, "101", "点击确定");
			}
		});
	}
	
	private void setPayInfo(String payState,int payPoint,String playerId,int money,int levelId,String propName,
			String desc,int payCount,String orderId,String errorCode,String errorMsg){
		TbuCloud.setPayInfo(
				payState, 
				money, 
				String.valueOf(payPoint),
				playerId, 
				desc, 
				payCount, 
				com.tallbigup.buffett.plugin.configuration.Configuration.enterId,
				orderId,
				errorCode,
				errorMsg,
				DeviceInfo.version,
				String.valueOf(Buffett.getInstance().getPayVersionId()),
				levelId+"",
				DeviceInfo.imsi,
				DeviceInfo.carrier+"",
				Buffett.getInstance().getPayPluginName(),
				String.valueOf(gameInfo.getData(GameInfoUtil.USER_TYPE)==0 ? "new" : "old"),
				new TbuCallback() {							
					@Override
					public void result(boolean success) {
						Log.i("MCH","上传支付结果：" + success);								
					}
				});
	}
	
	public void fullExitApplication(){
		setAppLaunch(false);
        instance = null;
        isAppLaunch = false;
        unRegisterMusicReciver();
        bgMusicStop();
        // 停止音乐服务
        stopMusicService();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	private void registerMusicReciever() {
        if (null == screenBroadReciever) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            // 用户解锁
            filter.addAction(Intent.ACTION_USER_PRESENT);
            filter.addAction(PoxiaoConstants.MUSIC_DISABLED);
            filter.addAction(PoxiaoConstants.MUSIC_ENABLED);
            filter.addAction(PoxiaoConstants.MUSIC_PLAY);
            screenBroadReciever = new ScreenBroadReciever();
            registerReceiver(screenBroadReciever, filter);
            Log.i("MCH","register music receiver");
        }
	}
	
	private void unRegisterMusicReciver() {
        if (null != screenBroadReciever) {
            unregisterReceiver(screenBroadReciever);
            screenBroadReciever = null;
        }
    }
	
	protected void stopMusicService() {
        if (null != playerService) {
            unbindPlayerService();
            playerService = null;
        }
        Log.d(TAG, "stop music service");
        Intent i = new Intent(this, MusicPlayService.class);
        stopService(i);
    }
	
	/**
     * 绑定声音播放服务
     */
    public void bindPlayerService() {
        String action = getMUSIC_SERVICE_FILTER_ACTION();
        Intent i = new Intent(action);
        if (!bindflag) {
            bindflag = bindService(i, sc, Context.BIND_AUTO_CREATE);
        }
    }
	 
	 /**
	     * 解绑声音播放服务
	     */
    private void unbindPlayerService() {
        if (bindflag && null != playerService) {
            // 销毁音乐服务连接
            unbindService(sc);
            bindflag = false;
        }
    }
	
	public class ScreenBroadReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent) {
                String a = intent.getAction();
                Log.d(TAG, "ScreenBroadReciever.onReceive:" + a);
                if (a.equals(Intent.ACTION_USER_PRESENT)) {
                    isUnLock = false;
                }
                if (a.equalsIgnoreCase(PoxiaoConstants.MUSIC_DISABLED)
                        || a.equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)
                        || !isAppVisible(BaseApplication.this)) {
                    if (a.equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {
                        Log.d(TAG, "背景音暂停:屏幕关闭");
                        isScreenOff = true;
                        isUnLock = true;
                    }
                    Log.d(TAG, "背景音暂停:屏幕状态[" + isScreenOff + "]");
                    if (null != setting) {
                        Log.d(TAG, "暂停背景音播放");
                        bgMusicPause();
                    }
                }
                if (a.equalsIgnoreCase(PoxiaoConstants.MUSIC_ENABLED)
                        || a.equalsIgnoreCase(Intent.ACTION_SCREEN_ON)
                        || a.equals(Intent.ACTION_USER_PRESENT)) {
                    if (a.equalsIgnoreCase(Intent.ACTION_SCREEN_ON)) {
                        Log.d(TAG, "背景音播放:屏幕开启");
                        isScreenOff = false;
                    }
                    if(a.equals(Intent.ACTION_USER_PRESENT)){
                    	isUnLock = false;
                    }
                    Log.d(TAG, "背景音播放:屏幕状态[" + isScreenOff + "]");
                    if (null != setting) {
                        Boolean b = setting.isBgMusicOpen();
                        Log.d(TAG, "背景音播放:播放状态[" + b + "]");                        
                        if (b && !isScreenOff && !isUnLock && isAppVisible(BaseApplication.this)) {
                            Log.d(TAG, "背景音:屏幕未关闭,并且背景音可播放,恢复背景音播放");
                            bgMusicResume();
                        }
                    }
                }
                if (a.equalsIgnoreCase(PoxiaoConstants.MUSIC_PLAY)) {
                    Boolean b = setting.isBgMusicOpen();
                    boolean firstUse = setting.isFirstUse(); 
                    // 如果是首次使用，则需要将背景音播放标识写入配置文件
                    if (firstUse) {
                    	setting.setFirstUse(false);
                    	setting.setBgMusicOpen(true);
                    }
                    if (b) {
                        Log.d(TAG, "背景音播放:播放状态[" + b + "]");
                        Log.d(TAG, "背景音开始播放");
                        bgMusicPlay(PoxiaoGameRes.getInstance().getBgMusicResId());
                    }
                }
            }
        }
    }
	
	   /**
     * 播放背景音
     * 
     * @param rid
     */
    private void bgMusicPlay(int rid) {
        try {
            if (null != playerService) {;
                switch (playerService.getBGState()) {
                    case -1:
                        // START 正在播放，不处理
                        Log.d(TAG, "背景音播放还未初始化,先初始化");
                        playerService.bgMusicInit(rid);
                        break;
                }
                if (setting.isBgMusicOpen()) {
                    playerService.bgMusicPlay(true);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    
    /**
     * 音乐播放服务连接类
     */
    protected final ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            if (null != playerService) {
                playerService = null;
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            playerService = MusicPlayAidlService.Stub.asInterface(binder);
            sendBroadcast(new Intent(PoxiaoConstants.MUSIC_PLAY));
        }
    };

    /**
     * 暂停播放背景音
     */
    private void bgMusicPause() {
        try {
            if (null != playerService) {
                switch (playerService.getBGState()) {
                    case 1:
                        // START 正在播放，不处理
                        Log.d(TAG, "背景音正在播放，暂停播放");
                        playerService.bgMusicPause();
                        break;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 恢复播放背景音
     */
    private void bgMusicResume() {
        try {
            if (null != playerService) {
                switch (playerService.getBGState()) {
                    case -1:
                        Log.d(TAG, "背景音乐还未初始化，需要初始化");
                        playerService.bgMusicInit(PoxiaoGameRes.getInstance().getBgMusicResId());
                        sendBroadcast(new Intent(PoxiaoConstants.MUSIC_ENABLED));
                        // playerService.bgMusicPlay(true);
                        break;
                    case 0:
                        Log.d(TAG, "背景音乐初始化完成，可以播放");
                        if (setting.isBgMusicOpen()) {
                            playerService.bgMusicPlay(true);
                        }
                        break;
                    case 1:
                        // START 正在播放，不处理
                        Log.d(TAG, "背景音正在播放，不做处理");
                        break;
                    case 2:
                        // PAUSE 已经暂停，恢复播放
                        Log.d(TAG, "背景音播放已暂停，准备恢复");
                        if (setting.isBgMusicOpen()) {
                            playerService.bgMusicResume();
                        }
                        break;
                    case 3:
                        // STOP 已经停止，重新播放
                        Log.d(TAG, "背景音播放已停止，准备重新播放");
                        if (setting.isBgMusicOpen()) {
                            playerService.bgMusicPlay(true);
                        }
                        break;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    
    /**
     * 播放游戏音
     * 
     * @param rid
     */
    public void gameMusicPlay(int rid) {
        try {
            if (null != playerService) {
                if (setting.isSoundEffectOpen()) {
                    playerService.gameMusicPlay(rid);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 停止播放背景音
     */
    public void bgMusicStop() {
        Log.d(TAG, "停止音乐播放");
        try {
            if (null != playerService) {
                playerService.bgMusicStop();
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void adjuestBGMusic() {
        Log.d(TAG, "调整背景音");
        try {
            if (null != playerService) {
                float volumn = getAudioVolumn();
                float maxVolumn = getAudioMaxVolumn();
                Log.d(TAG, "当前系统音量为:" + volumn);
                Log.d(TAG, "系统音量:" + volumn);
                float v = volumn / maxVolumn * 0.30f;
                Log.d(TAG, "调整后的的音量:" + volumn);
                playerService.adjustBgMusicVolume(v);
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    
    /**
     * 获取系统的音量值
     * 
     * @param activity
     * @return
     */
    protected int getAudioVolumn() {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return mVolume;
    }

    /**
     * 获取系统中类型为STREAM_MUSIC的最大音量
     * 
     * @param activity
     * @return
     */
    protected int getAudioMaxVolumn() {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return max;
    }
    
    public boolean isAppVisible(Context activity) {
        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (cn.getPackageName().equals(activity.getPackageName())) {
            Log.e("MCH", " --------- 应用可见[" + activity.getPackageName() + "] --------");
            return true;
        }
        Log.e("MCH", " --------- 应用不可见[" + activity.getPackageName() + "] --------");
        return false;
	}
    
    public void setAppLaunch(boolean isAppLaunch){
    	this.isAppLaunch = isAppLaunch;
    }
    
    public boolean isAppLaunch(){
    	return isAppLaunch;
    }
    
    public String getMUSIC_SERVICE_FILTER_ACTION(){
    	return MUSIC_SERVICE_FILTER_ACTION;
    }
    
    public void setMUSIC_SERVICE_FILTER_ACTION(String MUSIC_SERVICE_FILTER_ACTION){
    	this.MUSIC_SERVICE_FILTER_ACTION = MUSIC_SERVICE_FILTER_ACTION;
    }

	public boolean isOpenMoreGame() {
		return openMoreGame;
	}

	public void setOpenMoreGame(boolean openMoreGame) {
		this.openMoreGame = openMoreGame;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAvosAppId() {
		return avosAppId;
	}

	public void setAvosAppId(String avosAppId) {
		this.avosAppId = avosAppId;
	}

	public String getAvosAppKey() {
		return avosAppKey;
	}

	public void setAvosAppKey(String avosAppKey) {
		this.avosAppKey = avosAppKey;
	}

	public Class<? extends Activity> getMainClass() {
		return mainClass;
	}

	public void setMainClass(Class<? extends Activity> mainClass) {
		this.mainClass = mainClass;
	}
	
	/**
	 * 退出时不需要弹计费点，设为false
	 * @param needShowExitPay
	 */
	public void setNeedShowExitPay(boolean needShowExitPay){
		this.needShowExitPay = needShowExitPay;
	}
	
	/**
	 * 退出时是否需要弹计费点，默认为true
	 * @return
	 */
	public boolean isNeedShowExitPay(){
		return needShowExitPay;
	}
	
	/**
	 * 用户付费金额
	 * @return
	 */
	public int getPayMoney(){
		return gameInfo.getData(GameInfoUtil.PAY_MONEY);
	}
    
	/**
	 * 更多游戏及push的资源
	 */
	public abstract void setLayout();
	
	/**
	 * 用户退出时更新用户信息，用户付费金额可以通过getPayMOney()方法获得
	 */
	public abstract void updatePlayerInfo();
}
