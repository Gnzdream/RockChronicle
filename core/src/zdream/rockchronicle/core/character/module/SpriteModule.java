package zdream.rockchronicle.core.character.module;

import java.io.File;
import java.util.ArrayList;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.Config;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.motion.IBoxHolder;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.textures.TextureSheet;
import zdream.rockchronicle.textures.TextureSheetEntry;

public abstract class SpriteModule extends AbstractModule {
	
	public static final String NAME = "Sprite";

	public SpriteModule(CharacterEntry ch) {
		super(ch);
		texturePaths = new ArrayList<>();
		
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		motion = ((MotionModule) parent.getModule(MotionModule.NAME));
		
		if (value.has("textures")) {
			// 场一般没有 textures
			initTexturePaths(file, value.get("textures"));
		}
	}

	@Override
	public String name() {
		return NAME;
	}
	
	Sprite sprite;
	TextureSheet textures;
	/**
	 * 现在所采用的纹理的名称
	 */
	String curTexName;
	
	MotionModule motion;
	
	File baseFile;
	ArrayList<String> texturePaths;
	
	private void initTexturePaths(FileHandle file, JsonValue array) {
		baseFile = file.file().getParentFile();
		
		int len = array.size;
		for (int i = 0; i < len; i++) {
			String path = array.getString(i);
			texturePaths.add(path);
		}
		
		// 后面是读取文件. 这部分可能需要用多线程方式实现, 现在先单线程
		loadTexture();
	}
	
	public void loadTexture() {
		// TODO 这里假设 texturePaths 只有一个元素
		FileHandle texFile = new FileHandle(baseFile.getAbsolutePath() + File.separator + texturePaths.get(0));
		JsonReader r = new JsonReader();
		JsonValue json = r.parse(texFile);
		
		textures = new TextureSheet(texFile, json);
		System.out.println(textures);
		
		// sprite 找到默认的 textures ("normal")
		curTexName = "normal";
		sprite = new Sprite(textures.getTextureEntry(curTexName).region);
		// 没有设定位置, 所以默认是 0, 0
	}
	
	/**
	 * 返回现在正在使用的纹理.
	 * @return
	 */
	public TextureSheetEntry getCurrentTexture() {
		return textures.getTextureEntry(curTexName);
	}
	
	public Sprite getSprite() {
		return sprite;
	}
	
	public void draw(SpriteBatch batch, OrthographicCamera camera) {
		TextureSheetEntry entry = getCurrentTexture();
		float x, // 单位: 像素 -> 格子
				y = getY() + entry.offsety / (float) Config.INSTANCE.blockHeight; // 单位: 像素 -> 格子
		
		float fw = sprite.getWidth() / Config.INSTANCE.blockWidth,
				fh = sprite.getHeight() / Config.INSTANCE.blockHeight;
		
		if (motion.orientation) {
			sprite.setFlip(false, false);
			x = getX() + entry.offsetx / (float) Config.INSTANCE.blockWidth;
		} else {
			sprite.setFlip(true, false);
			x = getX() - entry.offsetx / (float) Config.INSTANCE.blockWidth - fw;
		}
		
		
		batch.begin();
//		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_DST_ALPHA);
//		batch.setColor(1, 1, 1, 1);
		batch.draw(sprite, x, y, fw, fh);
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.end();
	}
	
	private static final String[] PATH_ANCHOR_X = {"box", "anchor", "x"};
	private static final String[] PATH_ANCHOR_Y = {"box", "anchor", "y"};
	
	/**
	 * 获取 x 坐标.
	 */
	public float getX() {
		Box box = getSingleBox();
		if (box != null) {
			return box.anchor.x;
		}
		return parent.getFloat(PATH_ANCHOR_X, 0);
	}
	
	/**
	 * 获取 y 坐标.
	 */
	public float getY() {
		Box box = getSingleBox();
		if (box != null) {
			return box.anchor.y;
		}
		return parent.getFloat(PATH_ANCHOR_Y, 0);
	}
	
	/**
	 * 如果本角色只有一个碰撞盒子, 则调用该方法来获取其碰撞盒子
	 * @return
	 */
	protected Box getSingleBox() {
		MotionModule mm = parent.getMotion();
		if (mm instanceof IBoxHolder) {
			return ((IBoxHolder) mm).getBox();
		}
		return null;
	}
	
	@Override
	public int priority() {
		return -0x100;
	}

}
