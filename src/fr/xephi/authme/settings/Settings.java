package fr.xephi.authme.settings;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.datasource.DataSource.DataSourceType;
import fr.xephi.authme.security.HashAlgorithm;


public final class Settings extends YamlConfiguration {

    public static final String PLUGIN_FOLDER = "./plugins/AuthMe";
    public static final String CACHE_FOLDER = Settings.PLUGIN_FOLDER + "/cache";
    public static final String AUTH_FILE = Settings.PLUGIN_FOLDER + "/auths.db";
    public static final String MESSAGE_FILE = Settings.PLUGIN_FOLDER + "/messages";
    public static final String SETTINGS_FILE = Settings.PLUGIN_FOLDER + "/config.yml";
    public static List<String> allowCommands = null;
    public static List<String> getJoinPermissions = null;
    public static List<String> getUnrestrictedName = null;
    private static List<String> getRestrictedIp;
    public static List<String> getMySQLOtherUsernameColumn = null;
    public static List<String> getForcedWorlds = null;
    public static List<String> countries = null;
    public final Plugin plugin;
    private final File file;
    public static DataSourceType getDataSource;
    public static HashAlgorithm getPasswordHash;
    public static HashAlgorithm rakamakHash;
    public static Boolean useLogging = false;

    public static Boolean isPermissionCheckEnabled, isRegistrationEnabled, isForcedRegistrationEnabled,
            isTeleportToSpawnEnabled, isSessionsEnabled, isChatAllowed, isAllowRestrictedIp, 
            isMovementAllowed, isKickNonRegisteredEnabled, isForceSingleSessionEnabled,
            isForceSpawnLocOnJoinEnabled, isSaveQuitLocationEnabled,
            isForceSurvivalModeEnabled, isResetInventoryIfCreative, isCachingEnabled, isKickOnWrongPasswordEnabled,
            getEnablePasswordVerifier, protectInventoryBeforeLogInEnabled, isBackupActivated, isBackupOnStart,
            isBackupOnStop, enablePasspartu, isStopEnabled, reloadSupport, rakamakUseIp, noConsoleSpam, removePassword, displayOtherAccounts,
            useCaptcha, emailRegistration, multiverse, notifications, chestshop, bungee, banUnsafeIp, doubleEmailCheck, sessionExpireOnIpChange,
            disableSocialSpy, useMultiThreading, forceOnlyAfterLogin, useEssentialsMotd,
            usePurge, purgePlayerDat, purgeEssentialsFile, supportOldPassword, purgeLimitedCreative,
            purgeAntiXray, purgePermissions, enableProtection, enableAntiBot;
 
    public static String getNickRegex, getUnloggedinGroup, getMySQLHost, getMySQLPort, 
            getMySQLUsername, getMySQLPassword, getMySQLDatabase, getMySQLTablename, 
            getMySQLColumnName, getMySQLColumnPassword, getMySQLColumnIp, getMySQLColumnLastLogin,
            getMySQLColumnSalt, getMySQLColumnGroup, getMySQLColumnEmail, unRegisteredGroup, backupWindowsPath,
            getcUnrestrictedName, getRegisteredGroup, messagesLanguage, getMySQLlastlocX, getMySQLlastlocY, getMySQLlastlocZ,
            rakamakUsers, rakamakUsersIp, getmailAccount, getmailPassword, getmailSMTP, getMySQLColumnId, getmailSenderName, 
            getMailSubject, getMailText, getMySQLlastlocWorld, defaultWorld,
            getPhpbbPrefix, getWordPressPrefix;

    public static int getWarnMessageInterval, getSessionTimeout, getRegistrationTimeout, getMaxNickLength,
            getMinNickLength, getPasswordMinLen, getMovementRadius, getmaxRegPerIp, getNonActivatedGroup,
            passwordMaxLength, getRecoveryPassLength, getMailPort, maxLoginTry, captchaLength, saltLength, getmaxRegPerEmail,
            bCryptLog2Rounds, purgeDelay, getPhpbbGroup, antiBotSensibility, antiBotDuration;

    protected static YamlConfiguration configFile;

