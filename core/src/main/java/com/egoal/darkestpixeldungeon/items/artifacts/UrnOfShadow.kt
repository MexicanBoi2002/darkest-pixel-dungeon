package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.Window
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.IconTitle
import com.egoal.darkestpixeldungeon.windows.WndTitledMessage
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.lang.RuntimeException
import java.util.ArrayList
import kotlin.math.round

class UrnOfShadow : Artifact() {
    init {
        image = ItemSpriteSheet.URN_OF_SHADOW
        unique = true
        defaultAction = AC_CONSUME

        levelCap = 10
    }

    var volume: Int = 0

    private val isFull: Boolean get() = volume == MAX_VOLUME

    override fun isUpgradable(): Boolean = false // no by SoU

    override fun upgrade(): Item {
        GLog.p(M.L(this, "Levelup"))
        return super.upgrade()
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply {
        if (isEquipped(hero) && volume > 0) add(AC_CONSUME)
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_CONSUME) {
            if (!isEquipped(hero)) GLog.i(M.L(Artifact::class.java, "need_to_equip"))
            else GameScene.show(WndUrnOfShadow())
        }
    }

    fun collectSoul(mob: Mob) {
        Item.curUser = Dungeon.hero
        if (!isEquipped(Item.curUser)) return

        if (Dungeon.level.distance(Item.curUser.pos, mob.pos) > COLLECT_RANGE) return

        if (!mob.hostile || mob.properties().contains(Char.Property.UNDEAD)) return

        if (isFull) {
            GLog.w(M.L(this, "full"))
        } else {
            volume += 1
            updateQuickslot()

            GLog.i(M.L(this, "collected", mob.name))
            CellEmitter.get(Item.curUser.pos).burst(ShadowParticle.CURSE, 5)
            Sample.INSTANCE.play(Assets.SND_BURNING)
        }
    }

    fun consume(value: Int) {
        volume -= value
        updateQuickslot()
    }

    override fun status(): String = "$volume"

    override fun desc(): String = super.desc() + "\n\n" + M.L(this, "desc_hint")

    private fun cost(spell: String): Int {
        return when (spell) {
            CAST_SOUL_BURN -> 3
            CAST_SOUL_MARK -> 5
            CAST_DEMENTAGE -> 10
            else -> throw RuntimeException("undefined spell.")
        }
    }

    private fun cast(spell: String) {
        when (spell) {
            CAST_SOUL_BURN -> castSoulBurn()
            CAST_SOUL_MARK -> castSoulMark()
            CAST_DEMENTAGE -> castDementage()
            else -> throw RuntimeException("undefined spell.")
        }
    }

    private fun castSoulBurn() {
        caster.onChar = { ch: Char ->
            if (ch === Item.curUser) GLog.w(M.L(UrnOfShadow::class.java, "not_yourself"))
            else {
                volume -= 3
                Item.curUser.sprite.zap(ch.pos)
                Item.curUser.spend(1f)
                Item.curUser.busy()

                MagicMissile.shadow(Item.curUser.sprite.parent, Item.curUser.pos, ch.pos) {
                    val value = 5 + Random.NormalIntRange(Dungeon.depth, Dungeon.depth * 2) + level() * 5
                    val dmg = Damage(value, Item.curUser, ch).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.ACCURATE)
                    ch.takeDamage(dmg)
                    Buff.affect(ch, SoulBurning::class.java).reignite(ch)
                    Item.curUser.next()
                }
                Sample.INSTANCE.play(Assets.SND_ZAP)
            }
        }

