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
package com.egoal.darkestpixeldungeon.levels.traps;

import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.Beam;
import com.egoal.darkestpixeldungeon.items.bags.Bag;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.TrapSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class DisintegrationTrap extends Trap {

  {
    color = TrapSprite.VIOLET;
    shape = TrapSprite.LARGE_DOT;
  }

  @Override
  public void activate() {

    if (Dungeon.visible[pos]) {
      sprite.parent.add(new Beam.DeathRay(DungeonTilemap.tileCenterToWorld
              (pos - 1),
              DungeonTilemap.tileCenterToWorld(pos + 1)));
      sprite.parent.add(new Beam.DeathRay(DungeonTilemap.tileCenterToWorld
              (pos - Dungeon.level.width()),
              DungeonTilemap.tileCenterToWorld(pos + Dungeon.level.width())));
      Sample.INSTANCE.play(Assets.SND_RAY);
    }

    Heap heap = Dungeon.level.getHeaps().get(pos);
    if (heap != null) heap.explode();

    Char ch = Actor.Companion.findChar(pos);
    if (ch != null) {
      ch.takeDamage(new Damage(Math.max(ch.getHT() / 5, Random.Int(ch.getHP() / 2, 2 *
              ch.getHP() / 3)),
              this, ch).addElement(Damage.Element.SHADOW));
      if (ch == Dungeon.hero) {
        Hero hero = (Hero) ch;
        if (!hero.isAlive()) {
          Dungeon.fail(getClass());
          GLog.n(Messages.get(this, "ondeath"));
        } else {
          Item item = hero.getBelongings().randomUnequipped();
          Bag bag = hero.getBelongings().getBackpack();
          //bags do not protect against this trap
          if (item instanceof Bag) {
            bag = (Bag) item;
            item = Random.element(bag.items);
          }
          if (item == null || item.level() > 0 || item.getUnique()) return;
          if (!item.getStackable()) {
            item.detachAll(bag);
            GLog.w(Messages.get(this, "one", item.name()));
          } else {
            int n = Random.NormalIntRange(1, (item.quantity() + 1) / 2);
            for (int i = 1; i <= n; i++)
              item.detach(bag);
            GLog.w(Messages.get(this, "some", item.name()));
          }
        }
      }
    }

  }
}
