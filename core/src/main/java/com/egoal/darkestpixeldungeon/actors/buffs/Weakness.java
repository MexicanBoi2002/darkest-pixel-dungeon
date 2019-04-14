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

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.rings.RingOfElements.Resistance;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;

public class Weakness extends FlavourBuff {

  private static final float DURATION = 20f;

  {
    type = buffType.NEGATIVE;
  }

  @Override
  public int icon() {
    return BuffIndicator.WEAKNESS;
  }

  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  @Override
  public boolean attachTo(Char target) {
    boolean attached = super.attachTo(target);
    
    if(attached && target instanceof Hero){
      ((Hero) target).setWeakened(true);
    }
    
    return attached;
  }

  @Override
  public void detach() {
    super.detach();
    if(target instanceof Hero)
      ((Hero) target).setWeakened(false);
  }

  public static float duration(Char ch) {
    Resistance r = ch.buff(Resistance.class);
    return r != null ? r.durationFactor() * DURATION : DURATION;
  }

  @Override
  public String heroMessage() {
    return Messages.get(this, "heromsg");
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc", dispTurns());
  }
}
