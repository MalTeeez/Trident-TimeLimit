package net.sxmaa.timelimiter;

import java.io.File;
import java.util.HashMap;

import net.minecraftforge.common.config.Configuration;

public class PlayerTimeWallet extends Configuration{

    private String[] TimeWalletDump;
    public HashMap<String, Integer> TimeWallet = new HashMap<>();
    public String lastUpdate;

    public static final String CATEGORY_TIME_WALLET = "time wallet";

    public PlayerTimeWallet(String configDir) {

        super(
            new File(configDir + "/PlayerTimeWallet.cfg")
        );
        super.load();
        loadWallet();
        super.save();
    }

    private void loadWallet() {

        this.TimeWalletDump = super.getStringList(
            "player time wallet",
            CATEGORY_TIME_WALLET,
            new String[0],
            "Stores the time each player has left to play in the current cycle."
        );

        for(String IndividualWallet : TimeWalletDump) {

            String[] DataDumpArray = IndividualWallet.split(" ");
            String PlayerUuid = DataDumpArray[0];
            Integer PlayerTimeAllowance = Integer.valueOf(DataDumpArray[1]);

            TimeWallet.put(PlayerUuid, PlayerTimeAllowance);
        }

        this.lastUpdate = super.getString(
            "last update",
            CATEGORY_TIME_WALLET,
            "",
            "Saves the date stamp of the last player time update event."
        );
    }

    private void updateWallet() {

        TimeWalletDump = new String[TimeWallet.size()];
        int dumpIteration = 0;

        TimeWallet.forEach((uuid, time) ->
            TimeWalletDump[dumpIteration] = uuid + " " + String.valueOf(time)
        );

        super.get(
            CATEGORY_TIME_WALLET,
            "player time wallet",
            new String[0]
        ).set(
            TimeWalletDump
        );

        super.save();
    }

    public void updateGlobalTimeStamp() {

        super.get(
            CATEGORY_TIME_WALLET,
            "last update",
            ""
        ).set(
            this.lastUpdate
        );

        super.save();
    }

    public void update(String uuid) {

        int timeUpdateInterval = (int)Math.floor(TimeLimiter.proxy.modConfig.get_playerTimeLimitUpdateInterval());
        this.update(uuid, timeUpdateInterval);
    }

    public void update(String uuid, int time) {

        Integer legacyTimeLimit = this.TimeWallet.get(uuid);
        if(legacyTimeLimit == null) {
            legacyTimeLimit = TimeLimiter.proxy.modConfig.get_playerTimeLimit();
        }

        this.TimeWallet.put(uuid, legacyTimeLimit - time);
        updateWallet();
    }

    public void overrideLastUpdate(String newStamp) {

        this.lastUpdate = newStamp;
        this.updateGlobalTimeStamp();
    }

    public int getTime(String uuid) {

        int time = TimeWallet.get(uuid);
        time = Math.max(time, 0);
        return time;
    }

    public void reset() {

        int defaultTimeAllowance = TimeLimiter.proxy.modConfig.get_playerTimeLimit();
        TimeWallet.forEach((uuid, time) -> TimeWallet.put(uuid, time + defaultTimeAllowance));

        updateWallet();
    }

    public String getLastUpdate() {

        return this.lastUpdate;
    }
}
