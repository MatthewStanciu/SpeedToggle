package com.hektropolis.SpeedToggle;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.logging.Level;

public class SpeedToggle extends JavaPlugin implements Listener {

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            this.getLogger().log(Level.SEVERE, "SpeedToggle requires WorldGuard 6.1 or above.");
        }

        return (WorldGuardPlugin) plugin;
    }

    public String getRegionName(Player p, Location l) { //get all regions at p location then filter out parent regions
        RegionManager rm = getWorldGuard().getRegionManager(p.getWorld());
        ApplicableRegionSet ars = rm.getApplicableRegions(l);
        LinkedList<String> parentNames = new LinkedList<>();
        LinkedList<String> regions = new LinkedList<>();
        for (ProtectedRegion region : ars) {
            String id = region.getId();
            regions.add(id);
            ProtectedRegion parent = region.getParent();
            while (parent != null) {
                parentNames.add( parent.getId());
                parent = parent.getParent();
            }
        }
        for (String name : parentNames)
            regions.remove(name);

        return regions.getFirst();
    }

    @EventHandler
    public void outOfBounds(PlayerMoveEvent e) { //disable effect if player steps out of the mall
        Player p = e.getPlayer();

        if (getRegionName(p, e.getFrom()).equals("mall")
                && getRegionName(p, e.getTo()).equals("city")
                && p.getWalkSpeed() > 0.4f) {
            p.setWalkSpeed(0.2f);
            p.sendMessage(ChatColor.RED + "Your super speed effect was removed because it can only be used in the mall.");
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("superspeed")) {
            if (!(getRegionName(p, p.getLocation()).equals("mall"))) {
                p.sendMessage(ChatColor.RED + "The superspeed command can only be used in the mall!");
                return false;
            } else {
                if (p.getWalkSpeed() < 1.0f) { // if they don't have it enabled
                    p.setWalkSpeed(1.0f);
                    p.sendMessage(ChatColor.GREEN + "You are now going super speedy!");
                }
                 else {
                    p.setWalkSpeed(0.2f);
                    p.sendMessage(ChatColor.GREEN + "You are no longer going super speedy!");
                }
            }
        }

        return true;
    }
}