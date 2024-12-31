exports.getClass = function (name) {
    return Packages.rhino.NativeJavaClass(Vars.mods.scripts.scope, Vars.mods.mainLoader().loadClass(name));
}