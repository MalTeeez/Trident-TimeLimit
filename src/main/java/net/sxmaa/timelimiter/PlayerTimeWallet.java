package net.sxmaa.timelimiter;

import java.io.File;
import java.util.HashMap;

import net.minecraftforge.common.config.Configuration;

public class PlayerTimeWallet extends Configuration{

    private String[] TimeWalletDump;
    public HashMap<String, Integer> TimeWallet = new HashMap<String, Integer>();

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

    public void update(String uuid) {

        int timeUpdateInterval = (int)Math.floor(TimeLimiter.proxy.modConfig.get_playerTimeLimitUpdateInterval());
        this.update(uuid, timeUpdateInterval);
    }

    public void update(String uuid, int time) {

        Integer legacyTimeLimit = this.TimeWallet.get(uuid);
        if(legacyTimeLimit == null) {
            legacyTimeLimit = TimeLimiter.proxy.modConfig.get_playerTimeLimit();
        }

        System.out.println(time);
        System.out.println(legacyTimeLimit);

        this.TimeWallet.put(uuid, legacyTimeLimit - time);
        updateWallet();
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
}
