package org.yidan.idea.plugin.jasmine;

import org.apache.commons.io.FileUtils;
import org.yidan.idea.plugin.jasmine.dao.MetaDataDao;
import org.yidan.idea.plugin.jasmine.meta.Database;
import org.yidan.idea.plugin.jasmine.settings.GenerateSetting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

public class Generator {
	private Logger logger;

	public Generator(Logger logger){
		this.logger = logger;
	}

	public void generate(String configFilePath){
		File configFile = new File(configFilePath);
		File dir = configFile.getParentFile();
		if(dir == null){
			logger.error("目录不存在");
			return;
		}
		String moduleName = dir.getName();
		GenerateSetting setting = loadSetting(configFilePath);
		if(setting == null){
			return;
		}
		Collection<File> entries = FileUtils.listFiles(dir, new String[]{"jm.vm"}, true);
		if(entries.isEmpty()){
			logger.error("No template found [module:" + moduleName + "]");
			return;
		}
		logger.info("Generating " + moduleName + "...");

		try {
			MetaDataDao dao = new MetaDataDao(setting);
			Database database = dao.getDatabase();

			logger.setProgress(0.1);
			int index = 1;
			for(File file : entries){
				generate(file, database, setting);
				float percent = index * 1.0f / entries.size();
				logger.setProgress(percent * 100);
				index ++;
			}
			logger.info("Generated");

		} catch (Throwable e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	private GenerateSetting loadSetting(String configFile){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(configFile));
		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}

		return GenerateSetting.getInstance(prop);
	}

	private void generate(File templateEntry, Database database, GenerateSetting setting){
		TemplateProcessor processor = new TemplateProcessor(setting, templateEntry, database, logger);
		processor.process();;

	}
}
