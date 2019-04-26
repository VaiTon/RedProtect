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

package br.net.fabiozumbi12.RedProtect.Bukkit.config;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Core.config.GuiLangCore;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class LangGuiManager extends GuiLangCore {

    public LangGuiManager() {
        String resLang = "gui" + RedProtect.get().config.configRoot().language + ".properties";
        pathLang = RedProtect.get().getDataFolder() + File.separator + resLang;

        File lang = new File(pathLang);
        if (!lang.exists()) {
            if (RedProtect.get().getResource("assets/redprotect/" + resLang) == null) {
                resLang = "guiEN-US.properties";
                pathLang = RedProtect.get().getDataFolder() + File.separator + resLang;
            }
            RPUtil.saveResource("/assets/redprotect/" + resLang, null, new File(RedProtect.get().getDataFolder(), resLang));
            RedProtect.get().logger.info("Created GUI language file: " + pathLang);
        }

        loadLang();
        loadBaseLang();
        updateLang();
    }

    private void loadLang() {
        loadDefaultLang();

        if (loadedLang.get("_lang.version") != null) {
            int langv = Integer.parseInt(loadedLang.get("_lang.version").toString().replace(".", ""));
            int rpv = Integer.parseInt(RedProtect.get().getDescription().getVersion().replace(".", ""));
            if (RedProtect.get().getDescription().getVersion().length() > loadedLang.get("_lang.version").toString().length()) {
                langv = Integer.parseInt(loadedLang.get("_lang.version").toString().replace(".", "") + 0);
            }
            if (langv < rpv || langv == 0) {
                loadedLang.put("_lang.version", RedProtect.get().getDescription().getVersion());
            }
        }
    }

    private void updateLang() {
        if (updateLang(RedProtect.get().getDescription().getVersion())){
            RedProtect.get().logger.warning("- Removed invalid entries from GUI language files");
        }
    }

    public String getFlagName(String flag) {
        return ChatColor.translateAlternateColorCodes('&', getRaw("gui.flags." + flag + ".name"));
    }

    public List<String> getFlagDescription(String flag) {
        return Arrays.asList(ChatColor.translateAlternateColorCodes('&', getRaw("gui.flags." + flag + ".description")).split("/n"));
    }

    public String getFlagString(String key) {
        return ChatColor.translateAlternateColorCodes('&', getRaw("gui.strings." + key));
    }
}
