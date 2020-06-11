package zdream.rockchronicle.core.foe;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface IFoePainter {
	
	void draw(SpriteBatch batch, OrthographicCamera camera);
	
	/**
	 * <p>越高的在前面.
	 * <p>洛克人: 1000
	 * <p>一般的怪: 501 - 700
	 * </p>
	 * @return
	 */
	int zIndex();

}
