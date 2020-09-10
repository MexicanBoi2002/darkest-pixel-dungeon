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
package com.egoal.darkestpixeldungeon.items.weapon.enchantments;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Poison;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.PoisonParticle;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Venomous extends Weapon.Enchantment {

  private static ItemSprite.Glowing PURPLE = new ItemSprite.Glowing(0x4400AA);

  @Override
  public Damage proc(Weapon weapon, Damage damage) {
    Char defender = (Char) damage.to;
    Char attacker = (Char) damage.from;
    // lvl 0 - 33%
    // lvl 1 - 50%
    // lvl 2 - 60%
    int level = Math.max(0, weapon.level());

    if (Random.Int(level + 3) >= 2) {

      Buff.Companion.affect(defender, Poison.class).extend(Poison.Companion.durationFactor
              (defender) * ((level / 2) + 1));
      CellEmitter.center(defender.getPos()).burst(PoisonParticle.SPLASH, 5);

    }

    return damage.addElement(Damage.Element.POISON);
  }

  @Override
  public Glowing glowing() {
    return PURPLE;
  }
}
