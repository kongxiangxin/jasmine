package org.yidan.idea.plugin.jasmine;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jelly.util.FileUtil;
import org.jelly.util.StringUtil;
import org.yidan.idea.plugin.jasmine.meta.Database;
import org.yidan.idea.plugin.jasmine.settings.GenerateSetting;

import java.io.File;
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
            String e = StringUtil.substringAfterLast(templatePath, "/");
            String templateFileFolder = StringUtil.substringBefore(templatePath, e);
            VelocityEngine engine = new VelocityEngine();
            Properties prop = new Properties();
            prop.put("file.resource.loader.path", templateFileFolder);
            engine.init(prop);
            Template template = engine.getTemplate(e);
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
        return FilenameUtils.concat(basePath, path);

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

        String content = parseTemplate(template.getAbsolutePath(), model);
        FileUtil.write(content, output);
    }
}
