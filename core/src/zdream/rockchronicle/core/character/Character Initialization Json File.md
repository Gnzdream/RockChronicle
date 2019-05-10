
#	角色初始化的 JSON 数据格式说明
Character Initialization JSON Data Format Description

@since v0.0.1

@date 2019-05-05 (create)

---

相关类

*	zdream.rockchronicle.core.character.CharacterBuilder
	<br/>用于从文件系统中扫描 JSON 文件，并预加载。
	<br/>当出现了产生新的角色的需求时，将取出相关数据，并引导角色实例的初始化工作。
	这部分工作包含重建``角色初始化数据``，并配置添加模块。
	
*	zdream.rockchronicle.core.character.CharacterEntry
	<br/>角色父类。相关的敌人、主控角色、子弹、陷阱、场等均由该类继承。
	<br/>需要利用``角色初始化数据``进行初始化。

---

角色初始化数据

*	格式: Json Object

下面描述数据格式

*	name
	<br/>string, 必需
	<br/>角色名称

*	class
	<br/>string, 必需
	<br/>全类名
	
*	modules
	<br/>object{key : string}
	<br/>模块声明
	<br/>key 为模块属性（比如 "motion" 为动作模块，"sprite" 为绘制模块等），value 为模板类名称。
	每个模板类都有一个名称，而没一类属性里的所有模板类的名称都是唯一的，可作为唯一标识符

*	box
	<br/>object
	<br/>该角色的碰撞盒子的描述
	
*	box.inTerrain
	<br/>boolean, 默认 true
	<br/>描述该角色是否受地形的约束。一般能够自由穿墙的角色将不受地形的约束，设置为 false。
	<br/>另外注意，不受地形约束的角色可能受``场``的约束。
	
*	box.anchor
	<br/>object
	<br/>描述该角色的锚点位置。一个角色的位置可以看做一系列点的运动，而锚点是这些点中比较突出、容易计算的点。
	比如，洛克人的锚点在其底边的中点上。锚点之所以设置在这里时，当其从直立状态转变为滑铲状态时，锚点基本没有变化，
	计算起来比较容易。
	<br/>由于大多数情况配置文件不清楚角色的固定位置，而这些位置要在游戏运行中才能确定，
	因此该值通常由后期补充的，文件中将不会注明。
	
*	box.anchor.x
	<br/>double
	<br/>角色锚点横坐标
	
*	box.anchor.y
	<br/>double
	<br/>角色锚点纵坐标
	
*	box.rect
	<br/>object
	<br/>角色的碰撞盒子相对于锚点的位置数据。
	<br/>box.rect.def 存在或 box.rect.x、box.rect.y、box.rect.width、box.rect.height 同时存在即可。
	如果 box.rect.def 存在且 box.rect.x、box.rect.y、box.rect.width、box.rect.height 四项中最少一项存在，
	则后者中四项的存在项以后者为准，后者中四项的不存在项以 box.rect.def 为准。

*	box.rect.x
	<br/>double
	<br/>角色的碰撞盒子左下角点相对于锚点的横坐标。

*	box.rect.y
	<br/>double
	<br/>角色的碰撞盒子左下角点相对于锚点的纵坐标。

*	box.rect.width
	<br/>double
	<br/>角色的碰撞盒子的宽度。

*	box.rect.height
	<br/>double
	<br/>角色的碰撞盒子的高度。

*	box.rect.def
	<br/>string
	<br/>角色的碰撞盒子的参数定位符。
	<br/>适用于存在不同形状的角色。洛克人拥有直立和滑铲（以及更多）状态，而每种状态的形状并不同，
	因此可以在 motion.state 中定义每个状态的碰撞盒子，
	并使用 box.rect.def 等于 motion.state 中的任意一个状态的名称（motion.state[?].name）。
	
*	box.velocity
	<br/>object
	<br/>角色的初始速度数据。

*	box.velocity.x
	<br/>double
	<br/>角色的初始速度横坐标分量。默认为 0

*	box.velocity.y
	<br/>double
	<br/>角色的初始速度纵坐标分量。默认为 0

*	motion
	<br/>object
	<br/>行动模块

*	motion.orientation
	<br/>boolean
	<br/>水平方向的朝向。右为 true，左为 false

*	motion.state
	<br/>array{object}
	<br/>角色的各个形态组成的列表
	
*	motion.state[?].name
	<br/>string{object}
	<br/>角色状态名称

*	motion.state[?].rect
	<br/>object | undefined
	<br/>某个形态的碰撞盒子相对于其锚点的位置及大小。
	<br/>如果存在一个实体有多个碰撞盒子，则该值为 undefined，另外 motion.state.[?].rects 则存在。

*	motion.state[?].rect.x
	<br/>double
	<br/>某个形态下，角色的碰撞盒子左下角点相对于锚点的横坐标。

*	motion.state[?].rect.y
	<br/>double
	<br/>某个形态下，角色的碰撞盒子左下角点相对于锚点的纵坐标。

