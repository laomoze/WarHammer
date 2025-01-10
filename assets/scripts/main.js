// <--- Java核心系统  勿删！--->
function whPack(name) {
	var p = Packages.rhino.NativeJavaPackage(name, Vars.mods.mainLoader());
	Packages.rhino.ScriptRuntime.setObjectProtoAndParent(p, Vars.mods.scripts.scope)
	return p
}

var wh = whPack('wh')

importPackage(wh)
importPackage(wh.content)
importPackage(wh.core)
importPackage(wh.content)
importPackage(wh.entities)
importPackage(wh.entities.abilities)
importPackage(wh.entities.bullet)
importPackage(wh.expand.block)
importPackage(wh.entities.effect)
importPackage(wh.gen)
importPackage(wh.graphics)
importPackage(wh.maps)
importPackage(wh.maps.planets)
importPackage(wh.math)
importPackage(wh.struct)
importPackage(wh.type)
importPackage(wh.type.unit)
importPackage(wh.ui)
importPackage(wh.ui.dialogs)
importPackage(wh.util)
importPackage(wh.world.blocks.defense)
importPackage(wh.world.blocks.defense.turrets)
importPackage(wh.world.blocks.distribution)
importPackage(wh.world.blocks.production)
importPackage(wh.world.blocks.storage)
importPackage(wh.world.consumers)

// <--- 加载java内容 --->

require('loadjava');

// <--- 加载js --->

require('whunittyps');
require('whblocks');