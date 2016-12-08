package jo.techacademy.haruki.saburi.jumpactiongame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by tgaiacontentsdev on 2016/12/07.
 */

public class GameObject extends Sprite {
    public final Vector2 velocity;

    public GameObject(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight){
        super(texture, srcX, srcY, srcWidth, srcHeight);

        velocity = new Vector2();
    }
}
