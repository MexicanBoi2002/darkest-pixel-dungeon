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

import com.egoal.darkestpixeldungeon.actors.buffs.Amok;
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility;
import com.egoal.darkestpixeldungeon.actors.mobs.Mimic;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class ScrollOfRage extends Scroll {

  {
    initials = 6;
  }

  @Override
  protected void doRead() {

    for (Mob mob : Dungeon.level.getMobs().toArray(new Mob[0])) {
      mob.beckon(curUser.pos);
      if (Level.Companion.getFieldOfView()[mob.pos]) {
        Buff.prolong(mob, Amok.class, 5f);
      }
    }

    for (Heap heap : Dungeon.level.getHeaps().values()) {
      if (heap.getType() == Heap.Type.MIMIC) {
        Mimic m = Mimic.Companion.SpawnAt(heap.getPos(), heap.getItems());
        if (m != null) {
          m.beckon(curUser.pos);
          heap.destroy();
        }
      }
    }

    GLog.w(Messages.get(this, "roar"));
    setKnown();

    curUser.sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.3f, 3);
    Sample.INSTANCE.play(Assets.SND_CHALLENGE);
    Invisibility.dispel();

    readAnimation();
  }

  @Override
  public int price() {
    return isKnown() ? 30 * quantity : super.price();
  }
}
