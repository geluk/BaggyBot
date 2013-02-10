package main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jibble.pircbot.*;

/*
 * This is the main class for the bot.
 */

public class BaggyBot extends PircBot{
	
	// Which prefix to use for commands
	private String commandIdentifier = "-";
	
	private List<String> queuedMessages = new ArrayList<String>();
	
	// The current version of the bot. Only increment this each time there is a release.
	// Convention: (milestone).(major)[.(minor).[(revision/bugfix)]]
	public static final String version = "1.9.1.1";
	
	// More debug output?
	private static final boolean verbose = false;
	
	// This list contains all unread exceptions so they can be read directly from irc, using the -ed command.
	private List<Exception> unreadExceptions = new ArrayList<Exception>();
	
	public boolean addException(Exception e) {
		StackTraceElement[] elements = e.getStackTrace();
		String stackTrace = null;
		for(int i = elements.length-1; i >= 0; i--){
			StackTraceElement element = elements[i];
			stackTrace += ("--> " +  element.getClassName() + ":" + element.getMethodName() + "() at line " + element.getLineNumber() +" ");
		}
		Logger.log("[WARNING] An exception occured: " + e.getMessage());
		Logger.log(stackTrace);
		return unreadExceptions.add(e);
	}
	public Exception readException(int arg0) {
		return unreadExceptions.remove(arg0);
	}
	private CommandHandler ch;
	
	// Should generate a getter for this at some point and update my code to use it. CBA to do it now.
	public static BaggyBot instance;
	
	// List of all rems. Currently not persistent. Will eventually be saved in a MySQL database.
	private Map<String, String> rems = new HashMap<String,String>();

	// When true, enables rems and the $g command
	public boolean cadburyMode = false;
	
	// Initializes some variables, shows login data and connects to the MySQL DB.
	public BaggyBot(){
		String name = SettingsManager.getInstance().getSetting("nick");
		String login = SettingsManager.getInstance().getSetting("ident");
		String port = SettingsManager.getInstance().getSetting("sqlport");
		String host = SettingsManager.getInstance().getSetting("sqlhost");
		String user = SettingsManager.getInstance().getSetting("sqluser");
		String pass = SettingsManager.getInstance().getSetting("sqlpass");
		
		if(name.equals("") || login.equals("") || port.equals("") || host.equals("") || user.equals("")){
			System.out.println("Please make sure you've entered the bot settings in your settings file. Bot will now shut down.");
			System.exit(0);
		}
		int iPort = 0;
		try{
			iPort = Integer.parseInt(port);
		}catch(NumberFormatException e){
			System.out.println("Please check your settings file. The 'Port' setting may only contain numbers. Bot will now shut down.");
			System.exit(0);
		}
		
		System.out.println("\t IRC Server:\tirc.esper.net");
		System.out.println("\t IRC Nick:\t" + name);
		System.out.println("\t IRC Login:\t" + login);
		System.out.println("\t===========================");
		System.out.println("\t SQL Host:\t" + host);
		System.out.println("\t SQL Port:\t" + port);
		System.out.println("\t SQL User:\t" + user);
		System.out.println("\t SQL DB:\t" + "stats_bot");
		
		
		
		this.setName(name);
		this.setLogin(login);
		
		SqlConnector.getInstance().connect(host, iPort, user, pass, "stats_bot");
		
		ch = new CommandHandler();
		
		instance = this;
	}
	public void onAction(String sender, String login, String hostname, String target, String action){
		StatsHandler.getInstance().processAction(sender,login,hostname,target,action);
	}
	
	// Gets executed whenever an IRC message is sent
	public void onMessage(String channel, String sender, String login, String hostname, String message){
		// Process the line as a command if it starts with the command identifier or, if cadbury mode is enabled, the $ character.
		if(message.startsWith(commandIdentifier) || (cadburyMode && message.startsWith("$"))){
			ch.processCommand(channel, sender, login, hostname, message);
		}
		// Only update statistics for lines that are not Cadbury commands
		else if(!message.startsWith("$")){
			StatsHandler.getInstance().processMessage(channel, sender, login, hostname, message);
		}
	}
	// Gets executed whenever somebody joins the channel
	public void onJoin(String channel, String sender, String login, String hostname){
		// Automatically disable cadbury mode if Cadbury joins the channel
		if(login.equals("~Cadbury")){
			cadburyMode = false;
		}else if(login.equals(super.getLogin())){
			for(int i = 0; i < queuedMessages.size(); i++){
				sendMessage(channel, queuedMessages.get(i));
			}
			queuedMessages.clear();
		}
	}

