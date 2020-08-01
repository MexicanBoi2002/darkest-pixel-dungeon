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
package com.egoal.darkestpixeldungeon.items.armor.glyphs

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.sprites.ItemSprite

class Swiftness : Armor.Glyph() {

    //no proc effect, see hero.defenseskill and hero.speed for effect.
    override fun proc(armor: Armor, damage: Damage): Damage = damage

    override fun tierDRAdjust(): Int = -2

    override fun tierSTRAdjust(): Float = -1f

    override fun glowing(): ItemSprite.Glowing = YELLOW

    companion object {
        private val YELLOW = ItemSprite.Glowing(0xFFFF00)
    }

}
