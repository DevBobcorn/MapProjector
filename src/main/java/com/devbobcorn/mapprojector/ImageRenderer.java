package com.devbobcorn.mapprojector;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

public class ImageRenderer extends MapRenderer {

    private MapCanvas mapCanvas;

    public final int offsetX, offsetY;

    public ImageRenderer(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public MapCanvas getMapCanvas() {
        return mapCanvas;
    }

    public static ImageRenderer applyToMap(MapView map, int ox, int oy) {
        for (MapRenderer renderer : map.getRenderers())
            map.removeRenderer(renderer);

        ImageRenderer imageRenderer = new ImageRenderer(ox, oy);
        map.addRenderer(imageRenderer);

        return imageRenderer;
    }

    @Override
    public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
        this.mapCanvas = mapCanvas;

    }
}
