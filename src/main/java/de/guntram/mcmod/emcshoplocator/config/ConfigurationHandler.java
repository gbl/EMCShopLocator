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
        allowUpload=config.getBoolean("Allow Upload", Configuration.CATEGORY_CLIENT, allowUpload, "Allow Upload to central database");
        allowDownload=config.getBoolean("Allow Download", Configuration.CATEGORY_CLIENT, allowDownload, "Allow Download from central database (only if Upload is enabled as well)");
        saveEveryXMinutes=config.getInt("Save every X minutes", Configuration.CATEGORY_CLIENT, 1, 1, 60, "How often sign data will be saved locally");
        uploadEveryXMinutes=config.getInt("Upload every X minutes", Configuration.CATEGORY_CLIENT, 5, 5, 60, "How often sign data will be uploaded");
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
}
