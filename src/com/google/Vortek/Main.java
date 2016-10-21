package com.google.Vortek;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.bukkit.metadata.FixedMetadataValue;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.MovecraftLocation;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {

    private int priceCreate = 500;
    private final static int RANGEDEFAULT = 100;
    private final double VERSION = 1.1; // Version
    private final String AUTHOR = "Senarii"; // Author
    private Location locEnd, locStart;
    private CraftManager craftManager;
    private Block targetBlock; //Location of Selected Target

    public void onEnable() {
        PluginManager pm = this.getServer().getPluginManager(); // Define Plugin
        // Manager
        pm.registerEvents(this, this); // Registers
        // Blocklistener.java
        craftManager = CraftManager.getInstance();
    }

    public void onDisable() {

    }

    public final Block getTargetedBlock(Player player, int range) 
    {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() == Material.AIR) {
                continue;
            }
            break;
        }
        return lastBlock;
    }

    //
    // Tests If Target is Within Range
    //
    public final static int getDistance(Location loc1, Location loc2)  
    {
        int distance = (int) Math.sqrt(Math.pow(((int)loc2.getX()-(int)loc1.getX()), 2) + Math.pow(((int)loc2.getY()-(int)loc1.getY()), 2) + Math.pow(((int)loc2.getY()-(int)loc1.getY()), 2));
        return (int)distance;
    }

    
    public static void fireProjectile(Location startLoc, Location endLoc)
    {

    	if (Math.abs(startLoc.getY()-endLoc.getY()) <=10)
    	{
    		Vector vector = endLoc.toVector().subtract(startLoc.toVector());
    		Arrow snowball = startLoc.getWorld().spawn(startLoc, Arrow.class);
    		snowball.setVelocity(vector.normalize().multiply(3));
    		snowball.setGlowing(true);
    		startLoc.getWorld().playEffect(startLoc, Effect.EXPLOSION, 51);
    	}
    	else
    	{
    		startLoc.getWorld().playEffect(startLoc, Effect.EXPLOSION, 51);
    	}
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { // Plugin
        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("turretsigns") && sender instanceof Player) {
            player.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " TurretSigns by " + AUTHOR
                + " is Enabled. Version: " + VERSION);
            return true;
        }
        if (command.getName().equalsIgnoreCase("turretenable") && sender instanceof Player) {
            player.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Automatic Turrets Enabled.");
            fireProjectile(locStart, locEnd);
            return true;

        }
        if (command.getName().equalsIgnoreCase("turretdisable") && sender instanceof Player) {
            player.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Automatic Turrets Disabled.");
            return true;
        } 
        if (command.getName().equalsIgnoreCase("target") && sender instanceof Player) {
            targetBlock = getTargetedBlock(player, 300);
            locEnd = targetBlock.getLocation();

            player.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Target Selected. X:" + locEnd.getX()
                + " Y:" + locEnd.getY() + " Z:" + locEnd.getZ());
            return true;

        }
        return false;

    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.SIGN_POST) {
                Sign sign = (Sign) e.getClickedBlock().getState();
                Player p = (Player) e.getPlayer();
                // ----------------------------------------------- Tests For
                // People Attempting to place a turret sign & Confirms person is
                // willing to pay to place sign
                if (sign.getLine(0).equalsIgnoreCase("[turret]")) {
                    sign.setLine(0, ChatColor.GOLD + "[Turret]");
                    sign.setLine(1, ChatColor.RED + "Purchase For:");
                    sign.setLine(2, "$" + priceCreate);
                    sign.setLine(3, "Click to Confirm");
                    sign.update();
                }
                // ----------------------------------------------- Sets Correct
                // Text on sign
                else if (sign.getLine(1).equalsIgnoreCase(ChatColor.RED + "Purchase For:")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                        "eco take " + p.getName() + " " + priceCreate);
                    Location loc = (Location) sign.getLocation().add(0, -1, 0);
                    loc.getBlock().setType(Material.LAPIS_BLOCK);
                    sign.setLine(0, ChatColor.GRAY + "[Turret]");
                    sign.setLine(1, ChatColor.RED + "-Disabled-");
                    sign.setLine(2, ""+RANGEDEFAULT);
                    sign.setLine(3, ChatColor.stripColor(p.getName()));
                    sign.update();
                } 
                else if (sign.getLine(1).equalsIgnoreCase(ChatColor.RED + "-Disabled-") && sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName()))) { //Enable Turret Sign
                    locStart = (Location) sign.getLocation().add(0, 1, 0);
                    sign.setLine(0, ChatColor.GRAY + "[Turret]");
                    sign.setLine(1, ChatColor.GREEN + "-Enabled-");
                    sign.update();
                }
                else if (sign.getLine(1).equalsIgnoreCase(ChatColor.GREEN + "-Enabled-") && sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName()))) { //Disable Turret Sign
                    sign.setLine(0, ChatColor.GRAY + "[Turret]");
                    sign.setLine(1, ChatColor.RED + "-Disabled-");
                    locStart.setX(0);
                    locStart.setY(0);
                    locStart.setZ(0);
                    sign.update();
                }
                else {
                }
            } else {
            }
        } else {
        }
    }
    //                       //
    //                       //
    //    Block Protection   //
    //                       //
    //                       //
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = (Player) e.getPlayer();
        if (e.getBlock().getType() == Material.LAPIS_BLOCK) {
            e.setCancelled(true);
            p.sendMessage(
                ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Error: You May Not Break Turret Base Blocks.");
        } else if (e.getBlock().getType() == Material.SIGN || e.getBlock().getType() == Material.SIGN_POST) {

            Sign sign = (Sign) e.getBlock().getState();
            if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Turret]")
            && !sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName())) && !p.isOp())

            {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY
                    + " Error: You May Not Break a Turret You Don't Own!");
                p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Turret Owned By: " + sign.getLine(3));
            } else if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Turret]")
            && sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName()))) {
                p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " You Broke a Turret.");
                Location loc = (Location) sign.getLocation().add(0, -1, 0);
                loc.getBlock().setType(Material.AIR);

            } else if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Turret]")
            && !sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName())) && (p.isOp())) {
                p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " You Broke a Turret Owned By "
                    + sign.getLine(3));
                Location loc = (Location) sign.getLocation().add(0, -1, 0);
                loc.getBlock().setType(Material.AIR);

            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = (Player) e.getPlayer();
        if (e.getBlock().getType() == Material.LAPIS_BLOCK) {
            e.setCancelled(true);
            p.sendMessage(
                ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Error: You May Not Place Turret Base Blocks.");
        }
    }
    //                       //
    //                       //
    //    Target Selection   //
    //                       //
    //                       //

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player p = (Player) e.getPlayer();
        if (p.getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD && e.getAction() == Action.RIGHT_CLICK_AIR) { //Sets Target
            targetBlock = getTargetedBlock(p, 300);
            locEnd = targetBlock.getLocation();
            p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Target Selected. X:" + locEnd.getX()
            + " Y:" + locEnd.getY() + " Z:" + locEnd.getZ() + " Distance: " + getDistance(locStart, locEnd));
        }

        if (p.getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD && e.getAction() == Action.LEFT_CLICK_AIR) { //Fires Turret
            fireProjectile(locStart, locEnd);
        }
    }    

    
    /*//Movecraft Integration - WIP 
    @EventHandler
    public void onClick(PlayerInteractEvent e) {
    Player p = (Player) e.getPlayer();

    if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD && e.getAction() == Action.RIGHT_CLICK_AIR) {
    p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Blaze Rod Right Clicked");
    Craft craft = craftManager.getCraftByPlayer(p);
    if (craftManager.getCraftByPlayer(p) != null)
    {
    p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Craft Detected!");
    //                                                         //
    // --- Tests For Signs On Craft after Craft is Detected    //
    //                                                         //
    List<Location> signLocations = new ArrayList<Location>();
    for( MovecraftLocation blockLoc : craftManager.getCraftByPlayer(p).getBlockList())
    {
    Sign sign;
    for(Location signLocation :   signLocations ){
    sign = (Sign) p.getWorld().getBlockAt(signLocation).getState();
    if(sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Turret]") && sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName())))
    {
    p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Sign Detected With [Turret]!");
    sign.setMetadata("turretAim", new FixedMetadataValue(this, sign.getLocation()));
    }
    }

    //p.sendMessage(ChatColor.RED + "[Turrets] " + ChatColor.GRAY + blockLoc.getX()+ " " + blockLoc.getY()+ " " +blockLoc.getZ() + " " + craft.getW().getBlockAt(blockLoc.getX(),blockLoc.getY(),blockLoc.getZ()).getType());
    if(p.getWorld().getBlockAt(blockLoc.getX(),blockLoc.getY(),blockLoc.getZ()).getType().equals(Material.SIGN_POST)) 
    {
    p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Sign Detected With *");
    signLocations.add(new Location(p.getWorld(), blockLoc.getX(),blockLoc.getY(),blockLoc.getZ()));
    //sign = (Sign)p.getWorld().getBlockAt(blockLoc.getX(),blockLoc.getY(),blockLoc.getZ());
    //if(sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Turret]") && sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName())))
    // {
    // 	p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Sign Detected With [Turret]!");
    //      sign.setMetadata("turretAim", new FixedMetadataValue(this, sign.getLocation()));
    //   }

    }	
    }
    Block block = getTargetedBlock(p, 100);
    locStart = p.getLocation();
    locEnd = block.getLocation();
    //p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Target Selected. X:" + locEnd.getX() + " Y:"
    //+ locEnd.getY() + " Z:" + locEnd.getZ());
    }

    }
    if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD && e.getAction() == Action.LEFT_CLICK_AIR) {
    Sign sign;
    p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Blaze Rod Left Clicked");
    Craft craft = craftManager.getCraftByPlayer(p);
    if (craftManager.getCraftByPlayer(p) != null)
    {
    p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Craft Detected!");
    for( MovecraftLocation blockLoc : craftManager.getCraftByPlayer(p).getBlockList())
    {
    //p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " For Loop Works!");
    if(p.getWorld().getBlockAt(blockLoc.getX(),blockLoc.getY(),blockLoc.getZ()) !=null && (p.getWorld().getBlockAt(blockLoc.getX(),blockLoc.getY(),blockLoc.getZ()).getType() == Material.SIGN_POST))
    {
    sign = (Sign)p.getWorld().getBlockAt(blockLoc.getX(),blockLoc.getY(),blockLoc.getZ());
    if(sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Turret]") && sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName())))
    {
    if (sign.hasMetadata("turretAim"))
    {
    sign.getMetadata("turretAim").get(sign.getMetadata("turretAim").size()-1).value();
    fireProjectile(sign.getLocation(), locEnd);
    p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Firing Projectile!!");
    }
    }
    }
    }	
    }
    }
    }
     */    
}
