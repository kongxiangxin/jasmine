package org.yidan.idea.plugin.jasmine;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.yidan.idea.plugin.jasmine.meta.Database;
import org.yidan.idea.plugin.jasmine.settings.GenerateSetting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;


/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class TemplateProcessor {

    private VirtualFile templateEntry;
    private Database database;
    private GenerateSetting setting;
    private Logger logger;
    private static final int BUFFER_SIZE = 1024 * 1024 / 2;

    public TemplateProcessor(GenerateSetting setting, VirtualFile templateEntry, Database database, Logger logger){
        this.setting = setting;
        this.templateEntry = templateEntry;
        this.database = database;
        this.logger = logger;
    }

    public TemplateProcessor(){}

    public String parseTemplate(String templatePath, Object model) {
        Thread thread = Thread.currentThread();
        ClassLoader loader = thread.getContextClassLoader();
        thread.setContextClassLoader(this.getClass().getClassLoader());

        String content;
        try {
            String e = StringUtils.substringAfterLast(templatePath, "/");
            String templateFileFolder = StringUtils.substringBefore(templatePath, e);
            VelocityEngine engine = new VelocityEngine();
            Properties prop = new Properties();
            prop.put(Velocity.ENCODING_DEFAULT, setting.getFileEncoding());//全局编码,如果以下编码不设置它就生效
            prop.put(Velocity.INPUT_ENCODING, setting.getFileEncoding());//输入流的编码，其实是打酱油!非模板文件编码
            prop.put(Velocity.OUTPUT_ENCODING, setting.getFileEncoding());//输入流编码,很关键!
            prop.put("file.resource.loader.path", templateFileFolder);
            prop.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            prop.put("runtime.log.logsystem.log4j.category", "velocity");
            prop.put("runtime.log.logsystem.log4j.logger", "velocity");
            engine.init(prop);
            Template template = engine.getTemplate(e, setting.getFileEncoding());
            StringWriter writer = new StringWriter();
            VelocityContext context = new VelocityContext();
            context.put("model", model);
            context.put("processor", this);
            context.put("setting", this.setting);

            template.merge(context, writer);
            content = writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            thread.setContextClassLoader(loader);
        }

        return content;
    }

    public void process(){
        parseTemplate(templateEntry.getPath(), database);
    }

    public void process(String templatePath, Object model){
        VirtualFile folder = templateEntry.getParent();
        String basePath = folder.getPath();

        String path = concatPath(basePath, templatePath);
        parseTemplate(path, model);
    }


    public String concatPath(String basePath, String path){
        String result = FilenameUtils.concat(basePath, path);
        if(result != null && File.separatorChar == '\\'){
            result = result.replace(File.separator, "/");
        }
        return result;
    }

    public String getProperty(String name, String def){
        String value = System.getProperty("name");
        if(value == null){
            value = System.getenv("name");
        }
        if(value == null){
            return def;
        }
        return value;
    }

    public void generate(String templateFile, String outputFile, boolean replaceIfExists, Object model){
        VirtualFile folder = templateEntry.getParent();

        String outputFilePath = folder.getPath() + File.separator + outputFile;
        File output = new File(outputFilePath);
        if(output.exists() && !replaceIfExists){
            return;
        }

        String templatePath = folder.getPath() + File.separator + templateFile;
        File template = new File(templatePath);
        if(!template.exists()){
            logger.showError("模板" + templateFile + "不存在");
            return;
        }

        String absolutePath = template.getAbsolutePath();
        if(File.separatorChar == '\\'){
            absolutePath = absolutePath.replace(File.separator, "/");
        }

        String content = parseTemplate(absolutePath, model);
        write(content, output);
    }

    private void write(String content, File output){
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new StringReader(content));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), setting.getFileEncoding()));
            char[] buffer = new char[BUFFER_SIZE];
            int read;
            while((read = reader.read(buffer)) != -1){
                writer.write(buffer, 0, read);
            }
        } catch (Throwable e) {
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
