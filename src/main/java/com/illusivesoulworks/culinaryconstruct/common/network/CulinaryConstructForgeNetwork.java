/*
 * Copyright (C) 2018-2022 Illusive Soulworks
 *
 * Culinary Construct is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Culinary Construct is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Culinary Construct.  If not, see <https://www.gnu.org/licenses/>.
 */


package com.illusivesoulworks.culinaryconstruct.common.network;

import com.illusivesoulworks.culinaryconstruct.CulinaryConstructConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class CulinaryConstructForgeNetwork {

  private static final String PTC_VERSION = "1";

  public record Rename(CPacketRename message) implements CustomPacketPayload {

    public static final Type<Rename> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(CulinaryConstructConstants.MOD_ID, "rename"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Rename> CODEC = StreamCodec.of(
        (buf, packet) -> CPacketRename.encode(packet.message(), buf),
        buf -> new Rename(CPacketRename.decode(buf)));

    @Override
    public Type<Rename> type() {
      return TYPE;
    }
  }

  public static void register(final RegisterPayloadHandlersEvent evt) {
    evt.registrar(CulinaryConstructConstants.MOD_ID).versioned(PTC_VERSION).optional()
        .playToServer(Rename.TYPE, Rename.CODEC, (packet, context) -> context.enqueueWork(() -> {
          if (context.player() instanceof ServerPlayer sender) {
            CPacketRename.handle(packet.message(), sender);
          }
        }));
  }

  public static void sendToServer(CPacketRename msg) {
    PacketDistributor.sendToServer(new Rename(msg));
  }
}