   public Settings(Plugin plugin) {
        this.file = new File(plugin.getDataFolder(),"config.yml");
        this.plugin = plugin;
        if(exists()) {
            load();
         }
        else {
            loadDefaults(file.getName());
            load();
        }
        configFile = (YamlConfiguration) plugin.getConfig();
    }

@SuppressWarnings("unchecked")
public void loadConfigOptions() {
        plugin.getLogger().info("Loading Configuration File...");
        mergeConfig();

        messagesLanguage = checkLang(configFile.getString("settings.messagesLanguage","en"));
        isPermissionCheckEnabled = configFile.getBoolean("permission.EnablePermissionCheck", false);
        isForcedRegistrationEnabled  = configFile.getBoolean("settings.registration.force", true);
        isRegistrationEnabled = configFile.getBoolean("settings.registration.enabled", true);
        isTeleportToSpawnEnabled = configFile.getBoolean("settings.restrictions.teleportUnAuthedToSpawn",false);
        getWarnMessageInterval = configFile.getInt("settings.registration.messageInterval",5);
        isSessionsEnabled = configFile.getBoolean("settings.sessions.enabled",false);
        getSessionTimeout = configFile.getInt("settings.sessions.timeout",10);
        getRegistrationTimeout = configFile.getInt("settings.restrictions.timeout",30);
        isChatAllowed = configFile.getBoolean("settings.restrictions.allowChat",false);
        getMaxNickLength = configFile.getInt("settings.restrictions.maxNicknameLength",20);
        getMinNickLength = configFile.getInt("settings.restrictions.minNicknameLength",3);
        getPasswordMinLen = configFile.getInt("settings.security.minPasswordLength",4);
        getNickRegex = configFile.getString("settings.restrictions.allowedNicknameCharacters","[a-zA-Z0-9_?]*");
        isAllowRestrictedIp = configFile.getBoolean("settings.restrictions.AllowRestrictedUser",false);
        getRestrictedIp = configFile.getStringList("settings.restrictions.AllowedRestrictedUser");
        isMovementAllowed = configFile.getBoolean("settings.restrictions.allowMovement",false);
        getMovementRadius = configFile.getInt("settings.restrictions.allowedMovementRadius",100);
        getJoinPermissions = configFile.getStringList("GroupOptions.Permissions.PermissionsOnJoin");
        isKickOnWrongPasswordEnabled = configFile.getBoolean("settings.restrictions.kickOnWrongPassword",false);
        isKickNonRegisteredEnabled = configFile.getBoolean("settings.restrictions.kickNonRegistered",false);
        isForceSingleSessionEnabled = configFile.getBoolean("settings.restrictions.ForceSingleSession",true);
        isForceSpawnLocOnJoinEnabled = configFile.getBoolean("settings.restrictions.ForceSpawnLocOnJoinEnabled",false);
        isSaveQuitLocationEnabled = configFile.getBoolean("settings.restrictions.SaveQuitLocation", false);
        isForceSurvivalModeEnabled = configFile.getBoolean("settings.GameMode.ForceSurvivalMode", false);
        isResetInventoryIfCreative = configFile.getBoolean("settings.GameMode.ResetInventoryIfCreative",false);
        getmaxRegPerIp = configFile.getInt("settings.restrictions.maxRegPerIp",1);
        getPasswordHash = getPasswordHash();
        getUnloggedinGroup = configFile.getString("settings.security.unLoggedinGroup","unLoggedInGroup");
        getDataSource = getDataSource();
        isCachingEnabled = configFile.getBoolean("DataSource.caching",true);
        getMySQLHost = configFile.getString("DataSource.mySQLHost","127.0.0.1");
        getMySQLPort = configFile.getString("DataSource.mySQLPort","3306");
        getMySQLUsername = configFile.getString("DataSource.mySQLUsername","authme");
        getMySQLPassword = configFile.getString("DataSource.mySQLPassword","12345");
        getMySQLDatabase = configFile.getString("DataSource.mySQLDatabase","authme");
        getMySQLTablename = configFile.getString("DataSource.mySQLTablename","authme");
        getMySQLColumnEmail = configFile.getString("DataSource.mySQLColumnEmail","email");
        getMySQLColumnName = configFile.getString("DataSource.mySQLColumnName","username");
        getMySQLColumnPassword = configFile.getString("DataSource.mySQLColumnPassword","password");
        getMySQLColumnIp = configFile.getString("DataSource.mySQLColumnIp","ip");
        getMySQLColumnLastLogin = configFile.getString("DataSource.mySQLColumnLastLogin","lastlogin");
        getMySQLColumnSalt = configFile.getString("ExternalBoardOptions.mySQLColumnSalt");
        getMySQLColumnGroup = configFile.getString("ExternalBoardOptions.mySQLColumnGroup","");
        getMySQLlastlocX = configFile.getString("DataSource.mySQLlastlocX","x");
        getMySQLlastlocY = configFile.getString("DataSource.mySQLlastlocY","y");
        getMySQLlastlocZ = configFile.getString("DataSource.mySQLlastlocZ","z");
        getMySQLlastlocWorld = configFile.getString("DataSource.mySQLlastlocWorld", "world");
        getNonActivatedGroup = configFile.getInt("ExternalBoardOptions.nonActivedUserGroup", -1);
        unRegisteredGroup = configFile.getString("GroupOptions.UnregisteredPlayerGroup","");
        getUnrestrictedName = configFile.getStringList("settings.unrestrictions.UnrestrictedName");
        getRegisteredGroup = configFile.getString("GroupOptions.RegisteredPlayerGroup","");
        getEnablePasswordVerifier = configFile.getBoolean("settings.restrictions.enablePasswordVerifier" , true);
        protectInventoryBeforeLogInEnabled = configFile.getBoolean("settings.restrictions.ProtectInventoryBeforeLogIn", true);
        passwordMaxLength = configFile.getInt("settings.security.passwordMaxLength", 20);
        isBackupActivated = configFile.getBoolean("BackupSystem.ActivateBackup",false);
        isBackupOnStart = configFile.getBoolean("BackupSystem.OnServerStart",false);
        isBackupOnStop = configFile.getBoolean("BackupSystem.OnServeStop",false);
        backupWindowsPath = configFile.getString("BackupSystem.MysqlWindowsPath", "C:\\Program Files\\MySQL\\MySQL Server 5.1\\");
        enablePasspartu = configFile.getBoolean("Passpartu.enablePasspartu",false);
        isStopEnabled = configFile.getBoolean("Security.SQLProblem.stopServer", true);
        reloadSupport = configFile.getBoolean("Security.ReloadCommand.useReloadCommandSupport", true);
        allowCommands = (List<String>) configFile.getList("settings.restrictions.allowCommands");        
        if (configFile.contains("allowCommands")) {
            if (!allowCommands.contains("/login"))
            	allowCommands.add("/login");
            if (!allowCommands.contains("/register"))
            	allowCommands.add("/register");
            if (!allowCommands.contains("/l"))
            	allowCommands.add("/l");
            if (!allowCommands.contains("/reg"))
            	allowCommands.add("/reg");
            if (!allowCommands.contains("/passpartu"))
            	allowCommands.add("/passpartu");
            if (!allowCommands.contains("/email"))
            	allowCommands.add("/email");
            if(!allowCommands.contains("/captcha"))
            	allowCommands.add("/captcha");
        }
        rakamakUsers = configFile.getString("Converter.Rakamak.fileName", "users.rak");
        rakamakUsersIp = configFile.getString("Converter.Rakamak.ipFileName", "UsersIp.rak");
        rakamakUseIp = configFile.getBoolean("Converter.Rakamak.useIp", false);
        rakamakHash = getRakamakHash();
        noConsoleSpam = configFile.getBoolean("Security.console.noConsoleSpam", false);
        removePassword = configFile.getBoolean("Security.console.removePassword", true);
        getmailAccount = configFile.getString("Email.mailAccount", "");
        getmailPassword = configFile.getString("Email.mailPassword", "");
        getmailSMTP = configFile.getString("Email.mailSMTP", "smtp.gmail.com");
        getMailPort = configFile.getInt("Email.mailPort", 465);
        getRecoveryPassLength = configFile.getInt("Email.RecoveryPasswordLength", 8);
        getMySQLOtherUsernameColumn = (List<String>) configFile.getList("ExternalBoardOptions.mySQLOtherUsernameColumns", new ArrayList<String>());
        displayOtherAccounts = configFile.getBoolean("settings.restrictions.displayOtherAccounts", true);
        getMySQLColumnId = configFile.getString("DataSource.mySQLColumnId", "id");
        getmailSenderName = configFile.getString("Email.mailSenderName", "");
        useCaptcha = configFile.getBoolean("Security.captcha.useCaptcha", false);
        maxLoginTry = configFile.getInt("Security.captcha.maxLoginTry", 5);
        captchaLength = configFile.getInt("Security.captcha.captchaLength", 5);
        getMailSubject = configFile.getString("Email.mailSubject", "Your new AuthMe Password");
        getMailText = configFile.getString("Email.mailText", "Dear <playername>, <br /><br /> This is your new AuthMe password for the server <br /><br /> <servername> : <br /><br /> <generatedpass><br /><br />Do not forget to change password after login! <br /> /changepassword <generatedpass> newPassword");
        emailRegistration = configFile.getBoolean("settings.registration.enableEmailRegistrationSystem", false);
        saltLength = configFile.getInt("settings.security.doubleMD5SaltLength", 8);
        getmaxRegPerEmail = configFile.getInt("Email.maxRegPerEmail", 1);
        multiverse = configFile.getBoolean("Hooks.multiverse", true);
        chestshop = configFile.getBoolean("Hooks.chestshop", true);
        notifications = configFile.getBoolean("Hooks.notifications", true);
        bungee = configFile.getBoolean("Hooks.bungeecord", false);
        getForcedWorlds = (List<String>) configFile.getList("settings.restrictions.ForceSpawnOnTheseWorlds");
        banUnsafeIp = configFile.getBoolean("settings.restrictions.banUnsafedIP", false);
        doubleEmailCheck = configFile.getBoolean("settings.registration.doubleEmailCheck", false);
        sessionExpireOnIpChange = configFile.getBoolean("settings.sessions.sessionExpireOnIpChange", false);
        useLogging = configFile.getBoolean("Security.console.logConsole", false);
        disableSocialSpy = configFile.getBoolean("Hooks.disableSocialSpy", true);
        useMultiThreading = configFile.getBoolean("Performances.useMultiThreading", true);
        bCryptLog2Rounds = configFile.getInt("ExternalBoardOptions.bCryptLog2Round", 10);
        forceOnlyAfterLogin = configFile.getBoolean("settings.GameMode.ForceOnlyAfterLogin", false);
        useEssentialsMotd = configFile.getBoolean("Hooks.useEssentialsMotd", false);
        usePurge = configFile.getBoolean("Purge.useAutoPurge", false);
        purgeDelay = configFile.getInt("Purge.daysBeforeRemovePlayer", 60);
        purgePlayerDat = configFile.getBoolean("Purge.removePlayerDat", false);
        purgeEssentialsFile = configFile.getBoolean("Purge.removeEssentialsFile", false);
        defaultWorld = configFile.getString("Purge.defaultWorld", "world");
        getPhpbbPrefix = configFile.getString("ExternalBoardOptions.phpbbTablePrefix", "phpbb_");
        getPhpbbGroup = configFile.getInt("ExternalBoardOptions.phpbbActivatedGroupId", 2);
        supportOldPassword = configFile.getBoolean("settings.security.supportOldPasswordHash", false);
        getWordPressPrefix = configFile.getString("ExternalBoardOptions.wordpressTablePrefix", "wp_");
        purgeLimitedCreative = configFile.getBoolean("Purge.removeLimitedCreativesInventories", false);
        purgeAntiXray = configFile.getBoolean("Purge.removeAntiXRayFile", false);
        //purgePermissions = configFile.getBoolean("Purge.removePermissions", false);
        enableProtection = configFile.getBoolean("Protection.enableProtection", false);
        countries = (List<String>) configFile.getList("Protection.countries");
        enableAntiBot = configFile.getBoolean("Protection.enableAntiBot", false);
        antiBotSensibility = configFile.getInt("Protection.antiBotSensibility", 5);
        antiBotDuration = configFile.getInt("Protection.antiBotDuration", 10);

        saveDefaults();
   }

@SuppressWarnings("unchecked")
public static void reloadConfigOptions(YamlConfiguration newConfig) {
       configFile = newConfig;

        messagesLanguage = checkLang(configFile.getString("settings.messagesLanguage","en"));
        isPermissionCheckEnabled = configFile.getBoolean("permission.EnablePermissionCheck", false);
        isForcedRegistrationEnabled = configFile.getBoolean("settings.registration.force", true);
        isRegistrationEnabled = configFile.getBoolean("settings.registration.enabled", true);
        isTeleportToSpawnEnabled = configFile.getBoolean("settings.restrictions.teleportUnAuthedToSpawn",false);
        getWarnMessageInterval = configFile.getInt("settings.registration.messageInterval",5);
        isSessionsEnabled = configFile.getBoolean("settings.sessions.enabled",false);
        getSessionTimeout = configFile.getInt("settings.sessions.timeout",10);
        getRegistrationTimeout = configFile.getInt("settings.restrictions.timeout",30);
        isChatAllowed = configFile.getBoolean("settings.restrictions.allowChat",false);
        getMaxNickLength = configFile.getInt("settings.restrictions.maxNicknameLength",20);
        getMinNickLength = configFile.getInt("settings.restrictions.minNicknameLength",3);
        getPasswordMinLen = configFile.getInt("settings.security.minPasswordLength",4);
        getNickRegex = configFile.getString("settings.restrictions.allowedNicknameCharacters","[a-zA-Z0-9_?]*");
        isAllowRestrictedIp = configFile.getBoolean("settings.restrictions.AllowRestrictedUser",false);
        getRestrictedIp = configFile.getStringList("settings.restrictions.AllowedRestrictedUser");
        isMovementAllowed = configFile.getBoolean("settings.restrictions.allowMovement",false);
        getMovementRadius = configFile.getInt("settings.restrictions.allowedMovementRadius",100);
        getJoinPermissions = configFile.getStringList("GroupOptions.Permissions.PermissionsOnJoin");
        isKickOnWrongPasswordEnabled = configFile.getBoolean("settings.restrictions.kickOnWrongPassword",false);
        isKickNonRegisteredEnabled = configFile.getBoolean("settings.restrictions.kickNonRegistered",false);
        isForceSingleSessionEnabled = configFile.getBoolean("settings.restrictions.ForceSingleSession",true);
        isForceSpawnLocOnJoinEnabled = configFile.getBoolean("settings.restrictions.ForceSpawnLocOnJoinEnabled",false);     
        isSaveQuitLocationEnabled = configFile.getBoolean("settings.restrictions.SaveQuitLocation",false);
        isForceSurvivalModeEnabled = configFile.getBoolean("settings.GameMode.ForceSurvivalMode",false);
        isResetInventoryIfCreative = configFile.getBoolean("settings.GameMode.ResetInventoryIfCreative",false);
        getmaxRegPerIp = configFile.getInt("settings.restrictions.maxRegPerIp",1);
        getPasswordHash = getPasswordHash();
        getUnloggedinGroup = configFile.getString("settings.security.unLoggedinGroup","unLoggedInGroup");
        getDataSource = getDataSource();
        isCachingEnabled = configFile.getBoolean("DataSource.caching",true);
        getMySQLHost = configFile.getString("DataSource.mySQLHost","127.0.0.1");
        getMySQLPort = configFile.getString("DataSource.mySQLPort","3306");
        getMySQLUsername = configFile.getString("DataSource.mySQLUsername","authme");
        getMySQLPassword = configFile.getString("DataSource.mySQLPassword","12345");
        getMySQLDatabase = configFile.getString("DataSource.mySQLDatabase","authme");
        getMySQLTablename = configFile.getString("DataSource.mySQLTablename","authme");
        getMySQLColumnEmail = configFile.getString("DataSource.mySQLColumnEmail","email");
        getMySQLColumnName = configFile.getString("DataSource.mySQLColumnName","username");
        getMySQLColumnPassword = configFile.getString("DataSource.mySQLColumnPassword","password");
        getMySQLColumnIp = configFile.getString("DataSource.mySQLColumnIp","ip");
        getMySQLColumnLastLogin = configFile.getString("DataSource.mySQLColumnLastLogin","lastlogin");
        getMySQLlastlocX = configFile.getString("DataSource.mySQLlastlocX","x");
        getMySQLlastlocY = configFile.getString("DataSource.mySQLlastlocY","y");
        getMySQLlastlocZ = configFile.getString("DataSource.mySQLlastlocZ","z");
        getMySQLlastlocWorld = configFile.getString("DataSource.mySQLlastlocWorld", "world");
        getMySQLColumnSalt = configFile.getString("ExternalBoardOptions.mySQLColumnSalt","");
        getMySQLColumnGroup = configFile.getString("ExternalBoardOptions.mySQLColumnGroup","");
        getNonActivatedGroup = configFile.getInt("ExternalBoardOptions.nonActivedUserGroup", -1);
        unRegisteredGroup = configFile.getString("GroupOptions.UnregisteredPlayerGroup","");
        getUnrestrictedName = configFile.getStringList("settings.unrestrictions.UnrestrictedName");
        getRegisteredGroup = configFile.getString("GroupOptions.RegisteredPlayerGroup",""); 
        getEnablePasswordVerifier = configFile.getBoolean("settings.restrictions.enablePasswordVerifier" , true);
        protectInventoryBeforeLogInEnabled = configFile.getBoolean("settings.restrictions.ProtectInventoryBeforeLogIn", true);
        passwordMaxLength = configFile.getInt("settings.security.passwordMaxLength", 20);
        isBackupActivated = configFile.getBoolean("BackupSystem.ActivateBackup",false);
        isBackupOnStart = configFile.getBoolean("BackupSystem.OnServerStart",false);
        isBackupOnStop = configFile.getBoolean("BackupSystem.OnServeStop",false);     
        backupWindowsPath = configFile.getString("BackupSystem.MysqlWindowsPath", "C:\\Program Files\\MySQL\\MySQL Server 5.1\\");
        enablePasspartu = configFile.getBoolean("Passpartu.enablePasspartu",false);
        isStopEnabled = configFile.getBoolean("Security.SQLProblem.stopServer", true);
        reloadSupport = configFile.getBoolean("Security.ReloadCommand.useReloadCommandSupport", true);
        allowCommands = (List<String>) configFile.getList("settings.restrictions.allowCommands");
        if (configFile.contains("allowCommands")) {
            if (!allowCommands.contains("/login"))
            	allowCommands.add("/login");
            if (!allowCommands.contains("/register"))
            	allowCommands.add("/register");
            if (!allowCommands.contains("/l"))
            	allowCommands.add("/l");
            if (!allowCommands.contains("/reg"))
            	allowCommands.add("/reg");
            if (!allowCommands.contains("/passpartu"))
            	allowCommands.add("/passpartu");
            if (!allowCommands.contains("/email"))
            	allowCommands.add("/email");
            if(!allowCommands.contains("/captcha"))
            	allowCommands.add("/captcha");
        }
        rakamakUsers = configFile.getString("Converter.Rakamak.fileName", "users.rak");
        rakamakUsersIp = configFile.getString("Converter.Rakamak.ipFileName", "UsersIp.rak");
        rakamakUseIp = configFile.getBoolean("Converter.Rakamak.useIp", false);
        rakamakHash = getRakamakHash();
        noConsoleSpam = configFile.getBoolean("Security.console.noConsoleSpam", false);
        removePassword = configFile.getBoolean("Security.console.removePassword", true);
        getmailAccount = configFile.getString("Email.mailAccount", "");
        getmailPassword = configFile.getString("Email.mailPassword", "");
        getmailSMTP = configFile.getString("Email.mailSMTP", "smtp.gmail.com");
        getMailPort = configFile.getInt("Email.mailPort", 465);
        getRecoveryPassLength = configFile.getInt("Email.RecoveryPasswordLength", 8);
        getMySQLOtherUsernameColumn = (List<String>) configFile.getList("ExternalBoardOptions.mySQLOtherUsernameColumns", new ArrayList<String>());
        displayOtherAccounts = configFile.getBoolean("settings.restrictions.displayOtherAccounts", true);
        getMySQLColumnId = configFile.getString("DataSource.mySQLColumnId", "id");
        getmailSenderName = configFile.getString("Email.mailSenderName", "");
        useCaptcha = configFile.getBoolean("Security.captcha.useCaptcha", false);
        maxLoginTry = configFile.getInt("Security.captcha.maxLoginTry", 5);
        captchaLength = configFile.getInt("Security.captcha.captchaLength", 5);
        getMailSubject = configFile.getString("Email.mailSubject", "Your new AuthMe Password");
        getMailText = configFile.getString("Email.mailText", "Dear <playername>, <br /><br /> This is your new AuthMe password for the server <br /><br /> <servername> : <br /><br /> <generatedpass><br /><br />Do not forget to change password after login! <br /> /changepassword <generatedpass> newPassword");
        emailRegistration = configFile.getBoolean("settings.registration.enableEmailRegistrationSystem", false);
        saltLength = configFile.getInt("settings.security.doubleMD5SaltLength", 8);
        getmaxRegPerEmail = configFile.getInt("Email.maxRegPerEmail", 1);
        multiverse = configFile.getBoolean("Hooks.multiverse", true);
        chestshop = configFile.getBoolean("Hooks.chestshop", true);
        notifications = configFile.getBoolean("Hooks.notifications", true);
        bungee = configFile.getBoolean("Hooks.bungeecord", false);
        getForcedWorlds = (List<String>) configFile.getList("settings.restrictions.ForceSpawnOnTheseWorlds");
        banUnsafeIp = configFile.getBoolean("settings.restrictions.banUnsafedIP", false);
        doubleEmailCheck = configFile.getBoolean("settings.registration.doubleEmailCheck", false);
        sessionExpireOnIpChange = configFile.getBoolean("settings.sessions.sessionExpireOnIpChange", false);
        useLogging = configFile.getBoolean("Security.console.logConsole", false);
        disableSocialSpy = configFile.getBoolean("Hooks.disableSocialSpy", true);
        useMultiThreading = configFile.getBoolean("Performances.useMultiThreading", true);
        bCryptLog2Rounds = configFile.getInt("ExternalBoardOptions.bCryptLog2Round", 10);
        forceOnlyAfterLogin = configFile.getBoolean("settings.GameMode.ForceOnlyAfterLogin", false);
        useEssentialsMotd = configFile.getBoolean("Hooks.useEssentialsMotd", false);
        usePurge = configFile.getBoolean("Purge.useAutoPurge", false);
        purgeDelay = configFile.getInt("Purge.daysBeforeRemovePlayer", 60);
        purgePlayerDat = configFile.getBoolean("Purge.removePlayerDat", false);
        purgeEssentialsFile = configFile.getBoolean("Purge.removeEssentialsFile", false);
        defaultWorld = configFile.getString("Purge.defaultWorld", "world");
        getPhpbbPrefix = configFile.getString("ExternalBoardOptions.phpbbTablePrefix", "phpbb_");
        getPhpbbGroup = configFile.getInt("ExternalBoardOptions.phpbbActivatedGroupId", 2);
        supportOldPassword = configFile.getBoolean("settings.security.supportOldPasswordHash", false);
        getWordPressPrefix = configFile.getString("ExternalBoardOptions.wordpressTablePrefix", "wp_");
        purgeLimitedCreative = configFile.getBoolean("Purge.removeLimitedCreativesInventories", false);
        purgeAntiXray = configFile.getBoolean("Purge.removeAntiXRayFile", false);
        //purgePermissions = configFile.getBoolean("Purge.removePermissions", false);
        enableProtection = configFile.getBoolean("Protection.enableProtection", false);
        countries = (List<String>) configFile.getList("Protection.countries");
        enableAntiBot = configFile.getBoolean("Protection.enableAntiBot", false);
        antiBotSensibility = configFile.getInt("Protection.antiBotSensibility", 5);
        antiBotDuration = configFile.getInt("Protection.antiBotDuration", 10);
}

public void mergeConfig() {
       if(!contains("DataSource.mySQLColumnEmail"))
    	   set("DataSource.mySQLColumnEmail","email");
       if(!contains("Email.RecoveryPasswordLength"))
    	   set("Email.RecoveryPasswordLength", 8);
       if(!contains("Email.mailPort"))
    	   set("Email.mailPort", 465);
       if(!contains("Email.mailSMTP"))
    	   set("Email.mailSMTP", "smtp.gmail.com");
       if(!contains("Email.mailAccount"))
    	   set("Email.mailAccount", "");
       if(!contains("Email.mailPassword"))
    	   set("Email.mailPassword", "");
       if(!contains("ExternalBoardOptions.mySQLOtherUsernameColumns"))
    	   set("ExternalBoardOptions.mySQLOtherUsernameColumns", new ArrayList<String>());
       if(!contains("settings.restrictions.displayOtherAccounts"))
    	   set("settings.restrictions.displayOtherAccounts", true);
       if(!contains("DataSource.mySQLColumnId"))
    	   set("DataSource.mySQLColumnId", "id");
       if(!contains("Email.mailSenderName"))
    	   set("Email.mailSenderName", "");
       if(!contains("Security.captcha.useCaptcha"))
    	   set("Security.captcha.useCaptcha", false);
       if(!contains("Security.captcha.maxLoginTry"))
    	   set("Security.captcha.maxLoginTry", 5);
       if(!contains("Security.captcha.captchaLength"))
    	   set("Security.captcha.captchaLength", 5);
       if(!contains("Email.mailSubject"))
    	   set("Email.mailSubject", "");
       if(!contains("Email.mailText"))
    	   set("Email.mailText", "Dear <playername>, <br /><br /> This is your new AuthMe password for the server <br /><br /> <servername> : <br /><br /> <generatedpass><br /><br />Do not forget to change password after login! <br /> /changepassword <generatedpass> newPassword");
       if(contains("Email.mailText")) {
    	   try {
        	   String s = getString("Email.mailText");
        	   s = s.replaceAll("\n", "<br />");
        	   set("Email.mailText", null);
        	   set("Email.mailText", s);
    	   } catch (Exception e) {}
       }
       if(!contains("settings.registration.enableEmailRegistrationSystem"))
    	   set("settings.registration.enableEmailRegistrationSystem", false);
       if(!contains("settings.security.doubleMD5SaltLength"))
    	   set("settings.security.doubleMD5SaltLength", 8);
       if(!contains("Email.maxRegPerEmail"))
    	   set("Email.maxRegPerEmail", 1);
       if(!contains("Hooks.multiverse")) {
    	   set("Hooks.multiverse", true);
           set("Hooks.chestshop", true);
           set("Hooks.notifications", true);
           set("Hooks.bungeecord", false);
       }
       if(!contains("settings.restrictions.ForceSpawnOnTheseWorlds"))
    	   set("settings.restrictions.ForceSpawnOnTheseWorlds", new ArrayList<String>());
       if(!contains("settings.restrictions.banUnsafedIP"))
    	   set("settings.restrictions.banUnsafedIP", false);
       if(!contains("settings.registration.doubleEmailCheck"))
    	   set("settings.registration.doubleEmailCheck", false);
       if(!contains("settings.sessions.sessionExpireOnIpChange"))
    	   set("settings.sessions.sessionExpireOnIpChange", false);
       if(!contains("Security.console.logConsole"))
    	   set("Security.console.logConsole", false);
       if(!contains("Hooks.disableSocialSpy"))
    	   set("Hooks.disableSocialSpy", true);
       if(!contains("Performances.useMultiThreading"))
    	   set("Performances.useMultiThreading", true);
       if(!contains("ExternalBoardOptions.bCryptLog2Round"))
    	   set("ExternalBoardOptions.bCryptLog2Round", 10);
       if(!contains("DataSource.mySQLlastlocWorld"))
    	   set("DataSource.mySQLlastlocWorld", "world");
       if(!contains("settings.GameMode.ForceOnlyAfterLogin"))
    	   set("settings.GameMode.ForceOnlyAfterLogin", false);
       if(!contains("Hooks.useEssentialsMotd"))
    	   set("Hooks.useEssentialsMotd", false);
       if(!contains("Purge.useAutoPurge")) {
    	   set("Purge.useAutoPurge", false);
    	   set("Purge.daysBeforeRemovePlayer", 60);
    	   set("Purge.removePlayerDat", false);
    	   set("Purge.removeEssentialsFile", false);
    	   set("Purge.defaultWorld", "world");
       }
       if(!contains("ExternalBoardOptions.phpbbTablePrefix")) {
    	   set("ExternalBoardOptions.phpbbTablePrefix", "phpbb_");
    	   set("ExternalBoardOptions.phpbbActivatedGroupId", 2);
       }
       if(!contains("settings.security.supportOldPasswordHash"))
    	   set("settings.security.supportOldPasswordHash", false);
       if(!contains("ExternalBoardOptions.wordpressTablePrefix"))
    	   set("ExternalBoardOptions.wordpressTablePrefix", "wp_");
       if(contains("Xenoforo.predefinedSalt"))
    	   set("Xenoforo.predefinedSalt", null);
       if(configFile.getString("settings.security.passwordHash","SHA256").toUpperCase().equals("XFSHA1") || configFile.getString("settings.security.passwordHash","SHA256").toUpperCase().equals("XFSHA256"))
    	   set("settings.security.passwordHash", "XENFORO");
       if(!contains("Purge.removeLimitedCreativesInventories"))
    	   set("Purge.removeLimitedCreativesInventories", false);
       if(!contains("Purge.removeAntiXRayFile"))
    	   set("Purge.removeAntiXRayFile", false);
       /*if(!contains("Purge.removePermissions"))
    	   set("Purge.removePermissions", false);*/
       if(!contains("Protection.enableProtection"))
    	   set("Protection.enableProtection", false);
       if(!contains("Protection.countries")) {
    	   countries = new ArrayList<String>();
    	   countries.add("US");
    	   countries.add("GB");
    	   set("Protection.countries", countries);
       }
       if(!contains("Protection.enableAntiBot"))
    	   set("Protection.enableAntiBot", false);
       if(!contains("Protection.antiBotSensibility"))
    	   set("Protection.antiBotSensibility", 5);
       if(!contains("Protection.antiBotDuration"))
    	   set("Protection.antiBotDuration", 10);
       
       plugin.getLogger().info("Merge new Config Options if needed..");
       plugin.saveConfig();

       return;
   }

