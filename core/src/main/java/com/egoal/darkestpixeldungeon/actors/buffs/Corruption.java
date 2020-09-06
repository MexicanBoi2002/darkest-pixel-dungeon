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
package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;

public class Corruption extends Buff {

  {
    type = buffType.NEGATIVE;
  }

  private float buildToDamage = 0f;

  @Override
  public boolean attachTo(Char target) {
    target.setCamp(Char.Camp.HERO);
    return super.attachTo(target);
  }

  @Override
  public boolean act() {
    buildToDamage += target.getHT() / 200f;

    int damage = (int) buildToDamage;
    buildToDamage -= damage;

    if (damage > 0)
      target.takeDamage(new Damage(damage, this, target).addElement(Damage.Element.SHADOW));

    spend(Actor.TICK);

    return true;
  }

  @Override
  public void fx(boolean on) {
    if (on) target.getSprite().add(CharSprite.State.DARKENED);
    else if (target.getInvisible() == 0)
      target.getSprite().remove(CharSprite.State.DARKENED);
  }

  @Override
  public int icon() {
    return BuffIndicator.CORRUPT;
  }

  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc");
  }
}
