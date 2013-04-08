package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;

public class CommandHandler {
	
	private String[] shutdownMessages = {"He- oh.. Goodbye world ;~;", "Nobody loves me D:", "Hey, I didn't hurt you!", "It's okay.", "AAAAAAHHHHHHH!!!!", "I don't blame you...", "No hard feelings.", "Shutting down.", "Nap time.", "Nononono I can fix this I ca-", "NNNNNOOOOOOOOOOOOOOOOOOOO~"};
	
	public void processCommand(String channel, String sender, String login, String hostname, String message){
		String command = message.substring(1);
		String[] params = command.split(" ");
		System.out.println("COMMAND: " + command);
		if(BaggyBot.instance.cadburyMode){
			if(command.startsWith("rem ")){
				// Optional: grab rems from www.jefe323.com/data/je/fe/bot/ler/global.txt once the remlist is back up
				addRem(channel, sender, command);
			}else if(command.startsWith("forget ")){
				String results = SqlConnector.getInstance().sendSelectQuery("SELECT rem FROM rems WHERE rem = '" + params[1] +  "'");
				if(!(results.equals("") || results == null)){
					SqlConnector.getInstance().sendQuery("DELETE FROM rems WHERE rem = '" + params[1] + "'");
					BaggyBot.instance.sendMessage(channel, sender + ", I have forgotten " + command.substring("forget ".length()) + ".");
				}
				
			}else if(command.startsWith("g ")){
				processGoogleSearch(channel, sender, command);
			/*}else if(command.startsWith("mcf ")){
				processGoogleSearch(channel, sender, "site:minecraftforum.net " + command);
			}else if(command.startsWith("y ")){
				processGoogleSearch(channel, sender, "site:youtube.com " + command);*/
			}else if(command.startsWith("gis ")){
				processGoogleImageSearch(channel,sender,command);
			}else if(command.startsWith("ur ")){
				processUrbanLookup(channel, sender, login, hostname, params[1]);
			}else if(command.startsWith("u ")){
				if(params.length == 1){
					sendMessage(channel, sender + ", usage: $u <username> [profile|reputation|posts|admin|etc]");
				}else if(params.length == 2){
					sendMessage(channel, sender + ", http://u.mcf.li/" + params[1]);
				}else if(params.length == 3){
					sendMessage(channel, sender + ", http://u.mcf.li/" + params[1] + "/" + params[2]);
				}else if(params.length == 4){
					sendMessage(channel, sender + ", http://u.mcf.li/" + params[1] + "/" + params[2]+ "/page/" + params[3]);
				}else if(params.length == 5){
					sendMessage(channel, sender + ", http://u.mcf.li/" + params[1] + "/" + params[2]+ "/" + params[3]+ "/" + params[4]);
				}
				
			}
		}
		if(command.startsWith("ed") && authorize(channel,login,hostname)){
			if(BaggyBot.instance.unreadExceptionsAvailable()){
				BaggyBot.instance.sendMessage(channel, "Last exception details: " + BaggyBot.instance.readException(0).getMessage());
			}else{
				BaggyBot.instance.sendMessage(channel, "No unread exceptions left.");
			}
		
		}else if(command.equals("shutdown") && authorize(channel, login, hostname)){
			BaggyBot.instance.sendMessage(channel, shutdownMessages[new Random().nextInt(shutdownMessages.length)]);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			BaggyBot.instance.shutdown();
		}else if(command.startsWith("update") && authorize(channel, login, hostname)){
			if(params.length > 1 && params[1].equals("-d")){
				BaggyBot.instance.downloadUpdate();
			}
			BaggyBot.instance.update();
		}else if(command.startsWith("query ")){
			processSqlCommand(channel, sender, login, hostname, command);
			
		
		}else if(command.startsWith("ffl") && authorize(channel, login, hostname)){
			if(Logger.getInstance().flush()){
				sendMessage(channel, "Successfully flushed the log file.");
			}else{
				sendMessage(channel, "Failed to flush the log file.");
			}
		}
		else if(command.startsWith("help ")){
			if(params[1].equals("quote")){
				sendMessage(channel, sender + ", For each line sent to the channel, if it contains more than 6 words, it has a 5% chance of becoming that user's random quote.");
			}else if(params[1].equals("topics")){
				sendMessage(channel, sender + ", Use -help <topic> to get help about a specific topic. Available topics are: login, quote, commands");
			}else if(params[1].equals("login")){
				sendMessage(channel, sender + ", Currently, people are identified by their login. This means that statistics are shared if you use webchat or if you haven't set up your login. Refer to your IRC client's documentation to find out how to set it up.");
			}else if(params[1].equals("commands")){
				sendMessage(channel, "The following commands are available for everyone to use: info/help/version, get, ping. If cadbury mode is toggled (when Cadbury is not in the channel), the following commands are also available: rem, g, forget, ur, u. Other commands are: ed, shutdown, query, tcm, set, del, mem, c*tricpuns++, snag.");
				sendMessage(channel, "For help on a specific command, use -help <command>");
			}else if(params[1].equals("get")){
				if(params.length == 2)
				sendMessage(channel, "Returns statistics data. Available parameters: quote, lines, glines, swears. for more information, use -help get <parameter>");
				else if(params[2].equals("quote"))
				sendMessage(channel, "Returns the random quote for that user. Usage: -get quote <username>");
				else if(params[2].equals("lines"))
				sendMessage(channel, "Returns the line count for that user. Usage: -get lines <username>");
				else if(params[2].equals("glines"))
				sendMessage(channel, "Returns the global line count. No other parameters required. Usage: -get glines");
				else if(params[2].equals("swears"))
				sendMessage(channel, "Returns a user's swear count. Usage: -get swears <username>");
				
			}else if(params[1].equals("rem")){
				sendMessage(channel, "Allows you to save rems. Uses Cadbury's syntax. Additionally, you can use the {SENDER} variabele to put the name of the person who executed the command in the message.");
			}else if(params[1].equals("help")){
				if(params.length == 2){
					sendMessage(channel, "Yo dawg..");
				}else if(params[2].equals("help")){
					if(params.length == 3){
						sendMessage(channel, "There's nothing here!");
					}else if(params[3].equals("help")){
						if(params.length == 4){
							sendMessage(channel, "Quit wasting your time and start doing something useful with your life.");
						}else if(params[4].equals("help")){
							if(params.length == 5){
								sendMessage(channel, "No, seriously. I'm not kidding. Why are you still here.");
							}
						}
					}
				}
			}
		}else if(command.startsWith("info") || command.startsWith("help") || command.startsWith("version")){
			BaggyBot.instance.sendMessage(channel, "StatsBot " + BaggyBot.version +" - made by baggerboot. Stats page: http://jgeluk.net/stats/ - To see a list of help topics, use the '-help topics' command.");
		
		
		}else if(command.equals("tcm") && authorize(channel, login, hostname)){
			BaggyBot.instance.cadburyMode = !BaggyBot.instance.cadburyMode;
			if(BaggyBot.instance.cadburyMode){
				BaggyBot.instance.sendMessage(channel, "'$g' and rems enabled.");
			}else{
				BaggyBot.instance.sendMessage(channel, "'$g' and rems disabled.");
			}
		
		}else if(command.startsWith("set ") && authorize(channel, login, hostname)){
			if(params.length == 5 && params[1].equals("primary") && params[3].equals("for")){
				String _login = params[4];
				String newPrimary = params[2];
				String result = SqlConnector.getInstance().sendQuery("UPDATE alts SET `primary` = '" + newPrimary + "' WHERE `login` = '" + _login + "'");
				BaggyBot.instance.sendMessage(channel, sender + ", " + result);
			}
			
		}else if(command.startsWith("get ")){
			if(params.length == 2){
				if(params[1].equals("glines")){
					sendMessage(channel, sender + ", the global line count is " + SqlConnector.getInstance().sendSelectQuery("SELECT `value` FROM `varia` WHERE `key` = 'global_line_count'"));
				}
			}else if(params.length == 3){
				if(params[1].equals("lines")){
					sendMessage(channel, sender + ", " + params[2] + " has sent " + SqlConnector.getInstance().sendSelectQuery("SELECT `lines` FROM `users` LEFT JOIN alts ON nick = login WHERE (`primary` = '" + params[2] + "' OR `additional` LIKE '%" + params[2] + "%')") + " lines so far.");
				}else if(params[1].equals("quote")){
					sendMessage(channel, sender + ", " + params[2] + "'s random quote: \"" + SqlConnector.getInstance().sendSelectQuery("SELECT `random_quote` FROM `users` LEFT JOIN alts ON nick = login WHERE (`primary` = '" + params[2] + "' OR `additional` LIKE '%" + params[2] + "%')") + "\"");
				}else if(params[1].equals("words")){
					sendMessage(channel, sender + ", " + params[2] + " has written " + SqlConnector.getInstance().sendSelectQuery("SELECT `words` FROM `users` LEFT JOIN alts ON nick = login WHERE (`primary` = '" + params[2] + "' OR `additional` LIKE '%" + params[2] + "%')") + " words so far.");
				}else if(params[1].equals("swears")){
					sendMessage(channel, sender + ", " + params[2] + "'s swear count: " + SqlConnector.getInstance().sendSelectQuery("SELECT `profanities` FROM `users` LEFT JOIN alts ON nick = login WHERE (`primary` = '" + params[2] + "' OR `additional` LIKE '%" + params[2] + "%')"));
				}
			}
			
		}else if(command.startsWith("del ") && authorize(channel, login, hostname)){
			if(command.substring("del ".length()).startsWith("word")){
				String word = command.substring("del word ".length());
				SqlConnector.getInstance().sendQuery("DELETE FROM words WHERE word = '" + word + "'");
				BaggyBot.instance.sendMessage(channel, sender + ", I have removed " + word + " from the words list.");
			}
			
		}else if(command.startsWith("mem")){
			Runtime runtime = Runtime.getRuntime();

		    NumberFormat format = NumberFormat.getInstance();

		    StringBuilder sb = new StringBuilder();
		    long maxMemory = runtime.maxMemory();
		    long allocatedMemory = runtime.totalMemory();
		    long freeMemory = runtime.freeMemory();

		    sb.append("free memory: " + format.format(freeMemory / 1024));
		    sb.append(" - allocated memory: " + format.format(allocatedMemory / 1024));
		    sb.append(" - max memory: " + format.format(maxMemory / 1024));
		    sb.append(" - total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
		    System.out.println(sb.toString());
		    if(params.length > 1 && params[1].equals("-c")){
		    	sendMessage(channel, sender + ", " + sb.toString());
		    }
		
		
		}else if(command.equals("citricpuns++") && (login.equals("~baggerboo") || login.equals("~citricsqu"))){
			SqlConnector.getInstance().tryIncrementVaria("citricpuns");
			BaggyBot.instance.sendMessage(channel, sender + ", done.");
		
		
		}else if(command.equals("calc ")){
			//String formattedStatement = command.substring("calc ".length()).replaceAll(" ", "");
			
		
		
		}else if(command.equals("ping")){
			BaggyBot.instance.sendMessage(channel, "Pong!");
		
		}else if(command.startsWith("queery")){
			
			sendMessage(channel, "Dohohohoho~");
			
		}else if(command.equals("snag") && authorize(channel, login, hostname)){
			sendMessage(channel, "Snagging next line.");
			StatsHandler.getInstance().snagNextLine("*");
			
		}else if(command.startsWith("snag ") && params.length > 1 && authorize(channel, login, hostname)){
			sendMessage(channel, "Snagging next line by " + params[1]);
			StatsHandler.getInstance().snagNextLine(params[1]);
			
		}else if(BaggyBot.instance.cadburyMode){
			processRem(channel, sender, login, hostname, command);
		
		
		}else{
			System.out.println("Ignoring invalid command.");
		}
	}
	private void processUrbanLookup(String channel, String sender, String login, String hostname, String search) {
		System.out.println("Processing urban lookup.");
			
	    String urban = "http://api.urbandictionary.com/v0/define?term=";
	    String charset = "UTF-8";

	    URL url;
	    Reader reader = null;
		try {
			url = new URL(urban + URLEncoder.encode(search, charset));
			reader = new InputStreamReader(url.openStream(), charset);
		} catch (Exception e) {
			System.out.println("Exception! " + e.getMessage());
			BaggyBot.instance.sendMessage(channel, sender + ", something bad happened ;~;");
			BaggyBot.instance.addException(e);
			e.printStackTrace();
		}
		BufferedReader bReader = new BufferedReader(reader);
		String result = sender + ", looks like something went wrong :(";
		String line = null;
		try {
			line = bReader.readLine();
			System.out.println("Read line: " + line );
			//System.out.println("D: " + definition + ", E: "+ example);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("SOMETHING BAD HAPPENED");
			
		}
		int iDStart = line.indexOf("\",\"definition\":\"") + "\",\"definition\":\"".length();
		int iDEnd = line.indexOf("\",\"example\":\"");
		
		String definition = line.substring(iDStart, iDEnd);
		
		int iEStart = line.indexOf("\",\"example\":\"") + "\",\"example\":\"".length();
		int iEEnd = line.indexOf("\",\"thumbs_up\":");
		
		String example = line.substring(iEStart, iEEnd);
		
		result = sender + ", " + definition + " - Example: " + example;
		result = result.replaceAll("\\\\r\\\\n", "");
		result = result.replaceAll("\\\\", "");
		System.out.println("Returning " + result);
		sendMessage(channel, result);
	}
	private void sendMessage(String channel, String message) {
		BaggyBot.instance.sendMessage(channel, message);
		
	}
	private boolean authorize(String channel, String login, String hostname){
		if(login.equals("~baggerboo") && hostname.equals("199.115.228.30")){
			return true;
		}else{
			sendMessage(channel, "You are not authorized to use this command.");
			return false;
		}
	}
	private void processSqlCommand(String channel, String sender, String login, String hostname, String command) {
		if(!authorize(channel, login, hostname)){
			return;
		}
		command = command.substring(6);
		List<String> lines = new ArrayList<String>();
		
		if(command.toLowerCase().startsWith("select")){
			try {
				String[] results = SqlConnector.getInstance().sendSelectQueryArr(command);
				for(int i = 0; i < results.length; i++){
					String nextLine = "";
					while(nextLine.length() < 414){
						if(nextLine.equals("")){
							nextLine = results[i];
							if(i < results.length-1) i++;
							else break;
						}else{
							nextLine = nextLine.concat(", " + results[i]);
							if(i < results.length-1) i++;
							else break;
						}
					}
					lines.add(nextLine);
				}
			} catch (Exception e) {
				e.printStackTrace();
				lines.add("ERROR: " + e.getMessage());
			}
		}else{
			try {
				lines.add(SqlConnector.getInstance().sendQuery(command));
			} catch (Exception e) {
				lines.add("ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
		for(int i = 0; i < lines.size(); i++){
			if(i == 0){
				BaggyBot.instance.sendMessage(channel, sender + ": " + lines.get(i));
			}else{
				BaggyBot.instance.sendMessage(channel, lines.get(i));
			}
			
		}
	}
	private void addRem(String channel, String sender, String command) {
		String[] args = command.split(" ");
		
		int count = Integer.parseInt(SqlConnector.getInstance().sendSelectQuery("SELECT COUNT(rem) FROM rems WHERE rem = '"+ args[1] + "'"));
		
		if(count != 0){
			BaggyBot.instance.sendMessage(channel, "I already have something saved for " + args[1]);
		}else{
			SqlConnector.getInstance().sendQuery("INSERT INTO rems VALUES ('" + args[1] + "', '" + command.substring(4 + args[1].length()) + "')");
			BaggyBot.instance.sendMessage(channel, "Added $" + args[1] + " to remlist.");
		}
	}
	private void processRem(String channel, String sender, String login, String hostname, String command){
		System.out.println("Processing rem for " + command);
		String[] args = command.split(" ");
			String definition = SqlConnector.getInstance().sendSelectQuery("SELECT value FROM rems WHERE rem = '" + args[0] + "'");
			if(definition == "" || definition == null){
				return;
			}
			for(int i = 0; definition.contains("{"+i+"}"); i++){
				if(i < args.length){
					definition = definition.replace("{"+i+"}", args[i+1]);
				}
			}
			definition = definition.replace("{SENDER}", sender);
			
			sendMessage(channel, definition.substring(1));
		}
	private void processGoogleImageSearch(String channel, String sender, String command) {
		String search = command.substring(2);
	    String google = "http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=";
	    String charset = "UTF-8";

	    URL url;
	    Reader reader = null;
		try {
			url = new URL(google + URLEncoder.encode(search, charset));
			reader = new InputStreamReader(url.openStream(), charset);
		} catch (Exception e) {
			BaggyBot.instance.sendMessage(channel, sender + ", something bad happened ;~;");
			BaggyBot.instance.addException(e);
			e.printStackTrace();
		}
		BufferedReader bReader = new BufferedReader(reader);
		String result = sender + ", looks like something went wrong :(";
		String line = null;
		try {
			line = bReader.readLine();
			System.out.println("Read line: " + line );
			//System.out.println("D: " + definition + ", E: "+ example);
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		int iDStart = line.indexOf("\",\"unescapedUrl\":\"") + "\",\"unescapedUrl\":\"".length();
		int iDEnd = line.indexOf("\",\"url\":\"");
		
		String definition = line.substring(iDStart, iDEnd);

		
		result = sender + ", " + definition;
		sendMessage(channel, result);
	}
	private void processGoogleSearch(String channel, String sender, String command){
		String search = command.substring(2);
	    String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
	    String charset = "UTF-8";

	    URL url;
	    Reader reader = null;
		try {
			url = new URL(google + URLEncoder.encode(search, charset));
			reader = new InputStreamReader(url.openStream(), charset);
		} catch (Exception e) {
			BaggyBot.instance.sendMessage(channel, sender + ", something bad happened ;~;");
			BaggyBot.instance.addException(e);
			e.printStackTrace();
		}
	    GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);

	    // Show title and URL of 1st result.
	    String title = results.getResponseData().getResults().get(0).getTitle();
	    title = title.replace("<b>", "");
	    title = title.replace("</b>", "");
	    String URL = results.getResponseData().getResults().get(0).getUrl();
	    BaggyBot.instance.sendMessage(channel, sender + ", " + title + ": " + URL);
	}
}