    private static HashAlgorithm getPasswordHash() {
        String key = "settings.security.passwordHash";
        try {
            return HashAlgorithm.valueOf(configFile.getString(key,"SHA256").toUpperCase());
        } catch (IllegalArgumentException ex) {
            ConsoleLogger.showError("Unknown Hash Algorithm; defaulting to SHA256");
            return HashAlgorithm.SHA256;
        }
    }

    private static HashAlgorithm getRakamakHash() {
        String key = "Converter.Rakamak.newPasswordHash";

        try {
            return HashAlgorithm.valueOf(configFile.getString(key,"SHA256").toUpperCase());
        } catch (IllegalArgumentException ex) {
            ConsoleLogger.showError("Unknown Hash Algorithm; defaulting to SHA256");
            return HashAlgorithm.SHA256;
        }
    }

    private static DataSourceType getDataSource() {
        String key = "DataSource.backend";
        try {
            return DataSource.DataSourceType.valueOf(configFile.getString(key).toUpperCase());
        } catch (IllegalArgumentException ex) {
            ConsoleLogger.showError("Unknown database backend; defaulting to file database");
            return DataSource.DataSourceType.FILE;
        }
    }

    /**
     * Config option for setting and check restricted user by
     * username;ip , return false if ip and name doesnt amtch with
     * player that join the server, so player has a restricted access
    */   
    public static Boolean getRestrictedIp(String name, String ip) {

        Iterator<String> iter = getRestrictedIp.iterator();
        Boolean trueonce = false;
        Boolean namefound = false;
          while (iter.hasNext()) {
             String[] args =  iter.next().split(";");
             String testname = args[0];
             String testip = args[1];
             if(testname.equalsIgnoreCase(name) ) {
            	 namefound = true;
            	 if(testip.equalsIgnoreCase(ip)) {
            		 trueonce = true;
            	 };
             } 
          }   
       if ( namefound == false){
      	 return true;
       }
       	else { 
       		if ( trueonce == true ){
       		return true;
       	} else { 
       		return false;
       		}
       	}		
}

