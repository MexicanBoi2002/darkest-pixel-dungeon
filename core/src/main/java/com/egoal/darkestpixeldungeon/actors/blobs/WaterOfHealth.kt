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
package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.ShaftParticle
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.items.unclassified.DewVial
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.items.unclassified.Rune
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

class WaterOfHealth : WellWater() {

    override fun affectHero(hero: Hero): Boolean {

        Sample.INSTANCE.play(Assets.SND_DRINK)

        PotionOfHealing.heal(hero)
        hero.belongings.uncurseEquipped()
        (hero.buff(Hunger::class.java) as Hunger).satisfy(Hunger.STARVING)
        hero.recoverSanity(Random.IntRange(8, 12))

        CellEmitter.get(pos).start(ShaftParticle.FACTORY, 0.2f, 3)

        Dungeon.hero.interrupt()

        GLog.p(Messages.get(this, "procced"))

        Journal.remove(Journal.Feature.WELL_OF_HEALTH)

        return true
    }

    override fun affectItem(item: Item): Item? {
        // throw vial to full fill it
        if (item is DewVial && (!item.full || item.rune == null)) {
            item.fill()
            if (item.rune == null)
                item.collectRune(Generator.RUNE.generate() as Rune)
            
            Journal.remove(Journal.Feature.WELL_OF_HEALTH)
            return item
        }

        return null
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)
        emitter.start(Speck.factory(Speck.HEALING), 0.5f, 0)
    }

    override fun tileDesc(): String = Messages.get(this, "desc")

}
