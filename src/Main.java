package src;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {

    private int priceCreateTurret = 500;
    private int priceCreateShield = 5000;
    private int price20mmSingle = 5000;
    private int price20mmQuad = 25000;
    private int price40mmSingle = 20000;
    private int price40mmDouble = 50000;
    private int price75mmAP = 80000;
    private int price75mmHE = 100000;

    private final double VERSION = 1.5; // Version
    private final String AUTHOR = "Senarii"; // Author
    HashMap<Player, Location> finalTarget = new HashMap<Player, Location>();
    HashMap<Player, Boolean> isMounted = new HashMap<Player, Boolean>();
    HashMap<Player, ArrayList<Location>> startTarget = new HashMap<Player, ArrayList<Location>>();
    ArrayList<Location> shieldGen = new ArrayList<Location>();
    ArrayList<Player> reload = new ArrayList<Player>();
    ArrayList<Player> regionList = new ArrayList<Player>();
    //CraftManager cm;

    //Runs on start
    public void onEnable() {
        PluginManager pm = this.getServer().getPluginManager(); // Define Plugin
        // Manager
        pm.registerEvents(this, this); // Registers
        // Blocklistener.java
    }

    public void onDisable() {


        for (ArrayList<Location> locList : startTarget.values()) {
            for (Location loc : locList) {
                Sign sign = (Sign) loc.clone().add(0, -1, 0).getBlock().getState();
                sign.setLine(1, ChatColor.RED + "-Disabled-");
            }
        }
    }

    public final Block getTargetedBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() == Material.AIR || lastBlock.getType() == Material.GLASS || lastBlock.getType() == Material.STAINED_GLASS || lastBlock.getType() == Material.STAINED_GLASS_PANE || lastBlock.getType() == Material.THIN_GLASS) {
                continue;
            }
            break;
        }
        return lastBlock;
    }

    //
    // Tests If Target is Within Range
    //
    public final static int getDistance(Location loc1, Location loc2) {
        int distance = (int) Math.sqrt(Math.pow(((int) loc2.getX() - (int) loc1.getX()), 2) + Math.pow(((int) loc2.getY() - (int) loc1.getY()), 2) + Math.pow(((int) loc2.getY() - (int) loc1.getY()), 2));
        return (int) distance;
    }

    //                        //
    //                        //
    //    Firing Projectile   //
    //                        //
    //                        //
    public void fireProjectile(Player p, String id) {
        if (reload.contains(p)) {
        } else {
            for (Location startPosition : startTarget.get(p.getPlayer())) {
                if (startPosition.clone().add(0, -1, 0).getBlock().getType() == Material.SIGN_POST) {
                    Sign sign = (Sign) startPosition.clone().add(0, -1, 0).getBlock().getState();
                    if (startPosition.clone().add(0, -2, 0).getBlock().getType() == Material.CHEST) {
                        Chest ammoChest = (Chest) startPosition.clone().add(0, -2, 0).getBlock().getState();

                        if (ammoChest.getBlockInventory().first(Material.TNT) != -1 && ammoChest.getBlockInventory().getItem(ammoChest.getBlockInventory().first(Material.TNT)).getAmount() >= 4 && sign.getLine(2).equalsIgnoreCase(id) || ammoChest.getBlockInventory().first(Material.TNT) != -1 && ammoChest.getBlockInventory().getItem(ammoChest.getBlockInventory().first(Material.TNT)).getAmount() >= 4 && sign.getLine(2).equalsIgnoreCase("")) {
                            ItemStack stack = ammoChest.getBlockInventory().getItem(ammoChest.getBlockInventory().first(Material.TNT)).clone();
                            stack.setAmount(4);
                            ammoChest.getBlockInventory().removeItem(stack);
                            Location targetPosition = finalTarget.get(p.getPlayer());
                            Vector vector = targetPosition.toVector().subtract(startPosition.toVector());

                            Snowball snowball = startPosition.getWorld().spawn(startPosition, Snowball.class);
                            snowball.setVelocity(vector.normalize().multiply(3));
                            snowball.setCustomName("Secondary");
                            snowball.setCustomNameVisible(false);
                            snowball.setShooter(p);
                            snowball.isOnGround();
                            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3.0F, 0.5F);
                            startPosition.getWorld().playEffect(startPosition, Effect.EXPLOSION, 5);
                            reload.add(p);
                            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                public void run() {
                                    reload.remove(p);
                                }
                            }, 100);
                        } else {
                            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 3.0F, 0.5F);
                        }
                    }
                } else {
                    startTarget.remove(p.getPlayer(), startPosition);
                }
            }
        }
    }

    //                       //
    //                       //
    //    Ingame Commands    //
    //                       //
    //                       //
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { // Plugin
        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("turretsigns") && sender instanceof Player) {
            player.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " TurretSigns by " + AUTHOR
                    + " is Enabled. Version: " + VERSION);
            return true;
        }
        if (command.getName().equalsIgnoreCase("turretdemount") && sender instanceof Player && sender.hasPermission("turretsigns.turretdemount")) {
            Player target = Bukkit.getServer().getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "[Turrets] " + "Error: Please Specify a Player to Force-Release from Their Turrets.");
                return true;
            }
            startTarget.remove(target.getPlayer());
            player.sendMessage(ChatColor.RED + "[Turrets] " + ChatColor.GRAY + target.getName() + " Has Been Force Released From Their Turrets.");
            return true;
        }
        if (command.getName().equalsIgnoreCase("beam") && sender instanceof Player && sender.hasPermission("turretsigns.beam")) {
            Bukkit.broadcastMessage(ChatColor.RED + "[Beaming] " + ChatColor.GRAY + player.getName() + " Beamed to Their Ship!");
            player.setHealth(0);
            return true;
        }
        return false;

    }

    //                        //
    //                        //
    //    Creating Explosion  //
    //                        //
    //                        //
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSecondaryHitTarget(ProjectileHitEvent e) { //Creates Explosion Where Snowball Lands
        if (e.getEntity() instanceof Snowball) {
            Snowball snowball = (Snowball) e.getEntity();
            Location loc = snowball.getLocation();
            Player p = (Player) snowball.getShooter();
            if (snowball.getName().equalsIgnoreCase("secondary")/* && testForShield(loc, p) */ && snowball.getTicksLived() >= 10) {
                snowball.getWorld().createExplosion(loc, 4.0F);
            } else if (!snowball.getName().equalsIgnoreCase("secondary")/* && testForShield(loc, p)*/) {
                snowball.getLocation().getBlock().setType(Material.WATER);
                snowball.getLocation().getBlock().setType(Material.AIR);
            }

        }

    }


    //                       //
    //                       //
    //    Sign Creation      // 
    //                       //
    //                       //

    @EventHandler
    public void onFlakClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() == Material.WALL_SIGN || e.getClickedBlock().getType() == Material.SIGN_POST) {
                Sign sign = (Sign) e.getClickedBlock().getState();
                Player p = e.getPlayer();

                if (sign.getLine(1).equalsIgnoreCase(ChatColor.RED + "20mm Flak")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "eco take " + p.getName() + " "
                            + price20mmSingle);
                    sign.setLine(0, "[20mm Flak]");
                    sign.setLine(1, "[-------------]");
                    sign.setLine(2, ChatColor.DARK_RED + "Mounted");
                    sign.setLine(3, ChatColor.DARK_RED + "Turret");
                    setDelay(e.getPlayer());
                    sign.update();
                } else if (sign.getLine(1).equalsIgnoreCase(ChatColor.RED + "20mm Quad Flak")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "eco take " + p.getName() + " "
                            + price20mmQuad);
                    sign.setLine(0, "[20mm Quad Flak]");
                    sign.setLine(1, "[-------------]");
                    sign.setLine(2, ChatColor.DARK_RED + "Mounted");
                    sign.setLine(3, ChatColor.DARK_RED + "Turret");
                    setDelay(e.getPlayer());
                    sign.update();
                } else if (sign.getLine(1).equalsIgnoreCase(ChatColor.RED + "40mm Flak")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "eco take " + p.getName() + " "
                            + price40mmSingle);
                    sign.setLine(0, "[40mm Flak]");
                    sign.setLine(1, "[-------------]");
                    sign.setLine(2, ChatColor.DARK_RED + "Mounted");
                    sign.setLine(3, ChatColor.DARK_RED + "Turret");
                    setDelay(e.getPlayer());
                    sign.update();
                } else if (sign.getLine(1).equalsIgnoreCase(ChatColor.RED + "40mm Dual Flak")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "eco take " + p.getName() + " "
                            + price40mmDouble);
                    sign.setLine(0, "[40mm Dual Flak]");
                    sign.setLine(1, "[-------------]");
                    sign.setLine(2, ChatColor.DARK_RED + "Mounted");
                    sign.setLine(3, ChatColor.DARK_RED + "Turret");
                    setDelay(e.getPlayer());
                    sign.update();
                } else if (sign.getLine(1).equalsIgnoreCase(ChatColor.RED + "75mm AP Cannon")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "eco take " + p.getName() + " "
                            + price75mmAP);
                    sign.setLine(0, "[75mm DP Gun]");
                    sign.setLine(1, "[-------------]");
                    sign.setLine(2, ChatColor.DARK_RED + "Mounted");
                    sign.setLine(3, ChatColor.DARK_RED + "Turret");
                    setDelay(e.getPlayer());
                    sign.update();
                } else if (sign.getLine(1).equalsIgnoreCase(ChatColor.RED + "75mm HE Cannon")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "eco take " + p.getName() + " "
                            + price75mmHE);
                    sign.setLine(0, "[75mm HE Gun]");
                    sign.setLine(1, "[-------------]");
                    sign.setLine(2, ChatColor.DARK_RED + "Mounted");
                    sign.setLine(3, ChatColor.DARK_RED + "Turret");
                    setDelay(e.getPlayer());
                    sign.update();
                }
                //
                // Mounting Turrets
                //
                if (sign.getLine(0).contains("[20mm Flak]") && !isMounted.containsKey(p) && p.getLocation().getBlock().getType() != Material.STATIONARY_WATER && p.getLocation().getBlock().getType() != Material.WATER) {
                    isMounted.put(p, true);
                    p.addPotionEffect((new PotionEffect(PotionEffectType.JUMP, 1000000, 200, true)));
                    p.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 1000000, 10, true)));
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "minecraft:tp " + p.getName() + " " + sign.getX() + " " + sign.getY() + " " + sign.getZ());
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "shot give " + p.getName() + " 20mmFlak");
                } else if (sign.getLine(0).contains("[20mm Quad Flak]") && !isMounted.containsKey(p) && p.getLocation().getBlock().getType() != Material.STATIONARY_WATER && p.getLocation().getBlock().getType() != Material.WATER) {
                    isMounted.put(p, true);
                    p.addPotionEffect((new PotionEffect(PotionEffectType.JUMP, 1000000, 200, true)));
                    p.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 1000000, 10, true)));
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "minecraft:tp " + p.getName() + " " + sign.getX() + " " + sign.getY() + " " + sign.getZ());
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "shot give " + p.getName() + " 20mmQuadFlak");
                } else if (sign.getLine(0).contains("[40mm Flak]") && !isMounted.containsKey(p) && p.getLocation().getBlock().getType() != Material.STATIONARY_WATER && p.getLocation().getBlock().getType() != Material.WATER) {
                    isMounted.put(p, true);
                    p.addPotionEffect((new PotionEffect(PotionEffectType.JUMP, 1000000, 200, true)));
                    p.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 1000000, 10, true)));
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "minecraft:tp " + p.getName() + " " + sign.getX() + " " + sign.getY() + " " + sign.getZ());
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "shot give " + p.getName() + " 40mmFlak");
                } else if (sign.getLine(0).contains("[40mm Dual Flak]") && !isMounted.containsKey(p) && p.getLocation().getBlock().getType() != Material.STATIONARY_WATER && p.getLocation().getBlock().getType() != Material.WATER) {
                    isMounted.put(p, true);
                    p.addPotionEffect((new PotionEffect(PotionEffectType.JUMP, 1000000, 200, true)));
                    p.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 1000000, 10, true)));
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "minecraft:tp " + p.getName() + " " + sign.getX() + " " + sign.getY() + " " + sign.getZ());
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "shot give " + p.getName() + " 40mmDualFlak");
                } else if (sign.getLine(0).contains("[75mm DP Gun]") && !isMounted.containsKey(p) && p.getLocation().getBlock().getType() != Material.STATIONARY_WATER && p.getLocation().getBlock().getType() != Material.WATER) {
                    isMounted.put(p, true);
                    p.addPotionEffect((new PotionEffect(PotionEffectType.JUMP, 1000000, 200, true)));
                    p.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 1000000, 10, true)));
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "minecraft:tp " + p.getName() + " " + sign.getX() + " " + sign.getY() + " " + sign.getZ());
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "shot give " + p.getName() + " 75mmArtillery");
                } else if (sign.getLine(0).contains("[75mm HE Gun]") && !isMounted.containsKey(p) && p.getLocation().getBlock().getType() != Material.STATIONARY_WATER && p.getLocation().getBlock().getType() != Material.WATER) {
                    isMounted.put(p, true);
                    p.addPotionEffect((new PotionEffect(PotionEffectType.JUMP, 1000000, 200, true)));
                    p.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 1000000, 10, true)));
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "minecraft:tp " + p.getName() + " " + sign.getX() + " " + sign.getY() + " " + sign.getZ());
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "shot give " + p.getName() + " 75mmArtilleryHE");
                }


            }
        }
    }

    //Prevents Drinking Milk to Heal Potion From Turret
    @EventHandler
    public void onMilkDrink(PlayerInteractEvent e) {
        if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.MILK_BUCKET) {
            if (isMounted.containsKey(e.getPlayer())) {
                e.setCancelled(true);
            }

        }
    }

    //Prevents Dropping of Turret
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (isMounted.containsKey(e.getPlayer()) && e.getItemDrop().getItemStack().getType() == Material.IRON_HOE) {
            e.setCancelled(true);
        }
    }

    //Prevents Players Picking up Turret After Death
    @EventHandler
    public void onTurretPickup(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();
        Material droppeditem = (Material) e.getItem().getItemStack().getType();
        if (!isMounted.containsKey(e.getPlayer()) && droppeditem == (Material.IRON_HOE)) {
            e.setCancelled(true);
        }
    }

    //Prevent Moving Turret in Inventory
    @EventHandler
    public void onTurretMove(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem().getType() != null && e.getCurrentItem().getType() != Material.AIR && e.getCurrentItem().getType() == Material.IRON_HOE) {
            e.setCancelled(true);
        }
    }


    //Demount Turret
    @EventHandler
    public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
        Player p = event.getPlayer();
        if (p.isSneaking()) {
            if (isMounted.containsKey(p)) {
                isMounted.remove(p);
                p.removePotionEffect(PotionEffectType.JUMP);
                p.removePotionEffect(PotionEffectType.SLOW);
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "minecraft:clear " + p.getName() + " "
                        + "minecraft:iron_hoe");
            }
        }
    }


    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.SIGN_POST) {
                Sign sign = (Sign) e.getClickedBlock().getState();
                Player p = (Player) e.getPlayer();
                if (sign.getLine(0).equalsIgnoreCase(ChatColor.GOLD + "[Turret]")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                            "eco take " + p.getName() + " " + priceCreateTurret);
                    Location loc = (Location) sign.getLocation().add(0, -1, 0);
                    loc.getBlock().setType(Material.CHEST);
                    sign.setLine(0, ChatColor.GRAY + "[Turret]");
                    sign.setLine(1, ChatColor.RED + "-Disabled-");
                    sign.setLine(3, ChatColor.stripColor(p.getName()));
                    sign.update();
                } else if (sign.getLine(1).equalsIgnoreCase(ChatColor.RED + "-Disabled-") && sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName()))) { //Enable Turret Sign
                    if (!startTarget.containsKey(p.getPlayer()))
                        startTarget.put(p.getPlayer(), new ArrayList<Location>());

                    startTarget.get(p.getPlayer()).add(sign.getLocation().add(0, 1, 0));

                    sign.setLine(0, ChatColor.GRAY + "[Turret]");
                    sign.setLine(1, ChatColor.GREEN + "-Enabled-");
                    sign.update();
                } else if (sign.getLine(1).equalsIgnoreCase(ChatColor.GREEN + "-Enabled-") && sign.getLine(3).equalsIgnoreCase(ChatColor.stripColor(p.getName()))) { //Disable Turret Sign
                    sign.setLine(0, ChatColor.GRAY + "[Turret]");
                    sign.setLine(1, ChatColor.RED + "-Disabled-");
                    startTarget.get(p.getPlayer()).remove(sign.getLocation().add(0, 1, 0));
                    sign.update();
                }
            }
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
        if (e.getBlock().getType() == Material.CHEST) {
            Location signLoc = e.getBlock().getLocation().add(0, 1, 0);
            if (signLoc.getBlock().getType() == Material.SIGN_POST) {
                Sign sign = (Sign) signLoc.getBlock().getState();
                if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Turret]") || sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Shield]")) {
                    e.setCancelled(true);
                    p.sendMessage(
                            ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Error: You May Not Break " + sign.getLine(0) + " Chests.");
                }
            }
        } else if (e.getBlock().getType() == Material.SIGN || e.getBlock().getType() == Material.SIGN_POST) {

            Sign sign = (Sign) e.getBlock().getState();
            //
            //Turret Protection
            //
            if (sign.getLine(1).equalsIgnoreCase(ChatColor.GREEN + "-Enabled-")) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY
                        + " Error: You May Not Break Enabled Turrets!");
            } else if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[Turret]")
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

    //                       //
    //                       //
    //    Target Selection   //
    //                       //
    //                       //

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player p = (Player) e.getPlayer();
        if (p.getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD && e.getAction() == Action.RIGHT_CLICK_AIR) { //Sets Target
            Block targetBlock = getTargetedBlock(p, 300);
            finalTarget.put(p.getPlayer(), targetBlock.getLocation());
            p.sendMessage(ChatColor.RED + "[Turrets]" + ChatColor.GRAY + " Target Selected.");
        }

        if (p.getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD && e.getAction() == Action.LEFT_CLICK_AIR) { //Fires Turret
            String id = "";
            if (p.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) {
                id = p.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
            }
            fireProjectile(p.getPlayer(), id);
        }
    }

    //                       //
    //                       //
    //    Initial Creation   //
    //                       //
    //                       //
    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if (e.getLine(0).equalsIgnoreCase("[turret]")) { //For Creating Turrets
            e.setLine(0, ChatColor.GOLD + "[Turret]");
            e.setLine(1, ChatColor.RED + "Price: " + priceCreateTurret);
            e.setLine(3, "Click to Confirm");
        }

        if (e.getLine(0).equalsIgnoreCase("[20mm]")) { //For 20mm Flak Turrets
            e.setLine(0, ChatColor.GOLD + "[Purchase]");
            e.setLine(1, ChatColor.RED + "20mm Flak");
            e.setLine(2, "Price:");
            e.setLine(3, "" + price20mmSingle);
        }
        if (e.getLine(0).equalsIgnoreCase("[20mm Quad]")) { //For 20mm Quad Flak Turrets
            e.setLine(0, ChatColor.GOLD + "[Purchase]");
            e.setLine(1, ChatColor.RED + "20mm Quad Flak");
            e.setLine(2, "Price:");
            e.setLine(3, "" + price20mmQuad);
        }
        if (e.getLine(0).equalsIgnoreCase("[40mm]")) { //For 40mm Flak Turrets
            e.setLine(0, ChatColor.GOLD + "[Purchase]");
            e.setLine(1, ChatColor.RED + "40mm Flak");
            e.setLine(2, "Price:");
            e.setLine(3, "" + price40mmSingle);
        }
        if (e.getLine(0).equalsIgnoreCase("[40mm Dual]")) { //For 40mm Double Flak Turrets
            e.setLine(0, ChatColor.GOLD + "[Purchase]");
            e.setLine(1, ChatColor.RED + "40mm Dual Flak");
            e.setLine(2, "Price:");
            e.setLine(3, "" + price40mmDouble);
        }
        if (e.getLine(0).equalsIgnoreCase("[75mm AP]")) { //For 75mm AP Flak Turrets
            e.setLine(0, ChatColor.GOLD + "[Purchase]");
            e.setLine(1, ChatColor.RED + "75mm AP Cannon");
            e.setLine(2, "Price:");
            e.setLine(3, "" + price75mmAP);
        }
        if (e.getLine(0).equalsIgnoreCase("[75mm HE]")) { //For 75mm HE Flak Turrets
            e.setLine(0, ChatColor.GOLD + "[Purchase]");
            e.setLine(1, ChatColor.RED + "75mm HE Cannon");
            e.setLine(2, "Price:");
            e.setLine(3, "" + price75mmHE);
        }
        // Prevents Direct Turret Creation
        if (e.getLine(0).contains("[20mm Flak]")) {
            e.setCancelled(true);
        }
        if (e.getLine(0).contains("[20mm Quad Flak]")) {
            e.setCancelled(true);
        }
        if (e.getLine(0).contains("[40mm Flak]")) {
            e.setCancelled(true);
        }
        if (e.getLine(0).contains("[40mm Dual Flak]")) {
            e.setCancelled(true);
        }
        if (e.getLine(0).contains("[75mm DP Gun]")) {
            e.setCancelled(true);
        }
        if (e.getLine(0).contains("[75mm HE Gun]")) {
            e.setCancelled(true);
        }
    }

    public void setDelay(Player p) {
        isMounted.put(p, true);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                isMounted.remove(p);
            }
        }, 10);
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (e.getDeathMessage().contains("died")) {
            e.setDeathMessage(null);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerChatEvent(PlayerChatEvent e) {
        String message = e.getMessage();
        if (message.length() > 0 && message.charAt(0) != '/' && message.contains("@") && message.length() > 1 && message.charAt(0) != '@') {
            String playerMentionLong = message.substring(message.indexOf("@") + 1, message.length()) + " ";
            String playerMentionName = playerMentionLong.substring(0, playerMentionLong.indexOf(" "));
            if (Bukkit.getServer().getPlayer(playerMentionName) instanceof Player && playerMentionName.length() >= 3) {
                Player named = (Player) Bukkit.getServer().getPlayer(playerMentionName);
                named.getWorld().playSound(named.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 2);
                named.sendMessage(ChatColor.RED + e.getMessage());
                e.setCancelled(true);
            }
        }

    }
}


