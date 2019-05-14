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
package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class Greatshield : MeleeWeapon() {

    init {
        image = ItemSpriteSheet.GREATSHIELD

        tier = 5
    }

    //12 base, down from 30, +3 per level, down from +6
    override fun max(lvl: Int): Int = 2 * (tier + 1) + lvl * (tier - 2)

    override fun defendDamage(dmg: Damage): Damage {
        var value = 10 + 3 * level()

        val burden = STRReq() - (dmg.to as Hero).STR()
        if (burden > 0) value -= 2 * burden

        if (value > 0) {
            if (dmg.type == Damage.Type.NORMAL)
                dmg.value -= value
            else if (dmg.type == Damage.Type.MAGICAL)
                dmg.value -= value * 4 / 5
        }

        return dmg
    }
}
