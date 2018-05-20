package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Created by kongxiangxin on 2017/8/2.
 */
public class JasmineIcons {
    public static final Icon Jasmine = load("/icons/jasmine.png");

    public JasmineIcons() {
    }

    private static Icon load(String path) {
        return IconLoader.getIcon(path, JasmineIcons.class);
    }
}
