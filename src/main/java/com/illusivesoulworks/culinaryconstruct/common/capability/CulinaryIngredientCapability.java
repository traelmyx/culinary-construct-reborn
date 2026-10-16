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


package com.illusivesoulworks.culinaryconstruct.common.capability;

import com.illusivesoulworks.culinaryconstruct.api.CulinaryConstructCapabilities;
import com.illusivesoulworks.culinaryconstruct.api.ICulinaryIngredient;
import com.mojang.datafixers.util.Pair;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CulinaryIngredientCapability {

  public static void register(final RegisterCapabilitiesEvent evt) {

    for (Item item : BuiltInRegistries.ITEM) {

      for (Pair<Predicate<Item>, Function<ItemStack, ICulinaryIngredient>> entry : CulinaryIngredients.getDefaults()) {

        if (entry.getFirst().test(item)) {
          Function<ItemStack, ICulinaryIngredient> factory = entry.getSecond();
          evt.registerItem(CulinaryConstructCapabilities.CULINARY_INGREDIENT,
              (stack, context) -> factory.apply(stack), item);
          break;
        }
      }
    }
  }
}
