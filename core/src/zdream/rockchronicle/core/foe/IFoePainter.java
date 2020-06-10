package zdream.rockchronicle.core.foe;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface IFoePainter {
	
	void draw(SpriteBatch batch, OrthographicCamera camera);
	
	int zIndex();

}
