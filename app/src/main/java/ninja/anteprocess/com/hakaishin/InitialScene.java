package ninja.anteprocess.com.hakaishin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.KeyEvent;
import android.widget.Toast;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.BitmapFont;
import org.andengine.ui.dialog.StringInputDialogBuilder;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.call.Callback;
import org.andengine.util.modifier.ease.EaseBackInOut;
import org.andengine.util.modifier.ease.EaseQuadOut;

import java.io.IOException;

import anteprocess.com.zombie.R;

import static android.content.Context.MODE_PRIVATE;

public class InitialScene extends KeyListenScene implements
		ButtonSprite.OnClickListener {

	private static final int INITIAL_START = 1;
	private static final int INITIAL_RANKING = 2;
	private static final int INITIAL_SHARE = 3;
	private static final int NORMAL = 5;
	private static final int HARD = 6;

	// Class for checking if the button has been clicked.
	private ClickHandleState mClickHandleState = new ClickHandleState();
	// ボタンが押された時の効果音
	private Sound btnPressedSound;
    public InitialScene(MultiSceneActivity context) {
		super(context);
		init();
		if (isFirstTime()) {
			//NOP
		} else if (SPUtil.getInstance(getBaseActivity()).getPlayerName() == "Guest" ||
				SPUtil.getInstance(getBaseActivity()).getPlayerName() == "") {
			//Tell the user to enter a proper name.
			savePlayerName();
		}
	}
	/***
	 * Checks that application runs first time and write flag at SharedPreferences
	 * @return true if 1st time
	 */
	private boolean isFirstTime()
	{
		SharedPreferences preferences = getBaseActivity().getPreferences(MODE_PRIVATE);
		boolean ranBefore = preferences.getBoolean("RanBefore", false);
		if (!ranBefore) {
			// TODO: first time
			//1. Show dialog to save name
			//2. Save the name to pref
			    savePlayerName();
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("RanBefore", true);
				editor.commit();
			}
		return !ranBefore;
	}

	//Return true if the user enters the name. Return false if user cancels.
	private void savePlayerName() {
		// UIスレッド上でないと動きません
		getBaseActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				// Dialogを生成
				StringInputDialogBuilder builder = new StringInputDialogBuilder(getBaseActivity(),
						R.string.title_username,    // タイトル文言のリソースID
						R.string.title_dialog_desc,   // 本文のリソースID
						R.string.title_error,    // エラー時のToastで表示される文言のリソースID
						R.mipmap.ic_launcher,        // タイトル横のアイコン画像のリソースID

						// OKボタンを押した時のコールバック.
						// 引数pCallbackValueに入力した文字列が入ってくる
						new Callback<String>() {
							@Override
							public void onCallback(String pCallbackValue) {
								if (pCallbackValue.trim().length() != 0) {
									// save name
									String name = pCallbackValue.toString().trim();
									name.replaceAll("[\n\r]", "");
									SPUtil.getInstance(getBaseActivity()).setPlayerName(name);

								} else {
									Toast.makeText(getBaseActivity(), "Type in your usename.",
											Toast.LENGTH_SHORT).show();
								}
							}
						},

						// Cancelボタンを押した時のコールバック.
						new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								Toast.makeText(getBaseActivity(), "Canceled",
										Toast.LENGTH_SHORT).show();
							}
						});
				// createで生成
				final Dialog dialog = builder.create();
				// 表示
				dialog.show();
			}
		});
	}
	@Override
	public void init() {
		Sprite bg = getBaseActivity().getResourceUtil().getSprite(
				"main_bg_title.png");
		bg.setPosition(0, 0);
		attachChild(bg);

		Sprite titleSprite = getBaseActivity().getResourceUtil().getSprite(
				"title2.png");
		placeToCenterX(titleSprite, 10);
		float titleX = titleSprite.getX();
		attachChild(titleSprite);

		titleSprite.setPosition(titleSprite.getX()
				+ getBaseActivity().getEngine().getCamera().getWidth(),
				titleSprite.getY() - 200);

		titleSprite.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(0.5f), new MoveModifier(1.0f, titleSprite
						.getX(), titleX, titleSprite.getY(), 10, EaseBackInOut
						.getInstance())));

		// ボタンの追加
		ButtonSprite btnStart = getBaseActivity().getResourceUtil()
				.getButtonSprite("initial_btn_01.png", "initial_btn_01.png");
		placeToCenterX(btnStart, 200);
		btnStart.setTag(INITIAL_START);
		btnStart.setOnClickListener(this);
		attachChild(btnStart);
		// ボタンをタップ可能に
		registerTouchArea(btnStart);

		float btnX = btnStart.getX();
		btnStart.setPosition(btnStart.getX()
				+ getBaseActivity().getEngine().getCamera().getWidth(),
				btnStart.getY());
		btnStart.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(1.4f), new MoveModifier(1.0f,
						btnStart.getX(), btnX, btnStart.getY(),
						btnStart.getY(), EaseBackInOut.getInstance())));

		ButtonSprite btnRanking = getBaseActivity().getResourceUtil()
				.getButtonSprite("initial_btn_02.png", "initial_btn_02.png");
		placeToCenterX(btnRanking, 280);
		btnRanking.setTag(INITIAL_RANKING);
		btnRanking.setOnClickListener(this);
		attachChild(btnRanking);
		registerTouchArea(btnRanking);

		btnX = btnRanking.getX();
		btnRanking.setPosition(btnRanking.getX()
				+ getBaseActivity().getEngine().getCamera().getWidth(),
				btnRanking.getY());
		btnRanking.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(1.6f), new MoveModifier(1.0f, btnRanking
						.getX(), btnX, btnRanking.getY(), btnRanking.getY(),
						EaseBackInOut.getInstance())));

		ButtonSprite btnRecommend = getBaseActivity().getResourceUtil()
				.getButtonSprite("initial_btn_03.png", "initial_btn_03.png");
		placeToCenterX(btnRecommend, 360);
		btnRecommend.setTag(INITIAL_SHARE);
		btnRecommend.setOnClickListener(this);
		attachChild(btnRecommend);
		registerTouchArea(btnRecommend);
		btnX = btnRecommend.getX();
		btnRecommend.setPosition(btnRecommend.getX()
				+ getBaseActivity().getEngine().getCamera().getWidth(),
				btnRecommend.getY());
		btnRecommend.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(1.8f), new MoveModifier(1.0f, btnRecommend
						.getX(), btnX, btnRecommend.getY(),
						btnRecommend.getY(), EaseBackInOut.getInstance())));
	}
	@Override
	public void prepareSoundAndMusic() {
		// 効果音をロード
		try {
			btnPressedSound = SoundFactory.createSoundFromAsset(
					getBaseActivity().getSoundManager(), getBaseActivity(),
					"door03.wav");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return false;
	}
	public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		// 効果音を再生
		btnPressedSound.play();
		switch (pButtonSprite.getTag()) {
		case INITIAL_START:
			//TODO: First show selection
			    if (mClickHandleState.getClickState() == ClickHandleState.State.ON) {
					mClickHandleState.proceed();
					levelSelector();
					//Intent i = new Intent(getBaseActivity(), SettingsActivity.class);
					//getBaseActivity().startActivity(i);
					break;
				}
			break;
		case INITIAL_RANKING:
			//Todo: Add a condition to check if the internet is connected
			if (mClickHandleState.getClickState() != ClickHandleState.State.OFF) {
				if (isNetworkAvailable()) {
					Intent intent = new Intent(getBaseActivity(),ScoreActivity.class);
					intent.putExtra("score", SPUtil.getInstance(getBaseActivity()).getHighScore());
					android.util.Log.d("Test",""+SPUtil.getInstance(getBaseActivity()).getHighScore());
					getBaseActivity().startActivity(intent);
				} else {
					Toast.makeText(getBaseActivity(),"Needs network connection",
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case INITIAL_SHARE:
			if (mClickHandleState.getClickState() != ClickHandleState.State.OFF) {
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.setType("text/plain");
				sendIntent.putExtra(Intent.EXTRA_TEXT, "Just a zombie survival ! " +
						" https://play.google.com/store/apps/details?id=anteprocess.com.zombie");
				getBaseActivity().startActivity(sendIntent);
			}
			break;
			// The following case condition are for level selectors
			case NORMAL:
				if (mClickHandleState.getClickState() != ClickHandleState.State.ON) {
					loadGameScene(0);
					/*
					// リソースの解放
					ResourceUtil.getInstance(getBaseActivity()).resetAllTexture();
					// MainSceneへ移動
					getBaseActivity().getEngine().setScene(scene);
					// 遷移管理用配列に追加
					getBaseActivity().appendScene(scene);
					*/
				}
				break;
			case HARD:
				if (mClickHandleState.getClickState() != ClickHandleState.State.ON) {
					loadGameScene(1);
				}
				break;
		}
	}
	/*
	This method allows you to show the loading screen before starting the game
	 */
	public void loadGameScene(int difficulty)
	{
		//Save the dificulty level
		SPUtil.getInstance(getBaseActivity()).setDifficultyLevel(difficulty);

		ResourceUtil.getInstance(getBaseActivity()).resetAllTexture();
		KeyListenScene loadingScene = new SplashScene(getBaseActivity());
		getBaseActivity().getEngine().setScene(loadingScene);
		getBaseActivity().getEngine().registerUpdateHandler(new TimerHandler(1.5f, new ITimerCallback()
		{
			public void onTimePassed(final TimerHandler pTimerHandler)
			{
				getBaseActivity().getEngine().unregisterUpdateHandler(pTimerHandler);
				// リソースの解放
				KeyListenScene scene = new MainScene(getBaseActivity());
				ResourceUtil.getInstance(getBaseActivity()).resetAllTexture();
				// MainSceneへ移動
				getBaseActivity().getEngine().setScene(scene);
				// 遷移管理用配列に追加
				getBaseActivity().appendScene(scene);
			}
		}));
	}

	/*
	Thsi method is to show a view to the user to select the game level
	 */
	private void levelSelector() {
		Sprite resultBg = getBaseActivity().getResourceUtil().getSprite(
				"dead.png");
		resultBg.setPosition(0, -getBaseActivity().getEngine().getCamera()
				.getWidth());
		resultBg.setZIndex(3);
		attachChild(resultBg);
		sortChildren();
		//Zombie image
		resultBg.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(0.5f), new MoveModifier(0.5f,
				resultBg.getX(), resultBg.getX(), resultBg.getY(), 0,
				EaseQuadOut.getInstance())));
		AnimatedSprite title = getBaseActivity().getResourceUtil().getAnimatedSprite(
				"zomb.png",1,3);
		title.animate(100);
		placeToCenterX(title, 20);
		resultBg.attachChild(title);

		BitmapFont bitmapFont = new BitmapFont(getBaseActivity()
				.getTextureManager(), getBaseActivity().getAssets(),
				"font/bitmap.fnt");
		bitmapFont.load();

		// ビットマップフォントを元にスコアを表示
		Text resultText = new Text(0, 0, bitmapFont, "" , 20,
				new TextOptions(HorizontalAlign.CENTER), getBaseActivity()
				.getVertexBufferObjectManager());
		resultText.setPosition(getBaseActivity().getEngine().getCamera()
				.getWidth()
				/ 2.0f - resultText.getWidth() / 2.0f, 90);
		resultBg.attachChild(resultText);

		ButtonSprite normalBtn = getBaseActivity().getResourceUtil()
				.getButtonSprite("normal.png", "normal2.png");
		normalBtn.setPosition(getBaseActivity().getEngine().getCamera().getWidth() / 2.0f -
				normalBtn.getWidth()/2.0f + 180 ,
				getBaseActivity().getEngine().getCamera().getHeight() / 2.0f - normalBtn.getHeight()/2.0f);
		normalBtn.setTag(NORMAL);
		normalBtn.setOnClickListener(this);
		resultBg.attachChild(normalBtn);
		registerTouchArea(normalBtn);

		ButtonSprite hardBtn = getBaseActivity().getResourceUtil()
				.getButtonSprite("hard.png", "hard2.png");
		hardBtn.setPosition(getBaseActivity().getEngine().getCamera().getWidth() / 2.0f -
				hardBtn.getWidth() / 2.0f - 180,
				getBaseActivity().getEngine().getCamera().getHeight() / 2.0f - hardBtn.getHeight()/2.0f);
		hardBtn.setTag(HARD);
		hardBtn.setOnClickListener(this);
		resultBg.attachChild(hardBtn);
		registerTouchArea(hardBtn);

	}
	// Checks the network condition
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getBaseActivity().getSystemService(Context.
				CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

}
