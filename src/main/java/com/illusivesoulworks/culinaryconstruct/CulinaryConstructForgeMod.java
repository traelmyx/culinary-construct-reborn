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


package com.illusivesoulworks.culinaryconstruct;

import com.illusivesoulworks.culinaryconstruct.client.CulinaryConstructForgeClientMod;
import com.illusivesoulworks.culinaryconstruct.client.CulinaryStationScreen;
import com.illusivesoulworks.culinaryconstruct.common.advancement.CraftFoodTrigger;
import com.illusivesoulworks.culinaryconstruct.common.capability.CulinaryIngredientCapability;
import com.illusivesoulworks.culinaryconstruct.common.config.CulinaryConstructConfig;
import com.illusivesoulworks.culinaryconstruct.common.item.FoodBowlItem;
import com.illusivesoulworks.culinaryconstruct.common.item.SandwichItem;
import com.illusivesoulworks.culinaryconstruct.common.network.CulinaryConstructForgeNetwork;
import com.illusivesoulworks.culinaryconstruct.common.registry.CulinaryConstructRegistry;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CulinaryConstructConstants.MOD_ID)
public class CulinaryConstructForgeMod {

  private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
      DeferredRegister.create(Registries.TRIGGER_TYPE, CulinaryConstructConstants.MOD_ID);

  static {
    TRIGGERS.register("craft_food", () -> CraftFoodTrigger.INSTANCE);
  }

  public CulinaryConstructForgeMod(IEventBus eventBus, ModContainer container, Dist dist) {
    CulinaryConstructMod.setup();
    container.registerConfig(ModConfig.Type.SERVER, CulinaryConstructConfig.serverSpec);
    eventBus.addListener((ModConfigEvent evt) -> {

      if (evt.getConfig().getSpec() == CulinaryConstructConfig.serverSpec) {
        CulinaryConstructConfig.reload();
      }
    });
    TRIGGERS.register(eventBus);
    eventBus.addListener(CulinaryIngredientCapability::register);
    eventBus.addListener(CulinaryConstructForgeNetwork::register);
    eventBus.addListener(this::creativeTabs);

    if (dist.isClient()) {
      eventBus.register(new CulinaryConstructForgeClientMod());
      eventBus.addListener(this::registerScreens);
    }
  }

  private void registerScreens(final RegisterMenuScreensEvent evt) {
    evt.register(CulinaryConstructRegistry.CULINARY_STATION_MENU.get(), CulinaryStationScreen::new);
  }

  private void creativeTabs(final BuildCreativeModeTabContentsEvent evt) {
    ResourceKey<CreativeModeTab> tab = evt.getTabKey();

    if (tab == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
      evt.accept(CulinaryConstructRegistry.CULINARY_STATION_ITEM.get());
    } else if (tab == CreativeModeTabs.FOOD_AND_DRINKS) {
      evt.accept(SandwichItem.generateCreativeItem());
      evt.accept(FoodBowlItem.generateCreativeItem());
    }
  }
}
