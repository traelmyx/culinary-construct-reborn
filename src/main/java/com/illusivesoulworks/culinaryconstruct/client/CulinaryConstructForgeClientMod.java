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

package com.illusivesoulworks.culinaryconstruct.client;

import com.illusivesoulworks.culinaryconstruct.CulinaryConstructConstants;
import com.illusivesoulworks.culinaryconstruct.client.model.FoodBowlLoader;
import com.illusivesoulworks.culinaryconstruct.client.model.SandwichLoader;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.bus.api.SubscribeEvent;

public class CulinaryConstructForgeClientMod {

  @SubscribeEvent
  public void registerModels(final ModelEvent.RegisterGeometryLoaders evt) {
    evt.register(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CulinaryConstructConstants.MOD_ID, CulinaryConstructConstants.SANDWICH_ID), SandwichLoader.INSTANCE);
    evt.register(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CulinaryConstructConstants.MOD_ID, CulinaryConstructConstants.FOOD_BOWL_ID), FoodBowlLoader.INSTANCE);
  }

  public static int getFluidColor(Fluid fluid) {
    return IClientFluidTypeExtensions.of(fluid).getTintColor();
  }

  public static net.minecraft.core.HolderLookup.Provider getRegistries() {
    var level = net.minecraft.client.Minecraft.getInstance().level;
    return level != null ? level.registryAccess() : null;
  }
}
