package brawl.snaxv2.warz19;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import net.md_5.bungee.api.ChatColor;


public class main extends JavaPlugin implements Listener{
    public void openConnection() throws SQLException, ClassNotFoundException {
	    if (connection != null && !connection.isClosed()) {
	        return;
	    }
	 
	    synchronized (this) {
	        if (connection != null && !connection.isClosed()) {
	            return;
	        }
	        Class.forName("com.mysql.jdbc.Driver");
	        connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
	    }
    }
    
    public Connection connection;
    public String host, database, username, password;
    public int port;
    @Override
    public void onEnable() {
        host = "localhost";
        port = 3306;
        database = "brawl";
        username = "root";
        password = "haxor";  
    	Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }
    @Override
    public void onDisable() {}
	@EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
		//map boundaries move the player back unless he is an Operator
		if (!event.getPlayer().isOp()) {
			if (event.getPlayer().getLocation().getX()<-1379) {
				event.getPlayer().teleport(new Location(event.getPlayer().getWorld(), -1379, event.getPlayer().getLocation().getY(), event.getPlayer().getLocation().getZ()));
			}
			if (event.getPlayer().getLocation().getX()>-798) {
				event.getPlayer().teleport(new Location(event.getPlayer().getWorld(), -798, event.getPlayer().getLocation().getY(), event.getPlayer().getLocation().getZ()));
			}
			if (event.getPlayer().getLocation().getZ()<80) {
				event.getPlayer().teleport(new Location(event.getPlayer().getWorld(), event.getPlayer().getLocation().getX(), event.getPlayer().getLocation().getY(), 80));
			}
			if (event.getPlayer().getLocation().getZ()>572) {
				event.getPlayer().teleport(new Location(event.getPlayer().getWorld(), event.getPlayer().getLocation().getX(), event.getPlayer().getLocation().getY(), 572));
			}
			
		}
		//scoreboard aka sidebar
		metaData meta = new metaData();
		canbeDamaged dam = new canbeDamaged();
		if (event.getPlayer().getLocation().distance(event.getPlayer().getWorld().getSpawnLocation())>12) {
			dam.canbeDamaged(event.getPlayer(), 1);
		}else {
			dam.canbeDamaged(event.getPlayer(), 0);
		}
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		 
		Objective objective = board.registerNewObjective("test", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.WHITE + "Stats");
		 
		Score kills = objective.getScore(ChatColor.GREEN + "Kills: "); //Get a fake offline player
		kills.setScore((int)meta.getMetadata(event.getPlayer(), "kills"));
		Score deaths = objective.getScore(ChatColor.GREEN + "Deaths: "); //Get a fake offline player
		deaths.setScore((int)meta.getMetadata(event.getPlayer(), "deaths"));
		Score gems = objective.getScore(ChatColor.GREEN + "Gems: "); //Get a fake offline player
		gems.setScore((int)meta.getMetadata(event.getPlayer(), "gems"));
		event.getPlayer().setScoreboard(board);
    }
	@EventHandler
	public void onSignClick(PlayerInteractEvent e) {
		//the spawn warp signs. Booring stuff
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN) {
			    Sign sign = (Sign) e.getClickedBlock().getState();
			    if(sign.getLine(0).contains("Warp - ")) {
			    	e.getPlayer().teleport(new Location(e.getPlayer().getWorld(), Integer.valueOf(sign.getLine(1)), Integer.valueOf(sign.getLine(2)), Integer.valueOf(sign.getLine(3))));
			    }
			}
		  }
	}
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
		final Player p = event.getPlayer();
		final metaData meta = new metaData();
		BukkitRunnable getStats = new BukkitRunnable() {
			   @Override
			   public void run() {
			      try {
			         openConnection();
			         Statement statsQuery = connection.createStatement();
			         ResultSet stats = statsQuery.executeQuery("SELECT * FROM warz19profile WHERE UUID = '"+p.getUniqueId().toString()+"';");
			         int kills = -1;
			         int deaths = 0;
			         int gems = 0;
			         if (stats != null) {
			        	 while(stats.next()) {
			        		 kills = stats.getInt("KILLS");
			        		 deaths = stats.getInt("DEATHS");
			        		 gems = stats.getInt("gems");
			        	 }
			         }
			         //if the player isnt in the database, add him to it
			         if (kills == -1) {
    			         Statement statsUpdate= connection.createStatement();
    			         statsUpdate.executeUpdate("INSERT INTO warz19profile (UUID, KILLS, DEATHS, gems) VALUES ('"+p.getUniqueId().toString()+"', 0, 0, 0);");
    			         kills = 0;
			         }	
			 		meta.setMetadata(p, "kills", kills);
					meta.setMetadata(p, "deaths", deaths);
					meta.setMetadata(p, "gems", gems);
			      } catch(ClassNotFoundException e) {
			         e.printStackTrace();
			      } catch(SQLException e) {
			         e.printStackTrace();
			      }
			   }
			};
			 
			getStats.runTaskAsynchronously(this);
    }
	@EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
		final Player p = event.getPlayer();
		final metaData meta = new metaData();
 		final String kills = String.valueOf(meta.getMetadata(p, "kills"));
 		final String deaths = String.valueOf(meta.getMetadata(p, "deaths"));
 		final String gems = String.valueOf(meta.getMetadata(p, "gems"));
		BukkitRunnable updateStats = new BukkitRunnable() {
			   @Override
			   public void run() {
			      try {
			         openConnection();
			         Statement deleteold = connection.createStatement();
			         deleteold.executeUpdate("DELETE FROM warz19profile WHERE UUID = '"+p.getUniqueId().toString()+"';");
			         Statement insertnew = connection.createStatement();
    			     insertnew.executeUpdate("INSERT INTO warz19profile (UUID, KILLS, DEATHS, gems) VALUES ('"+p.getUniqueId().toString()+"', "+kills+", "+deaths+", "+gems+");");
			      } catch(ClassNotFoundException e) {
			         e.printStackTrace();
			      } catch(SQLException e) {
			         e.printStackTrace();
			      }
			   }
			};
			 
			updateStats.runTaskAsynchronously(this);
    }
	@EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
		event.setDeathMessage("");
		permissionStuff perm = new permissionStuff();
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			lastDamager last = new lastDamager();
			Player shooter = Bukkit.getPlayer(last.getLastDamager(player));
			shooter.sendMessage(ChatColor.GREEN  + "You have killed: " + ChatColor.WHITE + player.getName() + ChatColor.GREEN + " - +"+2*perm.getRankId((Player) shooter) + "gems!");
			metaData meta = new metaData();
			meta.setMetadata(event.getEntity(), "deaths", (int)meta.getMetadata(event.getEntity(), "deaths")+1);
			meta.setMetadata(shooter, "kills", (int)meta.getMetadata(shooter, "kills")+1);
			meta.setMetadata(shooter, "gems", (int)meta.getMetadata(shooter, "gems")+2*perm.getRankId((Player) shooter));
		}
    }
	@EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
		event.setCancelled(true);
		permissionStuff perm = new permissionStuff();
		Bukkit.broadcastMessage("[" + perm.getRankColor(event.getPlayer()) + perm.getRankName(event.getPlayer()) + ChatColor.WHITE + "] " + event.getPlayer().getName() + " > " +  event.getMessage());
    }
}
