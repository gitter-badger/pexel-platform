// @formatter:off
/*
 * Pexel Project - Minecraft minigame server platform. 
 * Copyright (C) 2014 Matej Kormuth <http://www.matejkormuth.eu>
 * 
 * This file is part of Pexel.
 * 
 * Pexel is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Pexel is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
// @formatter:on
package eu.matejkormuth.pexel.master;

import eu.matejkormuth.pexel.network.ProxiedPlayer;
import eu.matejkormuth.pexel.network.ProxyBrand;

/**
 * Provider or chat on master server.
 */
public class ChatProvider extends MasterComponent {
    public void sendMessage(final ProxiedPlayer player, final String message) {
        // Currently send only throught bungee.
        if (PexelMaster.getInstance().getProxy().getBrand() == ProxyBrand.BUNGEE_CORD) {
            PexelMaster.getInstance().getProxy().sendMessage(player, message);
        }
        else {
            // TODO: Fallback throught pexel-connection.
            
        }
    }
}
