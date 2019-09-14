package org.yidan.jasmine;

import org.apache.commons.io.FileUtils;
import org.yidan.jasmine.dao.MetaDataDao;
import org.yidan.jasmine.meta.Database;
import org.yidan.jasmine.settings.GenerateSetting;

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
		try {
			configFile = configFile.getCanonicalFile();
		} catch (IOException e) {
			logger.error(e);
			return;
		}
		File dir = configFile.getParentFile();
		if(dir == null){
			logger.error("Cannot found the directory for file " + configFilePath);
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

			logger.setProgress(1);
			int index = 1;
			for(File file : entries){
				generate(file, database, setting);
				float percent = index * 1.0f / entries.size();
				percent = percent * 100;
				logger.setProgress((int)percent);
				index ++;
				Thread.sleep(100);
			}
			logger.info("\nDone.");

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
