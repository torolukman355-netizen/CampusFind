package com.campusfind.ui.components;

import com.campusfind.model.Item;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Utilitas memuat foto barang dari database ({@code foto_bytes}) maupun
 * dari file lokal ({@code foto_path}) sebagai fallback.
 */
public final class ItemImages {

    private ItemImages() {
    }

    /**
     * Memuat gambar barang. Mengembalikan {@code null} bila barang tidak punya foto
     * atau berkas fotonya tidak dapat dibaca.
     */
    public static BufferedImage load(Item item) {
        if (item == null) {
            return null;
        }
        try {
            if (item.hasFotoBytes()) {
                return ImageIO.read(new ByteArrayInputStream(item.getFotoBytes()));
            }
            String path = item.getFotoPath();
            if (path != null && !path.isBlank()) {
                File file = new File(path);
                if (!file.isAbsolute() && !file.exists()) {
                    file = new File("data" + File.separator + path);
                }
                if (file.exists()) {
                    return ImageIO.read(file);
                }
            }
        } catch (Exception ignored) {
            // Foto rusak / format tidak didukung -> tampilkan placeholder.
        }
        return null;
    }

    /**
     * Memperkecil gambar agar muat di dalam kotak {@code maxWidth x maxHeight}
     * tanpa merusak rasio aspek.
     */
    public static BufferedImage fit(BufferedImage source, int maxWidth, int maxHeight) {
        if (source == null) {
            return null;
        }
        double ratio = Math.min((double) maxWidth / source.getWidth(),
                (double) maxHeight / source.getHeight());
        if (ratio <= 0) {
            return source;
        }
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(source, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }
}
