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

package br.net.fabiozumbi12.RedProtect.Sponge.hooks;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.actions.DefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.region.RegionBuilder;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.sponge.SpongeWorld;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class WEHook {

    private static final HashMap<String, EditSession> eSessions = new HashMap<>();

    public static boolean undo(String rid) {
        if (eSessions.containsKey(rid)) {
            eSessions.get(rid).undo(eSessions.get(rid));
            return true;
        }
        return false;
    }

    private static void setSelection(SpongeWorld ws, Player p, Location pos1, Location pos2) {
        RegionSelector regs = SpongeWorldEdit.inst().getSession(p).getRegionSelector(ws);
        regs.selectPrimary(BlockVector3.at(pos1.getX(), pos1.getY(), pos1.getZ()), null);
        regs.selectSecondary(BlockVector3.at(pos2.getX(), pos2.getY(), pos2.getZ()), null);
        SpongeWorldEdit.inst().getSession(p).setRegionSelector(ws, regs);
        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.select-we.show")
                .replace("{pos1}", pos1.getBlockX() + "," + pos1.getBlockY() + "," + pos1.getBlockZ())
                .replace("{pos2}", pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ())
        );
        SpongeWorldEdit.inst().getSession(p).dispatchCUISelection(SpongeWorldEdit.inst().wrapPlayer(p));
    }

    public static void setSelectionRP(Player p, Location pos1, Location pos2) {
        SpongeWorld ws = SpongeWorldEdit.inst().getWorld(p.getWorld());
        setSelection(ws, p, pos1, pos2);
    }

    public static void setSelectionFromRP(Player p, Location pos1, Location pos2) {
        SpongeWorldEdit worldEdit = SpongeWorldEdit.inst();
        SpongeWorld ws = SpongeWorldEdit.inst().getWorld(p.getWorld());
        if (worldEdit.getSession(p) == null || !worldEdit.getSession(p).isSelectionDefined(ws)) {
            setSelection(ws, p, pos1, pos2);
        } else {
            worldEdit.getSession(p).getRegionSelector(ws).clear();
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.select-we.hide"));
        }
        worldEdit.getSession(p).dispatchCUISelection(worldEdit.wrapPlayer(p));
    }

    public static Region pasteWithWE(Player p, File file) {
        Location<World> loc = p.getLocation();
        Region r = null;

        if (p.getLocation().getBlockRelative(Direction.DOWN).getBlock().getType().equals(BlockTypes.WATER) ||
                p.getLocation().getBlockRelative(Direction.DOWN).getBlock().getType().equals(BlockTypes.FLOWING_WATER)) {
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.needground");
            return null;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.copyfail");
            return null;
        }
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();


            BlockVector3 bmin = clipboard.getMinimumPoint();
            BlockVector3 bmax = clipboard.getMaximumPoint();

            Location<World> min = loc.add(bmin.getX(), bmin.getY(), bmin.getZ());
            Location<World> max = loc.add(bmax.getX(), bmax.getY(), bmax.getZ());

            String leader = p.getUniqueId().toString();
            if (!RedProtect.get().config.configRoot().online_mode) {
                leader = p.getName().toLowerCase();
            }

            String regionName = RPUtil.regionNameConform("", p);
            RegionBuilder rb2 = new DefineRegionBuilder(p, min, max, regionName, leader, new HashSet<>(), false);
            if (rb2.ready() && rb2.build().getArea() > 1) {
                r = rb2.build();
            }

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().
                    getEditSession(SpongeWorldEdit.inst().getWorld(p.getWorld()), -1)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return r;
    }

    public static void regenRegion(final Region region, final World world, final Location<World> p1, final Location<World> p2, final int delay, final CommandSource sender, final boolean remove) {
        Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
            if (RPUtil.stopRegen) {
                return;
            }

            RegionSelector selector = new LocalSession().getRegionSelector(SpongeWorldEdit.inst().getWorld(world));
            selector.selectPrimary(BlockVector3.at(p1.getX(), p1.getY(), p1.getZ()), null);
            selector.selectSecondary(BlockVector3.at(p2.getX(), p2.getY(), p2.getZ()), null);

            com.sk89q.worldedit.regions.Region wreg = null;
            try {
                wreg = selector.getRegion();
            } catch (IncompleteRegionException e1) {
                e1.printStackTrace();
            }

            EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(SpongeWorldEdit.inst().getWorld(world), -1);

            eSessions.put(region.getID(), session);
            int delayCount = 1 + delay / 10;

            if (sender != null) {
                if (SpongeWorldEdit.inst().getWorld(world).regenerate(wreg, session)) {
                    RedProtect.get().lang.sendMessage(sender, "[" + delayCount + "]" + " &aRegion " + region.getID().split("@")[0] + " regenerated with success!");
                } else {
                    RedProtect.get().lang.sendMessage(sender, "[" + delayCount + "]" + " &cTheres an error when regen the region " + region.getID().split("@")[0] + "!");
                }
            } else {
                if (SpongeWorldEdit.inst().getWorld(world).regenerate(wreg, session)) {
                    RedProtect.get().logger.warning("[" + delayCount + "]" + " &aRegion " + region.getID().split("@")[0] + " regenerated with success!");
                } else {
                    RedProtect.get().logger.warning("[" + delayCount + "]" + " &cTheres an error when regen the region " + region.getID().split("@")[0] + "!");
                }
            }

            if (remove) {
                RedProtect.get().getServer().getWorld(region.getWorld()).ifPresent(regionWorld -> {
                    region.notifyRemove();
                    RedProtect.get().rm.remove(region, regionWorld);
                });
            }

            if (delayCount % 50 == 0) {
                RedProtect.get().rm.saveAll(true);
            }

            if (RedProtect.get().config.configRoot().purge.regen.stop_server_every > 0 && delayCount > RedProtect.get().config.configRoot().purge.regen.stop_server_every) {
                Sponge.getScheduler().getScheduledTasks(RedProtect.get().container).forEach(Task::cancel);
                RedProtect.get().rm.saveAll(false);

                Sponge.getServer().shutdown();
            }

        }, delay, TimeUnit.MILLISECONDS);
    }
}
