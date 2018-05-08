package red.man10.man10mapsample;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

public final class Man10mapsample extends JavaPlugin implements Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length >= 1 && args[0].equalsIgnoreCase("stop")) {
            timerstop();
            return true;
        }
        Player p = (Player)sender;
        giveMap(p,"timer");
        giveMap(p,"timers");
        return true;
    }

    FileConfiguration config1;
    DynamicMapRenderer map;
    @Override
    public void onEnable() {
        // Plugin startup logic
        saveConfig();
        config1 = getConfig();
        map = new DynamicMapRenderer();
        map.setupMaps(this);
        map.register("timer",20,clock);
        getCommand("mmap").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onIntract(PlayerInteractEvent e){
        if(e.getAction()==Action.RIGHT_CLICK_BLOCK){
            if(e.getClickedBlock()==null){
                return;
            }
            if(e.getClickedBlock().getType()== Material.WOOD_BUTTON||e.getClickedBlock().getType()== Material.STONE_BUTTON){
                Collection<Entity> entitys = e.getClickedBlock().getLocation().getWorld().getNearbyEntities(
                        e.getClickedBlock().getLocation(),e.getClickedBlock().getLocation().getX(),e.getClickedBlock().getLocation().getY(),e.getClickedBlock().getLocation().getZ());
                for(Entity en:entitys){
                    if(en instanceof ItemFrame) {
                        ItemFrame frame = (ItemFrame) en;
                        ItemStack getitem = frame.getItem();
                        if(getitem.getType() == Material.MAP){
                            if(containMap(getitem)){
                                if(!timerpower) {
                                    if(time == 0) {
                                        new timerstart().start();
                                        frame.setItem(map.getMapItem(this, "timers"));
                                    }else{
                                        time = 0;
                                        map.refresh("timers");
                                    }
                                }else{
                                    timerstop();
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public boolean giveMap(Player p, String target){

        //      アイテム作成
        ItemStack map = DynamicMapRenderer.getMapItem(this,target);
        p.getInventory().addItem(map);

        DynamicMapRenderer.updateAll();

        return true;
    }

    //アイテムからそのマップが動くかチェックする
    public boolean containMap(ItemStack item){
        int id = (int)item.getDurability();
        List<String> mlist = config1.getStringList("Maps");
        for(DynamicMapRenderer renderer:map.renderers){
            String key = renderer.key;
            if(mlist.contains(id+","+key)){
                return true;
            }
        }
        return false;
    }
    static DynamicMapRenderer.DrawFunction clock = (String key,Graphics2D g) -> {

        //      背景を黒に
        g.setColor(Color.BLACK);
        g.fillRect(0,0,128,128);

        LocalDateTime now = LocalDateTime.now();
        String date = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(now);
        String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(now);

        g.setColor(Color.RED);
        g.setFont(new Font( "SansSerif", Font.BOLD ,18 ));
        g.drawString(date,10,30);
        g.drawString(time,10,60);

        return true;
    };


    //timerストップ。
    public boolean timerstop(){
        timerpower = false;
        return true;
    }


    static int time = 0;
    static boolean timerpower = false;

    static DynamicMapRenderer.DrawFunction stopwatch = (String key,Graphics2D g) -> {

        //      背景を黒に
        g.setColor(Color.BLACK);
        g.fillRect(0,0,128,128);

        String time = Man10mapsample.time+" sec.";

        g.setColor(Color.RED);
        g.setFont(new Font( "SansSerif", Font.BOLD ,18 ));
        g.drawString(time,10,60);

        return true;
    };

    //timerをスタートする。別スレッドで実行
    public class timerstart extends Thread {
        public void run(){
            map.register("timers",0,stopwatch);
            Man10mapsample.timerpower = true;
            new BukkitRunnable(){
                @Override
                public void run(){
                    if(!Man10mapsample.timerpower){
                        cancel();
                        return;
                    }
                    map.refresh("timers");
                    Man10mapsample.time++;
                }
            }.runTaskTimer(Man10mapsample.this,0,20);
        }
    }
}
