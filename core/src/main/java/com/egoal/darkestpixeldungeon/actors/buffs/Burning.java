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

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfEnchanting;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.Fire;
import com.egoal.darkestpixeldungeon.actors.mobs.Thief;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.armor.glyphs.Brimstone;
import com.egoal.darkestpixeldungeon.items.food.ChargrilledMeat;
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat;
import com.egoal.darkestpixeldungeon.items.rings.RingOfResistance.Resistance;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Burning extends Buff implements Hero.Doom {

  private static final float DURATION = 8f;

  private float left;
  private boolean burnedSomething = false;

  private static final String LEFT = "left";
  private static final String BURNED = "burned";

  {
    type = buffType.NEGATIVE;
  }

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(LEFT, left);
    bundle.put(BURNED, burnedSomething);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    left = bundle.getFloat(LEFT);
    burnedSomething = bundle.getBoolean(BURNED);
  }

  @Override
  public boolean act() {

    if (target.isAlive()) {

      //maximum damage scales from 6 to 2 depending on remaining hp.
      int maxDmg = 3 + Math.round(4 * target.HP / (float) target.HT);
      int damage = Random.Int(1, maxDmg);
      detach(target, Chill.class);

      if (target instanceof Hero) {

        Hero hero = (Hero) target;

        if (hero.getBelongings().armor != null && hero.getBelongings().armor.hasGlyph
                (Brimstone.class)) {
          // wear armor with brimstone
          
          float heal = hero.getBelongings().armor.level() / 5f;
          if (Random.Float() < heal % 1) heal++;
          if (heal >= 1 && hero.HP < hero.HT) {
            hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), (int) 
                    heal);
            hero.HP = Math.min(hero.HT, hero.HP + (int) heal);
          }

        } else {
          hero.takeDamage(new Damage(damage, this, hero).type(Damage.Type
                  .MAGICAL).addElement(Damage.Element.FIRE));

          // burn something
          if (!burnedSomething) {
            Item item = hero.getBelongings().randomUnequipped();
            if (item instanceof Scroll
                    && !(item instanceof ScrollOfUpgrade)) {

              item = item.detach(hero.getBelongings().backpack);
              GLog.w(Messages.get(this, "burnsup", Messages.capitalize(item
                      .toString())));

              Heap.Companion.burnFX(hero.pos);

              burnedSomething = true;
            } else if (item instanceof MysteryMeat) {

              item = item.detach(hero.getBelongings().backpack);
              ChargrilledMeat steak = new ChargrilledMeat();
              if (!steak.collect(hero.getBelongings().backpack)) {
                Dungeon.level.drop(steak, hero.pos).getSprite().drop();
              }
              GLog.w(Messages.get(this, "burnsup", item.toString()));

              Heap.Companion.burnFX(hero.pos);

              burnedSomething = true;
            }
          }
        }

      } else {
        // target.damage( damage, this );
        target.takeDamage(new Damage(damage, this, target).type(Damage.Type
                .MAGICAL).addElement(Damage.Element.FIRE));
      }

      if (target instanceof Thief) {

        Item item = ((Thief) target).item;

        if (item instanceof Scroll &&
                !(item instanceof ScrollOfUpgrade)) {
          target.sprite.emitter().burst(ElmoParticle.FACTORY, 6);
          ((Thief) target).item = null;
        }

      }

    } else {
      detach();
    }

    if (Level.Companion.getFlamable()[target.pos]) {
      GameScene.add(Blob.seed(target.pos, 4, Fire.class));
    }

    spend(TICK);
    left -= TICK;

    if (left <= 0 ||
            (Level.Companion.getWater()[target.pos] && !target.flying)) {

      detach();
    }

    return true;
  }

  public void reignite(Char ch) {
    left = duration(ch);
  }

  @Override
  public int icon() {
    return BuffIndicator.FIRE;
  }

  @Override
  public void fx(boolean on) {
    if (on) target.sprite.add(CharSprite.State.BURNING);
    else target.sprite.remove(CharSprite.State.BURNING);
  }

  @Override
  public String heroMessage() {
    return Messages.get(this, "heromsg");
  }

  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  public static float duration(Char ch) {
    Resistance r = ch.buff(Resistance.class);
    return r != null ? r.durationFactor() * DURATION : DURATION;
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc", dispTurns(left));
  }

  @Override
  public void onDeath() {

    Badges.validateDeathFromFire();

    Dungeon.fail(getClass());
    GLog.n(Messages.get(this, "ondeath"));
  }
}
