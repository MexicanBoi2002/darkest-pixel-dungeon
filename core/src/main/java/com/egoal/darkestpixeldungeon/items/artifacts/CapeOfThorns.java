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
package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import javax.microedition.khronos.opengles.GL;

//* check in Hero::takeDamage
public class CapeOfThorns extends Artifact {

  {
    image = ItemSpriteSheet.ARTIFACT_CAPE;

    levelCap = 10;

    charge = 0;
    chargeCap = 100;
    cooldown = 0;

    defaultAction = "NONE"; //so it can be quickslotted
  }

  @Override
  protected ArtifactBuff passiveBuff() {
    return new Thorns();
  }

  @Override
  public String desc() {
    String desc = Messages.get(this, "desc");
    if (isEquipped(Dungeon.hero)) {
      desc += "\n\n";
      if (cooldown == 0)
        desc += Messages.get(this, "desc_inactive");
      else
        desc += Messages.get(this, "desc_active");
    }

    return desc;
  }

  public class Thorns extends ArtifactBuff {

    @Override
    public boolean act() {
      if (cooldown > 0) {
        cooldown--;
        if (cooldown == 0) {
          BuffIndicator.refreshHero();
          GLog.w(Messages.get(this, "inert"));
        }
        updateQuickslot();
      }
      spend(Actor.TICK);
      return true;
    }

    public Damage proc(Damage dmg){
      Hero hero = (Hero)dmg.to;
      if(hero.STR()<16) return dmg;

      if(cooldown==0){
        // getting charged
        charge  +=  dmg.value*(.5+level()*.05);
        if(charge>=chargeCap){
          charge  = 0;
          cooldown  = 10+level();
          GLog.p(Messages.get(this, "radiating"));
          BuffIndicator.refreshHero();
        }
      }else{
        // has the buff
        int deflected = Random.NormalIntRange(0, dmg.value);
        dmg.value -=  deflected;
        
        if(dmg.from instanceof Mob && Dungeon.level.adjacent(((Mob) dmg.from).getPos(), hero.getPos()))
          ((Mob) dmg.from).takeDamage(new Damage(deflected, dmg.to, dmg.from));
        
        exp += deflected;
        int requireExp  = (level()+1)*5;
        if(exp>=requireExp && level()<levelCap){
          exp -=  requireExp;
          upgrade();
          GLog.p(Messages.get(this, "levelup"));
        }
      }
      
      updateQuickslot();
      
      return dmg;
    }

    @Override
    public String toString() {
      return Messages.get(this, "name");
    }

    @Override
    public String desc() {
      return Messages.get(this, "desc", dispTurns(cooldown));
    }

    @Override
    public int icon() {
      if (cooldown == 0)
        return BuffIndicator.NONE;
      else
        return BuffIndicator.THORNS;
    }

    @Override
    public void detach() {
      cooldown = 0;
      charge = 0;
      super.detach();
    }

  }


}
