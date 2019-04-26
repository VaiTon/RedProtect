/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 25/04/19 07:02
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.actions.EncompassRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPContainer;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.Crops;

import java.util.ArrayList;
import java.util.List;

public class RPBlockListener implements Listener {

    private static final RPContainer cont = new RPContainer();

    public RPBlockListener() {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Loaded RPBlockListener...");
    }

    @EventHandler(ignoreCancelled = true)
    public void onDispenser(BlockDispenseEvent e) {
        if (RPUtil.denyPotion(e.getItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignPlace(SignChangeEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "BlockListener - Is SignChangeEvent event!");

        Block b = e.getBlock();
        Player p = e.getPlayer();

        if (b == null) {
            this.setErrorSign(e, p, RedProtect.get().lang.get("blocklistener.block.null"));
            return;
        }

        Region signr = RedProtect.get().rm.getTopRegion(b.getLocation());

        if (signr != null && !signr.canPlaceSign(p)) {
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantinteract");
            e.setCancelled(true);
            return;
        }

        String[] lines = e.getLines();
        String line1 = lines[0];

        if (lines.length != 4) {
            this.setErrorSign(e, p, RedProtect.get().lang.get("blocklistener.sign.wronglines"));
            return;
        }

        if (RedProtect.get().config.configRoot().server_protection.sign_spy.enabled && !(lines[0].isEmpty() && lines[1].isEmpty() && lines[2].isEmpty() && lines[3].isEmpty())) {
            Bukkit.getConsoleSender().sendMessage(RedProtect.get().lang.get("blocklistener.signspy.location").replace("{x}", "" + b.getX()).replace("{y}", "" + b.getY()).replace("{z}", "" + b.getZ()).replace("{world}", b.getWorld().getName()));
            Bukkit.getConsoleSender().sendMessage(RedProtect.get().lang.get("blocklistener.signspy.player").replace("{player}", e.getPlayer().getName()));
            Bukkit.getConsoleSender().sendMessage(RedProtect.get().lang.get("blocklistener.signspy.lines12").replace("{line1}", lines[0]).replace("{line2}", lines[1]));
            Bukkit.getConsoleSender().sendMessage(RedProtect.get().lang.get("blocklistener.signspy.lines34").replace("{line3}", lines[2]).replace("{line4}", lines[3]));
            if (!RedProtect.get().config.configRoot().server_protection.sign_spy.only_console) {
                for (Player play : Bukkit.getOnlinePlayers()) {
                    if (play.hasPermission("redprotect.signspy")/* && !play.equals(p)*/) {
                        play.sendMessage(RedProtect.get().lang.get("blocklistener.signspy.location").replace("{x}", "" + b.getX()).replace("{y}", "" + b.getY()).replace("{z}", "" + b.getZ()).replace("{world}", b.getWorld().getName()));
                        play.sendMessage(RedProtect.get().lang.get("blocklistener.signspy.player").replace("{player}", e.getPlayer().getName()));
                        play.sendMessage(RedProtect.get().lang.get("blocklistener.signspy.lines12").replace("{line1}", lines[0]).replace("{line2}", lines[1]));
                        play.sendMessage(RedProtect.get().lang.get("blocklistener.signspy.lines34").replace("{line3}", lines[2]).replace("{line4}", lines[3]));
                    }
                }
            }
        }

        if ((RedProtect.get().config.configRoot().private_cat.use && b.getType().equals(Material.WALL_SIGN))) {
            boolean out = RedProtect.get().config.configRoot().private_cat.allow_outside;
            if (cont.validatePrivateSign(e.getLines())) {
                if (out || signr != null) {
                    if (cont.isContainer(b)) {
                        int length = p.getName().length();
                        if (length > 16) {
                            length = 16;
                        }
                        e.setLine(1, p.getName().substring(0, length));
                        RedProtect.get().lang.sendMessage(p, "blocklistener.container.protected");
                        return;
                    } else {
                        RedProtect.get().lang.sendMessage(p, "blocklistener.container.notprotected");
                        b.breakNaturally();
                        return;
                    }
                } else {
                    RedProtect.get().lang.sendMessage(p, "blocklistener.container.notregion");
                    b.breakNaturally();
                    return;
                }
            }
        }

        if (line1.equalsIgnoreCase("[rp]")) {
            String claimmode = RedProtect.get().config.getWorldClaimType(p.getWorld().getName());
            if ((!claimmode.equalsIgnoreCase("BLOCK") && !claimmode.equalsIgnoreCase("BOTH")) && !p.hasPermission("redprotect.admin.create")) {
                this.setErrorSign(e, p, RedProtect.get().lang.get("blocklistener.region.claimmode"));
                return;
            }

            RegionBuilder rb = new EncompassRegionBuilder(e);
            if (rb.ready()) {
                Region r = rb.build();
                e.setLine(0, RedProtect.get().lang.get("blocklistener.region.signcreated"));
                e.setLine(1, r.getName());
                //RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("blocklistener.region.created").replace("{region}",  r.getName()));
                RedProtect.get().rm.add(r, RedProtect.get().getServer().getWorld(r.getWorld()));
            }
        } else if (RedProtect.get().config.configRoot().region_settings.enable_flag_sign && line1.equalsIgnoreCase("[flag]") && signr != null) {
            if (signr.getFlags().containsKey(lines[1])) {
                String flag = lines[1];
                if (!(signr.getFlags().get(flag) instanceof Boolean)) {
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.region.sign.cantflag"));
                    b.breakNaturally();
                    return;
                }
                if (RedProtect.get().ph.hasPerm(p, "redprotect.flag." + flag)) {
                    if (signr.isAdmin(p) || signr.isLeader(p) || RedProtect.get().ph.hasPerm(p, "redprotect.admin.flag." + flag)) {
                        e.setLine(1, flag);
                        e.setLine(2, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + signr.getName());
                        e.setLine(3, RedProtect.get().lang.get("region.value") + " " + RedProtect.get().lang.translBool(signr.getFlagString(flag)));
                        RedProtect.get().lang.sendMessage(p, "playerlistener.region.sign.placed");
                        RedProtect.get().config.putSign(signr.getID(), b.getLocation());
                        return;
                    }
                }
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.flag.nopermregion");
                b.breakNaturally();
            } else {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.sign.invalidflag");
                b.breakNaturally();
            }
        }
    }

    private void setErrorSign(SignChangeEvent e, Player p, String error) {
        e.setLine(0, RedProtect.get().lang.get("regionbuilder.signerror"));
        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("regionbuilder.signerror") + ": " + error);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "BlockListener - Is BlockPlaceEvent event!");

        Player p = e.getPlayer();
        Block b = e.getBlockPlaced();
        World w = p.getWorld();
        Material m = null;
        if (e.getItemInHand() != null) {
            m = e.getItemInHand().getType();
        }

        boolean antih = RedProtect.get().config.configRoot().region_settings.anti_hopper;
        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());

