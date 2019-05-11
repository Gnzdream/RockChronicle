package zdream.rockchronicle.core.module.sprite;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.body.Box;

/**
 * <p>测试使用的绘画模块
 * <p>只画出图形
 * </p>
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-09 (create)
 */
public class ShapeSpriteModule extends SpriteModule {
	
	protected ShapeRenderer renderer;
	private Color color;

	public ShapeSpriteModule(CharacterEntry ch) {
		super(ch);
		renderer = new ShapeRenderer();
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue osprite = value.get("sprite");
		if (osprite != null) {
			String colorStr = osprite.getString("stroke", "#FFFFFFFF");
			color = Color.valueOf(colorStr);
		} else {
			color = Color.WHITE;
		}
	}

	public void draw(SpriteBatch batch, OrthographicCamera camera) {
		renderer.setProjectionMatrix(camera.combined);
		
		renderer.begin(ShapeType.Line);
		renderer.setColor(color);
		
		Box box = getSingleBox();
		if (box != null) {
			Rectangle pos = box.getPosition();
			float xstart = pos.x;
			float xend = xstart + pos.width;
			float ystart = pos.y;
			float yend = ystart + pos.height;
			
			renderer.line(xstart, ystart, xend, ystart);
			renderer.line(xend, ystart, xend, yend);
			renderer.line(xend, yend, xstart, yend);
			renderer.line(xstart, yend, xstart, ystart);
		}
		
		renderer.end();
	}

}