        GameScene.selectCell(caster)
    }

    private fun castSoulMark() {
        caster.onChar = { ch: Char ->
            if (ch === Item.curUser) GLog.w(M.L(UrnOfShadow::class.java, "not_yourself"))
            else {
                volume -= 5
                Item.curUser.sprite.zap(ch.pos)
                Item.curUser.spend(1f)
                Item.curUser.busy()

                MagicMissile.shadow(Item.curUser.sprite.parent, Item.curUser.pos, ch.pos) {
                    Buff.prolong(ch, SoulMark::class.java, SoulMark.DURATION * Math.pow(1.1, level().toDouble()).toFloat()).level = 1 + level()
                    Item.curUser.next()
                }
                Sample.INSTANCE.play(Assets.SND_ZAP)
            }
        }

        GameScene.selectCell(caster)
    }

    private fun castDementage() {
        caster.onChar = { ch: Char ->
            if (ch === Item.curUser) GLog.w(M.L(UrnOfShadow::class.java, "not_yourself"))
            else if (ch.buff(Corruption::class.java) != null) GLog.w(M.L(UrnOfShadow::class.java, "already_dementage"))
            else if (ch.properties().contains(Char.Property.BOSS) || ch.properties().contains(Char.Property.MINIBOSS))
                GLog.w(M.L(UrnOfShadow::class.java, "boss"))
            else if (ch is Mob && (!ch.hostile || ch.properties().contains(Char.Property.UNDEAD) ||
                            ch.immunizedBuffs().contains(Dementage::class.java)))
                GLog.w(M.L(UrnOfShadow::class.java, "no_soul"))
            else {
                volume -= 10
                Item.curUser.sprite.zap(ch.pos)
                Item.curUser.spend(1f)
                Item.curUser.busy()

                MagicMissile.shadow(Item.curUser.sprite.parent, Item.curUser.pos, ch.pos) {
                    Buff.append(ch, Dementage::class.java)
                    ch.HT += (ch.HT * 0.1f * level()).toInt()
                    ch.HP = ch.HT
                    GLog.i(M.L(UrnOfShadow::class.java, "sucess_dementage", ch.name))
                    Item.curUser.next()
                }
                Sample.INSTANCE.play(Assets.SND_ZAP)
            }
        }

        GameScene.selectCell(caster)
    }

    private val caster = object : CellSelector.Listener {
        var onChar: ((Char) -> Unit)? = null

        override fun onSelect(cell: Int?) {
            if (cell != null) {
                val shot = Ballistica(Item.curUser.pos, cell, Ballistica.MAGIC_BOLT)
                val c = Actor.findChar(shot.collisionPos)
                if (c != null) {
                    if (onChar != null) onChar!!(c)
                } else
                    GLog.w(M.L(UrnOfShadow::class.java, "not_select_target"))
            }
        }

        override fun prompt(): String = M.L(UrnOfShadow::class.java, "prompt")
    }

    override fun passiveBuff(): ArtifactBuff = Urn()

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(VOLUME, volume)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        volume = bundle.getInt(VOLUME)
    }

    inner class Urn : ArtifactBuff()

    companion object {
        private const val MAX_VOLUME = 10
        private const val COLLECT_RANGE = 6f

        private const val AC_CONSUME = "CONSUME"
        private const val VOLUME = "volume"

        ///
        private const val WIN_WIDTH = 80f
        private const val BTN_HEIGHT = 20f
        private const val GAP = 2f

        private const val CAST_SOUL_BURN = "soul_burn"
        private const val CAST_SOUL_MARK = "soul_mark"
        private const val CAST_DEMENTAGE = "dementage"

        private const val WIDTH_CAST_BUTTON = 60f
        private const val WIDTH_HELP_BUTTON = 15f

        private const val TIME_TO_CAST = 1
    }

    inner class WndUrnOfShadow : Window() {
        init {
            val title = IconTitle().apply {
                icon(ItemSprite(image(), null))
                label(M.T(name()))
                setRect(0f, 0f, WIN_WIDTH, 0f)
            }
            add(title)

            var y = title.bottom()
            y = addCastAndHelpButton(CAST_SOUL_BURN, y + GAP)
            y = addCastAndHelpButton(CAST_SOUL_MARK, y + GAP)
            y = addCastAndHelpButton(CAST_DEMENTAGE, y + GAP)

            resize(WIN_WIDTH.toInt(), (y + GAP).toInt())
        }

        private fun addCastAndHelpButton(spell: String, y: Float): Float {
            val cost = cost(spell)

            val btnCast = object : RedButton(M.L(UrnOfShadow::class.java, spell)) {
                override fun onClick() {
                    hide()
                    this@UrnOfShadow.cast(spell)
                }
            }.apply {
                setRect(0f, y, WIDTH_CAST_BUTTON, BTN_HEIGHT)
                enable(cost <= volume)
            }
            add(btnCast)

            val btnHelp = object : RedButton("?") {
                override fun onClick() {
                    GameScene.show(WndTitledMessage(ItemSprite(image(), null),
                            M.L(UrnOfShadow::class.java, spell),
                            M.L(UrnOfShadow::class.java, spell + "_desc") + M.L(UrnOfShadow::class.java, "cost", cost)))
                }
            }.apply { setRect(WIN_WIDTH - WIDTH_HELP_BUTTON, btnCast.top(), WIDTH_HELP_BUTTON, BTN_HEIGHT) }
            add(btnHelp)

            return btnCast.bottom()
        }
    }
}