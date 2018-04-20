package src;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class BlockListener implements Listener {
	int price = 500; //Sets Price of Turret Creation
	
	@EventHandler
	public void onSignClick(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.SIGN_POST) {
				Sign sign = (Sign) e.getClickedBlock().getState();
				Player p = (Player) e.getPlayer();
				// ----------------------------------------------- Tests For People Attempting to place a turret sign & Confirms person is willing to pay to place sign
				if (sign.getLine(0).equalsIgnoreCase("[turret]")) {
					sign.setLine(0, ChatColor.GOLD + "[Turret]");
					sign.setLine(1, ChatColor.RED + "Purchase For:");
					sign.setLine(2, "$" + price);
					sign.setLine(3, "Click to Confirm");
					sign.update();
				}
				// ----------------------------------------------- Sets Correct Text on sign
				else if (sign.getLine(1).equalsIgnoreCase(ChatColor.RED + "Purchase For:")) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "eco take " + p.getName() + " " + price);
					Location loc = (Location)sign.getLocation().add(0, -1, 0);
					loc.getBlock().setType(Material.LAPIS_BLOCK);
					sign.setLine(0, ChatColor.GRAY + "[Turret]");
					sign.setLine(1, ChatColor.GREEN + "-Enabled-");
					sign.setLine(2, "");
					sign.setLine(3, ChatColor.stripColor(p.getName()));
					sign.update();
				} else {
				}
			} else {
			}
		} else {
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = (Player) e.getPlayer();
		if (e.getBlock().getType() == Material.LAPIS_BLOCK)
		{
			e.setCancelled(true);
			p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Error: You May Not Break Turret Base Blocks.");
		}
		else if (e.getBlock().getType() == Material.SIGN || e.getBlock().getType() == Material.SIGN_POST) {
			
			Sign sign = (Sign) e.getBlock().getState();
			if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Turret]")
					&& !sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName())) && !p.isOp()) 
					
				{
					e.setCancelled(true);
					p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Error: You May Not Break a Turret You Don't Own!");
					p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Turret Owned By: " + sign.getLine(3));
				}
					else if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Turret]")
							&& sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName())))
					{
						p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " You Broke a Turret.");
						Location loc = (Location)sign.getLocation().add(0, -1, 0);
						loc.getBlock().setType(Material.AIR);
						
					}
					else if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Turret]")
							&& !sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName())) && (p.isOp()))
					{
						p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " You Broke a Turret Owned By " + sign.getLine(3));
						Location loc = (Location)sign.getLocation().add(0, -1, 0);
						loc.getBlock().setType(Material.AIR);
						
					}
				}
			}
	
@EventHandler
public void onBlockPlace(BlockPlaceEvent e) {
	Player p = (Player) e.getPlayer();
	if (e.getBlock().getType() == Material.LAPIS_BLOCK)
	{
		e.setCancelled(true);
		p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Error: You May Not Place Turret Base Blocks.");
	}
}

	

}






