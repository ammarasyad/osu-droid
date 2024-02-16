package ru.nsu.ccfit.zuev.osu.menu;

import org.andengine.engine.Engine;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;

import ru.nsu.ccfit.zuev.audio.BassSoundProvider;
import ru.nsu.ccfit.zuev.osu.Config;
import ru.nsu.ccfit.zuev.osu.GlobalManager;
import ru.nsu.ccfit.zuev.osu.ResourceManager;
import ru.nsu.ccfit.zuev.osu.ToastLogger;
import ru.nsu.ccfit.zuev.osu.game.GameScene;
import ru.nsu.ccfit.zuev.osuplus.R;

public class PauseMenu implements IOnMenuItemClickListener {
    static final int ITEM_SAVE_REPLAY = 0;
    static final int ITEM_CONTINUE = 1;
    static final int ITEM_RETRY = 2;
    static final int ITEM_BACK = 3;
    private final MenuScene scene;
    private final GameScene game;
    private final boolean fail;
    private boolean replaySaved;

    public PauseMenu(final Engine engine, final GameScene game,
                     final boolean fail) {
        this.game = game;
        this.fail = fail;
        replaySaved = false;
        scene = new MenuScene(engine.getCamera());

        final SpriteMenuItem saveFailedReplay = new SpriteMenuItem(ITEM_SAVE_REPLAY,
                ResourceManager.getInstance().getTexture("pause-save-replay"), GlobalManager.getInstance().getEngine().getVertexBufferObjectManager());
        scene.addMenuItem(saveFailedReplay);
        final SpriteMenuItem itemContinue = new SpriteMenuItem(ITEM_CONTINUE,
                ResourceManager.getInstance().getTexture("pause-continue"), GlobalManager.getInstance().getEngine().getVertexBufferObjectManager());
        scene.addMenuItem(itemContinue);
        final SpriteMenuItem itemRetry = new SpriteMenuItem(ITEM_RETRY,
                ResourceManager.getInstance().getTexture("pause-retry"), GlobalManager.getInstance().getEngine().getVertexBufferObjectManager());
        scene.addMenuItem(itemRetry);
        final SpriteMenuItem itemBack = new SpriteMenuItem(ITEM_BACK,
                ResourceManager.getInstance().getTexture("pause-back"), GlobalManager.getInstance().getEngine().getVertexBufferObjectManager());
        scene.addMenuItem(itemBack);
        scene.setBackgroundEnabled(false);
        TextureRegion tex;
        if (fail) {
            itemContinue.setVisible(false);
            tex = ResourceManager.getInstance().getTexture("fail-background");
            if (game.getReplaying()){
                saveFailedReplay.setVisible(false);
            }
        } else {
            saveFailedReplay.setVisible(false);
            tex = ResourceManager.getInstance().getTexture("pause-overlay");
        }

        if (tex != null) {
            float height = tex.getHeight();
            height *= Config.getRES_WIDTH() / (float) (tex.getWidth());
            final Sprite bg = new Sprite(0,
                    (Config.getRES_HEIGHT() - height) / 2,
                    Config.getRES_WIDTH(), height, tex, GlobalManager.getInstance().getEngine().getVertexBufferObjectManager());
            scene.attachChild(bg, 0);
        }

        scene.buildAnimations();
        scene.setOnMenuItemClickListener(this);
    }

    public MenuScene getScene() {
        return scene;
    }


    public boolean onMenuItemClicked(final MenuScene pMenuScene,
                                     final IMenuItem pMenuItem, final float pMenuItemLocalX,
                                     final float pMenuItemLocalY) {

        if (pMenuItem.getAlpha() < 0.75f) {
            return false;
        }
        BassSoundProvider playSnd;
        switch (pMenuItem.getID()) {
            case ITEM_SAVE_REPLAY:
                if(fail && !replaySaved && !game.getReplaying() && game.saveFailedReplay()){
                    ToastLogger.showTextId(R.string.message_save_replay_successful, true);
                    replaySaved = true;
                }
                return true;

            case ITEM_CONTINUE:
                if (fail) {
                    return false;
                }
                playSnd = ResourceManager.getInstance().getSound("menuback");
                if (playSnd != null) {
                    playSnd.play();
                }
                game.resume();
                return true;

            case ITEM_BACK:
                GlobalManager.getInstance().getScoring().setReplayID(-1);
                playSnd = ResourceManager.getInstance().getSound("menuback");
                if (playSnd != null) {
                    playSnd.play();
                }
                game.quit();
                return true;

            case ITEM_RETRY:
                ResourceManager.getInstance().getSound("failsound").stop();
                playSnd = ResourceManager.getInstance().getSound("menuhit");
                if (playSnd != null) {
                    playSnd.play();
                }
                game.restartGame();
                return true;
        }
        return false;
    }
}
