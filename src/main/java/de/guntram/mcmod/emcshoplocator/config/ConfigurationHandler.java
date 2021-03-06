package de.guntram.mcmod.emcshoplocator.config;

import de.guntram.mcmod.emcshoplocator.EMCShopLocator;
import de.guntram.mcmod.emcshoplocator.SignDownloaderThread;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class ConfigurationHandler {

    private static ConfigurationHandler instance;

    private Configuration config;
    private String configFileName;
    
    private boolean allowDownload=false;
    private boolean allowUpload=true;
    private int saveEveryXMinutes=1;
    private int uploadEveryXMinutes=5;
    private int ignoreOlderThan=365;
    private int rescaleGui=-1;
    private boolean tryAnyway=false;
    private boolean serverEnabled[];
    private static final int numberOfServers=10;       // utopia=0, smp1..9
    
    public static ConfigurationHandler getInstance() {
        if (instance==null)
            instance=new ConfigurationHandler();
        return instance;
    }
    
    public void load(final File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            configFileName=configFile.getPath();
            loadConfig();
        }
    }
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        // System.out.println("OnConfigChanged for "+event.getModID());
        if (event.getModID().equalsIgnoreCase(EMCShopLocator.MODID)) {
            loadConfig();
            if (allowDownload)
                new SignDownloaderThread(EMCShopLocator.instance).start();
        }
    }
    
    private void loadConfig() {
        serverEnabled=new boolean[numberOfServers];
        allowUpload=config.getBoolean("Allow Upload", Configuration.CATEGORY_CLIENT, true, "Allow Upload to central database");
        allowDownload=config.getBoolean("Allow Download", Configuration.CATEGORY_CLIENT, true, "Allow Download from central database (only if Upload is enabled as well)");
        saveEveryXMinutes=config.getInt("Save every X minutes", Configuration.CATEGORY_CLIENT, 1, 1, 60, "How often sign data will be saved locally");
        uploadEveryXMinutes=config.getInt("Upload every X minutes", Configuration.CATEGORY_CLIENT, 5, 5, 60, "How often sign data will be uploaded");
        ignoreOlderThan=config.getInt("Ignore signs older than X days", Configuration.CATEGORY_CLIENT, 365, 0, 3650, "Signs that haven't been seen in this many days won't show up in search results");
        tryAnyway=config.getBoolean("Try to work on small screens/large GUI scales", Configuration.CATEGORY_CLIENT, false,
                "Experimental: With large GUI scales, don't show the warning screen and try to work anyway.\n"+
                "Warning, this might crash your client.");
        rescaleGui=config.getInt("Rescale GUI while mod is open", Configuration.CATEGORY_CLIENT, -1, -1, 3, 
                "Experimental: rescale the GUI while the mod screen is open.\n"+
                        "-1 = don't rescale, 0=auto, 1=small, 2=normal, 3=large\n"+
                        "Recommended setting is either -1 to not change anything, or 2 for most screens");
        for (int i=0; i<numberOfServers; i++) {
            serverEnabled[i]=config.getBoolean("Enable Server "+getServerName(i), Configuration.CATEGORY_CLIENT, true, "Search for shops on this server");
        }
        if (config.hasChanged())
            config.save();
    }
    
    public static Configuration getConfig() {
        return getInstance().config;
    }
    
    public static String getConfigFileName() {
        return getInstance().configFileName;
    }
    
    public static boolean isDownloadAllowed() {
        return getInstance().allowDownload;
    }

    public static boolean isUploadAllowed() {
        return getInstance().allowUpload;
    }
    
    public static int getSaveInterval() {
        return getInstance().saveEveryXMinutes;
    }
    
    public static int getUploadInterval() {
        return getInstance().uploadEveryXMinutes;
    }
    
    public static int getIgnoreOlderThan() {
        return getInstance().ignoreOlderThan;
    }

    public static boolean getTryAnyway() {
        return getInstance().tryAnyway;
    }

    public static int getRescaleGUI() {
        return getInstance().rescaleGui;
    }
    
    public static int getNumberOfServers() {
        return numberOfServers;
    }
    
    public static boolean isServerEnabled(int i) {
        return i<numberOfServers && getInstance().serverEnabled[i];
    }
    
    public static void setServerEnabled(int i, boolean val) {
        getInstance().serverEnabled[i]=val;
        // Todo: how do I save this into config? There's no setter methods?
    }
    
    public static String getServerName(int i) {
        if (i==0)
            return "UTOPIA";
        else
            return "SMP"+i;
    }

    public static int getServerIndex(String server) {
        if (server.startsWith("SMP"))
            return server.charAt(3)-'0';
        else
            return 0;
    }
}
