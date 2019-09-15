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

    private String parseTemplate(String templatePath, Object model) {
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

    void process(){
		try {
			parseTemplate(templateEntry.getCanonicalPath(), database);
		} catch (IOException e) {
			logger.error(e);
		}
	}

    private String concatPath(String basePath, String path){
        String result = FilenameUtils.concat(basePath, path);
        if(result != null && File.separatorChar == '\\'){
            //make the path linux style
            result = result.replace(File.separator, "/");
        }
        return result;
    }

    /**
     * 调用velocity引擎解析模板，忽略生成的内容。
     * 可以在模板中调用本方法，用来在模板中解析别的模板
     * @param templateFile 模板路径，相对于.jm.vm文件的路径
     * @param model 传入模板velocityContext中的对象，以model为key，在模板中可以用$model引用它
     */
    public void process(String templateFile, Object model){
        try {
            String basePath = templateEntry.getParentFile().getCanonicalPath();
            String templatePath = concatPath(basePath, templateFile);
            File template = new File(templatePath);
            if(!template.exists()){
                logger.error("template " + templateFile + " not found");
                return;
            }
            parseTemplate(template.getCanonicalPath(), model);

        } catch (IOException e) {
            logger.error(e);
        }
    }

    /**
     * 调用velocity引擎解析模板，并把解析出的文本，保存至outputFile中
     * 可以在模板中调用本方法，用来在模板中解析别的模板，并保存至outputFile中
     * @param templateFile 模板路径，相对于.jm.vm文件的路径
     * @param outputFile 输出文件路径，相对于.jm.vm文件的路径
     * @param replaceIfExists 如果outputFile已经存在，是否替换
     * @param model 传入模板velocityContext中的对象，以model为key，在模板中可以用$model引用它
     */
    public void generate(String templateFile, String outputFile, boolean replaceIfExists, Object model){
		try {
			String basePath = templateEntry.getParentFile().getCanonicalPath();
			String outputFilePath = concatPath(basePath, outputFile);
			File output = new File(outputFilePath);
			if(output.exists() && !replaceIfExists){
				return;
			}

			String templatePath = concatPath(basePath, templateFile);
			File template = new File(templatePath);
			if(!template.exists()){
				logger.error("template " + templateFile + " not found");
				return;
			}

			String content = parseTemplate(template.getCanonicalPath(), model);
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
