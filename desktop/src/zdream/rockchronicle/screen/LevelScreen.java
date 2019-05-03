package zdream.rockchronicle.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import zdream.rockchronicle.character.megaman.Megaman;
import zdream.rockchronicle.core.Config;
import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.desktop.RockChronicleDesktop;
import zdream.rockchronicle.platform.body.OtherBodyParam;
import zdream.rockchronicle.platform.region.Region;
import zdream.rockchronicle.platform.region.Room;
import zdream.rockchronicle.platform.world.IPhysicsStep;
import zdream.rockchronicle.platform.world.LevelWorld;

public class LevelScreen implements Screen, IPhysicsStep {
	
	final RockChronicleDesktop game;
	
	/**
	 * 查看该世界的镜头, 一般长宽为 25 x 14
	 * 这个锁定的是人物位置.
	 */
	OrthographicCamera worldCamera;
	/**
	 * 查看整个屏幕的镜头, 用于放置字幕、头像、血槽等指示、辅助性物品.
	 * 一般呈现的物品浮于世界镜头之上
	 */
	OrthographicCamera symbolCamera;
	
	Region region;
	OrthogonalTiledMapRenderer mapRender;
	
	/*
	 * 缓存人物
	 */
	Megaman megaman;
	
	SpriteBatch batch = new SpriteBatch();
	
	public LevelScreen() {
		game = RockChronicleDesktop.INSTANCE;
		game.runtime.levelWorld = new LevelWorld();
		
		// tiled 地图
		region = game.regionBuilder.buildForTerrainOnly("res\\level\\level1\\cut.json");
		
		worldCamera = new OrthographicCamera();
		worldCamera.setToOrtho(false, game.width, game.height); // y 轴方向朝上
//		worldCamera.combined.scale(Config.INSTANCE.blockWidth, Config.INSTANCE.blockHeight, 1);
//		worldCamera.combined.scale(2, 2, 1);
		
		symbolCamera = new OrthographicCamera();
		symbolCamera.setToOrtho(false, game.widthInPixel, game.heightInPixel);
		
		megaman = new Megaman();
		megaman.load(Gdx.files.local("res\\megaman\\megaman.json"));
		game.runtime.player1 = megaman;
	}

	@Override
	public void show() {
		System.out.println(getClass().getName());
		GameRuntime runtime = game.runtime;
		
		/*
		 * 物理
		 */
		LevelWorld world = game.runtime.levelWorld;
		world.doCreate();
		world.setStepCallBack(this);
		world.doResume();
		
		// 设置 megaman 的初始位置, 到 room 所对应的 spawn 点
		// 人物设置必须晚于世界创建
		runtime.curRegion = region;
		runtime.room = region.spawnRoom;
		world.setCurrentRoom(runtime.curRegion.rooms[runtime.room]);
		
		megaman.setBlockPos(region.spawnx, region.spawny);
		megaman.createBody(world);
		
//		Room curRoom = region.rooms[runtime.room];
		
		// 显示部分
		// mapRender
		mapRender = new OrthogonalTiledMapRenderer(region.tmx, 1f / Config.INSTANCE.blockWidth);
		fixMapRender();
		
		batch.setProjectionMatrix(worldCamera.combined);
		
		// 设置控制端. 这里不一定是 megaman 要注意
		megaman.bindController(game.input.p1);
		
		
		// 测试部分: 向 world 添加方块
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.KinematicBody;
		bodyDef.position.set(16, 2.5f); // 锚点位置
		bodyDef.gravityScale = 0;
		bodyDef.fixedRotation = true; // 不旋转
		testBody = world.world.createBody(bodyDef);
		testBody.setUserData(OtherBodyParam.INSTANCE);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(1, 1);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 0f;
		fixtureDef.friction = 0f; // 摩擦因子
		fixtureDef.restitution = 0.6f;
		testFixture = testBody.createFixture(fixtureDef);
		testBody.setLinearVelocity(0, 30f);
		shape.dispose();
		Filter filter = new Filter();
		filter.categoryBits = 0x4;
		filter.maskBits = 0x1;
		testFixture.setFilterData(filter);
	}
	
	// 测试的
	Body testBody;
	Fixture testFixture;
	
	
	int count = 0;
	
	/**
	 * 在界面启动时, 和每帧
	 * 修正 MapRender 渲染的瓦片地图的位置
	 */
	private void fixMapRender() {
		Room curRoom = region.rooms[game.runtime.room];
		
		mapRender.setView(worldCamera);
		Rectangle viewBounds = mapRender.getViewBounds();
		
		viewBounds.setX(viewBounds.x + curRoom.offsetx);
		viewBounds.setY(viewBounds.y + curRoom.offsety);
		mapRender.getBatch().getProjectionMatrix()
		.translate (
			-curRoom.offsetx,
			-curRoom.offsety, 0);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.1f, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		float realDelta = Math.min(delta, 0.1f); // 该值不会大于 0.1f
		
		
		// 镜头控制
//		count++;
//		if (count % 2 == 0) {
//			worldCamera.translate(0.05f, 0);
//		}
//		worldCamera.update();
		
		// 物理
		LevelWorld world = game.runtime.levelWorld;
		world.doPhysicsStep(realDelta);
		
		// 渲染部分
		// symbol 层是不能被 render 的
		fixMapRender();
		mapRender.render();
		
		batch.setProjectionMatrix(worldCamera.combined);
		megaman.draw(batch);
		
		world.debugRenderer.render(world.world, worldCamera.combined);
	}

	@Override
	public void step(LevelWorld world, int index, boolean hasNext) {
		game.runtime.onWorldSteped(index, hasNext);
	}
	
	@Override
	public void onStepFinished(LevelWorld world, boolean isPause) {
		game.runtime.onStepFinished(isPause);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}
	
	public void willDispose() {
		// 删除控制端. 这里不一定是 megaman 要注意
		megaman.unbindController();
//		game.runtime.levelWorld.setStepCallBack(null);
		game.runtime.levelWorld = null;
	}

	@Override
	public void dispose() {
		// 仅处理资源回收. 请把逻辑相关的收尾工作放在 willDispose 方法中
	}

}
