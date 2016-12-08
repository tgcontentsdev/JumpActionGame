package jo.techacademy.haruki.saburi.jumpactiongame;

import com.badlogic.gdx.graphics.Texture;

/**
 * Created by tgaiacontentsdev on 2016/12/07.
 */

public class Ufo extends GameObject {

    public static final float UFO_WIDTH = 2.0f;
    public static final float UFO_HEIGHT = 1.3f;

    public Ufo(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight){
        super(texture, srcX, srcY, srcWidth, srcHeight);
        setSize(UFO_WIDTH, UFO_HEIGHT);
    }
}
