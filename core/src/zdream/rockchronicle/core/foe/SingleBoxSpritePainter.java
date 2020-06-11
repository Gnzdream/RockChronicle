package zdream.rockchronicle.core.foe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import zdream.rockchronicle.core.Config;
import zdream.rockchronicle.core.textures.TextureSelect;
import zdream.rockchronicle.core.textures.TextureSheet;
import zdream.rockchronicle.core.textures.TextureSheetEntry;
import zdream.rockchronicle.core.textures.Textures;

public abstract class SingleBoxSpritePainter implements IFoePainter {

	public SingleBoxSpritePainter(String[] paths) {
		
		initTexturePaths(paths);
	}

	Sprite sprite;
	TextureSheet textures;
	
	String[] texturePaths;
	
	protected TextureSelect select = new TextureSelect();
	
	/**
	 * @param paths
	 *  比如 ["res/characters/megaman/sprites/megaman7sheet.json"]
	 */
	private void initTexturePaths(String[] paths) {
		texturePaths = paths;
		
		// 后面是读取文件. 这部分可能需要用多线程方式实现, 现在先单线程
		loadTexture();
		
		select.setSheet(textures);
	}
	
	public void loadTexture() {
		// TODO 这里假设 texturePaths 只有一个元素
		FileHandle texFile = Gdx.files.local(texturePaths[0]);
		textures = TextureSheet.createSheet(texFile);
		
		// 没有设定位置, 所以默认是 0, 0
		sprite = new Sprite();
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
	
	public int getImmune() {
		return 0;
	}
	
	public Sprite getSprite() {
		return sprite;
	}
	
	/**
	 * 获得纹理的朝向, 是向左还是向右
	 * @return
	 *   是否向右
	 */
	public abstract boolean getOrientation();
	
	public void draw(SpriteBatch batch, OrthographicCamera camera) {
		TextureSheetEntry entry = getCurrentTexture();
		if (entry == null) {
			return;
		}
		sprite.setRegion(entry.region);
		
		int widthInPixel = Config.INSTANCE.blockWidth;
		int heightInPixel = Config.INSTANCE.blockHeight;
		
		float x, // 单位: 像素 -> 格子
				y = getBy() + entry.offsety / (float) widthInPixel; // 单位: 像素 -> 格子
		
		float fw = (float) entry.width / widthInPixel,
				fh = (float) entry.height / heightInPixel;
		
		boolean orientation = getOrientation();
		if (orientation) {
			sprite.setFlip(false, false);
			x = getBx() + entry.offsetx / (float) widthInPixel;
		} else {
			sprite.setFlip(true, false);
			x = getBx() - entry.offsetx / (float) widthInPixel - fw;
		}
		
		int immune = getImmune();
		
		// 绘画
		batch.begin();
		if (immune > 0) {
			if (immune % 12 < 6) {
				batch.setBlendFunction(GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
				batch.draw(sprite, x, y, fw, fh);
				batch.setBlendFunction(GL20.GL_ONE_MINUS_DST_ALPHA, GL20.GL_DST_ALPHA);
				batch.setColor(1, 1, 1, 0.8f);
				batch.draw(Textures.white, x, y, fw, fh);
				batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
				batch.setColor(1, 1, 1, 0.4f);
				batch.draw(sprite, x, y, fw, fh);
				batch.setColor(1, 1, 1, 1);
			} else {
				batch.setColor(1, 1, 1, 0.8f);
				batch.draw(sprite, x, y, fw, fh);
				batch.setColor(1, 1, 1, 1);
			}
		} else {
			batch.draw(sprite, x, y, fw, fh);
		}
		batch.end();
	}
	
	/**
	 * 获取 x 坐标. 单位: 块
	 */
	public abstract float getBx();
	
	/**
	 * 获取 y 坐标. 单位: 块
	 */
	public abstract float getBy();

	public void setState(String stateName) {
		select.setState(stateName);
	}
	
	public void tick() {
		select.tick(1);
	}
	
}
