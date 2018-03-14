package brawl.snaxv2.warz19;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class lastDamager {
	public String getLastDamager(Player player) {
		  List<MetadataValue> values = player.getMetadata("lastDamager");  
		  for (MetadataValue value : values) {
		     if (value.getOwningPlugin() == Bukkit.getPluginManager().getPlugin("brawlGuns")) {
		    	 return value.asString();
		     }
		  }
		  return player.getName();
		}
}
