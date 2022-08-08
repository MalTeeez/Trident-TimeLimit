package net.sxmaa.timelimiter;

import java.io.File;
import net.minecraftforge.common.config.*;

public class ModConfig extends Configuration
{
    // config elements
    public boolean doPlayerTimeLimit = true;
    public int playerTimeLimitResetInterval = 86400;
    public int playerTimeLimit = 7200;
    public int playerTimeLimitUpdateInterval = 300;

    public ModConfig(File configFile){
        super(configFile);

        this.load();
        loadConfiguration();
        this.save();
    }

    private void loadConfiguration() {

        this.doPlayerTimeLimit = this.getBoolean(
            "do player time limit",
            super.CATEGORY_GENERAL,
            true,
            "Limit the time every player can spend on the server"
        );

        this.playerTimeLimitResetInterval = this.getInt(
            "time limit reset interval",
            super.CATEGORY_GENERAL,
            86400, //1 day
            0,
            604800, //1 week
            "Time to pass between each reset of a players time wallet to the value of \"time limit\""
        );

        this.playerTimeLimit = this.getInt(
            "player time limit",
            super.CATEGORY_GENERAL,
            7200, //2 hours
            0,
            playerTimeLimitResetInterval,
            "Time each player can spend on the server within one interval."
        );

        this.playerTimeLimitResetInterval = this.getInt(
            "player time limit reset interval", 
            super.CATEGORY_GENERAL, 
            300, 1, 
            playerTimeLimit, 
            "How often should the file be updated with the remaining allowance in a players time wallet?"
        );

    }

    public boolean get_doPlayerTimeLimit() {
        return doPlayerTimeLimit;
    }

    public int get_playerTimeLimitResetInterval() {
        return playerTimeLimitResetInterval;
    }

    public int get_playerTimeLimit() {
        return playerTimeLimit;
    }

    public long get_playerTimeLimitUpdateInterval() {
        return playerTimeLimitUpdateInterval;
    }
}

