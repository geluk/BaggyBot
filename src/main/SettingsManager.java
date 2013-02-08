package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class SettingsManager {
	private static SettingsManager instance;
	public static SettingsManager getInstance(){
		if(instance == null) instance = new SettingsManager();
		return instance;
	}
	
	private Properties settings = new Properties();
	
	public SettingsManager(){
		try {
			loadSettings();
		} catch (IOException e) {
			System.out.println("Unable to load the settings. Program will now terminate.");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	public String getSetting(String identifier){
		return settings.getProperty(identifier);
	}
	
	private void loadSettings() throws IOException{
		File settingsFile = new File("settings.txt");
		if(!settingsFile.exists()){
			settingsFile.createNewFile();
			FileWriter fw = new FileWriter(settingsFile);
			BufferedWriter writer = new BufferedWriter(fw);
			
			writer.write("sqluser=\n");
			writer.write("sqlpass=\n");
			writer.write("sqlhost=\n");
			writer.write("sqlport=\n");
			writer.write("nick=\n");
			writer.write("ident=\n");
			writer.write("channel=\n");
			
			writer.close();
		}
		FileInputStream input = new FileInputStream(settingsFile);
		settings.load(input);
		input.close();
	}
	public void save(){
		try {
			settings.store(new FileOutputStream(new File("settings.txt")), "");
		} catch (FileNotFoundException e) {
			try {
				new File("settings.txt").createNewFile();
			} catch (IOException e1) {
				BaggyBot.instance.unreadExceptions.add(e);
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (IOException e) {
			BaggyBot.instance.unreadExceptions.add(e);
			e.printStackTrace();
		}
	}
}
