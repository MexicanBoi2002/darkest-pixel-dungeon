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
package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

abstract public class KindOfWeapon extends EquipableItem {

  protected static final float TIME_TO_EQUIP = 1f;

  @Override
  public boolean isEquipped(Hero hero) {
    return hero.getBelongings().getWeapon() == this;
  }

  @Override
  public boolean doEquip(Hero hero) {

    detachAll(hero.getBelongings().getBackpack());

    if (hero.getBelongings().getWeapon() == null || hero.getBelongings().getWeapon().doUnequip
            (hero, true)) {

      hero.getBelongings().setWeapon(this);
      activate(hero);

      updateQuickslot();

      cursedKnown = true;
      if (cursed) {
        equipCursed(hero);
        GLog.n(Messages.get(KindOfWeapon.class, "cursed"));
      }

      hero.spendAndNext(TIME_TO_EQUIP);
      return true;

    } else {

      collect(hero.getBelongings().getBackpack());
      return false;
    }
  }

  @Override
  public boolean doUnequip(Hero hero, boolean collect, boolean single) {
    if (super.doUnequip(hero, collect, single)) {

      hero.getBelongings().setWeapon(null);
      return true;

    } else {

      return false;

    }
  }

  public int min() {
    return min(level());
  }

  public int max() {
    return max(level());
  }

  abstract public int min(int lvl);

  abstract public int max(int lvl);

  // damage attach to normal attack, called in give damage
  public Damage giveDamage(Hero owner, Char target) {
    return new Damage(Random.NormalIntRange(min(), max()), owner, target);
  }

  public float accuracyFactor(Hero hero, Char target) {
    return 1f;
  }
  
  public float speedFactor(Hero hero) {
    return 1f;
  }

  public int reachFactor(Hero hero) {
    return 1;
  }

  public Damage defendDamage(Damage dmg){
    return dmg;
  }
  
  // process, called in attackProc
  public Damage proc(Damage dmg) {
    return dmg;
  }
}
