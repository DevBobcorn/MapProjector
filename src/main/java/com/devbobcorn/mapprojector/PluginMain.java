package com.devbobcorn.mapprojector;

import com.devbobcorn.mapprojector.commands.CommandProjectMap;
import com.devbobcorn.mapprojector.commands.CommandProjectStop;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Objects;

public class PluginMain extends JavaPlugin {

    private final HashMap<Integer, ImageRenderer> mapRenderers = new HashMap<>();
    private final HashMap<Integer, MapCanvas> mapCanvases = new HashMap<>();

    private final byte[] palette = new byte[256 * 256 * 256];

    private static PluginMain instance;

    @Nullable
    public static PluginMain getInstance() {
        return instance;
    }

    @SuppressWarnings("deprecation")
    public void updateMap(Player player, int mapIdStart, int mapCountX, int mapCountY) {
        try {
            for (int mapY = 0;mapY < mapCountY;mapY++)
                for (int mapX = 0;mapX < mapCountX;mapX++) {
                    int mapId = mapIdStart + mapCountX * mapY + mapX;
                    var map = Bukkit.getMap(mapId);

                    if (map == null) {
                        throw new Exception("Failed to update map: Map #" + mapId + " not found");
                    }

                    final int ox = mapX << 7;
                    final int oy = mapY << 7;

                    mapRenderers.computeIfAbsent(mapId, (id) -> {
                        player.spigot().sendMessage(new TextComponent("Creating Image renderer for map #" + id));
                        return ImageRenderer.applyToMap(map, ox, oy);
                    });

                    player.sendMap(map);
                    //player.spigot().sendMessage(new TextComponent("Updating map #" + mapId));
                }
        } catch (Exception e) {
            player.spigot().sendMessage(new TextComponent("Failed to update map: " + e.getMessage()));
            //e.printStackTrace();
        }
    }

    public void stopUpdatingMaps() {
        disposeRenderers();
    }

    @SuppressWarnings("deprecation")
    private void disposeRenderers() {
        for (var mapId : mapRenderers.keySet()) { // Remove all map renderers
            var map = Bukkit.getMap(mapId);

            for (MapRenderer renderer : map.getRenderers())
                map.removeRenderer(renderer);
        }

        mapRenderers.clear();
        mapCanvases.clear();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
        disposeRenderers();

        // Store plugin instance
        instance = this;

        // Register commands...
        Objects.requireNonNull(getCommand("projmap")).setExecutor(new CommandProjectMap());
        Objects.requireNonNull(getCommand("projstop")).setExecutor(new CommandProjectStop());

        // Prepare font for map graphics
        //Font font = new Font("Consolas", Font.PLAIN, 128);

        var dataFolder = getDataFolder();

        // Prepare map color palette
        var mapColorPalette = new File(dataFolder, "colorPalette.dat");

        if (mapColorPalette.exists()) {
            System.out.println("Color palette is available, reading...");

            try {
                FileInputStream fis = new FileInputStream(mapColorPalette);

                byte[] data = fis.readAllBytes();

                if (data.length != palette.length)
                {
                    fis.close();
                    throw new Exception("Data length (" + data.length + ") doesn't match palette length (" + palette.length + ")");
                }

                System.arraycopy(data, 0, palette, 0, data.length);

                fis.close();
            } catch (Exception e) {
                System.err.println("Failed to read map color palette: " + e.getMessage());
                e.printStackTrace();
            }

        } else {
            System.out.println("Color palette not present, creating...");

            try {
                FileOutputStream fos = new FileOutputStream(mapColorPalette);

                for (int r = 0;r < 256;r++) {
                    for (int g = 0;g < 256;g++)
                        for (int b = 0;b < 256;b++) {
                            palette[(r << 16) + (g << 8) + b] = MapPalette.matchColor(r, g, b);
                        }

                    System.out.println("Progress: [" + (r + 1) + "/256]");
                }

                fos.write(palette);

                fos.flush();
                fos.close();
            } catch (Exception e) {
                System.err.println("Failed to create map color palette: " + e.getMessage());
                e.printStackTrace();
            }

        }

        // Create a robot for screen capture
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        final Robot finalRobot = robot;

        final Rectangle rect =
                new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        System.out.println("Rect size: " + rect.width + ", " + rect.height);

        // Start updating things
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {

            if (mapRenderers.size() == 0)
                return;

            // Capture screenshot
            final BufferedImage captureImage = finalRobot.createScreenCapture(rect);
            final int captureW = captureImage.getWidth();
            final int captureH = captureImage.getHeight();

            for (var pair : mapRenderers.entrySet()) {

                int mapId = pair.getKey();
                var renderer = pair.getValue();

                var canvas = mapCanvases.computeIfAbsent(mapId, (id) -> renderer.getMapCanvas());

                if (canvas != null) {
                    for (int i = 0;i < 128;i++)
                        for (int j = 0;j < 128;j++) {
                            int x = i + renderer.offsetX;
                            int y = j + renderer.offsetY;

                            if (x < captureW && y < captureH) {
                                canvas.setPixel(i, j, palette[captureImage.getRGB(x, y) & 0x00FFFFFF]); // Ignore alpha channel
                            }

                        }

                    //canvas.drawText(32, 32, MinecraftFont.Font, String.valueOf(mapId));
                }
            }
        }, 20L, 1L);
    }

    @Override
    public void onDisable() {
        disposeRenderers();

        instance = null;

    }


}
