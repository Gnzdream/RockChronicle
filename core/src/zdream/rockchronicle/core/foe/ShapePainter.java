package zdream.rockchronicle.core.foe;

import static zdream.rockchronicle.core.foe.Box.p2block;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * debuger 用的 Painter
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-09 (create)
 */
public class ShapePainter implements IFoePainter {
	
	private final Box box;
	protected ShapeRenderer renderer;

	public ShapePainter(Box box) {
		this.box = box;
		renderer = new ShapeRenderer();
	}

	@Override
	public void draw(SpriteBatch batch, OrthographicCamera camera) {
		renderer.setProjectionMatrix(camera.combined);
		float dx = -camera.position.x + camera.viewportWidth / 2.0f;
		float dy = -camera.position.y + camera.viewportHeight / 2.0f;
		renderer.getProjectionMatrix().translate(dx, dy, 0);
		
		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.WHITE);
		
		if (box != null) {
			box.flush();
			
			float xstart = p2block(box.posX);
			float xend = xstart + p2block(box.posWidth);
			float ystart = p2block(box.posY);
			float yend = ystart + p2block(box.posHeight);
			
			renderer.line(xstart, ystart, xend, ystart);
			renderer.line(xend, ystart, xend, yend);
			renderer.line(xend, yend, xstart, yend);
			renderer.line(xstart, yend, xstart, ystart);
		}
		
		System.out.println(box);
		renderer.end();
	}

	@Override
	public int zIndex() {
		return 0;
	}

}
