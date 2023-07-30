package auto;

import haven.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static haven.Inventory.*;

public class BearScoutBot extends Bot{
    
    public BearScoutBot(List<Target> targets, BotAction... actions) {
	super(targets, actions);
    }
    
    public static void Start(GameUI gui) {
	gui.ui.message("Looking for bears", GameUI.MsgType.INFO);
	Gob player = gui.map.player();
	ArrayList<Target> targets = new ArrayList<Target>();
	targets.add(new Target(player));
	start(new BearScoutBot(targets, ScoutForBear(gui)), gui.ui);
    }
    
    public static Bot.BotAction ScoutForBear(GameUI gui) {
	return target -> {
	    /*
	    String token = System.getenv("DISCORD_TOKEN");
	    EnumSet<GatewayIntent> intents = EnumSet.of(
		// Enables MessageReceivedEvent for guild (also known as servers)
		GatewayIntent.GUILD_MESSAGES,
		// Enables the event for private channels (also known as direct messages)
		GatewayIntent.DIRECT_MESSAGES
	    );
	    
	    // By using createLight(token, intents), we use a minimalistic cache profile (lower ram usage)
	    // and only enable the provided set of intents. All other intents are disabled, so you won't receive events for those.
	    JDA jda = JDABuilder.createLight(token, intents).build();
	    try
	    {
		// Here you can now start using the jda instance before its fully loaded,
		// this can be useful for stuff like creating background services or similar.
		
		// The queue(...) means that we are making a REST request to the discord API server!
		// Usually, this is done asynchronously on another thread which handles scheduling and rate-limits.
		// The (ping -> ...) is called a lambda expression, if you're unfamiliar with this syntax it is HIGHLY recommended to look it up!
		jda.getRestPing().queue(ping ->
		    // shows ping in milliseconds
		    System.out.println("Logged in with ping: " + ping)
		);
		
		// If you want to access the cache, you can use awaitReady() to block the main thread until the jda instance is fully loaded
		jda.awaitReady();
			  
	    }
	    catch (InterruptedException e)
	    {
		// Thrown if the awaitReady() call is interrupted
		e.printStackTrace();
	    }
	    
	    
	    Coord whereToPlant = gui.map.player().rc.floor(OCache.posres);
	    Coord mc = gui.ui.mc;
	    gui.map.wdgmsg("itemact", Coord.z, whereToPlant, 0);
	    Thread.sleep(100);
	    
	    List<TextChannel> channels = jda.getTextChannelsByName("bot-notifications", true);
	    for(TextChannel ch : channels)
	    {
		ch.sendMessage("Bear Spotted").queue();
	    }*/
		    
	};
    }
}