*	motion.state[?].rect.width
	<br/>double
	<br/>某个形态下，角色的碰撞盒子的宽度。

*	motion.state[?].rect.height
	<br/>double
	<br/>某个形态下，角色的碰撞盒子的高度。

*	motion.state[?].rects
	<br/>array{object} | undefined
	<br/>某个形态的碰撞盒子相对于其锚点的位置及大小组成的列表。
	<br/>这里认为该状态下的碰撞盒子存在多个，每个碰撞盒子的定义在 motion.state[?].rects[index] 中。
	另外，如果该项存在，则 motion.state[?].rect 一定为 undefined
	<br/>相反，motion.state[?].rects 为 undefined 说明 
	otion.state[?].rect 存在，碰撞盒子只有一个。
	
*	jump
	<br/>object
	<br/>跳跃模块
	
*	jump.param
	<br/>object
	<br/>跳跃模块原始参数
	
*	jump.param.impulse
	<br/>double
	<br/>跳跃的向上的冲量，即跳跃的第一帧，每步向上的格子数。单位：格子 / 秒
	
*	jump.param.decay
	<br/>double
	<br/>在跳跃时，向上的速度会逐渐降下来。(负数)
	<br/>每过一步的时间，每步向上的格子数。单位：格子 / (秒^2)
	
*	jump.param.maxDropVelocity
	<br/>double
	<br/>最大的掉落速度（负数）。单位：格子 / 秒
	
*	camp
	<br/>object
	<br/>阵营模块
	
*	camp.camp
	<br/>integer
	<br/>角色所在的阵营
	
*	camp.attackAccepted
	<br/>object {integer : boolean}
	<br/>角色作为攻击方，攻击其它阵营是否被接受。如果该 object 没有写明的，默认为 true
	
*	camp.defenseAccepted
	<br/>object {integer : boolean}
	<br/>角色作为防御方，是否接受其它阵营的攻击。如果该 object 没有写明的，默认为 true
	
*	collision
	<br/>object
	<br/>碰撞模块
	
*	collision.damage
	<br/>double | undefined
	<br/>碰撞伤害量，非负数，默认为 0。
	<br/>如果为正数则说明碰撞对其它能够攻击且生效的角色产生伤害，负数则不产生伤害。
	比如一般的加生命值的道具的碰撞不会产生伤害（但也不设置为负数）
	
*	collision.level
	<br/>integer
	<br/>碰撞等级，默认为 9。
	<br/>如果角色自己与其它角色发生碰撞或重合时，如果能够产生伤害或施加效果，
	则下面就需要判断碰撞等级了。攻击方有碰撞等级，而防御方有屏蔽等级。
	除去特殊情况，当攻击方的碰撞等级大于等于防御方的屏蔽等级，碰撞伤害或效果的施加将生效。
	<br/>下面罗列了一些较为常用的等级数：
	*	洛克人（初始状态）：屏蔽等级 0，无碰撞模块（无碰撞等级）
	*	大多数子弹：无生命模块（无屏蔽等级），碰撞等级 6
	*	含碰撞伤害的小怪，以及大多数人形 BOSS：屏蔽等级 0，碰撞等级 1
	*	大多数道具、陷阱、场：无生命模块（无屏蔽等级），碰撞等级 9
	
*	collision.properties
	<br/>array{object} | undefined
	说明碰撞的属性。一般角色属于子弹类的话，这里会有数据。数据的格式与内容将在以后补充。TODO
	
*	collision.execute
	<br/>string
	<br/>默认 "repeat"。
	*	repeat：碰撞判断每帧进行判断，每次可同时对多个碰撞的目标产生伤害或效果
	*	once：碰撞判断到第一个目标，产生伤害或效果，之后并不再进行判断，并在该帧结束后删除该角色
	
*	sprite
	<br/>object
	<br/>绘画模块
	
*	sprite.stroke
	<br/>string
	<br/>类似于 "#FF5620E7" 的字符串，指出绘画时碰撞盒子外框的颜色。只在绘画模块为“shape”时启用。
	
*	health
	<br/>object
	<br/>健康模块
	
*	health.hp
	<br/>double
	<br/>显示血量。在系统中计算的血量实际值为显示血量 * 256 向下取整。
	<br/>BaseHealthModule 模块中使用。
	
*	health.hpMax
	<br/>double
	<br/>显示血量最大值。在系统中计算的血量实际值为显示血量最大值 * 256 向下取整。
	<br/>BaseHealthModule 模块中使用。
	
*	health.ref
	<br/>string
	<br/>生命值取值参数定位符。如果是特殊角色，该值将引导模块重定向角色生命值的位置。
	<br/>ReferenceHealthModule 模块中使用。
	
*	state
	<br/>object
	<br/>其它状态记录模块
	
*	state.param
	<br/>object
	<br/>其它状态记录模块原始参数
	
*	state.param.stiffness
	<br/>double
	<br/>硬直状态产生时的持续时间，单位：秒
	
*	state.param.immune
	<br/>double
	<br/>无敌状态产生时的持续时间，单位：秒
	
