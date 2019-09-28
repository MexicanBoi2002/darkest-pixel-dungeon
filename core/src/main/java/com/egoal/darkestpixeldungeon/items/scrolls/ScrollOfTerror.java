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
package com.egoal.darkestpixeldungeon.items.scrolls;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.Flare;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class ScrollOfTerror extends Scroll {

  {
    initials = 10;
  }

  @Override
  protected void doRead() {

    new Flare(5, 32).color(0xFF0000, true).show(curUser.sprite, 2f);
    Sample.INSTANCE.play(Assets.SND_READ);
    Invisibility.dispel();

    int count = 0;
    Mob affected = null;
    for (Mob mob : Dungeon.level.getMobs().toArray(new Mob[0])) {
      if (Level.Companion.getFieldOfView()[mob.pos]) {
        Buff.affect(mob, Terror.class, Terror.DURATION).object = curUser.id();

        if (mob.buff(Terror.class) != null) {
          count++;
          affected = mob;
        }
      }
    }

    switch (count) {
      case 0:
        GLog.i(Messages.get(this, "none"));
        break;
      case 1:
        GLog.i(Messages.get(this, "one", affected.name));
        break;
      default:
        GLog.i(Messages.get(this, "many"));
    }
    setKnown();

    readAnimation();
  }

  @Override
  public int price() {
    return isKnown() ? 30 * quantity : super.price();
  }
}