	// Intializes the bot by creating an instance of it, having it connect to an IRC server and join a channel.
	public static void main(String args[]){
		splash();
		Logger.log("Starting BaggyBot v" + version + "...");
		BaggyBot bot = new BaggyBot();
		System.out.println("\n==============================================");
		System.out.println("Connecting to IRC server...");
		bot.setVerbose(verbose);
		try {
			bot.connect("irc.esper.net");
		} catch (IOException | IrcException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("   Done");
		String channel = SettingsManager.getInstance().getSetting("channel");
		System.out.println("Joining " + channel);
		bot.joinChannel(channel);
		System.out.println("Ready to serve.");
		
		if(args.length > 1 && args[0].equals("-update")){
			if(args[1].equals("success")){
				bot.sendMessage(channel, "Succesfully updated to version " + version);
				Logger.log("Succesfully updated to version " + version);
			}
			else if(args[1].equals("sameversion")){
				bot.sendMessage(channel, "Failed to update: No newer version available.");
				Logger.log("Failed to update: No newer version available.");
			}
			else if(args[1].equals("nofile")){
				bot.sendMessage(channel, "Failed to update: Bot is already on the latest version.");
				Logger.log("Failed to update: Bot is already on the latest version.");
			}
		}
	}
	
	// This was not cheaply copied from Cadbury's source or anything. Nope. Not at all.
	// In my defense, my ASCII art sucks.
	private static void splash() {
		System.out.println("   ###   ##   ##   ## #   # ###   ##  #####");
		System.out.println("   #  # #  # #    #    # #  #  # #  #   #");
		System.out.println("   ###  #### #    #     #   ###  #  #   #");
		System.out.println("   #  # #  # #  # #  #  #   #  # #  #   #");
		System.out.println("   ###  #  #  ##   ##   #   ###   ##    #");
		System.out.println("\t===========================");
		System.out.println("\t Version:\t" + version);
		System.out.println("\t Author:\tbaggerboot");
		System.out.println("\t===========================");
		
	}
	public boolean remExists(String rem){
		return rems.containsKey(rem);
	}
	public void addRem(String rem, String definition) {
		rems.put(rem, definition);
	}
	public void removeRem(String rem){
		rems.remove(rem);
	}
	public void removeRemIfExists(String rem){
		if(remExists(rem)){
			removeRem(rem);
		}
	}
	public String getRem(String rem){
		return rems.get(rem);
	}
	
	// Closes all resources currently opened by the bot, allowing it to shut down cleanly.
	private void closeConnections(){
		Logger.log("[INFO] Closing all open resources.");
		quitServer();
		dispose();
		SqlConnector.getInstance().disconnect();
		Logger.getInstance().close();
	}
	
	public void shutdown(){
		closeConnections();
		System.exit(0);
	}
	
	public void downloadUpdate(){
		sendMessage(getChannels()[0], "Downloading latest version...");
		URL jarLocation = null;
		try {
			jarLocation = new URL("http://home1.jgeluk.net/files/BaggyBot.jar");
		} catch (MalformedURLException e) {
			sendMessage(getChannels()[0], "Unable to download the latest version: " + e.getMessage());
			e.printStackTrace();
		}
		ReadableByteChannel rbc = null;
		try {
			rbc = Channels.newChannel(jarLocation.openStream());
		} catch (IOException e) {
			sendMessage(getChannels()[0], "Unable to open a connection with the server: " + e.getMessage());
			e.printStackTrace();
		}
		FileOutputStream fos= null;
		try {
			fos = new FileOutputStream("baggybot_new.jar");
		} catch (FileNotFoundException e) {
			sendMessage(getChannels()[0], "Unable to create a new file: " + e.getMessage());
			e.printStackTrace();
		}
		try {
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
			fos.close();
			rbc.close();
		} catch (IOException e) {
			sendMessage(getChannels()[0], "Unable to save the downloaded file: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	public void update(){
		sendMessage(getChannels()[0], "Preparing to update... Current version: " + BaggyBot.version);
		
		Logger.log("[INFO] Preparing to update. Current version: " + version + ".");
		closeConnections();
		try {
			Runtime.getRuntime().exec(new String[]{"bash","-c","~/bot/autoupdate.sh"});
		} catch (IOException e) {
			System.out.println("WARNING: Failed to run the update script. Please run it manually instead.");
			e.printStackTrace();
		}
	}
	public void queueMessage(String string) {
		queuedMessages.add(string);
	}
	public boolean unreadExceptionsAvailable() {
		return !unreadExceptions.isEmpty();
	}
}