    /**
     * Loads the configuration from disk
     *
     * @return True if loaded successfully
     */
    public final boolean load() {
        try {
            load(file);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public final void reload() {
        load();
        loadDefaults(file.getName());
    }

    /**
     * Saves the configuration to disk
     *
     * @return True if saved successfully
     */
    public final boolean save() {
        try {
            save(file);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Simple function for if the Configuration file exists
     *
     * @return True if configuration exists on disk
     */
    public final boolean exists() {
        return file.exists();
    }

    /**
     * Loads a file from the plugin jar and sets as default
     *
     * @param filename The filename to open
     */
    public final void loadDefaults(String filename) {
        InputStream stream = plugin.getResource(filename);
        if(stream == null) return;

        setDefaults(YamlConfiguration.loadConfiguration(stream));
    }

    /**
     * Saves current configuration (plus defaults) to disk.
     *
     * If defaults and configuration are empty, saves blank file.
     *
     * @return True if saved successfully
     */
    public final boolean saveDefaults() {
        options().copyDefaults(true);
        options().copyHeader(true);
        boolean success = save();
        options().copyDefaults(false);
        options().copyHeader(false);
        return success;
    }

    /**
     * Clears current configuration defaults
     */
    public final void clearDefaults() {
        setDefaults(new MemoryConfiguration());
    }

/**
* Check loaded defaults against current configuration
*
* @return false When all defaults aren't present in config
*/
    public boolean checkDefaults() {
        if (getDefaults() == null) {
            return true;
        }
        return getKeys(true).containsAll(getDefaults().getKeys(true));
    }

    public static String checkLang(String lang) {
        for(messagesLang language: messagesLang.values()) {
            if(lang.toLowerCase().contains(language.toString())) {
                ConsoleLogger.info("Set Language: "+lang);
                return lang;
            }    
        }
        ConsoleLogger.info("Set Default Language: En ");
        return "en";
    }

    public static void switchAntiBotMod(boolean mode) {
    	if (mode)
    		isKickNonRegisteredEnabled = true;
    	else
    		isKickNonRegisteredEnabled = configFile.getBoolean("settings.restrictions.kickNonRegistered",false);
    }

    public enum messagesLang {
        en, de, br, cz, pl, fr, ru, hu, sk, es, zhtw, fi, zhcn, lt, it, ko, pt, nl
    }
}
