
public class ChatBot {
	public static void main(String [] args) {
		bot ChatBot = new bot(); // bot being created
		ChatBot.setVerbose(true); // allow it is chat
		// connecting to specified server
		try {
			ChatBot.connect("irc.freenode.net");
			}
			catch (Exception e) {
			System.out.println("Can’t connect: " + e);
			return;
			}
		// joins channel and prints an entry message.
		ChatBot.joinChannel("#testChannel"); 
	    ChatBot.sendMessage("#testChannel", "Hey! Enter 'weather' for the weather or 'history' for some history.");

	}
}
