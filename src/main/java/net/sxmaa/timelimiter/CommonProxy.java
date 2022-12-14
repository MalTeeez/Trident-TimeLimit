package net.sxmaa.timelimiter;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.UsernameCache;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class CommonProxy {

    //stores the time of the players login, and then the time of the first update
    private static final HashMap<EntityPlayer, Date> playerlist = new HashMap<>();
    public ModConfig modConfig;
    public PlayerTimeWallet playerTimeWallet;
    private Timer timer;
    private long now;
    private long Udelay;

    public static String getPlayerUUID(String username) {
        for (Map.Entry<UUID, String> entry : UsernameCache.getMap().entrySet()) {
            if (entry.getValue().equalsIgnoreCase(username)) {
                return entry.getKey().toString();
            }
        }
        return null;
    }

    public static void addPlayer(EntityPlayer player, Date time) {
        playerlist.put(player, time);
        final String uuid = String.valueOf(getPlayerUUID(player.getDisplayName()));
        TimeLimiter.proxy.playerTimeWallet.update(uuid,0);
        System.out.println("Players: "+playerlist);
    }

    public static void removePlayer(EntityPlayer player) {

        int playerUpdateTime = (int)(System.currentTimeMillis() - playerlist.get(player).getTime());
        TimeLimiter.proxy.playerTimeWallet.update(getPlayerUUID(player.getDisplayName()), playerUpdateTime);
        playerlist.remove(player);
    }

    // preInit "Run before anything else. Read your config, create blocks, items,
    // etc, and register them with the GameRegistry."
    public void preInit(FMLPreInitializationEvent event) {

        Config.syncronizeConfiguration(event.getSuggestedConfigurationFile());

        TimeLimiter.info("I am " + Tags.MODNAME + " at version " + Tags.VERSION + " and group name " + Tags.GROUPNAME);

        this.modConfig = new ModConfig(
            event.getSuggestedConfigurationFile()
        );

        this.playerTimeWallet = new PlayerTimeWallet(
            event.getModConfigurationDirectory().toString()
        );

    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes."
    public void init(FMLInitializationEvent event) {
        System.out.println("hello from common fml init");
        FMLCommonHandler.instance().bus().register(new EventListener());
    }

    // postInit "Handle interaction with other mods, complete your setup based on this."
    public void postInit(FMLPostInitializationEvent event) {}

    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
    }

    // register server commands in this event handler
    public void serverStarting(FMLServerStartingEvent event) {}

    public void serverStarted(FMLServerStartedEvent event) {
        Udelay = modConfig.get_playerTimeLimitUpdateInterval();
        this.setupTimerDelay(Udelay);
        this.playerTimeWallet.overrideLastUpdate(Instant.now().toString());
    }

    public void serverStopping(FMLServerStoppingEvent event) {}

    public void serverStopped(FMLServerStoppedEvent event) {}

    void setupTimer(final long time) {
        if (this.timer != null) {
            this.timer.cancel();
        }
        this.timer = new Timer();
        if (Udelay > 0L) {
            this.timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    now = System.currentTimeMillis();
                    playerlist.forEach((entityPlayer, date) -> {
                        int activeTimeSinceLastUpdate = (int) (now - date.getTime());
                        playerlist.put(entityPlayer, Date.from(Instant.now()));
                        playerTimeWallet.update(entityPlayer.getUniqueID().toString(), activeTimeSinceLastUpdate);

                        if(playerTimeWallet.getTime(getPlayerUUID(entityPlayer.getDisplayName())) <= 0) {
                            ((EntityPlayerMP)entityPlayer).playerNetServerHandler.kickPlayerFromServer("Your free trial of life has expired");
                        }
                        System.out.println(activeTimeSinceLastUpdate);
                    });
                    Instant currentUpdate = Instant.now();
                    Instant lastUpdate = Instant.parse(playerTimeWallet.getLastUpdate()).truncatedTo(ChronoUnit.DAYS);
                    playerTimeWallet.overrideLastUpdate(currentUpdate.toString());
                    if (currentUpdate.truncatedTo(ChronoUnit.DAYS).equals(lastUpdate)) {
                    } else {
                        System.out.println("Detected new day, resetting playtimes.");
                        playerTimeWallet.reset();
                    }
                }
            }, new Date(time), Udelay);
        }
        else {
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TimeLimiter.error("idk what just happened, but its related to the timer");
                }
            }, new Date(time));
        }
    }

    void setupTimerDelay(final long delay) {
        this.setupTimer(System.currentTimeMillis() + delay);
    }

}
