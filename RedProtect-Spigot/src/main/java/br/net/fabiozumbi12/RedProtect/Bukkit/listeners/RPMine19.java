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
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RPMine19 implements Listener {

    public RPMine19() {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Loaded RPMine19...");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        ItemStack itemInHand = event.getItem();

        Location l;

        if (b != null) {
            l = b.getLocation();
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "RPPlayerListener - Is PlayerInteractEvent event. The block is " + b.getType().name());
        } else {
            l = p.getLocation();
        }

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        if (itemInHand != null && (event.getAction().name().equals("RIGHT_CLICK_BLOCK") || b == null)) {
            Material hand = itemInHand.getType();
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && hand.equals(Material.CHORUS_FRUIT) && !r.canTeleport(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {

        final Player p = e.getPlayer();
        Location lfrom = e.getFrom();
        Location lto = e.getTo();

        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)) {
            final Region rfrom = RedProtect.get().rm.getTopRegion(lfrom);
            final Region rto = RedProtect.get().rm.getTopRegion(lto);

            if (rfrom != null && !rfrom.canTeleport(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);
            }
            if (rto != null && !rto.canTeleport(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onShootBow(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity();
        Entity proj = e.getProjectile();
        List<String> Pots = RedProtect.get().config.configRoot().server_protection.deny_potions;

        if ((proj instanceof TippedArrow)) {
            TippedArrow arr = (TippedArrow) proj;
            if (Pots.contains(arr.getBasePotionData().getType().name())) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.denypotion");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLingerPotion(LingeringPotionSplashEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity().getShooter();
        Entity ent = e.getEntity();

        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is LingeringPotionSplashEvent event.");

        Region r = RedProtect.get().rm.getTopRegion(ent.getLocation());
        if (r != null && !r.canGetEffects(p)) {
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
            e.setCancelled(true);
            return;
        }

        if (RPUtil.denyPotion(e.getEntity().getItem())) {
            e.setCancelled(true);
            if (e.getEntity().getShooter() instanceof Player) {
                RedProtect.get().lang.sendMessage((Player) e.getEntity().getShooter(), RedProtect.get().lang.get("playerlistener.denypotion"));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        if (e.getItem() == null) {
            return;
        }

        Player p = e.getPlayer();
        //deny potion
        if (p == null) {
            return;
        }

        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (r != null && e.getItem().getType().equals(Material.CHORUS_FRUIT) && !r.canTeleport(p)) {
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChangeBlock(EntityChangeBlockEvent e) {

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            Block b = e.getBlock();
            Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
            if (r != null && !r.canBuild(p)) {
                RedProtect.get().lang.sendMessage(p, "blocklistener.region.cantbreak");
                e.setCancelled(true);
            }
        }
    }
}
