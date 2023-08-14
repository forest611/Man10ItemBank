package red.man10.man10itembank

import org.bukkit.plugin.java.JavaPlugin
import red.man10.man10itembank.menu.MenuFramework
import red.man10.man10itembank.util.MySQLManager
import java.util.UUID

class Man10ItemBank : JavaPlugin() {

    companion object{
        lateinit var plugin : JavaPlugin
        var denyAutoCollectUsers = mutableListOf<UUID>()
    }

    override fun onEnable() {
        // Plugin startup logic

        plugin = this

        saveDefaultConfig()

        getCommand("mib")!!.setExecutor(Command)
        getCommand("mibop")!!.setExecutor(Command)

        server.pluginManager.registerEvents(MenuFramework.MenuListener,this)
        server.pluginManager.registerEvents(Event,this)

        MySQLManager.runAsyncMySQLQueue(this,"Man10ItemBank")
        ItemData.getQueueSize()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}