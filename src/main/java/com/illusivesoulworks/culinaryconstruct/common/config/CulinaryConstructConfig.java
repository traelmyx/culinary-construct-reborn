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

package com.illusivesoulworks.culinaryconstruct.common.config;

import com.illusivesoulworks.culinaryconstruct.CulinaryConstructConstants;
import com.illusivesoulworks.culinaryconstruct.api.CulinaryConstructApi;
import com.illusivesoulworks.culinaryconstruct.api.ICulinaryIngredient;
import com.illusivesoulworks.culinaryconstruct.platform.Services;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public class CulinaryConstructConfig {

  public static final ModConfigSpec serverSpec;
  public static final Server SERVER;
  private static final String CONFIG_PREFIX =
      "gui." + CulinaryConstructConstants.MOD_ID + ".config.";

  static {
    final Pair<Server, ModConfigSpec> specPair = new ModConfigSpec.Builder()
        .configure(Server::new);
    serverSpec = specPair.getRight();
    SERVER = specPair.getLeft();
  }

  public static class Server {

    public final ModConfigSpec.IntValue maxFoodPerSandwich;
    public final ModConfigSpec.DoubleValue maxIngredientSaturation;
    public final ModConfigSpec.IntValue maxIngredientFood;
    public final ModConfigSpec.ConfigValue<List<? extends String>> ingredientBlacklist;
    public final ModConfigSpec.BooleanValue showNutritionInfo;

    public Server(ModConfigSpec.Builder builder) {

      maxFoodPerSandwich = builder
          .comment("Maximum amount of food from eating a single sandwich.")
          .translation(CONFIG_PREFIX + "maxFoodPerSandwich")
          .defineInRange("maxFoodPerSandwich", 10, 1, 100);

      maxIngredientSaturation = builder.comment(
              "If greater than -1, players cannot use ingredients with greater saturation than this amount.")
          .translation(CONFIG_PREFIX + "maxIngredientSaturation")
          .defineInRange("maxIngredientSaturation", -1.0D, -1.0D, 100.0D);

      maxIngredientFood = builder
          .comment(
              "If greater than -1, players cannot use ingredients with greater food than this amount.")
          .translation(CONFIG_PREFIX + "maxIngredientFood")
          .defineInRange("maxIngredientFood", -1, -1, 100);

      ingredientBlacklist = builder.comment("Items or tags that cannot be used as ingredients.")
          .translation(CONFIG_PREFIX + "ingredientBlacklist")
          .defineList("ingredientBlacklist", new ArrayList<>(),
              s -> s instanceof String str && ResourceLocation.tryParse(str) != null);

      showNutritionInfo = builder.comment(
              "If enabled, shows food and saturation amounts in the extended tooltip.")
          .translation(CONFIG_PREFIX + "showNutritionInfo").define("showNutritionInfo", false);
    }
  }

  private static final List<Item> BLACKLIST = new ArrayList<>();
  private static boolean ingredientsInitialized = false;


  public static void reload() {
    ingredientsInitialized = false;
  }

  public static boolean isValidIngredient(ItemStack stack) {
    Item item = stack.getItem();
    FoodProperties food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
    Optional<ICulinaryIngredient> culinary = CulinaryConstructApi.getCulinaryIngredient(stack);
    int foodAmount = 0;
    float saturationAmount = 0;

    if (culinary.isPresent()) {
      foodAmount = culinary.map(ICulinaryIngredient::getFoodAmount).orElse(0);
      saturationAmount = culinary.map(ICulinaryIngredient::getSaturation).orElse(0.0F);
    } else if (food != null) {
      foodAmount = food.nutrition();
      saturationAmount = (food.nutrition() > 0 ? food.saturation() / (food.nutrition() * 2.0F) : 0.0F);
    }
    int maxFood = CulinaryConstructConfig.SERVER.maxIngredientFood.get();
    double maxSaturation = CulinaryConstructConfig.SERVER.maxIngredientSaturation.get();

    if (!ingredientsInitialized) {
      ingredientsInitialized = true;
      BLACKLIST.clear();

      for (String s : CulinaryConstructConfig.SERVER.ingredientBlacklist.get()) {
        Services.REGISTRY.getItem(ResourceLocation.tryParse(s)).ifPresent(BLACKLIST::add);
      }
    }
    boolean blacklisted = false;

    if (!BLACKLIST.isEmpty()) {
      blacklisted = BLACKLIST.contains(item);
    }
    return (maxFood < 0 || foodAmount <= maxFood) && (maxSaturation < 0
        || saturationAmount <= maxSaturation) && !blacklisted;
  }
}
