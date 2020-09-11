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
package com.egoal.darkestpixeldungeon.levels.traps

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Slow
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.sprites.TrapSprite

class WeakeningTrap : Trap() {

    init {
        color = TrapSprite.GREEN
        shape = TrapSprite.WAVES
    }

    override fun activate() {
        if (Dungeon.visible[pos]) {
            CellEmitter.get(pos).burst(ShadowParticle.UP, 5)
        }

        val ch = Actor.findChar(pos)
        if (ch === Dungeon.hero) {
            Buff.prolong(ch!!, Weakness::class.java, Weakness.duration(ch) * 2f)
        } else if (ch != null) {
            Buff.prolong(ch, Slow::class.java, Slow.duration(ch))
        }
    }
}
