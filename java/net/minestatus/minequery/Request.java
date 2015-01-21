package net.minestatus.minequery;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

/**
 * Handles Minequery requests.
 * 
 * @author Kramer Campbell
 * @author Blake Beaupain
 * @since 1.2
 */
public final class Request extends Thread {
	/**
	 * The parent plugin object.
	 */
	private final Minequery minequery;

	/**
	 * The socket we are using to obtain a request.
	 */
	private final Socket socket;

	/**
	 * The logging utility.
	 */
	private final Logger log = Logger.getLogger("Minecraft");

	/**
	 * Creates a new <code>QueryServer</code> object.
	 * 
	 * @param minequery
	 *            The parent plugin object
	 * @param socket
	 *            The socket we are using to obtain a request
	 */
	public Request(Minequery minequery, Socket socket) {
		this.minequery = minequery;
		this.socket = socket;
	}

	/**
	 * Listens for a request.
	 */
	public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Read the request and handle it.
			String line = reader.readLine();
			handleRequest(socket, line);

			// Finally close the socket.
			socket.close();
		} catch (IOException ex) {
			if(minequery.isDebugging())
				log.log(Level.WARNING, "There was an error while handling a request");
		}
	}

	/**
	 * Handles a received request.
	 * 
	 * @param request
	 *            The request message
	 * @throws java.io.IOException
	 *             If an I/O error occurs
	 */
	private void handleRequest(Socket socket, String request) throws IOException {
		
		// Handle a query request.
		if (request == null) {
			return;
		}
		
		if(minequery.isDebugging())
			log.info("Request received: " + request);

		// Handle a standard Minequery request.
		if (request.equalsIgnoreCase("QUERY")) {
			Minequery m = getMinequery();

			String[] playerList = new String[m.getServer().getOnlinePlayers().length];
			for (int i = 0; i < m.getServer().getOnlinePlayers().length; i++) {
				playerList[i] = m.getServer().getOnlinePlayers()[i].getName();
			}

			// Build the response.
			StringBuilder resp = new StringBuilder();
			resp.append("SERVERPORT " + m.getServerPort() + "\n");
			resp.append("PLAYERCOUNT " + m.getServer().getOnlinePlayers().length + "\n");
			resp.append("MAXPLAYERS " + m.getMaxPlayers() + "\n");
			resp.append("PLAYERLIST " + Arrays.toString(playerList) + "\n");

			// Send the response.
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeBytes(resp.toString());
		}

		// Handle a request, respond in JSON format.
		if (request.equalsIgnoreCase("QUERY_JSON")) {
			Minequery m = getMinequery();

			// Build the JSON response.
			StringBuilder resp = new StringBuilder();
			resp.append("{");
			resp.append("\"serverPort\":").append(m.getServerPort()).append(",");
			resp.append("\"playerCount\":").append(m.getServer().getOnlinePlayers().length).append(",");
			resp.append("\"maxPlayers\":").append(m.getMaxPlayers()).append(",");
			resp.append("\"playerList\":");
			resp.append("[");

			// Iterate through the players.
			int count = 0;
			for (Player player : m.getServer().getOnlinePlayers()) {
				resp.append("\"" + player.getName() + "\"");
				if (++count < m.getServer().getOnlinePlayers().length) {
					resp.append(",");
				}
			}

			resp.append("]");
			resp.append("}\n");

			// Send the JSON response.
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeBytes(resp.toString());
		}
		
		if(request.startsWith("QUERY_RANKS:"))
		{
			StringBuilder response = new StringBuilder();
			
			String player = request.substring(request.indexOf(":")+1);
			
			String[] allGroups = this.minequery.getPermissions().getPlayerGroups("", player);
			String[] others = new String[allGroups.length-1];
			String primary = minequery.getPermissions().getPrimaryGroup("", player);
			
			int groupIndex = 0;
			for(String g : allGroups)
			{
				if(!g.equals(primary))
				{
					others[groupIndex]=g;
					groupIndex++;
				}
			}
				
			
			response.append("{");
			response.append("\"primary\":").append("\""+primary+"\"").append(",");
			response.append("\"others\":");
			response.append("[");
			
			// Iterate through groups.
			int count = 0;
			for (String g : others) {
				response.append("\"" + g + "\"");
				if (++count < others.length) {
					response.append(",");
				}
			}
			
			response.append("]");
			response.append("}\n");
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeBytes(response.toString());
		}

		// Different requests may be introduced in the future.
	}

	/**
	 * Gets the <code>Minequery</code> parent plugin object.
	 * 
	 * @return The Minequery object
	 */
	public Minequery getMinequery() {
		return minequery;
	}
}
