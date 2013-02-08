package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Logger {
	private static Logger instance;
	
	private File logFile;
	private Writer fileWriter;
	private BufferedWriter writer;
	
	public static Logger getInstance(){
		if(instance == null) instance = new Logger();
		return instance;
	}
	public Logger(){
		try {
			logFile = new File("BaggyBot.log");
			fileWriter = new FileWriter(logFile);
			writer = new BufferedWriter(fileWriter);
		} catch (IOException e) {
			e.printStackTrace();
			BaggyBot.instance.queueMessage("WARNING: Failed to open the log file. Debug output will not be available.");
			
		}
	}
	public static void log(String line){
		if(instance == null) instance = new Logger();
		instance.write(line);
	}
	public void write(String line){
		try {
			writer.write(line + "\r\n");
		} catch (IOException e) {
			BaggyBot.instance.unreadExceptions.add(e);
			e.printStackTrace();
		}
	}
	public void close(){
		try {
			writer.close();
		} catch (IOException e) {
			BaggyBot.instance.unreadExceptions.add(e);
			e.printStackTrace();
		}
	}
}
