package red.man10.man10itembank

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import red.man10.man10itembank.menu.MainMenu
import red.man10.man10itembank.util.Utility
import red.man10.man10itembank.util.Utility.sendError
import red.man10.man10itembank.util.Utility.sendMsg

object Command : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (label=="mib"){
            if (sender !is Player)return false
            if (!Utility.hasUserPermission(sender))return false

            if (args.size == 2 && args[0] == "drop"){
                val isOn = args[1] == "on"
                val list = Man10ItemBank.plugin.config.getStringList("noDropItems")
                if (isOn) {
                    if (!list.contains(sender.uniqueId.toString())) {
                        sendError(sender, "すでに落とすようになっています")
                        return true
                    }
                    list.remove(sender.uniqueId.toString())
                    Man10ItemBank.plugin.config.set("noDropItems", list)
                    Man10ItemBank.plugin.saveConfig()
                    sendMsg(sender, "mibで違うアイテムを入れたときに落とすようになりました")
                } else {
                    if (list.contains(sender.uniqueId.toString())) {
                        sendError(sender, "すでに落とさないようになっています")
                        return true
                    }
                    list.add(sender.uniqueId.toString())
                    Man10ItemBank.plugin.config.set("noDropItems", list)
                    Man10ItemBank.plugin.saveConfig()
                    sendMsg(sender, "mibで違うアイテムを入れたときに落とさないようになりました")
                }
                return true
            }

