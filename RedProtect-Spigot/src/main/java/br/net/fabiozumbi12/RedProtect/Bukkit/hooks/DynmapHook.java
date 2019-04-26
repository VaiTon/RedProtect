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

package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.events.ChangeRegionFlagEvent;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.*;

public class DynmapHook implements Listener {

    private static MarkerSet MSet;
    private static MarkerAPI MApi;

    public DynmapHook(DynmapAPI dyn) {
        MApi = dyn.getMarkerAPI();
        MSet = MApi.getMarkerSet(RedProtect.get().config.configRoot().hooks.dynmap.marks_groupname);
        if (MSet == null) {
            MSet = MApi.createMarkerSet("RedProtect", RedProtect.get().config.configRoot().hooks.dynmap.marks_groupname, null, false);
        }
        MSet.setHideByDefault(RedProtect.get().config.configRoot().hooks.dynmap.hide_by_default);
        MSet.setLayerPriority(RedProtect.get().config.configRoot().hooks.dynmap.layer_priority);
        MSet.setLabelShow(RedProtect.get().config.configRoot().hooks.dynmap.show_label);
        MSet.setDefaultMarkerIcon(MApi.getMarkerIcon(RedProtect.get().config.configRoot().hooks.dynmap.marker.get("player").marker_icon));
        int minzoom = RedProtect.get().config.configRoot().hooks.dynmap.min_zoom;
        if (minzoom > 0) {
            MSet.setMinZoom(minzoom);
        } else {
            MSet.setMinZoom(0);
        }

        //start set markers
        for (World w : RedProtect.get().getServer().getWorlds()) {
            for (Region r : RedProtect.get().rm.getRegionsByWorld(w)) {
                if (!r.allowDynmap()) continue;
                addMark(r);
            }
        }
    }

    @EventHandler
    public void onChangeFlag(ChangeRegionFlagEvent event) {
        if (event.getFlag().equalsIgnoreCase("dynmap")) {
            boolean value = (boolean) event.getFlagValue();
            if (value) {
                addMark(event.getRegion());
            } else {
                removeMark(event.getRegion());
            }
        }
    }

    public void removeAll(World w) {
        for (Region r : RedProtect.get().rm.getRegionsByWorld(w)) {
            removeMark(r);
        }
    }

    public void removeMark(Region r) {
        AreaMarker am = MSet.findAreaMarker(r.getID());
        if (am != null) {
            am.deleteMarker();
        }
        Marker m = MSet.findMarker(r.getID());
        if (m != null) {
            m.deleteMarker();
        }
    }

    public void addMark(Region r) {
        AreaMarker am = MSet.findAreaMarker(r.getID());

        double[] x = new double[4];
        double[] z = new double[4];
        int i = 0;
        for (Location l : r.get4Points(90)) {
            x[i] = l.getBlockX() + 0.500;
            z[i] = l.getBlockZ() + 0.500;
            i++;
        }

        if (am == null) {
            am = MSet.createAreaMarker(r.getID(), r.getName(), false, r.getWorld(), x, z, true);
        } else {
            am.setCornerLocations(x, z);
        }

        String rName = RedProtect.get().lang.get("region.name") + " <span style=\"font-weight:bold;\">" + r.getName() + "</span><br>";
        String area = RedProtect.get().lang.get("region.area") + " <span style=\"font-weight:bold;\">" + r.getArea() + "</span>";
        am.setDescription(ChatColor.stripColor(rName + area));

        if (RedProtect.get().config.configRoot().hooks.dynmap.show_leaders_admins) {
            String leader = RedProtect.get().lang.get("region.leaders") + " <span style=\"font-weight:bold;\">" + r.getLeadersDesc() + "</span><br>";
            String admin = RedProtect.get().lang.get("region.admins") + " <span style=\"font-weight:bold;\">" + r.getAdminDesc() + "</span><br>";
            am.setDescription(ChatColor.stripColor(rName + leader + admin + area));
        }

        int center = -1;
        if (RedProtect.get().config.configRoot().hooks.dynmap.cuboid_region.enabled) {
            am.setRangeY(r.getMinLocation().getBlockY() + 0.500, r.getMaxLocation().getBlockY() + 0.500);
        } else {
            center = RedProtect.get().config.configRoot().hooks.dynmap.cuboid_region.if_disable_set_center;
            am.setRangeY(center, center);
        }

        String type = "player";
        if (r.isLeader(RedProtect.get().config.configRoot().region_settings.default_leader))
            type = "server";

        am.setLineStyle(
                RedProtect.get().config.configRoot().hooks.dynmap.marker.get(type).border_weight,
                RedProtect.get().config.configRoot().hooks.dynmap.marker.get(type).border_opacity,
                Integer.decode(RedProtect.get().config.configRoot().hooks.dynmap.marker.get(type).border_color.replace("#", "0x")));
        am.setFillStyle(
                RedProtect.get().config.configRoot().hooks.dynmap.marker.get(type).fill_opacity,
                Integer.decode(RedProtect.get().config.configRoot().hooks.dynmap.marker.get(type).fill_color.replace("#", "0x")));


        if (RedProtect.get().config.configRoot().hooks.dynmap.show_icon) {
            Marker m = MSet.findMarker(r.getID());
            if (center == -1) {
                center = r.getCenterY();
            }

            MarkerIcon icon = MApi.getMarkerIcon(RedProtect.get().config.configRoot().hooks.dynmap.marker.get(type).marker_icon);

            if (m == null) {
                MSet.createMarker(r.getID(), r.getName(), r.getWorld(), r.getCenterX(), center, r.getCenterZ(), icon, true);
            } else {
                m.setLocation(r.getWorld(), r.getCenterX(), center, r.getCenterZ());
            }
        }
    }
}
