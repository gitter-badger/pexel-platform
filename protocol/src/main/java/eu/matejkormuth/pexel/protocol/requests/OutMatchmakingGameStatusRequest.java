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
package eu.matejkormuth.pexel.protocol.requests;

import java.nio.ByteBuffer;
import java.util.UUID;

import eu.matejkormuth.pexel.network.AsyncRequest;
import eu.matejkormuth.pexel.network.Callback;
import eu.matejkormuth.pexel.protocol.responses.InMatchmakingStatusResponse;

public class OutMatchmakingGameStatusRequest extends AsyncRequest {
    public UUID gameId;
    
    public OutMatchmakingGameStatusRequest(
            final Callback<InMatchmakingStatusResponse> callback, final UUID gameId) {
        super(callback);
        this.gameId = gameId;
    }
    
    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.allocate(16)
                .putLong(this.gameId.getMostSignificantBits())
                .putLong(this.gameId.getLeastSignificantBits());
    }
    
    @Override
    public void fromByteBuffer(final ByteBuffer buffer) {
        this.gameId = new UUID(buffer.getLong(), buffer.getLong());
    }
    
}
