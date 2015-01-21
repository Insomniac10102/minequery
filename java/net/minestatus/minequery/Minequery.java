package net.minestatus.minequery;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A port of Minequery that works with the Bukkit plugin platform.
 * 
 * @author Blake Beaupain
 * @author Kramer Campbell
 * @since 1.0
 */
public final class Minequery extends JavaPlugin {

	/**
	 * The main configuration file.
	 */
	public static final String CONFIG_FILE = "server.properties";

	/**
	 * The logging utility (used for error logging).
	 */
	private final Logger log = Logger.getLogger("Minecraft");

	/**
	 * The host that the server listens on (any by default).
	 */
	private String serverIP;

	/**
	 * The port of the Minecraft server.
	 */
	private int serverPort;

	/**
	 * The port of the Minequery server.
	 */
	private int port;

	/**
	 * The maximum amount of players allowed on the Minecraft server.
	 */
	private int maxPlayers;

	/**
	 * The main Minequery server.
	 */
	private QueryServer server;
	
	private Permissions p;
	
	private boolean debug = true;

	/**
	 * Creates a new <code>Minequery</code> object.
	 */
	public Minequery()
	{
		init();
	}
	
	public void init()
	{
		// Initialize the Minequery plugin.
		try {
			Properties props = new Properties();
			props.load(new FileReader(CONFIG_FILE));
			serverIP = props.getProperty("server-ip", "ANY");
			serverPort = Integer.parseInt(props.getProperty("server-port", "25565"));
			port = Integer.parseInt(props.getProperty("minequery-port", "25566"));
			maxPlayers = Integer.parseInt(props.getProperty("max-players", "32"));

			// By default, "server-ip=" is set in server.properties which causes the default in getProperty() to not
			// apply. This checks if it's blank and sets it to "ANY" if so.
			if (serverIP.equals("")) {
				serverIP = "ANY";
			}

			server = new QueryServer(this, serverIP, port);
		} catch (IOException ex) {
			if(isDebugging())
				log.log(Level.SEVERE, "Error initializing Minequery", ex);
		}
	}
	
	public void reload()
	{
		try {
			server.getListener().close();
			server = new QueryServer(this, serverIP, port);
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(p.has(sender, cmd, true))
		{
			if(cmd.getName().equalsIgnoreCase("minequery"))
			{
				if(args.length > 1)
				{
					if(args[0].equalsIgnoreCase("port"))
					{					
						try
						{
							port = Integer.parseInt(args[1]);
						}
						catch(Exception e)
						{
							if(sender instanceof Player)
								sender.sendMessage("Invalid port received.");
							else
								log.info("Invalid port received.");
							
							return true;
						}
						
						if(sender instanceof Player)
							sender.sendMessage("Minequery port set to " + port + ". Reload required for changes to take effect.");
						else
							log.info("Minequery port set to " + port + ". Reload required for changes to take effect.");
						
						return true;
					}	
				}
				else if(args.length > 0)
				{
					if(args[0].equalsIgnoreCase("toggledebug"))
					{					
						debug = !debug; //Change debug to opposite value.
						
						if(sender instanceof Player)
							sender.sendMessage("Debug set to " + debug);
						else
							log.info("Debug set to " + debug);
						
						return true;
					}
					if(args[0].equalsIgnoreCase("port"))
					{					
						
						if(sender instanceof Player)
							sender.sendMessage("Minequery is listening on port " + port);
						else
							log.info("Minequery is listening on port " + port);
						
						return true;
					}
					if(args[0].equalsIgnoreCase("reload"))
					{					
						
						if(sender instanceof Player)
						{
							sender.sendMessage("Reloading Minequery...");
							this.reload();
							sender.sendMessage("Done reloading!");
						}
						else
						{
							log.info("Reloading Minequery...");
							this.reload();
							log.info("Done reloading!");
						}
						
						return true;
					}
					
				}
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() {
		try {
			server.getListener().close();
		} catch (IOException ex) {
			if(isDebugging())
				log.log(Level.WARNING, "Unable to close the Minequery listener", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	@Override
	public void onEnable() {
		if (server == null) {
			if(isDebugging())
				this.log.severe("Couldn't load Minequery -- Disabling plugin");
			this.getServer().getPluginManager().disablePlugin(this);
		}
		
		p = new Permissions(this);
		p.setupPermissions();
		
		// Start the server normally.
		server.start();
	}

	/**
	 * Gets the port that the Minecraft server is running on.
	 *
	 * @return The Minecraft server port
	 */
	public int getServerPort() {
		return serverPort;
	}

	/**
	 * Gets the port that the Minequery server is running on.
	 *
	 * @return The Minecraft server port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Gets the maximum amount of players the Minecraft server can hold.
	 * 
	 * @return The maximum amount of players
	 */
	public int getMaxPlayers() {
		return maxPlayers;
	}
	
	public Permission getPermissions()
	{
		return p.getPermissions();
	}
	
	public boolean isDebugging()
	{
		return debug;
	}

}
