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
package com.egoal.darkestpixeldungeon.items.keys

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class SkeletonKey(_depth: Int = 0) : Key(_depth) {

    init {
        image = ItemSpriteSheet.SKELETON_KEY
        stackable = false
    }
    
    override fun doPickUp(hero: Hero): Boolean {
        Dungeon.hero.belongings.specialKeys[depth]++
        return super.doPickUp(hero)
    }

    override fun isSimilar(item: Item) = false
}
