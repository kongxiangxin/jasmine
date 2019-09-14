package org.yidan.jasmine;

import org.apache.commons.io.FilenameUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.yidan.jasmine.meta.Database;
import org.yidan.jasmine.settings.GenerateSetting;

import java.io.*;
import java.util.Properties;


/**
 * Created by kongxiangxin on 2017/8/1.
 */
public class TemplateProcessor {

    private File templateEntry;
    private Database database;
    private GenerateSetting setting;
    private Logger logger;
    private static final int BUFFER_SIZE = 1024 * 1024 / 2;

    public TemplateProcessor(GenerateSetting setting, File templateEntry, Database database, Logger logger){
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
            File templateFile = new File(templatePath);
            String templateFileFolder = templateFile.getParentFile().getCanonicalPath();
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
            Template template = engine.getTemplate(templateFile.getName(), setting.getFileEncoding());
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
		try {
			parseTemplate(templateEntry.getCanonicalPath(), database);
		} catch (IOException e) {
			logger.error(e);
		}
	}

    /**
     * we can invoke it in template file
     * @param templatePath
     * @param model
     */
    public void process(String templatePath, Object model){
		try {
			String basePath = templateEntry.getParentFile().getCanonicalPath();
			String path = concatPath(basePath, templatePath);
			parseTemplate(path, model);
		} catch (IOException e) {
			logger.error(e);
		}
    }


    public String concatPath(String basePath, String path){
        String result = FilenameUtils.concat(basePath, path);
        if(result != null && File.separatorChar == '\\'){
            //make the path linux style
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

    /**
     * we can invoke it in template file
     * @param templateFile
     * @param outputFile
     * @param replaceIfExists
     * @param model
     */
    public void generate(String templateFile, String outputFile, boolean replaceIfExists, Object model){
		try {
			String basePath = templateEntry.getParentFile().getCanonicalPath();
			String outputFilePath = concatPath(basePath, outputFile);
			File output = new File(outputFilePath);
			if(output.exists() && !replaceIfExists){
				return;
			}

			String templatePath = basePath + File.separator + templateFile;
			File template = new File(templatePath);
			if(!template.exists()){
				logger.error("template " + templateFile + " not found");
				return;
			}

			String absolutePath = template.getCanonicalPath();
			String content = parseTemplate(absolutePath, model);
			write(content, output);
		} catch (IOException e) {
			logger.error(e);
		}
    }

    private void write(String content, File output) throws IOException {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            File targetDir = output.getParentFile();
            if(!targetDir.exists()){
                if(!targetDir.mkdirs()){
                    throw new IOException("failed to make dir " + targetDir.getCanonicalPath());
                }
            }
            reader = new BufferedReader(new StringReader(content));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), setting.getFileEncoding()));
            char[] buffer = new char[BUFFER_SIZE];
            int read;
            while((read = reader.read(buffer)) != -1){
                writer.write(buffer, 0, read);
            }
        }  finally {
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
