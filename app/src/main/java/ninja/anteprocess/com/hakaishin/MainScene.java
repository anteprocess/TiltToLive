package ninja.anteprocess.com.hakaishin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Toast;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.FadeInModifier;
import org.andengine.entity.modifier.FadeOutModifier;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierMatcher;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.BitmapFont;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.call.Callback;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseBackIn;
import org.andengine.util.modifier.ease.EaseBounceInOut;
import org.andengine.util.modifier.ease.EaseQuadOut;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import anteprocess.com.zombie.R;
import backend.DBUtil;
import backend.StringInputDialogBuilder;

public class MainScene extends KeyListenScene implements
		ButtonSprite.OnClickListener {

	// ゾンビ用タグ
	private static final int TAG_ZOMBIE_01 = 1;
	private static final int TAG_ZOMBIE_02 = 2;
	private static final int TAG_ZOMBIE_03 = 3;

	// アイテム用タグ
	private static final int TAG_ITEM_01 = 11;
	private static final int TAG_ITEM_02 = 12;
	private static final int TAG_ITEM_03 = 13;

	// ボタン用タグ
	private static final int MENU_MENU = 21;
	private static final int MENU_TWEET = 22;
	private static final int MENU_RANKING = 23;
	private static final int MENU_RETRY = 24;
	private static final int MENU_RESUME = 25;
	private static final int MENU_PAUSE = 26;

	// 主人公
	private AnimatedSprite boySprite;
	// 主人公のz-index
	private int zIndexBoy = 2;
	// 攻撃アイテムのz-index
	private int zIndexItem = 1;
	// ゲームオーバー画面のz-index
	private int zIndexGameOverLayer = 3;
	// 主人公が移動する先の座標を保持
	private float boyNewX;
	private float boyNewY;

	// ゾンビ出現穴の配列
	private ArrayList<Sprite> holeArray;
	// 出現済みゾンビの配列
	private ArrayList<AnimatedSprite> zombieArray;
	// 攻撃アイテムの配列
	private ArrayList<Sprite> itemArray;
	// 攻撃エフェクトのTextureRegionの配列
	private TextureRegion[] weaponTextureArray;
	// コンボ表示用フォント
	private BitmapFont[] comboBMFontArray;

	// ゲームオーバーか否かのフラグ
	private boolean isGameOver;
	// ポーズ中か否かのフラグ
	private boolean isPaused;
	// ポーズ画面の背景
	private Rectangle pauseBg;

	// 現在のスコアを表示するテキスト
	private Text currentScoreText;
	// 過去最高のスコアを表示するテキスト
	private Text highScoreText;

	//username Text
	private Text usernameText;
	// 現在のスコア
	private long currentScore;

	// 遊び方画面
	private Sprite instructionSprite;
	// 遊び方画面のボタン
	private ButtonSprite instructionBtn;
	// 遊び方画面が出ているかどうか
	private boolean isHelpVisible;

	// 効果音
	private Sound btnPressedSound;
	private Sound weapon01Sound;
	private Sound weapon02Sound;
	private Sound weapon03Sound;
	private Sound zombieAppearSound;
	private Sound gameoverSound;
	private Sound comboSound;

	private ClickHandleState clickHandleState = new ClickHandleState();

	// Random word generator
	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	static SecureRandom rnd = new SecureRandom();

	public MainScene(MultiSceneActivity baseActivity) {
		super(baseActivity);
		init();
	}

	public void init() {


        //setup the background
		attachChild(getBaseActivity().getResourceUtil()
				.getSprite("background.png"));

		// 主人公を追加
		boySprite = getBaseActivity().getResourceUtil().getAnimatedSprite(
				"play.png", 1, 2);
		placeToCenter(boySprite);
		attachChild(boySprite);
		// アニメーション開始
		boySprite.animate(200);
		boySprite.setZIndex(zIndexBoy);

		// Add a pause button
		ButtonSprite pauseBtn = getBaseActivity().getResourceUtil()
				.getButtonSprite("pause.png", "pause.png");
		pauseBtn.setPosition(getBaseActivity().getEngine().getCamera().getWidth() / 2.0f -
						pauseBtn.getWidth()/2.0f + 300,
				pauseBtn.getHeight()/2.0f);
		pauseBtn.setTag(MENU_PAUSE);
		pauseBtn.setOnClickListener(this);
		attachChild(pauseBtn);
		registerTouchArea(pauseBtn);



		// スコア表示
		currentScore = 0;
		BitmapFont bitmapFont = new BitmapFont(getBaseActivity()
				.getTextureManager(), getBaseActivity().getAssets(),
				"font/bitmap.fnt");
		bitmapFont.load();

		// ビットマップフォントを元にスコアを表示
		currentScoreText = new Text(20, 20, bitmapFont,
				"Score: " + currentScore, 20, new TextOptions(
						HorizontalAlign.LEFT), getBaseActivity()
						.getVertexBufferObjectManager());
		currentScoreText.setSize(50,50);
		attachChild(currentScoreText);
		highScoreText = new Text(20, 50, bitmapFont, "HighScore: "
				+ SPUtil.getInstance(getBaseActivity()).getHighScore(), 20,
				new TextOptions(HorizontalAlign.LEFT), getBaseActivity()
						.getVertexBufferObjectManager());
		attachChild(highScoreText);

		usernameText = new Text(20, 90, bitmapFont, "Player: "
				+ SPUtil.getInstance(getBaseActivity()).getPlayerName(), 20,
				new TextOptions(HorizontalAlign.LEFT), getBaseActivity()
				.getVertexBufferObjectManager());
		attachChild(usernameText);

		// 配列の初期化
		holeArray = new ArrayList<Sprite>();
		zombieArray = new ArrayList<AnimatedSprite>();
		itemArray = new ArrayList<Sprite>();

		// 攻撃エフェクトのSpriteを初期化
		weaponTextureArray = new TextureRegion[5];
		for (int i = 0; i < 5; i++) {
			weaponTextureArray[i] = (TextureRegion) getBaseActivity()
					.getResourceUtil().getSprite("weapon_0" + (i + 1) + ".png")
					.getTextureRegion();
		}
		// コンボ表示用フォントを初期化
		comboBMFontArray = new BitmapFont[3];
		for (int i = 0; i < 3; i++) {
			comboBMFontArray[i] = new BitmapFont(getBaseActivity()
					.getTextureManager(), getBaseActivity().getAssets(),
					"font/bitmap.fnt");
		}

		// ハイスコアが500以下の時のみヘルプ画面を出す
		if (SPUtil.getInstance(getBaseActivity()).getHighScore() > 500) {
			// ゾンビを出現させる
			showNewZombie();
			// 攻撃アイテムを出現させる
			showNewWeapon();
			isHelpVisible = false;
		} else {
			isHelpVisible = true;
			showHelp();
		}
	}

	// 遊び方画面を出現させる
	public void showHelp() {
		instructionSprite = ResourceUtil.getInstance(getBaseActivity())
				.getSprite("tutorial.png");
		placeToCenter(instructionSprite);
		attachChild(instructionSprite);

		// ボタン
		instructionBtn = ResourceUtil
				.getInstance(getBaseActivity())
				.getButtonSprite("initial_btn_01.png", "initial_btn_01.png");
		placeToCenterX(instructionBtn, 410);
		attachChild(instructionBtn);
		registerTouchArea(instructionBtn);
		instructionBtn.setOnClickListener(new ButtonSprite.OnClickListener() {
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				isHelpVisible = false;
				instructionSprite.detachSelf();
				instructionBtn.detachSelf();
				unregisterTouchArea(instructionBtn);

				// ゾンビを出現させる
				showNewZombie();
				// 攻撃アイテムを出現させる
				showNewWeapon();
			}
		});
	}

	@Override
	public void prepareSoundAndMusic() {
		// 効果音をロード
		try {

			btnPressedSound = SoundFactory.createSoundFromAsset(
					getBaseActivity().getSoundManager(), getBaseActivity(),
					"dokan.mp3");
			weapon01Sound = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "bakuahatsu.mp3");
			;
			weapon02Sound = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "moero.mp3");
			;
			weapon03Sound = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "bakuahatsu.mp3");
			;
			zombieAppearSound = SoundFactory.createSoundFromAsset(
					getBaseActivity().getSoundManager(), getBaseActivity(),
					"pi30.wav");
			;
			gameoverSound = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "gameover.mp3");
			;
			comboSound = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "upper.mp3");
			;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.getAction() == KeyEvent.ACTION_DOWN
				&& e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (isGameOver) {
				return false;
			}
			// ポーズ中ならポーズ画面を消去
			if (isPaused) {
				// detach系のメソッドは別スレッドで
				getBaseActivity().runOnUpdateThread(new Runnable() {
					public void run() {
						for (int i = 0; i < pauseBg.getChildCount(); i++) {
							// 忘れずにタッチの検知を無効に
							unregisterTouchArea((ButtonSprite) pauseBg
									.getChildByIndex(i));
						}
						pauseBg.detachSelf();
						pauseBg.detachChildren();
					}
				});

				resumeGame();

				isPaused = false;
				return true;
			} else {
				return false;
			}
		} else if (e.getAction() == KeyEvent.ACTION_DOWN
				&& e.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			// ポーズ中でなければポーズ画面を出す
			if (!isPaused) {
				showMenu();
			}
			return true;
		}
		return false;
	}

	// 主人公を端末の傾きに合わせて移動する関数
	public void updateByActivity(float velX, float velY) {
		if (isPaused || isGameOver) {
			return;
		}

		// 遊び方画面が出ている時は更新しない
		if (isHelpVisible) {
			return;
		}
		// スコアをインクリメント
		currentScore++;
		// スコアを表示
		currentScoreText.setText("Score " + currentScore);

		// 傾きが0ならば何もしない
		if ((velX != 0) || (velY != 0)) {
			// 移動の上限、下限を設定
			int minX = 0;
			int minY = 0;
			int maxX = (int) getBaseActivity().getEngine().getCamera()
					.getWidth()
					- (int) boySprite.getWidth();
			int maxY = (int) getBaseActivity().getEngine().getCamera()
					.getHeight()
					- (int) boySprite.getHeight();

			// 移動
			if (boyNewX >= minX) {
				boyNewX += velX;
			} else {
				boyNewX = minX;
			}
			if (boyNewX <= maxX) {
				boyNewX += velX;
			} else {
				boyNewX = maxX;
			}
			if (boyNewY >= minY) {
				boyNewY += velY;
			} else {
				boyNewY = minY;
			}
			if (boyNewY <= maxY) {
				boyNewY += velY;
			} else {
				boyNewY = maxY;
			}

			// 移動前の座標
			float[] currentXY = { boySprite.getX(), boySprite.getY() };
			// 移動
			boySprite.setPosition(boyNewX, boyNewY);
			// 移動後の座標
			float[] destinationXY = { boySprite.getX(), boySprite.getY() };

			// 壁にぶつかっている時等移動しない時には角度は変更しない
			if (currentXY[0] != destinationXY[0]
					|| currentXY[1] != destinationXY[1]) {
				// 移動前と移動後の座標の2点の角度を計算
				double angle = getAngleByTwoPosition(currentXY, destinationXY);
				// 向きを変更
				boySprite.setRotation((float) angle);
			}
		}
		for (int i = 0; i < zombieArray.size(); i++) {
			AnimatedSprite zombie = zombieArray.get(i);

			if (zombie.getTag() == TAG_ZOMBIE_01
					|| zombie.getTag() == TAG_ZOMBIE_02) {
				// ゾンビの種類によってスピードを変更する
				float[] zombieXY = { zombie.getX(), zombie.getY() };
				float[] boyXY = { boySprite.getX(), boySprite.getY() };
				double angle = getAngleByTwoPosition(zombieXY, boyXY);

				// 移動距離と角度からx方向、y方向の移動量を求める
				float distance = 0f;
				if (zombie.getTag() == TAG_ZOMBIE_01) {
					distance = 0.3f + (((zombieArray.size() - 1) - i) * 0.3f);
				} else {
					distance = 2.3f + (((zombieArray.size() - 1) - i) * 0.5f);
				}

				float x = -(float) (distance * Math
						.cos(angle * Math.PI / 180.0));
				float y = -(float) (distance * Math
						.sin(angle * Math.PI / 180.0));

				// 移動
				zombie.setPosition(zombie.getX() + x, zombie.getY() + y);
				// 向きを変更
				zombie.setRotation((float) angle);
			} else if (zombie.getTag() == TAG_ZOMBIE_03) {
				// 動かないゾンビの時はその場でぐるぐる回転させる
				zombie.setRotation(zombie.getRotation() + 1);
				//todo: want to make this zombie move
				float[] zombieXY = { zombie.getX(), zombie.getY() };
				float[] boyXY = { boySprite.getX(), boySprite.getY() };
				double angle = getAngleByTwoPosition(zombieXY, boyXY);

				// 移動距離と角度からx方向、y方向の移動量を求める
				float distance = 0f;
				if (zombie.getTag() == TAG_ZOMBIE_01) {
					distance = 0.5f + (((zombieArray.size() - 1) - i) * 0.3f);
				} else {
					distance = 2.5f + (((zombieArray.size() - 1) - i) * 0.5f);
				}

				float x = -(float) (distance * Math
						.cos(angle * Math.PI / 180.0));
				float y = -(float) (distance * Math
						.sin(angle * Math.PI / 180.0));

				// 移動
				zombie.setPosition(zombie.getX() + x, zombie.getY() + y);
			}

			if (zombie.collidesWith(boySprite)) {

				float xDistance = zombie.getX() - boySprite.getX();
				float yDistance = zombie.getY() - boySprite.getY();

				double distance = Math.sqrt(Math.pow(xDistance, 2)
						+ Math.pow(yDistance, 2));
				if (distance < 30) {
					showGameOver();
				}
			}
		}
		// アイテムをランダムに移動
		for (int i = 0; i < itemArray.size(); i++) {
			Sprite item = itemArray.get(i);
			//TODO: Test the movement of the item
			item.registerEntityModifier(new LoopEntityModifier(new MoveModifier(3.0f, item.getX(),
					item.getX(),item.getY(), item.getY() + 20, EaseBounceInOut.getInstance())));
			switch ((int) (Math.random() * 4)) {
			case 0:
				item.setX(item.getX() + 1);
				break;
			case 1:
				item.setX(item.getX() - 1);
				break;
			case 2:
				item.setY(item.getY() + 1);
				break;
			case 3:
				item.setY(item.getY() - 1);
				break;
			}
			// 攻撃アイテムに接触したら
			if (item.collidesWith(boySprite)) {
				// 攻撃
				fireWeapon(item);
				// アイテムを削除
				item.detachSelf();
				// アイテムを配列から削除
				itemArray.remove(item);
			}
		}
	}

	// ゾンビを一体出現
	public void showNewZombie() {
		for (int i = 0; i < holeArray.size(); i++) {
			final Sprite hole = holeArray.get(i);
			// 画面上に穴があれば2秒後にゾンビを出現させる
			TimerHandler delayHandler = new TimerHandler(2.0f,
					new ITimerCallback() {

						public void onTimePassed(TimerHandler pTimerHandler) {
							zombieAppearSound.play();
							AnimatedSprite zombieSprite;
							// ゾンビの出現確率を設定
							int r = (int) (Math.random() * 8);
							if (r == 0) {
								// 速いゾンビ
								zombieSprite = getBaseActivity()
										.getResourceUtil().getAnimatedSprite(
												"zombie.png", 1, 3);
								zombieSprite.setTag(TAG_ZOMBIE_02);
							} else if (r < 3) {
								// 動かないゾンビ
								zombieSprite = getBaseActivity()
										.getResourceUtil().getAnimatedSprite(
												"zombie2.png", 1, 3);
								zombieSprite.setTag(TAG_ZOMBIE_03);
							} else {
								// 普通のゾンビ
								zombieSprite = getBaseActivity()
										.getResourceUtil().getAnimatedSprite(
												"zombie.png", 1, 3);
								zombieSprite.setTag(TAG_ZOMBIE_01);
							}
							zombieSprite.setPosition(hole.getX(), hole.getY());
							// 主人公と同じ層に配置
							zombieSprite.setZIndex(zIndexBoy);
							attachChild(zombieSprite);
							sortChildren();

							zombieArray.add(zombieSprite);

							// アニメーション開始
							zombieSprite.animate(500);
							// 穴を削除
							hole.detachSelf();
							// 配列から削除
							holeArray.remove(hole);
						}
					});
			registerUpdateHandler(delayHandler);
		}
		int zombieCount = 0;
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildByIndex(i).getTag() == TAG_ZOMBIE_01
					|| getChildByIndex(i).getTag() == TAG_ZOMBIE_02
					|| getChildByIndex(i).getTag() == TAG_ZOMBIE_03) {
				zombieCount++;
			}
		}
		if (zombieCount < 9) {
			// ゾンビが出てくる穴を追加
			Sprite holeSprite = getBaseActivity().getResourceUtil().getSprite(
					"hole.png");
			// 出現場所の座標
			int x = (int) (Math.random() * (getBaseActivity().getEngine()
					.getCamera().getWidth() - holeSprite.getWidth()));
			int y = (int) (Math.random() * (getBaseActivity().getEngine()
					.getCamera().getHeight() - holeSprite.getHeight()));

			holeSprite.setPosition(x, y);
			attachChild(holeSprite);
			// z-indexを適用
			sortChildren();

			// 穴がうごめくアニメーション
			holeSprite.registerEntityModifier(new LoopEntityModifier(
					new SequenceEntityModifier(new ScaleModifier(0.2f, 1.0f,
							1.1f), new ScaleModifier(0.2f, 1.1f, 1.0f))));

			// 配列に追加
			holeArray.add(holeSprite);
		}
		// 3秒後にもう一度呼び出す
		int zombieShowValue = 3;
		if (SPUtil.getInstance(getBaseActivity()).getDifficultyLevel() == 1) {
			zombieShowValue = 1;
		}
		registerUpdateHandler(new TimerHandler(zombieShowValue, new ITimerCallback() {
			public void onTimePassed(TimerHandler pTimerHandler) {
				showNewZombie();
			}
		}));
	}

	// アイテムを一個出現
	public void showNewWeapon() {

		// アイテム用Sprite
		Sprite itemSprite = null;
		// それぞれ1/10の確率で出現
		int r = (int) (Math.random() * 10);
		if (r == 0) {
			itemSprite = getBaseActivity().getResourceUtil().getSprite(
					"red.png");
			itemSprite.setTag(TAG_ITEM_01);
		} else if (r == 1) {
			itemSprite = getBaseActivity().getResourceUtil().getSprite(
					"blue.png");
			itemSprite.setTag(TAG_ITEM_02);
		} else if (r == 2) {
			itemSprite = getBaseActivity().getResourceUtil().getSprite(
					"green.png");
			itemSprite.setTag(TAG_ITEM_03);
		}

		// アイテム出現に当選したら
		if (itemSprite != null) {
			// 出現場所の座標
			int x = (int) (Math.random() * (getBaseActivity().getEngine()
					.getCamera().getWidth() - itemSprite.getWidth()));
			int y = (int) (Math.random() * (getBaseActivity().getEngine()
					.getCamera().getHeight() - itemSprite.getHeight()));

			itemSprite.setZIndex(zIndexItem);
			itemSprite.setPosition(x, y);
			attachChild(itemSprite);
			// z-indexを適用
			sortChildren();

			itemArray.add(itemSprite);
		}

		// 3秒後に再度呼び出し
		TimerHandler handler = new TimerHandler(3, new ITimerCallback() {
			public void onTimePassed(TimerHandler pTimerHandler) {
				showNewWeapon();
			}
		});
		registerUpdateHandler(handler);
	}

	// 攻撃
	public void fireWeapon(Sprite item) {
		// 攻撃エフェクトを格納する配列
		ArrayList<Sprite> weaponSpriteArray = new ArrayList<Sprite>();

		// 炎攻撃
		if (item.getTag() == TAG_ITEM_01) {
			weapon01Sound.play();
			// 生成済みのテクスチャからSpriteを生成
			final Sprite weaponSprite = new Sprite(0, 0, weaponTextureArray[0],
					getBaseActivity().getVertexBufferObjectManager());
			weaponSprite.setPosition(boySprite.getX() + boySprite.getWidth()
					/ 2 - weaponSprite.getWidth() / 2, boySprite.getY()
					+ boySprite.getHeight() / 2 - weaponSprite.getHeight() / 2);
			weaponSprite.setZIndex(zIndexItem);
			// アルファ値の設定を可能に
			weaponSprite.setBlendFunction(GL10.GL_SRC_ALPHA,
					GL10.GL_ONE_MINUS_SRC_ALPHA);
			weaponSprite.setAlpha(0);
			attachChild(weaponSprite);
			sortChildren();

			weaponSpriteArray.add(weaponSprite);

			// 攻撃アイテムのアニメーション
			weaponSprite.registerEntityModifier(new SequenceEntityModifier(
					new FadeInModifier(0.2f), new FadeOutModifier(0.7f)));

			// アニメーション終了後に削除
			registerUpdateHandler(new TimerHandler(1, new ITimerCallback() {
				public void onTimePassed(TimerHandler pTimerHandler) {
					weaponSprite.detachSelf();
				}
			}));
		}
		// 氷攻撃
		else if (item.getTag() == TAG_ITEM_02) {
			weapon02Sound.play();
			for (int i = 0; i < 2; i++) {
				final Sprite weaponSprite = new Sprite(0, 0,
						weaponTextureArray[1 + i], getBaseActivity()
								.getVertexBufferObjectManager());
				switch (i) {
				case 0:
					weaponSprite.setPosition(
							boySprite.getX() + boySprite.getWidth() / 2
									- weaponSprite.getWidth() / 2, 0);
					break;
				case 1:
					weaponSprite.setPosition(0,
							boySprite.getY() + boySprite.getHeight() / 2
									- weaponSprite.getHeight() / 2);
					break;
				}
				weaponSprite.setZIndex(zIndexItem);
				// アルファ値の設定を可能に
				weaponSprite.setBlendFunction(GL10.GL_SRC_ALPHA,
						GL10.GL_ONE_MINUS_SRC_ALPHA);
				weaponSprite.setAlpha(0);
				attachChild(weaponSprite);
				sortChildren();

				weaponSpriteArray.add(weaponSprite);

				// 攻撃アイテムのアニメーション
				weaponSprite.registerEntityModifier(new SequenceEntityModifier(
						new FadeInModifier(0.2f), new FadeOutModifier(0.7f)));
				// アニメーション終了後に削除
				registerUpdateHandler(new TimerHandler(1, new ITimerCallback() {
					public void onTimePassed(TimerHandler pTimerHandler) {
						weaponSprite.detachSelf();
					}
				}));
			}
		}
		// 雷攻撃
		else {
			weapon03Sound.play();
			for (int i = 0; i < 4; i++) {
				final Sprite weaponSprite = new Sprite(0, 0,
						weaponTextureArray[3 + (i / 2)], getBaseActivity()
								.getVertexBufferObjectManager());
				switch (i) {
				case 0:
				case 2:
					weaponSprite.setPosition(0, 0);
					break;
				case 1:
					weaponSprite.setPosition(getBaseActivity().getEngine()
							.getCamera().getWidth()
							- weaponSprite.getWidth(), 0);
					break;
				case 3:
					weaponSprite.setPosition(0, getBaseActivity().getEngine()
							.getCamera().getHeight()
							- weaponSprite.getHeight());
					break;
				}
				weaponSprite.setZIndex(zIndexItem);
				// アルファ値の設定を可能に
				weaponSprite.setBlendFunction(GL10.GL_SRC_ALPHA,
						GL10.GL_ONE_MINUS_SRC_ALPHA);
				weaponSprite.setAlpha(0);
				attachChild(weaponSprite);
				sortChildren();

				weaponSpriteArray.add(weaponSprite);

				// 攻撃アイテムのアニメーション
				weaponSprite.registerEntityModifier(new SequenceEntityModifier(
						new FadeInModifier(0.2f), new FadeOutModifier(0.7f)));
				// アニメーション終了後に削除
				registerUpdateHandler(new TimerHandler(1, new ITimerCallback() {
					public void onTimePassed(TimerHandler pTimerHandler) {
						weaponSprite.detachSelf();
					}
				}));
			}
		}
		// 画面から削除されたゾンビを格納する配列
		final ArrayList<AnimatedSprite> detachedZombieArray = new ArrayList<AnimatedSprite>();
		// 衝突判定、ゾンビを消す
		for (Sprite weapon : weaponSpriteArray) {
			for (int i = 0; i < zombieArray.size(); i++) {
				final AnimatedSprite zombie = zombieArray.get(i);
				if (zombie.collidesWith(weapon)) {
					// 既に追加済みでなければ配列に格納
					if (!detachedZombieArray.contains(zombie)) {
						detachedZombieArray.add(zombie);
					}
					// ゾンビをフェードアウト
					zombie.registerEntityModifier(new FadeOutModifier(0.7f));
					registerUpdateHandler(new TimerHandler(0.8f,
							new ITimerCallback() {
								public void onTimePassed(
										TimerHandler pTimerHandler) {
									zombie.detachSelf();
								}
							}));
				}
			}
		}

		// 一回の攻撃で複数のゾンビを倒した時はコンボボーナスを与える
		if (detachedZombieArray.size() > 1) {
			comboSound.play();
			BitmapFont bitmapFont;
			if (detachedZombieArray.size() == 2) {
				bitmapFont = comboBMFontArray[0];
			} else if (detachedZombieArray.size() == 3) {
				bitmapFont = comboBMFontArray[1];
			} else {
				bitmapFont = comboBMFontArray[2];
			}
			bitmapFont.load();

			// ビットマップフォントを元にスコアを表示
			final Text comboText = new Text(0, 0, bitmapFont, "x"
					+ detachedZombieArray.size(), 20, new TextOptions(
					HorizontalAlign.CENTER), getBaseActivity()
					.getVertexBufferObjectManager());
			comboText.setPosition(boySprite.getX(), boySprite.getY());
			attachChild(comboText);

			// コンボのテキストを移動アニメーションさせる
			comboText.registerEntityModifier(new SequenceEntityModifier(
					new DelayModifier(2.0f), new MoveModifier(0.5f, comboText
							.getX(), currentScoreText.getX(), comboText.getY(),
							currentScoreText.getY(), EaseBackIn.getInstance()),
					new FadeOutModifier(0.5f)));
			final int multiplayer = detachedZombieArray.size();

			// 得点にコンボ数を乗算し適用する
			registerUpdateHandler(new TimerHandler(3, new ITimerCallback() {
				public void onTimePassed(TimerHandler pTimerHandler) {
					comboText.detachSelf();
					currentScore *= multiplayer;
				}
			}));
		}

		// 画面から削除されたゾンビを配列から削除
		for (AnimatedSprite zombie : detachedZombieArray) {
			zombieArray.remove(zombie);
		}
	}

	// ゲームオーバー
	public void showGameOver() {

		if (isGameOver) {
			return;
		}
		isGameOver = true;

		gameoverSound.play();
		// ハイスコア更新時はハイスコアのテキストも更新
		if (currentScore > SPUtil.getInstance(getBaseActivity()).getHighScore()) {
			// ハイスコアを保存
			SPUtil.getInstance(getBaseActivity()).setHighScore(currentScore);
			highScoreText.setText("Highscore "
					+ SPUtil.getInstance(getBaseActivity()).getHighScore());
		}

		// ゲームオーバー画面の背景画像
		Sprite resultBg = getBaseActivity().getResourceUtil().getSprite(
				"dead.png");
		resultBg.setPosition(0, -getBaseActivity().getEngine().getCamera()
				.getWidth());
		resultBg.setZIndex(zIndexGameOverLayer);
		attachChild(resultBg);
		sortChildren();

		// 主人公を縮小しながらフェードアウト
		boySprite.registerEntityModifier(new ParallelEntityModifier(
				new ScaleModifier(0.7f, 1f, 0.5f), new FadeOutModifier(0.7f)));

		// 全てのアップデートハンドラを削除
		unregisterUpdateHandlers(new IUpdateHandlerMatcher() {
			public boolean matches(IUpdateHandler pObject) {
				return true;
			}
		});
		// 出現穴がある場合はアニメーションをストップ
		for (Sprite hole : holeArray) {
			hole.unregisterEntityModifiers(new IEntityModifierMatcher() {
				public boolean matches(IModifier<IEntity> pObject) {
					return true;
				}
			});
		}
		// ゾンビにつかまり襲われるエフェクト
		registerUpdateHandler(zombieAttackHandler);
		// 上からゲームオーバー画面が振ってくるアニメーション
		resultBg.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(2.0f), new MoveModifier(1.0f,
						resultBg.getX(), resultBg.getX(), resultBg.getY(), 0,
						EaseQuadOut.getInstance())));

		Sprite title = getBaseActivity().getResourceUtil().getSprite(
				"yourscore.png");
		placeToCenterX(title, 20);
		resultBg.attachChild(title);

		BitmapFont bitmapFont = new BitmapFont(getBaseActivity()
				.getTextureManager(), getBaseActivity().getAssets(),
				"font/bitmap.fnt");
		bitmapFont.load();

		// ビットマップフォントを元にスコアを表示
		Text resultText = new Text(0, 0, bitmapFont, "" + currentScore, 20,
				new TextOptions(HorizontalAlign.CENTER), getBaseActivity()
						.getVertexBufferObjectManager());
		resultText.setPosition(getBaseActivity().getEngine().getCamera()
				.getWidth()
				/ 2.0f - resultText.getWidth() / 2.0f, 90);
		resultBg.attachChild(resultText);

		// ボタン類
		ButtonSprite btnRanking = getBaseActivity().getResourceUtil()
				.getButtonSprite("leaders.png", "leaders.png");
		placeToCenterX(btnRanking, 175);
		btnRanking.setTag(MENU_RANKING);
		btnRanking.setOnClickListener(this);
		resultBg.attachChild(btnRanking);
		registerTouchArea(btnRanking);

		ButtonSprite btnRetry = getBaseActivity().getResourceUtil()
				.getButtonSprite("retry.png", "retry.png");
		btnRetry.setPosition(100, 265);
		btnRetry.setTag(MENU_RETRY);
		btnRetry.setOnClickListener(this);
		resultBg.attachChild(btnRetry);
		registerTouchArea(btnRetry);

		ButtonSprite btnTweet = getBaseActivity().getResourceUtil()
				.getButtonSprite("sharescore.png", "sharescore.png");
		btnTweet.setPosition(265, 265);
		btnTweet.setTag(MENU_TWEET);
		btnTweet.setOnClickListener(this);
		resultBg.attachChild(btnTweet);
		registerTouchArea(btnTweet);

		ButtonSprite btnMenu = getBaseActivity().getResourceUtil()
				.getButtonSprite("menu.png", "menu.png");
		btnMenu.setPosition(590, 265);
		btnMenu.setTag(MENU_MENU);
		btnMenu.setOnClickListener(this);
		resultBg.attachChild(btnMenu);
		registerTouchArea(btnMenu);
	}

	public void showMenu() {
		if (isGameOver) {
			return;
		}
		pauseGame();

		// 四角形を描画
		pauseBg = new Rectangle(0, 0, getBaseActivity().getEngine().getCamera()
				.getWidth(), getBaseActivity().getEngine().getCamera()
				.getHeight(), getBaseActivity().getVertexBufferObjectManager());
		pauseBg.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		pauseBg.setColor(0, 0, 0);
		pauseBg.setAlpha(0.7f);
		attachChild(pauseBg);

		try {
			ButtonSprite btnMenu = getBaseActivity()
					.getResourceUtil()
					.getButtonSprite("retry.png", "retry.png");
			placeToCenterX(btnMenu, 100);
			btnMenu.setTag(MENU_RESUME);
			btnMenu.setOnClickListener(this);
			pauseBg.attachChild(btnMenu);
			registerTouchArea(btnMenu);

			ButtonSprite btnRanking = getBaseActivity().getResourceUtil()
					.getButtonSprite("menu.png", "menu.png");
			placeToCenterX(btnRanking, 240);
			btnRanking.setTag(MENU_MENU);
			btnRanking.setOnClickListener(this);
			pauseBg.attachChild(btnRanking);
			registerTouchArea(btnRanking);
		} catch (Exception e) {
			e.printStackTrace();
		}

		isPaused = true;
	}

	// ゲームを一時的に停止
	public void pauseGame() {
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildByIndex(i) instanceof AnimatedSprite) {
				((AnimatedSprite) getChildByIndex(i)).stopAnimation();
			}
		}
		unregisterUpdateHandlers(new IUpdateHandlerMatcher() {
			public boolean matches(IUpdateHandler pObject) {
				return true;
			}
		});
		for (Sprite hole : holeArray) {
			hole.unregisterEntityModifiers(new IEntityModifierMatcher() {
				public boolean matches(IModifier<IEntity> pObject) {
					return true;
				}
			});
		}
	}

	// 一時停止されたゲームを再開
	public void resumeGame() {
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildByIndex(i) instanceof AnimatedSprite) {
				((AnimatedSprite) getChildByIndex(i)).stopAnimation();
			}
		}

		for (Sprite hole : holeArray) {
			hole.registerEntityModifier(new LoopEntityModifier(
					new SequenceEntityModifier(new ScaleModifier(0.2f, 1.0f,
							1.1f), new ScaleModifier(0.2f, 1.1f, 1.0f))));
		}

		TimerHandler timerHandler = new TimerHandler(1, new ITimerCallback() {
			public void onTimePassed(TimerHandler pTimerHandler) {
				showNewZombie();
				showNewWeapon();
			}
		});
		registerUpdateHandler(timerHandler);
	}

	private TimerHandler zombieAttackHandler = new TimerHandler(1f / 60f, true,
			new ITimerCallback() {
				public void onTimePassed(TimerHandler pTimerHandler) {
					for (int i = 0; i < zombieArray.size(); i++) {
						AnimatedSprite zombie = zombieArray.get(i);


						if (zombie.getTag() == TAG_ZOMBIE_01
								|| zombie.getTag() == TAG_ZOMBIE_02) {
							float[] zombieXY = { zombie.getX(), zombie.getY() };
							float[] boyXY = { boySprite.getX(),
									boySprite.getY() };
							double angle = getAngleByTwoPosition(zombieXY,
									boyXY);

							float distance = 8f;

							float x = -(float) (distance * Math.cos(angle
									* Math.PI / 180.0));
							float y = -(float) (distance * Math.sin(angle
									* Math.PI / 180.0));

							// 移動
							zombie.setPosition(zombie.getX() + x, zombie.getY()
									+ y);
							// 向きを変更
							zombie.setRotation((float) angle);

						} else if (zombie.getTag() == TAG_ZOMBIE_03) {
							zombie.setRotation(zombie.getRotation() + 3);
						}
						//TODO: Test the movement of the zombie attack

					}
				}
			});

	// 2点間の角度を求める公式
	private double getAngleByTwoPosition(float[] start, float[] end) {
		double result = 0;

		float xDistance = end[0] - start[0];
		float yDistance = end[1] - start[1];

		result = Math.atan2((double) yDistance, (double) xDistance) * 180
				/ Math.PI;
		result += 180;

		return result;
	}

	public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		btnPressedSound.play();
		switch (pButtonSprite.getTag()) {
		case MENU_RESUME:
			getBaseActivity().runOnUpdateThread(new Runnable() {
				public void run() {
					for (int i = 0; i < pauseBg.getChildCount(); i++) {
						// 忘れずにタッチの検知を無効に
						unregisterTouchArea((ButtonSprite) pauseBg
								.getChildByIndex(i));
					}
					pauseBg.detachSelf();
					pauseBg.detachChildren();
				}
			});

			resumeGame();
			clickHandleState.proceed();
			isPaused = false;
			break;
			case MENU_PAUSE:
				//When the pause button is pressed
				if (clickHandleState.getClickState() == ClickHandleState.State.ON) {
					clickHandleState.proceed();
					showMenu();
				}
				break;
		case MENU_RETRY:
			MainScene newScene = new MainScene(getBaseActivity());
			getBaseActivity().refreshRunningScene(newScene);
			break;
		case MENU_MENU:
			getBaseActivity().backToInitial();
			break;
		case MENU_TWEET:
			//TODO: show dialog where you can enter your id
			String currentName = SPUtil.getInstance(getBaseActivity()).getPlayerName();
			if (currentName != "Guest") {
				sendScoreToDB(currentScore);
			} else {
				getBaseActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getBaseActivity(),"Guest can't post high scores",
								Toast.LENGTH_SHORT).show();
					}
				});

			}

			break;
		case MENU_RANKING:
			//connect to localhost for testing purpose
			Intent intent = new Intent(getBaseActivity(),ScoreActivity.class);
			intent.putExtra("score", SPUtil.getInstance(getBaseActivity()).getHighScore());
			getBaseActivity().startActivity(intent);
			break;
		}
	}
	private void sendScoreToDB(long currentScore) {
		final long sendScore = currentScore;
		// UIスレッド上でないと動きません
		getBaseActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//TODO: Randomly generate 5 letters and numbers.
				final String randomCode = randomString(3);
				// Dialogを生成
				StringInputDialogBuilder builder = new StringInputDialogBuilder(getBaseActivity(),
						randomCode,    // タイトル文言のリソースID
						R.string.content_sendscore,   // 本文のリソースID
						R.string.title_error,    // エラー時のToastで表示される文言のリソースID
						R.mipmap.ic_launcher,        // タイトル横のアイコン画像のリソースID

						// OKボタンを押した時のコールバック.
						// 引数pCallbackValueに入力した文字列が入ってくる
						new Callback<String>() {
							@Override
							public void onCallback(String pCallbackValue) {
								if (pCallbackValue.trim().length() != 0 && pCallbackValue.equals(randomCode)) {
									//dialogPack(sendScore);
									//send score
									String name = SPUtil.getInstance(getBaseActivity()).getPlayerName();
									DBUtil.getInstance().saveScoreValues("", name,
											sendScore+"");
									Toast.makeText(getBaseActivity(), name+" score has been sent successfully! ",
											Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(getBaseActivity(), "Type in the text ("+randomCode+") ",
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

	/*
	This method will be used for generating random code to be used for confirming if the user is not
	a robot.
	 */
	private String randomString( int len ){
		StringBuilder sb = new StringBuilder( len );
		for( int i = 0; i < len; i++ )
			sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		return sb.toString().trim();
	}

	}

