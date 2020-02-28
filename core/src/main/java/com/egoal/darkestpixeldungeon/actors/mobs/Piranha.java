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
package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.Statistics;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.actors.blobs.VenomGas;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Frost;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.buffs.Roots;
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.sprites.PiranhaSprite;
import com.watabou.utils.Random;

import java.util.HashSet;

public class Piranha extends Mob {

  {
    spriteClass = PiranhaSprite.class;

    baseSpeed = 2f;

    EXP = 0;
    
    addResistances(Damage.Element.LIGHT, -0.5f);
  }

  public Piranha() {
    super();

    HP = HT = 10 + Dungeon.depth * 5;
    defSkill = 10 + Dungeon.depth * 2;
  }

  @Override
  protected boolean act() {
    if (!Level.Companion.getWater()[pos]) {
      die(null);
      sprite.killAndErase();
      return true;
    } else {
      //this causes pirahna to move away when a door is closed on them.
      Dungeon.level.updateFieldOfView(this, Level.Companion.getFieldOfView());
      enemy = chooseEnemy();
      if (state == this.HUNTING &&
              !(enemy != null && enemy.isAlive() && Level.Companion.getFieldOfView()[enemy
                      .pos] && enemy.invisible <= 0)) {
        state = this.WANDERING;
        int oldPos = pos;
        int i = 0;
        do {
          i++;
          target = Dungeon.level.randomDestination();
          if (i == 100) return true;
        } while (!getCloser(target));
        moveSprite(oldPos, pos);
        return true;
      }

      return super.act();
    }
  }

  @Override
  public Damage giveDamage(Char target) {
    return new Damage(damageRoll(), this, target);
  }

  int damageRoll() {
    return Random.NormalIntRange(Dungeon.depth, 4 + Dungeon.depth * 2);
  }

  @Override
  public float attackSkill(Char target) {
    return 20 + Dungeon.depth * 2;
  }

  @Override
  public Damage defendDamage(Damage dmg) {
    dmg.value -= drRoll();
    return dmg;
  }

  int drRoll() {
    return Random.NormalIntRange(0, Dungeon.depth);
  }

  @Override
  public void die(Object cause) {
    Dungeon.level.drop(new MysteryMeat(), pos).getSprite().drop();
    super.die(cause);

    Statistics.INSTANCE.setPiranhasKilled(Statistics.INSTANCE.getPiranhasKilled()+1);
    Badges.INSTANCE.validatePiranhasKilled();
  }

  @Override
  public boolean reset() {
    return true;
  }

  @Override
  protected boolean getCloser(int target) {

    if (rooted) {
      return false;
    }

    int step = Dungeon.findStep(this, pos, target,
            Level.Companion.getWater(),
            Level.Companion.getFieldOfView());
    if (step != -1) {
      move(step);
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected boolean getFurther(int target) {
    int step = Dungeon.flee(this, pos, target,
            Level.Companion.getWater(),
            Level.Companion.getFieldOfView());
    if (step != -1) {
      move(step);
      return true;
    } else {
      return false;
    }
  }

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {
    IMMUNITIES.add(Burning.class);
    IMMUNITIES.add(Paralysis.class);
    IMMUNITIES.add(ToxicGas.class);
    IMMUNITIES.add(VenomGas.class);
    IMMUNITIES.add(Roots.class);
    IMMUNITIES.add(Frost.class);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    return IMMUNITIES;
  }
}
