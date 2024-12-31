package wh.files;

import arc.files.*;
import arc.util.*;

/** Use for jar internal navigation. */
public class InternalFileTree {
    public Class<?> anchorClass;

    public ZipFi root;

    /** @param owner navigation anchor */
    public InternalFileTree(Class<?> owner) {
        anchorClass = owner;

        String classPath = owner.getResource("").getFile().replaceAll("%20", " ");
        classPath = classPath.substring(classPath.indexOf(":") + 2);
        String jarPath = (OS.isLinux ? "/" : "") + classPath.substring(0, classPath.indexOf("!"));

        root = new ZipFi(new Fi(jarPath));
    }

    public Fi child(String name) {
        Fi out = root;
        for (String s : name.split("/")) {
            if (!"".equals(s))
                out = out.child(s);
        }
        return out;
    }
}
