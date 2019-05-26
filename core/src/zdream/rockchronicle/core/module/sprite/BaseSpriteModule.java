package zdream.rockchronicle.core.module.sprite;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.world.LevelWorld;
import zdream.rockchronicle.textures.TextureSelect;
import zdream.rockchronicle.textures.TextureSheetEntry;

/**
 * <p>默认的的绘画模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-13 (created)
 *   2019-05-16 (last modified)
 */
public class BaseSpriteModule extends SpriteModule {
	
	protected TextureSelect select = new TextureSelect();

	public BaseSpriteModule(CharacterEntry ch) {
		super(ch);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		select.setSheet(textures);
	}
	
	public void setState(String stateName) {
		select.setState(stateName);
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		selectTexture(world, index, hasNext);
	}
	
	protected void selectTexture(LevelWorld world, int index, boolean hasNext) {
		select.tick(1);
	}
	
	/**
	 * 返回现在正在使用的纹理.
	 * @return
	 */
	public TextureSheetEntry getCurrentTexture() {
		if (select.getSequence() == null) {
			return null;
		}
		
		return select.select();
	}

}