            MainMenu(sender).open()
        }

        if (label=="mibop"){ mibop(sender, args) }

        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String>? {
        if (args.isEmpty()){
            return null
        }

        if (args.size == 1){
            return mutableListOf("drop")
        }

        if (args.size == 2 && args[0] == "drop"){
            return mutableListOf("on","off")
        }

        return null
    }

    private fun mibop(sender: CommandSender, args: Array<out String>?){

        if (sender is Player && !Utility.hasOPPermission(sender))return

        if (args.isNullOrEmpty()){

            sendMsg(sender,"§d§lMan10ItemBank")
            sendMsg(sender,"§d§l----------運営用コマンド----------")
            sendMsg(sender,"§d§l/mibop register <識別名> <初期価格> <最低取引単位>   : 新アイテムを登録")
            sendMsg(sender,"§d§l/mibop unregister <id>                          : アイテムを削除")
            sendMsg(sender,"§d§l/mibop list                                     : 登録アイテム一覧 ")
            sendMsg(sender,"§d§l/mibop item <id>                                : アイテムのコピーを取得 ")
            sendMsg(sender,"§d§l----------指定プレイヤーのアイテムバンクを操作----------")
            sendMsg(sender,"§d§l/mibop add <player> <id> <amount>")
            sendMsg(sender,"§d§l/mibop take <player> <id> <amount>")
            sendMsg(sender,"§d§l/mibop set <player> <id> <amount>")
            sendMsg(sender,"§d§l/mibop show <player> <id>")

            return
        }

        when(args[0]){

            "queue" ->{
                sendMsg(sender,ItemData.getQueueSize().toString())

            }

            "register" ->{
                if (args.size!=4){
                    sendError(sender,"/mibop register <識別名> <初期価格> <Tick>")
                    return
                }

                if (sender !is Player){
                    sendError(sender,"このコマンドは、プレイヤーでないと実行できません")
                    return
                }

                val key = args[1]
                val initialPrice = args[2].toDoubleOrNull()
                val tick = args[3].toDoubleOrNull()

                if (initialPrice == null || tick == null){
                    sendError(sender,"入力に誤りがあります")
                    return
                }

                val item = sender.inventory.itemInMainHand

                if (item.amount == 0 || item.type == Material.AIR){
                    sendError(sender,"メインハンドにアイテムを持ってください")
                    return
                }

                ItemData.registerItem(sender,key,item.asOne(),initialPrice, tick){
                    when(it){

                        ItemData.EnumResult.SUCCESSFUL ->{
                            sendMsg(sender,"登録成功！")
                        }
                        ItemData.EnumResult.FAILED->{
                            sendError(sender,"失敗！同一識別名のアイテムが既に登録されている可能性があります！")
                        }
                    }
                }

            }

            "unregister" ->{
                if (args.size != 2){
                    sendError(sender,"/mib unregister <id>")
                    return
                }

                val id = args[1].toIntOrNull()

                if (id == null){
                    sendError(sender,"入力に誤りがあります")
                    return
                }

                if (sender !is Player){
                    sendError(sender,"このコマンドは、プレイヤーでないと実行できません")
                    return
                }

                ItemData.unregisterItem(sender,id){
                    when(it){

                        ItemData.EnumResult.SUCCESSFUL ->{
                            sendMsg(sender,"削除成功")
                        }
                        ItemData.EnumResult.FAILED->{
                            sendError(sender,"削除失敗")
                        }

                    }
                }
            }

            "list" ->{

                val list = ItemData.getItemIndexMap()

                for (data in list.values){
                    sendMsg(sender,"§e§lID:${data.id} Key:${data.itemKey}")
                }
            }

            "item" ->{

                if (args.size != 2){
                    sendError(sender,"/mib item <id>")
                    return
                }

                val id = args[1].toIntOrNull()

                if (id == null){
                    sendError(sender,"入力に誤りがあります")
                    return
                }

                if (sender !is Player){
                    sendError(sender,"このコマンドは、プレイヤーでないと実行できません")
                    return
                }

                val data = ItemData.getItemData(id)

                if (data==null){
                    sendError(sender,"存在しないIDです")
                    return
                }

                sender.inventory.addItem(data.item!!.clone())

            }

            //mibop add <player> <id> <amount>
            "add" ->{
                if (args.size!=4){
                    sendError(sender,"/mibop add <player> <id> <amount>")
                    return
                }

                val order = if (sender !is Player)null else sender.uniqueId
                val p = Bukkit.getPlayer(args[1])
                val id = args[2].toIntOrNull()
                val amount = args[3].toIntOrNull()

                if (p==null){
                    sendError(sender,"プレイヤーがオフラインです")
                    return
                }

                if (id ==null || amount == null){
                    sendError(sender,"入力に誤りがあります")
                    return
                }

                ItemData.addItemAmount(order,p.uniqueId,id,amount) {

                    if (it == -1){
                        sendError(sender,"失敗！")
                        return@addItemAmount
                    }

                    sendMsg(sender,"成功！現在の在庫数:${it}")
                }
            }

            "take" ->{

                if (args.size!=4){
                    sendError(sender,"/mibop take <player> <id> <amount>")
                    return
                }

                val order = if (sender !is Player)null else sender.uniqueId
                val p = Bukkit.getPlayer(args[1])
                val id = args[2].toIntOrNull()
                val amount = args[3].toIntOrNull()

                if (p==null){
                    sendError(sender,"プレイヤーがオフラインです")
                    return
                }

                if (id ==null || amount == null){
                    sendError(sender,"入力に誤りがあります")
                    return
                }

                ItemData.takeItemAmount(order,p.uniqueId,id,amount) {

                    if (it == -1){
                        sendError(sender,"失敗！")
                        return@takeItemAmount
                    }

                    sendMsg(sender,"成功！現在の在庫数:${it}")
                }
            }

            "set" ->{

                if (args.size!=4){
                    sendError(sender,"/mibop set <player> <id> <amount>")
                    return
                }

                val order = if (sender !is Player)null else sender.uniqueId
                val p = Bukkit.getPlayer(args[1])
                val id = args[2].toIntOrNull()
                val amount = args[3].toIntOrNull()

                if (p==null){
                    sendError(sender,"プレイヤーがオフラインです")
                    return
                }

                if (id ==null || amount == null){
                    sendError(sender,"入力に誤りがあります")
                    return
                }

                ItemData.setItemAmount(order,p.uniqueId,id,amount) {

                    if (it == -1){
                        sendError(sender,"失敗！")
                        return@setItemAmount
                    }

                    sendMsg(sender,"成功！現在の在庫数:${it}")
                }


            }

            "show" ->{

                if (args.size!=4){
                    sendError(sender,"/mibop show <player> <id>")
                    return
                }

                val p = Bukkit.getPlayer(args[1])
                val id = args[2].toIntOrNull()

                if (p==null){
                    sendError(sender,"プレイヤーがオフラインです")
                    return
                }

                if (id ==null){
                    sendError(sender,"入力に誤りがあります")
                    return
                }

                ItemData.getItemAmount(p.uniqueId,id) {

                    if (it == -1){
                        sendError(sender,"失敗！")
                        return@getItemAmount
                    }

                    sendMsg(sender,"現在の在庫数:${it}")
                }


            }

        }

    }

}