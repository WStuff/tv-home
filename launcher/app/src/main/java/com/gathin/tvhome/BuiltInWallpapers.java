package com.gathin.tvhome;

/** The five offline backgrounds available without a theme or network connection. */
final class BuiltInWallpapers {
    private static final String PREFIX = "built-in:";
    private static final int[] IMAGES = {
            R.drawable.wallpaper_dunes,
            R.drawable.wallpaper_moonlight,
            R.drawable.wallpaper_aurora,
            R.drawable.wallpaper_sunrise,
            R.drawable.wallpaper_nebula
    };
    private static final int[] LABELS = {
            R.string.wallpaper_dunes,
            R.string.wallpaper_moonlight,
            R.string.wallpaper_aurora,
            R.string.wallpaper_sunrise,
            R.string.wallpaper_nebula
    };

    private BuiltInWallpapers() { }

    static int count() { return IMAGES.length; }
    static int imageAt(int index) { return IMAGES[index]; }
    static int labelAt(int index) { return LABELS[index]; }

    static String choiceAt(int index) { return PREFIX + index; }

    static int indexOfChoice(String choice) {
        if (choice == null || !choice.startsWith(PREFIX)) return -1;
        try {
            int index = Integer.parseInt(choice.substring(PREFIX.length()));
            return index >= 0 && index < IMAGES.length ? index : -1;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    static int imageForChoice(String choice) {
        int index = indexOfChoice(choice);
        return index < 0 ? 0 : imageAt(index);
    }
}
