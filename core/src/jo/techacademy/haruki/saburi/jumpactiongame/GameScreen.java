package jo.techacademy.haruki.saburi.jumpactiongame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tgaiacontentsdev on 2016/12/07.
 */

public class GameScreen extends ScreenAdapter {
    static final float CAMERA_WIDTH = 10;
    static final float CAMERA_HEIGHT = 15;
    static final float WORLD_WIDTH = 10;
    static final float WORLD_HEIGHT = 15 * 20;
    static final float GUI_WIDTH = 320;
    static final float GUI_HEIGHT = 480;

    static final int GAME_STATE_READY = 0;
    static final int GAME_STATE_PLAYING = 1;
    static final int GAME_STATE_GAMEOVER = 2;
    static final int GAME_STATE_CLEARED = 3;

    static final float GRAVITY = -12;

    private JumpActionGame mGame;

    Sprite mBg;
    OrthographicCamera mCamera;
    OrthographicCamera mGuiCamera;

    FitViewport mViewPort;
    FitViewport mGuiViewPort;

    Random mRandom;
    List<Step> mSteps;
    List<Star> mStars;
    ArrayList<Enemy> mEnemies;
    Ufo mUfo;
    Player mPlayer;

    Sound sound_hit = Gdx.audio.newSound(Gdx.files.internal("hit.mp3"));
    Sound sound_cleared = Gdx.audio.newSound(Gdx.files.internal("cleared.mp3"));
    Sound sound_gameover = Gdx.audio.newSound(Gdx.files.internal("gameover.mp3"));

    float vanishLevel = 0.5f;
    float mHeightSoFar;
    int mGameState;
    Vector3 mTouchPoint;
    BitmapFont mFont;
    int mScore;
    int mHighScore;
    Preferences mPrefs;

