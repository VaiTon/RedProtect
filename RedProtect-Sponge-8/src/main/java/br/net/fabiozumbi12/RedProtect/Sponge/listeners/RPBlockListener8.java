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

package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPContainer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class RPBlockListener8 {

    private static final RPContainer cont = new RPContainer();

    public RPBlockListener8() {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "Loaded RPBlockListener 8...");
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPiston(ChangeBlockEvent.Pre e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "RPBlockListener8 - Is onChangeBlock event");

        EventContext context = e.getContext();
        Cause cause = e.getCause();
        LocatableBlock sourceLoc = cause.first(LocatableBlock.class).orElse(null);

        if (sourceLoc != null) {
            RedProtect.get().logger.debug(LogLevel.BLOCKS, "sourceLoc");

            if (context.containsKey(EventContextKeys.PISTON_EXTEND) || context.containsKey(EventContextKeys.PISTON_RETRACT)) {
                if (RedProtect.get().config.configRoot().performance.disable_PistonEvent_handler) {
                    return;
                }

                Region r = RedProtect.get().rm.getTopRegion(sourceLoc.getLocation(), this.getClass().getName());
                for (Location<World> pistonLoc : e.getLocations()) {
                    Region targetr = RedProtect.get().rm.getTopRegion(pistonLoc, this.getClass().getName());

                    boolean antih = RedProtect.get().config.configRoot().region_settings.anti_hopper;
                    RedProtect.get().logger.debug(LogLevel.BLOCKS, "getLocations");

                    if (targetr != null && (r == null || r != targetr)) {
                        if (cause.first(Player.class).isPresent() && targetr.canBuild(cause.first(Player.class).get())) {
                            continue;
                        }

                        if (antih) {
                            BlockSnapshot ib = e.getLocations().get(0).add(0, 1, 0).createSnapshot();
                            if (cont.canWorldBreak(ib) || cont.canWorldBreak(e.getLocations().get(0).createSnapshot())) {
                                continue;
                            }
                        }
                        e.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }
}
