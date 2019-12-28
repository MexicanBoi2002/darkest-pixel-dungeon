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

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.ConfusionGas
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Levitation
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample

class PotionOfLevitation : Potion() {
    init {
        initials = 4
    }

    override fun shatter(cell: Int) {

        if (Dungeon.visible[cell]) {
            setKnown()

            splash(cell)
            Sample.INSTANCE.play(Assets.SND_SHATTER)
        }

        GameScene.add(Blob.seed(cell, 1000, ConfusionGas::class.java))
    }

    override fun apply(hero: Hero) {
        setKnown()
        Buff.affect(hero, Levitation::class.java, Levitation.DURATION)
        GLog.i(Messages.get(this, "float"))
    }

    override fun price(): Int {
        return if (isKnown) (30 * quantity * if (reinforced) 1.5f else 1f).toInt() else super.price()
    }
}
