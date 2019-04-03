package ninja.anteprocess.com.hakaishin;

import android.view.KeyEvent;

import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.BitmapFont;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

public class SplashScene extends KeyListenScene {

    private  boolean mIsBegin = false;

    public SplashScene(MultiSceneActivity context) {
        super(context);
        init();
    }

    @Override
    public void init() {
        // 背景
        ResourceUtil.getInstance(getBaseActivity()).resetAllTexture();

        Sprite ground = getBaseActivity().getResourceUtil().getSprite(
                "main_bg_title.png");

        ground.setPosition(0, 0);
        attachChild(ground);


        BitmapFont bitmapFont = new BitmapFont(getBaseActivity()
                .getTextureManager(), getBaseActivity().getAssets(),
                "font/bitmap.fnt");
        bitmapFont.load();

        setBackground(new Background(Color.WHITE));
        // ビットマップフォントを元にスコアを表示
        Text resultText = new Text(0, 0, bitmapFont, "Loading" , 40,
                new TextOptions(HorizontalAlign.CENTER), getBaseActivity()
                .getVertexBufferObjectManager());
        resultText.setPosition(getBaseActivity().getEngine().getCamera()
                .getWidth()
                / 2.0f - resultText.getWidth() / 2.0f,  (getBaseActivity().getEngine().getCamera()
                .getHeight() / 2.0f) );
        attachChild(resultText);

/*
        Sprite bg = getBaseActivity().getResourceUtil().getSprite(
                "badge.png");
        bg.setWidth(bg.getWidth() / 2);
        bg.setHeight(bg.getHeight() / 2);
        placeToCenter(bg);
        attachChild(bg);
*/

/*
        if (!mIsBegin) {
            getBaseActivity().getEngine().registerUpdateHandler(new TimerHandler(2f, new ITimerCallback() {
                @Override
                public void onTimePassed(TimerHandler timerHandler) {
                    ResourceUtil.getInstance(getBaseActivity()).resetAllTexture();
                    KeyListenScene scene = new InitialScene(getBaseActivity());
                    //InitialSceneへ移動
                    getBaseActivity().getEngine().setScene(scene);
                    // 遷移管理用配列に追加
                    //getBaseActivity().appendScene(scene);

                }
            }));
        }
        */


    }


    @Override
    public void prepareSoundAndMusic() {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        return false;
    }


}
