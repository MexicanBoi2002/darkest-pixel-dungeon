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

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.watabou.utils.Random;

public class Bestiary {

  public static Mob mob(int depth) {
    @SuppressWarnings("unchecked")
    Class<? extends Mob> cl = (Class<? extends Mob>) mobClass(depth);
    try {
      return cl.newInstance();
    } catch (Exception e) {
      DarkestPixelDungeon.reportException(e);
      return null;
    }
  }

  public static Mob mutable(int depth) {
    @SuppressWarnings("unchecked")
    Class<? extends Mob> cl = (Class<? extends Mob>) mobClass(depth);

    if (Random.Int(30) == 0) {
      if (cl == Rat.class) {
        cl = Albino.class;
      } else if (cl == Thief.class) {
        cl = Bandit.class;
      } else if (cl == Brute.class) {
        cl = Shielded.class;
      } else if (cl == Monk.class) {
        cl = Senior.class;
      } else if (cl == Scorpio.class) {
        cl = Acidic.class;
      }
    }

    try {
      return cl.newInstance();
    } catch (Exception e) {
      DarkestPixelDungeon.reportException(e);
      return null;
    }
  }

  private static Class<?> mobClass(int depth) {
    // mobs can created in each depth
    float[] chances;
    Class<?>[] classes;

    switch (depth) {
      case 1:
        chances = new float[]{1};
        classes = new Class<?>[]{Rat.class};
        break;
      case 2:
        chances = new float[]{1, 1};
        classes = new Class<?>[]{Rat.class, Gnoll.class};
        break;
      case 3:
        chances = new float[]{2, 4, 1, 1};
        classes = new Class<?>[]{Rat.class, Gnoll.class, Crab.class, Swarm
                .class};
        break;
      case 4:
        chances = new float[]{1, 2, 3, 1, 0.02f};
        classes = new Class<?>[]{Rat.class, Gnoll.class, Crab.class, Swarm
                .class, MadMan.class};
        break;

      case 5:
        chances = new float[]{1};
        classes = new Class<?>[]{Goo.class};
        break;

      case 6:
        chances = new float[]{3, 1, 1, 0.2f};
        classes = new Class<?>[]{Skeleton.class, Thief.class, Swarm.class,
                Shaman.class};
        break;
      case 7:
        chances = new float[]{3, 1, 1, 1, .2f};
        classes = new Class<?>[]{Skeleton.class, Shaman.class, Thief.class,
                Guard.class, MadMan.class};
        break;
      case 8:
        chances = new float[]{3, 2, 2, 1, .5f, 0.02f};
        classes = new Class<?>[]{Skeleton.class, Shaman.class, Guard.class,
                Thief.class, MadMan.class, Bat.class};
        break;
      case 9:
        chances = new float[]{3, 2, 2, 1, 1, 0.02f};
        classes = new Class<?>[]{Skeleton.class, Guard.class, Shaman.class,
                Thief.class, Bat.class, SkeletonKnight.class,};
        break;

      case 10:
        chances = new float[]{1};
        classes = new Class<?>[]{Tengu.class};
        break;
      case 11:
        chances = new float[]{1, .5f, 0.2f};
        classes = new Class<?>[]{Bat.class, SkeletonKnight.class, Brute.class};
        break;
      case 12:
        chances = new float[]{1, 1, .5f, .2f};
        classes = new Class<?>[]{Bat.class, Brute.class, SkeletonKnight
                .class, MadMan.class};
        break;
      case 13:
        chances = new float[]{1, .5f, .2f, 3, 1, 1, 0.3f, 0.02f, .02f};
        classes = new Class<?>[]{Bat.class, SkeletonKnight.class, MadMan
                .class, Brute.class, Shaman.class, Spinner.class,
                Ballista.class, Elemental.class, Monk.class};
        break;
      case 14:
        chances = new float[]{1, .75f, 3, 4, 1f, 0.02f, 0.01f};
        classes = new Class<?>[]{Bat.class, SkeletonKnight.class, Brute
                .class, Spinner.class, Ballista.class, Elemental.class, Monk
                .class};
        break;

      case 15:
        chances = new float[]{1};
        classes = new Class<?>[]{DM300.class};
        break;

      case 16:
        chances = new float[]{1, 1, 0.2f};
        classes = new Class<?>[]{Elemental.class, Warlock.class, Monk.class};
        break;
      case 17:
        chances = new float[]{1, 1, 1, .25f};
        classes = new Class<?>[]{Elemental.class, Monk.class, Warlock.class,
                MadMan.class};
        break;
      case 18:
        chances = new float[]{1, 2, 1, 1, .25f};
        classes = new Class<?>[]{Elemental.class, Monk.class, Golem.class,
                Warlock.class, MadMan.class};
        break;
      case 19:
        chances = new float[]{1, 2, 3, 1, 0.02f};
        classes = new Class<?>[]{Elemental.class, Monk.class, Golem.class,
                Warlock.class, Succubus.class};
        break;

      case 20:
        chances = new float[]{1};
        classes = new Class<?>[]{King.class};
        break;

      case 22:
        chances = new float[]{1, 1, .1f};
        classes = new Class<?>[]{Succubus.class, Eye.class, MadMan.class};
        break;
      case 23:
        chances = new float[]{1, 2, 1, .1f};
        classes = new Class<?>[]{Succubus.class, Eye.class, Scorpio.class,
                MadMan.class};
        break;
      case 24:
        chances = new float[]{1, 2, 3};
        classes = new Class<?>[]{Succubus.class, Eye.class, Scorpio.class};
        break;

      case 25:
        chances = new float[]{1};
        classes = new Class<?>[]{Yog.class};
        break;

      default:
        chances = new float[]{1};
        classes = new Class<?>[]{Eye.class};
    }

    return classes[Random.chances(chances)];
  }
}
