package com.cinemamod.bukkit.command.theater;

import com.cinemamod.bukkit.CinemaModPlugin;
import com.cinemamod.bukkit.service.VideoURLParser;
import com.cinemamod.bukkit.theater.Theater;
import com.cinemamod.bukkit.util.ChatUtil;
import com.cinemamod.bukkit.util.NetworkUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VideoCommand extends TheaterCommandExecutor implements TabCompleter {

    private final CinemaModPlugin cinemaModPlugin;
    private final Set<Player> lock;

    public VideoCommand(CinemaModPlugin cinemaModPlugin) {
        super(cinemaModPlugin);
        this.cinemaModPlugin = cinemaModPlugin;
        lock = new HashSet<>();
    }

    @Override
    public boolean onTheaterCommand(Player player, Command command, String label, String[] args, Theater theater) {
        if(args.length == 0){
            player.sendMessage(ChatColor.RED + "Выберите действие (skip|play|volume)");
            return true;
        }
            switch (args[0].toLowerCase()) {
                case "skip":
                    if (!theater.isPlaying()) {
                        player.sendMessage(ChatColor.RED + "Нет видео, которое можно было бы пропустить.");
                    } else if (!theater.addVoteSkip(player)) {
                        player.sendMessage(ChatColor.RED + "Вы уже проголосовали за пропуск этого видео.");
                    }
                    break;
                case "play":
                    if (lock.contains(player)) {
                        player.sendMessage(ChatColor.RED + "Подождите, чтобы использовать эту команду снова.");
                        return true;
                    }

                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Неправильная ссылка. /" + label + " play <url>");
                        player.sendMessage(ChatColor.RED + "Пример: /" + label + " play https://www.youtube.com/watch?v=dQw4w9WgXcQ");
                        return true;
                    }
                    String url = args[1];
                    VideoURLParser parser = new VideoURLParser(cinemaModPlugin, url);

                    parser.parse(player);

                    if (!parser.found()) {
                        player.sendMessage(ChatColor.RED + "Эта ссылка или тип видео не поддерживается.");
                        return true;
                    }

                    if (!player.hasPermission(parser.getInfoFetcher().getPermission())) {
                        player.sendMessage(ChatColor.RED + "У вас нет разрешения запрашивать этот тип видео.");
                        return true;
                    }

                    player.sendMessage(ChatColor.GOLD + "Получение информации о видео...");

                    lock.add(player);

                    parser.getInfoFetcher().fetch().thenAccept(videoInfo -> {
                        lock.remove(player);

                        if (!player.isOnline()) return;

                        if (!theater.isViewer(player)) {
                            player.sendMessage(ChatColor.RED + "Запрошенное вами видео не было поставлено в очередь, потому что вы вышли из кинотеатра.");
                            return;
                        }

                        if (videoInfo == null) {
                            player.sendMessage(ChatColor.RED + "Не удалось получить информацию о видео.");
                            return;
                        }

                        theater.getVideoQueue().processPlayerRequest(videoInfo, player);
                    });
                    break;
                case "volume":
                    NetworkUtil.sendOpenSettingsScreenPacket(cinemaModPlugin, player);
                    break;
                case "info":
                    ChatUtil.showPlaying(player, theater, true);
                    break;
                case "op_skip":
                    if(player.hasPermission("cinemamod.admin")){
                        if (theater.isPlaying()) {
                            theater.forceSkip();
                            player.sendMessage(ChatColor.GOLD + "Видео скипнуто высшими силами.");
                        } else {
                            player.sendMessage(ChatColor.RED + "Этот театр ничего не играет.");
                        }
                    }
                    break;
                case "op_lock_video":
                    if(player.hasPermission("cinemamod.admin")){
                        boolean wasLocked = theater.getVideoQueue().isLocked();
                        theater.getVideoQueue().setLocked(!wasLocked);

                        if (wasLocked) {
                            player.sendMessage(ChatColor.GOLD + "Очередь видео теперь разблокирована высшими силами.");
                        } else {
                            player.sendMessage(ChatColor.GOLD + "Очередь видео теперь заблокирована высшими силами.");
                        }
                    }
                    break;
                case "history":
                    NetworkUtil.sendOpenHistoryScreenPacket(cinemaModPlugin, player);
                    break;
            }

        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length == 1) {
            return List.of("op_lock_video", "op_skip", "history", "play", "skip", "volume", "skip", "info");
        }else if(args.length == 2) {
            if (args[0].equals("play")) {
                return List.of("[Ссылка]");
            }
        }
        return null;
    }

}