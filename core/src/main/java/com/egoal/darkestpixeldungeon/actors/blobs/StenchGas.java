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
package com.egoal.darkestpixeldungeon.actors.blobs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.effects.BlobEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.messages.Messages;

public class StenchGas extends Blob {

  @Override
  protected void evolve() {
    super.evolve();

    Char ch;
    int cell;

    for (int i = area.left; i < area.right; i++) {
      for (int j = area.top; j < area.bottom; j++) {
        cell = i + j * Dungeon.level.width();
        if (cur[cell] > 0 && (ch = Actor.Companion.findChar(cell)) != null) {
          if (!ch.immunizedBuffs().contains(this.getClass()))
            Buff.prolong(ch, Paralysis.class, Paralysis.duration(ch) / 5);
        }
      }
    }
  }

  @Override
  public void use(BlobEmitter emitter) {
    super.use(emitter);

    emitter.pour(Speck.factory(Speck.STENCH), 0.4f);
  }

  @Override
  public String tileDesc() {
    return Messages.get(this, "desc");
  }
}
