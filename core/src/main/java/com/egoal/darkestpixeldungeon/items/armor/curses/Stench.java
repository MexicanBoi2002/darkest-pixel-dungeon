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
package com.egoal.darkestpixeldungeon.items.armor.curses;

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.items.armor.Armor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Stench extends Armor.Glyph {

  private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing(0x000000);

  @Override
  public Damage proc(Armor armor, Damage damage) {
    Char attacker = (Char) damage.from;
    Char defender = (Char) damage.to;

    if (Random.Int(8) == 0) {

      GameScene.add(Blob.seed(defender.getPos(), 250, ToxicGas.class));

    }

    return damage;
  }

  @Override
  public ItemSprite.Glowing glowing() {
    return BLACK;
  }

  @Override
  public boolean curse() {
    return true;
  }
}
