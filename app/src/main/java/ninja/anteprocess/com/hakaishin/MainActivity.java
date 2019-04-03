package ninja.anteprocess.com.hakaishin;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.KeyEvent;

import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;

import anteprocess.com.zombie.R;

public class MainActivity extends MultiSceneActivity implements
		SensorEventListener {
	// 画面のサイズ。
	private int CAMERA_WIDTH = 800;
	private int CAMERA_HEIGHT = 480;

	// 傾きの量
	private float velX;
	private float velY;

	// センサーマネージャ
	SensorManager mSensorManager;

	public EngineOptions onCreateEngineOptions() {
		// サイズを指定し描画範囲をインスタンス化
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		// ゲームのエンジンを初期化。
		// 第1引数 タイトルバーを表示しないモード
		// 第2引数 画面は縦向き（幅480、高さ800）
		// 第3引数 解像度の縦横比を保ったまま最大まで拡大する
		// 第4引数 描画範囲
		EngineOptions eo = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		// 効果音の使用を許可する
		eo.getAudioOptions().setNeedsSound(true);
        // Set up the music options
        eo.getAudioOptions().setNeedsMusic(true);
		return eo;
	}

	@Override
	protected Scene onCreateScene() {
		// サウンドファイルの格納場所を指定
		SoundFactory.setAssetBasePath("mfx/");
		// センサー系の処理を管理するクラス
		mSensorManager = (SensorManager) this
				.getSystemService(Context.SENSOR_SERVICE);
		// センサーイベントのリスナーを設定
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);

		getEngine().registerUpdateHandler(new IUpdateHandler() {
			public void onUpdate(float pSecondsElapsed) {
				if (getEngine().getScene() instanceof MainScene) {
					((MainScene) getEngine().getScene()).updateByActivity(velX,
							velY);
				}
			}

			public void reset() {

			}
		});
		// InitialSceneをインスタンス化し、エンジンにセット
		InitialScene initialScene = new InitialScene(this);
		return initialScene;
	}

	@Override
	public void onPause() {
		super.onPause();

		// MainScene実行中なら一時停止
		if (getEngine().getScene() instanceof MainScene) {
			((MainScene) getEngine().getScene()).showMenu();
		}
	}

	@Override
	protected int getLayoutID() {
		// ActivityのレイアウトのIDを返す
		return R.layout.activity_main;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		// SceneがセットされるViewのIDを返す
		return R.id.renderview;
	}

	@Override
	public void appendScene(KeyListenScene scene) {
		getSceneArray().add(scene);
	}

	@Override
	public void backToInitial() {
		// 遷移管理用配列をクリア
		getSceneArray().clear();
		// 新たにInitialSceneからスタート
		KeyListenScene scene = new InitialScene(this);
		getSceneArray().add(scene);
		getEngine().setScene(scene);
	}

	@Override
	public void refreshRunningScene(KeyListenScene scene) {
		getSceneArray().remove(getSceneArray().size() - 1);
		getSceneArray().add(scene);
		getEngine().setScene(scene);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		// バックボタンが押された時
		if (e.getAction() == KeyEvent.ACTION_DOWN
				&& e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			// 起動中のSceneのdispatchKeyEvent関数を呼び出し。追加の処理が必要な時はfalseが
			// 返ってくる為、処理
			int sceneCalc = 0;
			android.util.Log.d("Test",getSceneArray().size()+" :");
			if (getSceneArray().size() != 0) {
				/**
				 * This is a workaround to prevent the app from crashing when back button is
				 * is pressed.
				 */
				sceneCalc = 1;
			} else {
				//TODO: This is a workaround
				ResourceUtil.getInstance(this).resetAllTexture();
				finish();
				return false;
			}
			//android.util.Log.d("Test",getSceneArray().get(getSceneArray().size())
			//		+"");
			if (!getSceneArray().get(getSceneArray().size() - sceneCalc).dispatchKeyEvent(e)) {

				// Sceneが1つしか起動していない時はゲームを終了
				if (getSceneArray().size() == 1) {
					ResourceUtil.getInstance(this).resetAllTexture();
					finish();
				}
				// 複数のSceneが起動している時は1つ前のシーンへ戻る
				else {
					getEngine().setScene(
							getSceneArray().get(getSceneArray().size() - 2));
					getSceneArray().remove(getSceneArray().size() - 1);
				}
			}
			return true;
		} else if (e.getAction() == KeyEvent.ACTION_DOWN
				&& e.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			getSceneArray().get(getSceneArray().size() - 1).dispatchKeyEvent(e);
			return true;
		}
		return false;
	}

	// センサーの精度が変更された時に呼び出される関数
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	// センサー自体の値が変更された時に呼び出される関数
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			switch (event.sensor.getType()) {
			// 加速度センサーの場合
			case Sensor.TYPE_ACCELEROMETER:
				// 値を格納
				velX = event.values[1];
				velY = event.values[0];
				break;
			}
		}
	}
}
