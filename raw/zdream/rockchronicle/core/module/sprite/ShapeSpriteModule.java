package zdream.rockchronicle.core.module.sprite;

import static zdream.rockchronicle.platform.body.Box.p2block;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.textures.TextureSheetEntry;

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
		super(ch, "shape");
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
		float dx = -camera.position.x + camera.viewportWidth / 2.0f;
		float dy = -camera.position.y + camera.viewportHeight / 2.0f;
		renderer.getProjectionMatrix().translate(dx, dy, 0);
		
		renderer.begin(ShapeType.Line);
		renderer.setColor(color);
		
		Box box = getSingleBox();
		if (box != null) {
			float xstart = p2block(box.posX);
			float xend = xstart + p2block(box.posWidth);
			float ystart = p2block(box.posY);
			float yend = ystart + p2block(box.posHeight);
			
			renderer.line(xstart, ystart, xend, ystart);
			renderer.line(xend, ystart, xend, yend);
			renderer.line(xend, yend, xstart, yend);
			renderer.line(xstart, yend, xstart, ystart);
		}
		
		renderer.end();
	}

	@Override
	public TextureSheetEntry getCurrentTexture() {
		// 该绘画模块不需要拿到纹理
		return null;
	}

}
