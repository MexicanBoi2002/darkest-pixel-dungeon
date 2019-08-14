/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Mending
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.messages.Messages

class PotionOfHealing : Potion() {

    init {
        initials = 2

        bones = true
    }

    override fun canBeReinforced(): Boolean {
        return !reinforced
    }

    override fun apply(hero: Hero) {
        setKnown()
        cure(Dungeon.hero)
    }

    private fun cure(hero: Hero) {
        Buff.detach(hero, Bleeding::class.java)

        if (reinforced) {
            hero.HP = Math.min(2 * hero.HT, hero.HT + hero.HP)

            Buff.detach(hero, Poison::class.java)
            Buff.detach(hero, Cripple::class.java)
            Buff.detach(hero, Weakness::class.java)
            Buff.detach(hero, Burning::class.java)

            GLog.p(Messages.get(this, "heal"))
        } else {
            val value = Math.min(hero.HT, hero.HT / 3 + 50)
            // directly recover some health, since buff is act later than chars
            val directRecover = value / 3
            hero.HP = Math.min(hero.HT, hero.HP + directRecover)

            val m = hero.buff(Mending::class.java)
            if (m != null) {
                m.set(m.recoveryValue + value - directRecover)
            } else {
                Buff.affect(hero, Mending::class.java).set(value - directRecover)
            }
        }

        hero.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 4)
    }

    override fun price(): Int {
        return if (isKnown)
            (30.0 * quantity.toDouble() * if (reinforced) 1.5 else 1).toInt()
        else
            super
                    .price()
    }

    companion object {

        fun heal(hero: Hero) {
            // called in water of healing, so kept
            hero.HP = hero.HT
            Buff.detach(hero, Poison::class.java)
            Buff.detach(hero, Cripple::class.java)
            Buff.detach(hero, Weakness::class.java)
            Buff.detach(hero, Bleeding::class.java)

            hero.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 4)
        }
    }
}
