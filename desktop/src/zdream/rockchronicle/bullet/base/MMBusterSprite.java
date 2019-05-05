package zdream.rockchronicle.bullet.base;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

import zdream.rockchronicle.core.character.SpriteModule;

public class MMBusterSprite extends SpriteModule {
	
	final MMBuster parent;
	protected ShapeRenderer renderer;

	public MMBusterSprite(MMBuster mm) {
		super(mm);
		this.parent = mm;
		
		renderer = new ShapeRenderer();
	}

	@Override
	public float getX() {
		return parent.motion.box.anchor.x;
	}

	@Override
	public float getY() {
		return parent.motion.box.anchor.y;
	}
	
	public void draw(SpriteBatch batch, OrthographicCamera camera) {
		renderer.setProjectionMatrix(camera.combined);
		
		renderer.begin(ShapeType.Line);
		renderer.setColor(1, 1, 1, 1);
		
		Rectangle box = parent.motion.box.getPosition();
		float xstart = box.x;
		float xend = xstart + box.width;
		float ystart = box.y;
		float yend = ystart + box.height;
		
		renderer.line(xstart, ystart, xend, ystart);
		renderer.line(xend, ystart, xend, yend);
		renderer.line(xend, yend, xstart, yend);
		renderer.line(xstart, yend, xstart, ystart);
		
		renderer.end();
	}

}
