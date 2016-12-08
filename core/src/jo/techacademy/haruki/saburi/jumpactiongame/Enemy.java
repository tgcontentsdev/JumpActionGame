package jo.techacademy.haruki.saburi.jumpactiongame;

import com.badlogic.gdx.graphics.Texture;

/**
 * Created by tgaiacontentsdev on 2016/12/08.
 */

public class Enemy extends GameObject{
    public static final float ENEMY_WIDTH = 1.5f;
    public static final float ENEMY_HEIGHT = 1.5f;

    public static final int ENEMY_TYPE_STATIC = 0;
    public static final int ENEMY_TYPE_MOVING = 1;

    public static final float ENEMY_VELOCITY = 2.5f;

    int mType;

    public Enemy(int type, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight){
        super(texture, srcX, srcY, srcWidth, srcHeight);
        setSize(ENEMY_WIDTH, ENEMY_HEIGHT);
        mType = type;
        if (mType == ENEMY_TYPE_MOVING){
            velocity.x = ENEMY_VELOCITY;
        }
    }

    public void update(float deltaTime){
        if (mType == ENEMY_TYPE_MOVING){
            setX(getX() + velocity.x * deltaTime);

            if (getX() < ENEMY_WIDTH / 2) {
                velocity.x = -velocity.x;
                setX(ENEMY_WIDTH / 2);
            }
            if (getX() > GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2) {
                velocity.x = -velocity.x;
                setX(GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2);
            }

        }
    }

}
