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

package com.illusivesoulworks.culinaryconstruct.common.util;

import com.illusivesoulworks.culinaryconstruct.platform.Services;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

public class CulinaryNBT {

  public static final String TAG_BASE = "Base";
  public static final String TAG_INGREDIENTS = "Ingredients";
  public static final String TAG_FOOD = "Food";
  public static final String TAG_SATURATION = "Saturation";
  public static final String TAG_SIZE = "Size";
  public static final String TAG_QUALITY = "Quality";
  public static final String TAG_LIQUIDS = "Liquids";
  public static final String TAG_SOLIDS = "Solids";
  public static final String TAG_SOLIDS_SIZE = "SolidsSize";
  public static final String TAG_COUNT = "Count";

  public static CompoundTag getTagSafe(ItemStack stack) {
    return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
  }

  private static void edit(ItemStack stack, Consumer<CompoundTag> editor) {
    CustomData.update(DataComponents.CUSTOM_DATA, stack, editor);
  }

  // item stacks inside the food need registry access to (de)serialize their components
  public static HolderLookup.Provider registries() {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

    if (server != null) {
      return server.registryAccess();
    }
    HolderLookup.Provider client = Services.PLATFORM.getClientRegistries();
    return client != null ? client : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
  }

  public static ItemStack getBase(ItemStack stack) {
    CompoundTag compound = getTagSafe(stack);
    return ItemStack.parseOptional(registries(), compound.getCompound(TAG_BASE));
  }

  public static int getQuality(ItemStack stack) {
    CompoundTag compound = getTagSafe(stack);
    return compound.getInt(TAG_QUALITY);
  }

  public static int getSize(ItemStack stack) {
    CompoundTag compound = getTagSafe(stack);
    return compound.getInt(TAG_SIZE);
  }

  public static int getFoodAmount(ItemStack stack) {
    CompoundTag compound = getTagSafe(stack);
    return compound.getInt(TAG_FOOD);
  }

  public static float getSaturation(ItemStack stack) {
    CompoundTag compound = getTagSafe(stack);
    return compound.getFloat(TAG_SATURATION);
  }

  public static NonNullList<ItemStack> getIngredientsList(ItemStack stack) {
    CompoundTag compound = getTagSafe(stack);
    CompoundTag tag = compound.getCompound(TAG_INGREDIENTS);
    NonNullList<ItemStack> list = NonNullList.withSize(getSize(stack), ItemStack.EMPTY);
    ContainerHelper.loadAllItems(tag, list, registries());
    return list;
  }

  @Nullable
  public static List<Integer> getLiquids(ItemStack stack) {
    CompoundTag compound = getTagSafe(stack);

    if (!compound.contains(TAG_LIQUIDS)) {
      return null;
    }
    ListTag tag = compound.getList(TAG_LIQUIDS, 3);
    List<Integer> liquids = new ArrayList<>();
    tag.forEach(nbt -> liquids.add(((IntTag) nbt).getAsInt()));
    return liquids;
  }

  public static int getSolidsSize(ItemStack stack) {
    CompoundTag compound = getTagSafe(stack);
    return compound.getInt(TAG_SOLIDS_SIZE);
  }

  public static NonNullList<ItemStack> getSolids(ItemStack stack) {
    CompoundTag compound = getTagSafe(stack);
    CompoundTag tag = compound.getCompound(TAG_SOLIDS);
    NonNullList<ItemStack> list = NonNullList.withSize(getSolidsSize(stack), ItemStack.EMPTY);
    ContainerHelper.loadAllItems(tag, list, registries());
    return list;
  }

  public static int getOriginalCount(ItemStack stack) {
    CompoundTag compound = getTagSafe(stack);
    return compound.getInt(TAG_COUNT);
  }

  public static void setBase(ItemStack stack, ItemStack base) {
    edit(stack, tag -> tag.put(TAG_BASE, base.saveOptional(registries())));
  }

  public static void setQuality(ItemStack stack, int quality) {
    edit(stack, tag -> tag.putInt(TAG_QUALITY, quality));
  }

  public static void setSize(ItemStack stack, int size) {
    edit(stack, tag -> tag.putInt(TAG_SIZE, size));
  }

  public static void setFoodAmount(ItemStack stack, int food) {
    edit(stack, tag -> tag.putInt(TAG_FOOD, food));
  }

  public static void setSaturation(ItemStack stack, float saturation) {
    edit(stack, tag -> tag.putFloat(TAG_SATURATION, saturation));
  }

  public static void setIngredientsList(ItemStack stack, NonNullList<ItemStack> ingredients) {
    CompoundTag tag = new CompoundTag();
    ContainerHelper.saveAllItems(tag, ingredients, registries());
    edit(stack, compound -> compound.put(TAG_INGREDIENTS, tag));
  }

  public static void setLiquids(ItemStack stack, List<Integer> liquids) {
    ListTag tag = new ListTag();
    liquids.forEach(liquid -> tag.add(IntTag.valueOf(liquid)));
    edit(stack, compound -> compound.put(TAG_LIQUIDS, tag));
  }

  public static void setSolidsSize(ItemStack stack, int size) {
    edit(stack, tag -> tag.putInt(TAG_SOLIDS_SIZE, size));
  }

  public static void setSolids(ItemStack stack, NonNullList<ItemStack> solids) {
    CompoundTag tag = new CompoundTag();
    ContainerHelper.saveAllItems(tag, solids, registries());
    edit(stack, compound -> compound.put(TAG_SOLIDS, tag));
  }

  public static void setOriginalCount(ItemStack stack, int count) {
    edit(stack, tag -> tag.putInt(TAG_COUNT, count));
  }
}
