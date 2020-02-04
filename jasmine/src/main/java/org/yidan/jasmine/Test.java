package org.yidan.jasmine;

import net.openhft.compiler.CompilerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;

public class Test {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static void main(String[]  args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String file = "E:\\work\\scratch\\jasmine\\demo\\src\\main\\java\\com\\abc\\Demo1.java";

        CompilerUtils.addClassPath("E:\\work\\scratch\\jasmine\\demo\\target\\classes");
        CompilerUtils.addClassPath("D:/build/welfare-1.0.0-20191226.023707-8.jar");

        URL url1 = new URL("file:E:/work/scratch/jasmine/demo/target/classes/");
        URL url2 = new URL("file:D:/build/welfare-1.0.0-20191226.023707-8.jar");
//        URL url2 = new URL("file:D:/build/welfare-1.0.0-20191226.023707-8.jar");

        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url1, url2}, Thread.currentThread().getContextClassLoader());

        Class demo1Class = CompilerUtils.CACHED_COMPILER.loadFromJava(urlClassLoader, "com.abc.Demo1", readText(file));

        Runnable runnable = (Runnable)demo1Class.newInstance();

        runnable.run();
    }

    private static String readText(@NotNull String resourceName) throws IOException {
        if (resourceName.startsWith("="))
            return resourceName.substring(1);
        StringWriter sw = new StringWriter();
        Reader isr = new InputStreamReader(getInputStream(resourceName), UTF_8);
        try {
            char[] chars = new char[8 * 1024];
            int len;
            while ((len = isr.read(chars)) > 0)
                sw.write(chars, 0, len);
        } finally {
            close(isr);
        }
        return sw.toString();
    }

    @NotNull
    private static InputStream getInputStream(@NotNull String filename) throws FileNotFoundException {
        if (filename.isEmpty()) throw new IllegalArgumentException("The file name cannot be empty.");
        if (filename.charAt(0) == '=') return new ByteArrayInputStream(encodeUTF8(filename.substring(1)));
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = contextClassLoader.getResourceAsStream(filename);
        if (is != null) return is;
        InputStream is2 = contextClassLoader.getResourceAsStream('/' + filename);
        if (is2 != null) return is2;
        return new FileInputStream(filename);
    }

    @NotNull
    private static byte[] encodeUTF8(@NotNull String text) {
        try {
            return text.getBytes(UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    private static void close(@Nullable Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (IOException e) {
//                LOGGER.trace("Failed to close {}", closeable, e);
            }
    }
}
