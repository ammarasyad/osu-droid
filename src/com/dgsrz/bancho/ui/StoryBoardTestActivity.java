package com.dgsrz.bancho.ui;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.ui.activity.LegacyBaseGameActivity;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import ru.nsu.ccfit.zuev.audio.BassAudioPlayer;
import ru.nsu.ccfit.zuev.osu.ResourceManager;
import ru.nsu.ccfit.zuev.osu.storyboard.OsbParser;
import ru.nsu.ccfit.zuev.osu.storyboard.OsuSprite;

/**
 * Based on native touch event
 * <p>
 * Created by dgsrz on 15/10/11.
 */
public class StoryBoardTestActivity extends LegacyBaseGameActivity implements IUpdateHandler {

    // public static final String FOLDER = "/sdcard/osu!player/51142 Suzuta Miyako - one's future (Full Ver.)";
    // public static final String PATH = "/Suzuta Miyako - one's future (Full Ver.) (DJPop) [Insane].osu";
    public static final String FOLDER = "/sdcard/osu!player/EOS";
    public static final String PATH = "/1.osu";
    private static final int INVALID_POINTER_ID = -1;
    private static final int CAMERA_WIDTH = 640;
    private static final int CAMERA_HEIGHT = 480;
    public static StoryBoardTestActivity activity;
    public String mBackground;
    // public static final String FOLDER = "/sdcard/osu!player/Okaerinasai";
    // public static final String PATH = "/1.osu";
    public String mAudioFileName;
    public Entity background, fail, pass, foreground;
    public AtomicInteger onScreenDrawCalls = new AtomicInteger(0);
    private LinkedList<OsuSprite> osuSprites;
    private OsuSprite nextSprite;

    private Scene scene;

    private float totalElapsed = 0f;

    {
        activity = this;
    }

    @Override
    public Engine onLoadEngine() {
        Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        return new Engine(new EngineOptions(true,
                ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
                camera));
    }

    @Override
    public void onLoadResources() {
    }

    @Override
    protected void onUnloadResources() {
    }

    @Override
    public Scene onLoadScene() {
        this.mEngine.registerUpdateHandler(new FPSLogger(1f));
        scene = new Scene();
        scene.setBackground(new Background(0, 0, 0));
        scene.registerUpdateHandler(this);

        ResourceManager.getInstance().Init(this.mEngine, this);
        ResourceManager.getInstance().loadHighQualityAsset("cursor", "gfx/cursor.png");
        ResourceManager.getInstance().loadHighQualityFileUnderFolder(new File(FOLDER));

        BassAudioPlayer.initDevice();

        try {
            OsbParser.instance.parse(FOLDER + PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // File file = new File(FOLDER, mAudioFileName);

        background = new Entity(0, 0);
//        fail = new Entity(0, 0);
        pass = new Entity(0, 0);
        foreground = new Entity(0, 0);
        scene.attachChild(background);
//        scene.attachChild(fail);
        scene.attachChild(pass);
        scene.attachChild(foreground);

        osuSprites = OsbParser.instance.getSprites();
        if (osuSprites != null && osuSprites.size() > 0) {
            nextSprite = osuSprites.remove(0);
            // Log.i("switch sprite", "start line: " + nextSprite.getDebugLine());
        }
        return scene;
    }

    @Override
    public Scene onLoadComplete() {
        return scene;
    }

    @Override
    public void onUpdate(float pSecondsElapsed) {
        totalElapsed += pSecondsElapsed;
        if (null != nextSprite) {
            checkSpriteTime(totalElapsed * 1000);
        }
    }

    private void checkSpriteTime(float pSecondsElapsed) {
        if (pSecondsElapsed >= nextSprite.spriteStartTime) {
            nextSprite.play();
            if (osuSprites.size() > 0) {
                nextSprite = osuSprites.remove(0);
                // Log.i("switch sprite", "start line: " + nextSprite.getDebugLine());
                checkSpriteTime(pSecondsElapsed);
            } else {
                nextSprite = null;
            }
        }
    }

    @Override
    public void reset() {

    }

    public void attachBackground(Sprite sprite) {
        background.attachChild(sprite);
        background.sortChildren();
    }

//    public void attachFail(BaseSprite sprite)
//    {
//        fail.attachChild(sprite);
//        fail.sortChildren();
//    }

    public void attachPass(Sprite sprite) {
        pass.attachChild(sprite);
        pass.sortChildren();
    }

    public void attachForeground(Sprite sprite) {
        foreground.attachChild(sprite);
        foreground.sortChildren();
    }
}
