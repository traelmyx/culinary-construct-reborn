package com.illusivesoulworks.culinaryconstruct.test;

import com.illusivesoulworks.culinaryconstruct.api.CulinaryConstructApi;
import com.illusivesoulworks.culinaryconstruct.common.block.CulinaryStationBlockEntity;
import com.illusivesoulworks.culinaryconstruct.common.block.CulinaryStationMenu;
import com.illusivesoulworks.culinaryconstruct.common.registry.CulinaryConstructRegistry;
import com.illusivesoulworks.culinaryconstruct.common.util.CulinaryNBT;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CulinaryTests.MOD_ID)
@PrefixGameTestTemplate(false)
public class CulinaryTests {
    public static final String MOD_ID = "culinaryconstruct_gametest";

    private static CulinaryStationBlockEntity station(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, CulinaryConstructRegistry.CULINARY_STATION_BLOCK.get());
        return (CulinaryStationBlockEntity) helper.getBlockEntity(pos);
    }

    @GameTest(template = "empty")
    public static void sandwichFromStation(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        CulinaryStationBlockEntity station = station(helper);
        CulinaryStationMenu menu = new CulinaryStationMenu(1, player.getInventory(), station);
        station.setItem(0, new ItemStack(Items.BREAD, 2));
        station.setItem(1, new ItemStack(Items.COOKED_BEEF));
        station.setItem(2, new ItemStack(Items.CARROT));
        menu.updateOutput();
        ItemStack result = station.getItem(6);
        helper.assertTrue(result.is(CulinaryConstructRegistry.SANDWICH.get()), "expected a sandwich, got " + result);
        helper.assertTrue(CulinaryNBT.getFoodAmount(result) >= 8, "sandwich food too low: " + CulinaryNBT.getFoodAmount(result));
        NonNullList<ItemStack> ingredients = CulinaryNBT.getIngredientsList(result);
        helper.assertTrue(ingredients.stream().anyMatch(s -> s.is(Items.COOKED_BEEF)) && ingredients.stream().anyMatch(s -> s.is(Items.CARROT)),
            "ingredients not stored: " + ingredients);
        helper.assertTrue(CulinaryNBT.getBase(result).is(Items.BREAD), "base not stored");

        // taking the sandwich uses up one of each input
        menu.slots.get(6).onTake(player, result);
        helper.assertTrue(station.getItem(0).getCount() == 1, "bread not used up");
        helper.assertTrue(station.getItem(1).isEmpty() && station.getItem(2).isEmpty(), "ingredients not used up");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void eatingSandwichFeeds(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        CulinaryStationBlockEntity station = station(helper);
        CulinaryStationMenu menu = new CulinaryStationMenu(1, player.getInventory(), station);
        station.setItem(0, new ItemStack(Items.BREAD));
        station.setItem(1, new ItemStack(Items.COOKED_PORKCHOP));
        menu.updateOutput();
        ItemStack sandwich = station.getItem(6).copy();
        int food = CulinaryNBT.getFoodAmount(sandwich);
        player.getFoodData().setFoodLevel(2);
        sandwich.finishUsingItem(helper.getLevel(), player);
        int now = player.getFoodData().getFoodLevel();
        helper.assertTrue(now == Math.min(20, 2 + food), "expected food " + Math.min(20, 2 + food) + " after eating, got " + now);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void foodBowlWithLiquids(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        CulinaryStationBlockEntity station = station(helper);
        CulinaryStationMenu menu = new CulinaryStationMenu(1, player.getInventory(), station);
        station.setItem(0, new ItemStack(Items.BOWL));
        station.setItem(1, new ItemStack(Items.BEETROOT));
        station.setItem(2, new ItemStack(Items.MILK_BUCKET));
        menu.updateOutput();
        ItemStack result = station.getItem(6);
        helper.assertTrue(result.is(CulinaryConstructRegistry.BOWL.get()), "expected a food bowl, got " + result);
        helper.assertTrue(CulinaryNBT.getLiquids(result) != null && !CulinaryNBT.getLiquids(result).isEmpty(), "milk not stored as a liquid");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ingredientCapabilities(GameTestHelper helper) {
        helper.assertTrue(CulinaryConstructApi.getCulinaryIngredient(new ItemStack(Items.CAKE)).isPresent(), "cake has no ingredient capability");
        helper.assertTrue(CulinaryConstructApi.getCulinaryIngredient(new ItemStack(Items.MILK_BUCKET)).isPresent(), "milk has no ingredient capability");
        helper.assertTrue(CulinaryConstructApi.getCulinaryIngredient(new ItemStack(Items.POTION)).isPresent(), "potion has no ingredient capability");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void stationKeepsItems(GameTestHelper helper) {
        CulinaryStationBlockEntity station = station(helper);
        station.setItem(0, new ItemStack(Items.BREAD, 3));
        station.setItem(3, new ItemStack(Items.APPLE));
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        CompoundTag tag = station.saveWithoutMetadata(registries);
        station.setItem(0, ItemStack.EMPTY);
        station.setItem(3, ItemStack.EMPTY);
        station.loadWithComponents(tag, registries);
        helper.assertTrue(station.getItem(0).is(Items.BREAD) && station.getItem(0).getCount() == 3, "bread not restored");
        helper.assertTrue(station.getItem(3).is(Items.APPLE), "apple not restored");
        helper.succeed();
    }
}