    public GameScreen(JumpActionGame game){
        mGame = game;

        Texture bgTexture = new Texture("back.png");
        mBg = new Sprite(new TextureRegion(bgTexture, 0, 0, 540, 810));
        mBg.setSize(CAMERA_WIDTH,CAMERA_HEIGHT);
        mBg.setPosition(0, 0);

        mCamera = new OrthographicCamera();
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);
        mViewPort = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, mCamera);

        mGuiCamera = new OrthographicCamera();
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT);
        mGuiViewPort = new FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera);

        mRandom = new Random();
        mSteps = new ArrayList<Step>();
        mStars = new ArrayList<Star>();
        mEnemies = new ArrayList<Enemy>();
        mGameState = GAME_STATE_READY;
        mTouchPoint = new Vector3();
        mFont = new BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false);
        mScore = 0;
        mPrefs = Gdx.app.getPreferences("jo.techacademy.haruki.saburi.jumpactiongame");
        mHighScore = mPrefs.getInteger("HIGHSCORE", 0);

        createStage();
    }

    @Override
    public void render (float delta){
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (mPlayer.getY() > mCamera.position.y){
            mCamera.position.y = mPlayer.getY();
        }

        mCamera.update();
        mGame.batch.setProjectionMatrix(mCamera.combined);

        mGame.batch.begin();
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2, mCamera.position.y - CAMERA_HEIGHT / 2);
        mBg.draw(mGame.batch);

        for (int i = 0; i < mSteps.size(); i++){
            mSteps.get(i).draw(mGame.batch);
        }

        for (int i = 0; i < mStars.size(); i++){
            mStars.get(i).draw(mGame.batch);
        }

        for (int i = 0; i < mEnemies.size(); i++){
            mEnemies.get(i).draw(mGame.batch);
        }

        mUfo.draw(mGame.batch);
        mPlayer.draw(mGame.batch);

        mGame.batch.end();


        mGuiCamera.update();

        mGame.batch.setProjectionMatrix(mGuiCamera.combined);
        mGame.batch.begin();
        mFont.draw(mGame.batch, "HighScore:" + mHighScore, 16, GUI_HEIGHT - 15);
        mFont.draw(mGame.batch, "Score:" + mScore, 16, GUI_HEIGHT - 35);

        if (mGameState == GAME_STATE_GAMEOVER){
            mFont.draw(mGame.batch, "GameOver",  0, GUI_HEIGHT / 2 - 40, GUI_WIDTH, Align.center, false);
        }
        if (mGameState == GAME_STATE_CLEARED){
            mFont.draw(mGame.batch, "GameCleared!!",  0, GUI_HEIGHT / 2 - 40, GUI_WIDTH, Align.center, false);
        }

        mGame.batch.end();
    }

    @Override
    public void resize(int width, int height){
        mViewPort.update(width, height);
        mGuiViewPort.update(width, height);
    }

    private void createStage(){
        Texture stepTexture = new Texture("step.png");
        Texture starTexture = new Texture("star.png");
        Texture enemyTexture = new Texture("enemy.png");
        Texture playerTexture = new Texture("uma.png");
        Texture ufoTexture = new Texture("ufo.png");

        float y = 0;
        float maxJumpHeight = Player.PLAYER_JUMP_VELOCITY * Player.PLAYER_JUMP_VELOCITY / (2 * - GRAVITY);

        while (y < WORLD_HEIGHT - 5){
            int type = mRandom.nextFloat() > 0.8f ? Step.STEP_TYPE_MOVING : Step.STEP_TYPE_STATIC;
            float x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH);
            //敵
            int eType = mRandom.nextFloat() > 0.6 ? Enemy.ENEMY_TYPE_MOVING : Enemy.ENEMY_TYPE_STATIC;
            float eX = mRandom.nextFloat() * (WORLD_WIDTH - Enemy.ENEMY_WIDTH);
            //

            Step step = new Step(type, stepTexture, 0, 0, 144, 36);
            step.setPosition(x, y);
            mSteps.add(step);

            if (mRandom.nextFloat() > 0.6f){
                Star star = new Star(starTexture, 0, 0, 72, 72);
                star.setPosition(step.getX() + mRandom.nextFloat(), step.getY() + Star.STAR_HEIGHT + mRandom.nextFloat() * 3);
                mStars.add(star);
            }

            //敵
            if (mRandom.nextFloat() > 0.4) {
                Enemy enemy = new Enemy(eType, enemyTexture, 0, 0, 81, 67);
                enemy.setPosition(eX, y + mRandom.nextFloat() * 50);
                mEnemies.add(enemy);
            }
            //

            y += (maxJumpHeight - 0.5f);
            y -= mRandom.nextFloat() * (maxJumpHeight / 3);
        }

        mPlayer = new Player(playerTexture, 0, 0, 72, 72);
        mPlayer.setPosition(WORLD_WIDTH / 2 - mPlayer.getWidth() / 2, Step.STEP_HEIGHT);

        mUfo = new Ufo(ufoTexture, 0, 0, 120, 74);
        mUfo.setPosition(WORLD_WIDTH / 2 - Ufo.UFO_WIDTH / 2, y);
    }

    private void update(float delta){
        switch (mGameState){
            case GAME_STATE_READY:
                updateReady();
                break;
            case GAME_STATE_PLAYING:
                updatePlaying(delta);
                break;
            case GAME_STATE_GAMEOVER:
            case GAME_STATE_CLEARED:
                updateGameOver();
                break;

        }
    }

    private void updateReady(){
        if (Gdx.input.justTouched()){
            mGameState = GAME_STATE_PLAYING;
        }
    }

    private void updatePlaying(float delta){
        float fast = 0.0f;
        if (Gdx.input.isTouched()){
            mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            Rectangle left = new Rectangle(0, 0, GUI_WIDTH / 2, GUI_HEIGHT);
            Rectangle right = new Rectangle(GUI_WIDTH / 2, 0, GUI_WIDTH / 2, GUI_HEIGHT);
            if (left.contains(mTouchPoint.x, mTouchPoint.y)){
                fast = 5.0f;
            }
            if (right.contains(mTouchPoint.x, mTouchPoint.y)){
                fast = -5.0f;
            }
        }

        for (int i = 0; i < mSteps.size(); i++){
            mSteps.get(i).update(delta);
        }
        for (int i = 0; i < mEnemies.size(); i++){
            mEnemies.get(i).update(delta);
        }

        if (mPlayer.getY() <= 0.5f){
            mPlayer.hitStep();
        }
        mPlayer.update(delta, fast);
        mHeightSoFar = Math.max(mPlayer.getY(), mHeightSoFar);

        checkCollision();
        checkGameOver();
    }

    private void updateGameOver(){
        if (Gdx.input.justTouched()){
            mGame.setScreen(new ResultScreen(mGame, mScore));
        }
    }

    private void checkCollision(){
        if (mPlayer.getBoundingRectangle().overlaps(mUfo.getBoundingRectangle())){
            sound_cleared.play(1.0f);
            mGameState = GAME_STATE_CLEARED;
            return;
        }

        for (int i = 0; i < mStars.size(); i++){
            Star star = mStars.get(i);

            if (star.mState == Star.STAR_NONE){
                continue;
            }
            if (mPlayer.getBoundingRectangle().overlaps(star.getBoundingRectangle())){
                star.get();
                mScore++;
                if (mScore > mHighScore){
                    mHighScore = mScore;
                    mPrefs.putInteger("HIGHSCORE", mHighScore);
                    mPrefs.flush();
                }
                break;
            }
        }

        for (int i = 0; i < mEnemies.size(); i++){
            Enemy enemy = mEnemies.get(i);
            if (mPlayer.getBoundingRectangle().overlaps(enemy.getBoundingRectangle())){
                sound_hit.play(1.0f);

                mGameState = GAME_STATE_GAMEOVER;
                break;
            }
        }

        if (mPlayer.velocity.y > 0){
            return;
        }

        for (int i = 0; i < mSteps.size(); i++){
            Step step = mSteps.get(i);

            if (step.mState == Step.STEP_STATE_VANISH){
                continue;
            }

            if (mPlayer.getY() > step.getY()){
                if (mPlayer.getBoundingRectangle().overlaps(step.getBoundingRectangle())){
                    mPlayer.hitStep();
                    if (mRandom.nextFloat() > vanishLevel){
                        step.vanish();
                    }
                    break;
                }
            }
        }
    }

    private void checkGameOver(){
        if (mHeightSoFar - CAMERA_HEIGHT / 2 > mPlayer.getY()){
            sound_gameover.play(1.0f);
            mGameState = GAME_STATE_GAMEOVER;
        }
    }
}
