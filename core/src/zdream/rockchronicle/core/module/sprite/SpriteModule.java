package zdream.rockchronicle.core.module.sprite;

import java.io.File;
import java.util.ArrayList;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.Config;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.core.module.MotionModule;
import zdream.rockchronicle.core.module.motion.IBoxHolder;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.textures.TextureSequence;
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
	MotionModule motion;
	
	File baseFile;
	ArrayList<String> texturePaths;
	
	/*
	 * 下面是计时相关的参数
	 */
	protected String state = "stop";
	protected int steps;
	
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
		textures = TextureSheet.createSheet(texFile);
		
		// 没有设定位置, 所以默认是 0, 0
		sprite = new Sprite();
	}
	
	/**
	 * 返回现在正在使用的纹理.
	 * @return
	 */
	public TextureSheetEntry getCurrentTexture() {
		TextureSequence seq;
		if (state == null || (seq = textures.sequences.get(state)) == null) {
			return null;
		}
		
		if (seq.step <= 0) {
			return textures.entrys.get(seq.seqs[0]);
		} else {
			int index = steps / seq.step;
			return textures.entrys.get(seq.seqs[index % seq.seqs.length]);
		}
	}
	
	public Sprite getSprite() {
		return sprite;
	}
	
	public void draw(SpriteBatch batch, OrthographicCamera camera) {
		TextureSheetEntry entry = getCurrentTexture();
		if (entry == null) {
			return;
		}
		sprite.setRegion(entry.region);
		
		float x, // 单位: 像素 -> 格子
				y = getY() + entry.offsety / (float) Config.INSTANCE.blockHeight; // 单位: 像素 -> 格子
		
		float fw = (float) entry.width / Config.INSTANCE.blockWidth,
				fh = (float) entry.height / Config.INSTANCE.blockHeight;
		
		if (motion.orientation) {
			sprite.setFlip(false, false);
			x = getX() + entry.offsetx / (float) Config.INSTANCE.blockWidth;
		} else {
			sprite.setFlip(true, false);
			x = getX() - entry.offsetx / (float) Config.INSTANCE.blockWidth - fw;
		}
		
		int immune = parent.getInt(new String[] {"state", "immune"}, 0);
		
		// 绘画
		batch.begin();
		if (immune > 0) {
			if (immune % 12 < 6) {
				batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
				batch.setColor(1, 1, 1, 0.6f);
				batch.draw(sprite, x, y, fw, fh);
				batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
				batch.setColor(1, 1, 1, 1);
			} else {
				batch.setColor(1, 1, 1, 0.6f);
				batch.draw(sprite, x, y, fw, fh);
				batch.setColor(1, 1, 1, 1);
			}
		} else {
			batch.draw(sprite, x, y, fw, fh);
		}
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
