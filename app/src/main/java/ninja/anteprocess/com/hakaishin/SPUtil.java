package ninja.anteprocess.com.hakaishin;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtil {
	// 自身のインスタンス
	private static SPUtil instance;

	// シングルトン
	public static synchronized SPUtil getInstance(Context context) {
		if (instance == null) {
			instance = new SPUtil(context);
		}
		return instance;
	}

	private static SharedPreferences settings;
	private static SharedPreferences.Editor editor;

	private SPUtil(Context context) {
		settings = context.getSharedPreferences("shared_preference_1.0", 0);
		editor = settings.edit();
	}


	public long getHighScore() {
		return settings.getLong("highScore", 0l);
	}

	public void setHighScore(long value) {
		editor.putLong("highScore", value);
		editor.commit();
	}

	//SPUtil.getInstance(getBaseActivity()).setPlayerName("name");
	public void setPlayerName(String value) {
		editor.putString("playerName", value);
		editor.commit();
	}
	//SPUtil.getInstance(getBaseActivity()).getPlayerName()
	public String getPlayerName() {
		return settings.getString("playerName","Guest");
	}


	/*
	 The following methods are from the games difficulty level
	 */
	public void setDifficultyLevel(int value) {
		editor.putInt("level",value);
		editor.commit();
	}

	public int getDifficultyLevel() {
		return settings.getInt("level",0);
	}

}
