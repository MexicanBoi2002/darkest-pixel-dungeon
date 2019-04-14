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

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.utils.GLog

class PotionOfMight : Potion() {

    init {
        initials = 6

        bones = true
    }

    override fun apply(hero: Hero) {
        setKnown()

        hero.STR++
        hero.HT += 6
        hero.HP += 6
        hero.sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "msg_1"))
        GLog.p(Messages.get(this, "msg_2"))

        Badges.validateStrengthAttained()
    }

    override fun price(): Int =
            if (isKnown) (100f * quantity.toFloat() * if (reinforced) 1.5f else 1f).toInt()
            else super.price()
}
