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

import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.effects.BlobEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.plants.Plant;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.watabou.utils.Random;

public class Fire extends Blob {

  @Override
  protected void evolve() {

    boolean[] flamable = Level.Companion.getFlamable();
    int cell;
    int fire;

    boolean observe = false;

    for (int i = area.left - 1; i <= area.right; i++) {
      for (int j = area.top - 1; j <= area.bottom; j++) {
        cell = i + j * Dungeon.level.width();
        if (cur[cell] > 0) {

          burn(cell);

          fire = cur[cell] - 1;
          if (flamable[cell] && Random.Int(fire + 1) == 0) {

            int oldTile = Dungeon.level.getMap()[cell];
            Dungeon.level.destroy(cell);

            observe = true;
            GameScene.updateMap(cell);
            if (Dungeon.visible[cell]) {
              GameScene.discoverTile(cell, oldTile);
            }
          }

        } else {

          // expand, 
          // since the outer box must be wall, no need to do border check.
          if (flamable[cell] && 
                  (cur[cell - 1] > 0 || cur[cell + 1] > 0
                  || cur[cell - Dungeon.level.width()] > 0
                  || cur[cell + Dungeon.level.width()] > 0)) {
            fire = 4;
            burn(cell);
            area.union(i, j);
          } else {
            fire = 0;
          }

        }

        volume += (off[cell] = fire);
      }
    }

    if (observe) {
      Dungeon.observe();
    }
  }

  protected void burn(int pos) {
    Char ch = Actor.findChar(pos);
    if (ch != null) {
      Buff.affect(ch, Burning.class).reignite(ch);
    }

    Heap heap = Dungeon.level.getHeaps().get(pos);
    if (heap != null) {
      heap.burn();
    }

    Plant plant = Dungeon.level.getPlants().get(pos);
    if (plant != null) {
      plant.wither();
    }
  }

  @Override
  public void use(BlobEmitter emitter) {
    super.use(emitter);
    emitter.start(FlameParticle.FACTORY, 0.03f, 0);
  }

  @Override
  public String tileDesc() {
    return Messages.get(this, "desc");
  }
}
