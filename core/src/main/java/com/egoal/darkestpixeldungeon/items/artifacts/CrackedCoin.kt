package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import java.util.ArrayList
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

class CrackedCoin : Artifact() {
    init {
        image = ItemSpriteSheet.CRACKED_COIN
        levelCap = 10

        charge = 0
        chargeCap = 100

        defaultAction = AC_SHELL
        usesTargeting = true
    }

    private var shieldActived = false

    override fun desc(): String {
        var desc = super.desc()
        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n"
            desc += if (!cursed)
                M.L(this, "desc_hint") + "\n\n" + M.L(this, "desc_shield") + "\n" + M.L(this, "desc_shell")
            else M.L(this, "desc_cursed")
        }

        return desc
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero)) {
            actions.add(AC_SHIELD)
        }

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_SHIELD) {
            if (!shieldActived) {
                if (!isEquipped(hero))
                    GLog.i(M.L(Artifact::class.java, "need_to_equip"))
                else if (cursed) {
                    GLog.w(M.L(this, "cursed"))
                } else {
                    shieldActived = true
                    hero.spend(1f)
                    hero.busy()
                    Sample.INSTANCE.play(Assets.SND_MELD)
                    activeBuff = activeBuff()
                    activeBuff.attachTo(hero)
                    hero.sprite.operate(hero.pos)
                }
            } else {
                shieldActived = false
                activeBuff!!.detach()
                activeBuff = null
                hero.spend(1f)
                hero.sprite.operate(hero.pos)
            }
        } else if (action == AC_SHELL) {
            if (!isEquipped(hero))
                GLog.i(M.L(Artifact::class.java, "need_to_equip"))
            else if (cursed) {
                GLog.w(M.L(this, "cursed"))
            } else if (charge < chargeCap) {
                GLog.w(M.L(this, "not_charged"))
                QuickSlotButton.cancel()
            } else {
                GameScene.selectCell(dirSelector)
            }
        }
    }

    override fun activate(ch: Char) {
        super.activate(ch)
        if (shieldActived) {
            activeBuff = activeBuff()
            activeBuff.attachTo(ch)
        }
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        if (super.doUnequip(hero, collect, single)) {
            shieldActived = false
            return true
        } else
            return false
    }

    override fun passiveBuff(): ArtifactBuff = EatGold()

    override fun activeBuff(): ArtifactBuff = Shield()

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ACTIVED, shieldActived)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        shieldActived = bundle.getBoolean(ACTIVED)
    }

    inner class EatGold : ArtifactBuff() {
        override fun act(): Boolean {
            if (cursed) Dungeon.gold -= level()

            spend(10f)
            return true
        }
    }

    inner class Shield : Artifact.ArtifactBuff() {
        override fun icon(): Int = BuffIndicator.RESIST_ANY

        fun procTakenDamage(dmg: Damage) {
            val gold = min(Dungeon.gold, (dmg.value / 2 / dpg()).toInt())
            Dungeon.gold -= gold

            dmg.value -= round(gold * dpg()).toInt()
            if (charge < chargeCap) {
                charge += round(gold * 4f * 0.85f.pow(level())).toInt()
                if (charge >= chargeCap) {
                    charge = chargeCap
                    GLog.p(M.L(CrackedCoin::class.java, "charged"))
                }
                updateQuickslot()
            }
        }

        // damage per gold
        private fun dpg(): Float = 1f + 0.2f * level()

        override fun toString(): String = M.L(this, "name")
        override fun desc(): String = M.L(this, "desc", dpg())
    }

    private fun shellAt(pos: Int) {
        charge = 0
        updateQuickslot()
    }

    private val dirSelector = object : CellSelector.Listener {
        override fun prompt(): String = M.L(CrackedCoin::class.java, "prompt")

        override fun onSelect(cell: Int?) {
            if (cell != null && cell != Item.curUser.pos)
                shellAt(cell)
        }
    }

    companion object {
        private const val AC_SHIELD = "shield"
        private const val AC_SHELL = "shell"

        private const val ACTIVED = "actived"
    }
}