        if (!RedProtect.get().ph.hasPerm(p, "redprotect.bypass") && antih && m != null &&
                (m.equals(Material.HOPPER) || m.name().contains("RAIL"))) {
            int x = b.getX();
            int y = b.getY();
            int z = b.getZ();
            Block ib = w.getBlockAt(x, y + 1, z);
            if (!cont.canBreak(p, ib) || !cont.canBreak(p, b)) {
                RedProtect.get().lang.sendMessage(p, "blocklistener.container.chestinside");
                e.setCancelled(true);
                return;
            }
        }

        if (r == null && canPlaceList(p.getWorld(), b.getType().name())) {
            return;
        }

        if (r != null) {

            if (m != null && !r.canPlaceVehicle(p) && (m.name().contains("MINECART") || m.name().contains("BOAT"))) {
                RedProtect.get().lang.sendMessage(p, "blocklistener.region.cantplace");
                e.setCancelled(true);
                return;
            }

            if ((b.getType().name().equals("MOB_SPAWNER") || b.getType().name().equals("SPAWNER")) && r.canPlaceSpawner(p)) {
                return;
            }

            if (!r.canBuild(p) && !r.canPlace(b.getType())) {
                RedProtect.get().lang.sendMessage(p, "blocklistener.region.cantbuild");
                e.setCancelled(true);
            }
        }
    }

    private boolean canPlaceList(World w, String type) {
        //blacklist
        List<String> blt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.place_blocks.blacklist;
        if (blt.stream().anyMatch(type::matches)) return false;

        //whitelist
        List<String> wlt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.place_blocks.whitelist;
        if (!wlt.isEmpty() && wlt.stream().noneMatch(type::matches)) {
            return false;
        }
        return RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).build;
    }

    private boolean canBreakList(World w, String type) {
        //blacklist
        List<String> blt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.break_blocks.blacklist;
        if (blt.stream().anyMatch(type::matches)) return false;

        //whitelist
        List<String> wlt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.break_blocks.whitelist;
        if (!wlt.isEmpty() && wlt.stream().noneMatch(type::matches)) {
            return false;
        }
        return RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).build;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "BlockListener - Is BlockBreakEvent event!");

        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (RPUtil.pBorders.containsKey(p.getName()) && b != null && b.getType().equals(Material.getMaterial(RedProtect.get().config.configRoot().region_settings.border.material))) {
            RedProtect.get().lang.sendMessage(p, "blocklistener.cantbreak.borderblock");
            e.setCancelled(true);
            return;
        }

        boolean antih = RedProtect.get().config.configRoot().region_settings.anti_hopper;
        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());

        if (!RedProtect.get().ph.hasPerm(p, "redprotect.bypass")) {
            Block ib = b.getRelative(BlockFace.UP);
            if ((antih && !cont.canBreak(p, ib)) || !cont.canBreak(p, b)) {
                RedProtect.get().lang.sendMessage(p, "blocklistener.container.breakinside");
                e.setCancelled(true);
                return;
            }
        }

        if (r == null && canBreakList(p.getWorld(), b.getType().name())) {
            return;
        }

        if (r != null && (b.getType().name().equals("MOB_SPAWNER") || b.getType().name().equals("SPAWNER")) && r.canPlaceSpawner(p)) {
            return;
        }

        if (r != null && !r.canBuild(p) && !r.canTree(b) && !r.canMining(b) && !r.canCrops(b) && !r.canBreak(b.getType())) {
            RedProtect.get().lang.sendMessage(p, "blocklistener.region.cantbuild");
            e.setCancelled(true);
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "BlockListener - Is PlayerInteractEvent event!");

        Player p = e.getPlayer();
        Location l = e.getClickedBlock().getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l);

        Block b = p.getLocation().getBlock();
        if (r != null &&
                (b instanceof Crops
                        || b.getType().equals(Material.PUMPKIN_STEM)
                        || b.getType().equals(Material.MELON_STEM)
                        || b.getType().toString().contains("CROPS")
                        || b.getType().toString().contains("SOIL")
                        || b.getType().toString().contains("CHORUS_")
                        || b.getType().toString().contains("BEETROOT_")
                        || b.getType().toString().contains("SUGAR_CANE")) && !r.canCrops(b) && !r.canBuild(p)) {
            RedProtect.get().lang.sendMessage(p, "blocklistener.region.cantbreak");
            e.setCancelled(true);
            return;
        }

        Block relative = e.getClickedBlock().getRelative(e.getBlockFace());
        if (relative.getType().equals(Material.FIRE)) {
            Region r1 = RedProtect.get().rm.getTopRegion(relative.getLocation());
            if (r1 != null && !r1.canBuild(e.getPlayer())) {
                e.setCancelled(true);
                RedProtect.get().lang.sendMessage(e.getPlayer(), "playerlistener.region.cantinteract");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is BlockListener - EntityExplodeEvent event");
        List<Block> toRemove = new ArrayList<>();
        if (e.getEntity() == null) {
            return;
        }
        Region or = RedProtect.get().rm.getTopRegion(e.getEntity().getLocation());
        for (Block b : e.blockList()) {
            if (b == null) {
                continue;
            }
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "Blocks: " + b.getType().name());
            Location l = b.getLocation();
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.canFire() || !cont.canWorldBreak(b)) {
                RedProtect.get().logger.debug(LogLevel.DEFAULT, "canWorldBreak Called!");
                //e.setCancelled(true);
                toRemove.add(b);
                continue;
            }

            if (r == null) {
                continue;
            }

            if (r != or) {
                toRemove.add(b);
                continue;
            }

            if (e.getEntity() instanceof LivingEntity && !r.canMobLoot()) {
                toRemove.add(b);
            }
        }
        if (!toRemove.isEmpty()) {
            e.blockList().removeAll(toRemove);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFrameBrake(HangingBreakByEntityEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is BlockListener - HangingBreakByEntityEvent event");

        Entity remover = e.getRemover();
        Entity ent = e.getEntity();
        Location l = e.getEntity().getLocation();

        if ((ent instanceof ItemFrame || ent instanceof Painting) && remover instanceof Monster) {
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.canMobLoot()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFrameBrake(HangingBreakEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is BlockListener - HangingBreakEvent event");

        Entity ent = e.getEntity();
        Location l = e.getEntity().getLocation();

        if ((ent instanceof ItemFrame || ent instanceof Painting) && (e.getCause().toString().equals("EXPLOSION"))) {
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.canFire()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockStartBurn(BlockIgniteEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is BlockIgniteEvent event");

        Block b = e.getBlock();
        Block bignit = e.getIgnitingBlock();
        if (b == null) {
            return;
        }

        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (r != null && !r.canFire()) {
            if (e.getIgnitingEntity() != null) {
                if (e.getIgnitingEntity() instanceof Player) {
                    Player p = (Player) e.getIgnitingEntity();
                    if (!r.canBuild(p)) {
                        RedProtect.get().lang.sendMessage(p, "blocklistener.region.cantplace");
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    e.setCancelled(true);
                    return;
                }
            }

            if (bignit != null && (bignit.getType().equals(Material.FIRE) || bignit.getType().name().contains("LAVA"))) {
                e.setCancelled(true);
                return;
            }
            if (e.getCause().equals(IgniteCause.LIGHTNING) || e.getCause().equals(IgniteCause.EXPLOSION) || e.getCause().equals(IgniteCause.FIREBALL)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is BlockBurnEvent event");

        Block b = e.getBlock();

        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (r != null && !r.canFire()) {
            e.setCancelled(true);
            return;
        }

        if (!cont.canWorldBreak(b)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlow(BlockFromToEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is BlockFromToEvent event");

        Block bto = e.getToBlock();
        Block bfrom = e.getBlock();
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is BlockFromToEvent event is to " + bto.getType().name() + " from " + bfrom.getType().name());
        Region rto = RedProtect.get().rm.getTopRegion(bto.getLocation());
        Region rfrom = RedProtect.get().rm.getTopRegion(bfrom.getLocation());
        boolean isLiquid = bfrom.isLiquid() || bfrom.getType().name().contains("BUBBLE_COLUMN") || bfrom.getType().name().contains("KELP");
        if (rto != null && isLiquid && !rto.canFlow()) {
            e.setCancelled(true);
            return;
        }
        if (rfrom != null && isLiquid && !rfrom.canFlow()) {
            e.setCancelled(true);
            return;
        }
        if (rto != null && !bto.isEmpty() && !rto.canFlowDamage()) {
            e.setCancelled(true);
            return;
        }

        //deny blocks spread in/out regions
        if (rfrom != null && rto != null && rfrom != rto && !rfrom.sameLeaders(rto)) {
            e.setCancelled(true);
            return;
        }
        if (rfrom == null && rto != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLightning(LightningStrikeEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is LightningStrikeEvent event");
        Location l = e.getLightning().getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l);
        if (r != null && !r.canFire()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is BlockSpreadEvent event");

        Block bfrom = e.getSource();
        Block bto = e.getBlock();
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is BlockSpreadEvent event, source is " + bfrom.getType().name());
        Region rfrom = RedProtect.get().rm.getTopRegion(bfrom.getLocation());
        Region rto = RedProtect.get().rm.getTopRegion(bto.getLocation());
        if ((e.getNewState().getType().equals(Material.FIRE) || e.getNewState().getType().name().contains("LAVA")) && rfrom != null && !rfrom.canFire()) {
            e.setCancelled(true);
            return;
        }

        if ((e.getNewState().getType().equals(Material.VINE) || e.getNewState().getType().name().contains("SEAGRASS")) && ((rfrom != null && !rfrom.canGrow()) || (rto != null && !rto.canGrow()))) {
            e.setCancelled(true);
            return;
        }

        //deny blocks spread in/out regions
        if (rfrom != null && rto != null && rfrom != rto && !rfrom.sameLeaders(rto)) {
            e.setCancelled(true);
            return;
        }
        if (rfrom == null && rto != null) {
            e.setCancelled(true);
            return;
        }
        if (rfrom != null && rto == null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is StructureGrowEvent event");
        if (!RedProtect.get().config.configRoot().region_settings.deny_structure_bypass_regions) {
            return;
        }
        Region rfrom = RedProtect.get().rm.getTopRegion(e.getLocation());
        for (BlockState bstt : e.getBlocks()) {
            Region rto = RedProtect.get().rm.getTopRegion(bstt.getLocation());
            Block bloc = bstt.getLocation().getBlock();
            //deny blocks spread in/out regions
            if (rfrom != null && rto != null && rfrom != rto && !rfrom.sameLeaders(rto)) {
                bstt.setType(bloc.getType());
            }
            if (rfrom == null && rto != null) {
                bstt.setType(bloc.getType());
            }
            if (rfrom != null && rto == null) {
                bstt.setType(bloc.getType());
            }
            bstt.update();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleBreak(VehicleDestroyEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is VehicleDestroyEvent event");

        if (!(e.getAttacker() instanceof Player)) {
            return;
        }
        Vehicle cart = e.getVehicle();
        Player p = (Player) e.getAttacker();
        Region r = RedProtect.get().rm.getTopRegion(cart.getLocation());

        if (r != null && !r.canPlaceVehicle(p)) {
            RedProtect.get().lang.sendMessage(p, "blocklistener.region.cantbreak");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is BlockPistonExtendEvent event");
        if (RedProtect.get().config.configRoot().performance.disable_PistonEvent_handler) {
            return;
        }

        Block piston = e.getBlock();
        List<Block> blocks = e.getBlocks();
        Region pr = RedProtect.get().rm.getTopRegion(piston.getLocation());
        boolean antih = RedProtect.get().config.configRoot().region_settings.anti_hopper;
        World w = e.getBlock().getWorld();
        for (Block b : blocks) {
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "BlockPistonExtendEvent event - Block: " + b.getType().name());
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "BlockPistonExtendEvent event - Relative: " + b.getRelative(e.getDirection()).getType().name());
            Region br = RedProtect.get().rm.getTopRegion(b.getRelative(e.getDirection()).getLocation());
            if (pr == null && br != null || (pr != null && br != null && pr != br && !pr.sameLeaders(br))) {
                e.setCancelled(true);
                return;
            }
            if (antih) {
                int x = b.getX();
                int y = b.getY();
                int z = b.getZ();
                Block ib = w.getBlockAt(x, y + 1, z);
                if (!cont.canWorldBreak(ib) || !cont.canWorldBreak(b)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is BlockPistonRetractEvent event");
        if (RedProtect.get().config.configRoot().performance.disable_PistonEvent_handler) {
            return;
        }

        World w = e.getBlock().getWorld();
        boolean antih = RedProtect.get().config.configRoot().region_settings.anti_hopper;
        Block piston = e.getBlock();
        if (!Bukkit.getBukkitVersion().startsWith("1.8.") && !Bukkit.getBukkitVersion().startsWith("1.9.")) {
            Block b = e.getRetractLocation().getBlock();
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "BlockPistonRetractEvent not 1.8 event - Block: " + b.getType().name());
            Region pr = RedProtect.get().rm.getTopRegion(piston.getLocation());
            Region br = RedProtect.get().rm.getTopRegion(b.getLocation());
            if (pr == null && br != null || (pr != null && br != null && pr != br && !pr.sameLeaders(br))) {
                e.setCancelled(true);
                return;
            }
            if (antih) {
                int x = b.getX();
                int y = b.getY();
                int z = b.getZ();
                Block ib = w.getBlockAt(x, y + 1, z);
                if (!cont.canWorldBreak(ib) || !cont.canWorldBreak(b)) {
                    e.setCancelled(true);
                }
            }
        } else {
            List<Block> blocks = e.getBlocks();
            Region pr = RedProtect.get().rm.getTopRegion(piston.getLocation());
            for (Block b : blocks) {
                RedProtect.get().logger.debug(LogLevel.DEFAULT, "BlockPistonRetractEvent 1.8 event - Block: " + b.getType().name());
                Region br = RedProtect.get().rm.getTopRegion(b.getLocation());
                if (pr == null && br != null || (pr != null && br != null && pr != br && !pr.sameLeaders(br))) {
                    e.setCancelled(true);
                    return;
                }
                if (antih) {
                    int x = b.getX();
                    int y = b.getY();
                    int z = b.getZ();
                    Block ib = w.getBlockAt(x, y + 1, z);
                    if (!cont.canWorldBreak(ib) || !cont.canWorldBreak(b)) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeafDecay(LeavesDecayEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is LeavesDecayEvent event");

        Region r = RedProtect.get().rm.getTopRegion(e.getBlock().getLocation());
        if (r != null && !r.canLeavesDecay()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is BlockGrowEvent event: " + event.getNewState().getType().name());

        Region r = RedProtect.get().rm.getTopRegion(event.getBlock().getLocation());
        if (r != null && !r.canGrow()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPBlockListener - Is Blockform event!");

        BlockState b = event.getNewState();
        if (b == null) {
            return;
        }
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is Blockform event: " + b.getType().name());

        if (b.getType().equals(Material.SNOW) || b.getType().equals(Material.ICE)) {
            Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
            if (r != null && !r.canIceForm()) {
                event.setCancelled(true);
            }
        }
    }
}
