package com.illusivesoulworks.culinaryconstruct.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import com.illusivesoulworks.culinaryconstruct.client.CulinaryStationScreen;
import com.illusivesoulworks.culinaryconstruct.common.block.CulinaryStationBlockEntity;
import com.illusivesoulworks.culinaryconstruct.common.item.FoodBowlItem;
import com.illusivesoulworks.culinaryconstruct.common.item.SandwichItem;
import com.illusivesoulworks.culinaryconstruct.common.registry.CulinaryConstructRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.nio.file.Files;

// Run with -Dsmoketest=true: creates a flat world, takes a screenshot and quits. Exit code 1 on failure.
public final class ClientSmoke {

    public static final boolean ENABLED = Boolean.getBoolean("smoketest");
    private static final long TIMEOUT_MS = 5 * 60 * 1000;

    private static int stage;
    private static int ticks;

    private ClientSmoke() {
    }

    public static boolean start() {
        if (!ENABLED) {
            return false;
        }
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT_MS);
                fail("timed out");
            } catch (InterruptedException ignored) {
            }
        }, "smoketest-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
        return true;
    }

    public static void tick(Minecraft mc) {
        try {
            switch (stage) {
                case 0 -> {
                    Screen screen = mc.screen;
                    if (screen != null && mc.getOverlay() == null && ++ticks >= 20) {
                        if (screen.getClass().getSimpleName().equals("LoadingErrorScreen")) {
                            fail("mod loading errors");
                        }
                        ticks = 0;
                        mc.options.tutorialStep = net.minecraft.client.tutorial.TutorialSteps.NONE;
                        createWorld(mc);
                        stage = 1;
                    }
                }
                case 1 -> {
                    if (mc.level != null && mc.player != null && mc.screen == null) {
                        stage = 2;
                    }
                }
                case 2 -> {
                    ticks++;
                    if (ticks == 40) {
                        setup(mc);
                    }
                    if (ticks == 120) {
                        mc.options.hideGui = false;
                        Screenshot.grab(mc.gameDirectory, "held.png", mc.getMainRenderTarget(), msg -> {});
                        openStation(mc);
                    }
                    if (ticks == 170) {
                        Screenshot.grab(mc.gameDirectory, "station.png", mc.getMainRenderTarget(), msg -> {});
                    }
                    if (ticks >= 190) {
                        if (!(mc.screen instanceof CulinaryStationScreen)) {
                            fail("culinary station screen did not open (screen = " + mc.screen + ")");
                        }
                        mc.setScreen(null);
                        Files.writeString(mc.gameDirectory.toPath().resolve("smoke-ok"), "ok\n");
                        stage = 3;
                        mc.stop();
                    }
                }
                default -> {
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.toString());
        }
    }

    private static void createWorld(Minecraft mc) {
        LevelSettings settings = new LevelSettings("smoke", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel("smoke", settings, new WorldOptions(0L, false, false),
            registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
            mc.screen);
    }

    // a station in front of the player with a sandwich being made, and a sandwich and a food bowl in the hotbar
    private static BlockPos station;

    private static void setup(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        server.execute(() -> {
            var player = server.getPlayerList().getPlayers().get(0);
            var level = player.serverLevel();
            station = player.blockPosition().relative(player.getDirection(), 2);
            level.setBlockAndUpdate(station, CulinaryConstructRegistry.CULINARY_STATION_BLOCK.get().defaultBlockState());
            var be = (CulinaryStationBlockEntity) level.getBlockEntity(station);
            be.setItem(0, new ItemStack(Items.BREAD, 4));
            be.setItem(1, new ItemStack(Items.COOKED_BEEF, 4));
            be.setItem(2, new ItemStack(Items.CARROT, 4));
            be.setItem(3, new ItemStack(Items.BEETROOT, 4));
            player.getInventory().setItem(0, SandwichItem.generateCreativeItem());
            player.getInventory().setItem(1, FoodBowlItem.generateCreativeItem());
            player.getInventory().selected = 0;
        });
    }

    private static void openStation(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        server.execute(() -> {
            var player = server.getPlayerList().getPlayers().get(0);
            var level = player.serverLevel();
            player.openMenu(level.getBlockState(station).getMenuProvider(level, station));
        });
    }

    public static void fail(String why) {
        System.err.println("Smoke test failed: " + why);
        System.out.println("[clientsmoke] FAILED: " + why);
        System.err.flush();
        Runtime.getRuntime().halt(1);
    }
}
