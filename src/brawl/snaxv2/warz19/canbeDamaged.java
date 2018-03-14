package brawl.snaxv2.warz19;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class canbeDamaged {
		public boolean getState(Player object) {
		  List<MetadataValue> values = object.getMetadata("canBeShot");  
		  for (MetadataValue value : values) {
		     if (value.getOwningPlugin() == Bukkit.getPluginManager().getPlugin("brawlGuns")) {
		    	 if (value.asInt() == 1) { 
		    	 return true;
		    	 }else {
		    		 return false;
		    	 }
		     }
		  }
		  return false;
		}
	    
		public void canbeDamaged(Player player, int can) {
			  player.setMetadata("canBeShot", new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("brawlGuns"), can));
			}
}