package red.man10.man10itembank.menu

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import red.man10.man10itembank.ItemData
import red.man10.man10itembank.Man10ItemBank
import red.man10.man10itembank.util.Utility.format
import red.man10.man10itembank.util.Utility.sendMsg

class PutMenu(p:Player) : MenuFramework(p,54,"アイテムを保存する"){

    init {
        val putButton = Button(Material.RED_STAINED_GLASS_PANE)
        putButton.title("§c§l保存")

        putButton.setClickAction{e ->
            putItemToItemStorage(p,e.inventory)
        }

        arrayOf(45,46,47,48,49,50,51,52,53).forEach { setButton(putButton,it) }

        setCloseListener{e->
            putItemToItemStorage(p,e.inventory)
        }
    }

    private fun putItemToItemStorage(p:Player, menu:Inventory){

        val putData = HashMap<Int,Int>()

        for (i in 0 until 45){

            val item = menu.getItem(i)?:continue
            val data = ItemData.getItemData(item)

            if (data == null){
                if (!Man10ItemBank.plugin.config.getStringList("noDropItems").contains(p.uniqueId.toString())){
                    p.world.dropItem(p.location, item) {
                        it.owner = p.uniqueId
                        it.setCanMobPickup(false)
                        it.pickupDelay = 0
                    }
                }
                continue
            }

            val amount = putData[data.id]?:0

            putData[data.id] = amount+item.amount

            item.amount = 0
        }

        putData.forEach {
            ItemData.addItemAmount(p.uniqueId,p.uniqueId,it.key,it.value){ result ->

                val data = ItemData.getItemData(it.key)!!
                sendMsg(p,"${data.itemKey}を${format(it.value)}個追加しました(現在の在庫:${format(result)}個)")
            }
        }

    }

}