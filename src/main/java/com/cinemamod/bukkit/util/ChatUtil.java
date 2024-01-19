package com.cinemamod.bukkit.util;

import com.cinemamod.bukkit.service.VideoServiceType;
import com.cinemamod.bukkit.theater.PrivateTheater;
import com.cinemamod.bukkit.theater.Theater;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public final class ChatUtil {

    public static final ChatColor MAIN_COLOR = ChatColor.of("#8F2121");
    public static final ChatColor SECONDARY_COLOR = ChatColor.of("#5e6061");
    private static final String PADDING;

    static {
        StringBuilder paddingBuilder = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            final ChatColor color;
            if (i % 2 == 0) {
                color = MAIN_COLOR;
            } else {
                color = SECONDARY_COLOR;
            }
            paddingBuilder.append(color).append("-");
        }
        PADDING = paddingBuilder.toString();
    }

    public static void sendPaddedMessage(Player player, String... lines) {
        player.sendMessage(PADDING);
        for (String line : lines) {
            if (line != null)
                player.sendMessage(line);
        }
        player.sendMessage(PADDING);
    }

    public static void showPlaying(Player player, Theater theater, boolean showOriginUrl) {
        boolean privateTheater = theater instanceof PrivateTheater;
        String playingAt = ChatColor.RESET + " @ " + theater.getName() + " [" + (privateTheater ? "приватный" : "публичный") + "]";

        if (!theater.isPlaying()) {
            sendPaddedMessage(player,
                    ChatColor.BOLD + "НИЧЕГО НЕ ИГРАЕТ" + playingAt,
                    SECONDARY_COLOR + "Запросите видео с помощью команды /video play");
        } else {
            sendPaddedMessage(player,
                    ChatColor.BOLD + "СЕЙЧАС ИГРАЕТ" + playingAt,
                    SECONDARY_COLOR + theater.getPlaying().getVideoInfo().getTitle(),
                    SECONDARY_COLOR + "По запросу: " + theater.getPlaying().getRequester().getName(),
                    showOriginUrl ? SECONDARY_COLOR + theater.getPlaying().getVideoInfo().getServiceType().getOriginUrl(theater.getPlaying().getVideoInfo().getId()) : null);

            if (theater.getPlaying().getVideoInfo().getServiceType() == VideoServiceType.TWITCH) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "В потоке Twitch может быть 30-секундный отказ от ответственности перед началом.");
            }
        }
    }

}
