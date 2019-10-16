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

import com.egoal.darkestpixeldungeon.PropertyConfiger;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Charm;
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.GooWarn;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor;
import com.egoal.darkestpixeldungeon.actors.buffs.Ooze;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.artifacts.LloydsBeacon;
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.GooSprite;
import com.egoal.darkestpixeldungeon.ui.BossHealthBar;
import com.egoal.darkestpixeldungeon.utils.BArray;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.HashSet;

public class Goo extends Mob {

  {
    spriteClass = GooSprite.class;

    PropertyConfiger.INSTANCE.set(this, "Goo");

    loot = new LloydsBeacon().identify();
  }

  private int pumpedUp = 0;

  @Override
  public Damage giveDamage(Char target) {
    Damage dmg = new Damage(0, this, target);

    int min = 1;
    int max = (HP * 2 <= HT) ? 15 : 10;
    if (pumpedUp > 0) {
      // pumped attack
      pumpedUp = 0;
      PathFinder.buildDistanceMap(pos, BArray.not(Level.Companion.getSolid(), null), 2);
      for (int i = 0; i < PathFinder.distance.length; i++) {
        if (PathFinder.distance[i] < Integer.MAX_VALUE)
          CellEmitter.get(i).burst(ElmoParticle.FACTORY, 10);
      }
      Sample.INSTANCE.play(Assets.SND_BURNING);
      dmg.value = Random.NormalIntRange(min * 3, max * 3);
      dmg.addFeature(Damage.Feature.CRITICAL);
    } else {
      dmg.value = Random.NormalIntRange(min, max);
    }

    return dmg;
  }

  @Override
  public float attackSkill(Char target) {
    float attack = 10f;
    if (HP * 2 <= HT) attack = 15f;
    if (pumpedUp > 0) attack *= 2f;
    return attack;
  }

  @Override
  public float defenseSkill(Char enemy) {
    return super.defenseSkill(enemy) * ((HP * 2 <= HT) ? 1.5f : 1f);
  }

  @Override
  public boolean act() {
    // healing in the water, and update health bar animation
    if (Level.Companion.getWater()[pos] && HP < HT) {
      sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
      if (HP * 2 == HT) {
        BossHealthBar.bleed(false);
        ((GooSprite) sprite).spray(false);
      }
      HP++;
    }

    return super.act();
  }

  @Override
  protected boolean canAttack(Char enemy) {
    return (pumpedUp > 0) ? distance(enemy) <= 2 : super.canAttack(enemy);
  }

  @Override
  public Damage attackProc(Damage damage) {
    Char enemy = (Char) damage.to;
    if (!damage.isFeatured(Damage.Feature.CRITICAL) && Random.Int(3) == 0) {
      Buff.prolong(enemy, Vulnerable.class, 3).setRatio(1.25f);
      enemy.sprite.burst(0xFF0000, 5);
    }

    if (pumpedUp > 0) {
      Camera.main.shake(3, 0.2f);
    }

    return damage;
  }

  @Override
  public Damage defenseProc(Damage dmg) {
    if (pumpedUp == 0 && dmg.from instanceof Char &&
            !dmg.isFeatured(Damage.Feature.RANGED) && Random.Int(4) == 0) {
      Buff.affect((Char) dmg.from, Ooze.class);
      ((Char) dmg.from).sprite.burst(0x000000, 5);
    }

    return super.defenseProc(dmg);
  }

  @Override
  protected boolean doAttack(Char enemy) {
    if (pumpedUp == 1) {
      // pumped an extra turn
      ((GooSprite) sprite).pumpUp();
      PathFinder.buildDistanceMap(pos, BArray.not(Level.Companion.getSolid(), null), 2);
      for (int i = 0; i < PathFinder.distance.length; i++) {
        if (PathFinder.distance[i] < Integer.MAX_VALUE)
          GameScene.add(Blob.seed(i, 2, GooWarn.class));
      }
      pumpedUp++;

      spend(attackDelay());

      return true;
    } else if (pumpedUp >= 2 || Random.Int((HP * 2 <= HT) ? 2 : 6) > 0) {
      // pumped or life below half
      boolean visible = Dungeon.visible[pos];

      if (visible) {
        if (pumpedUp >= 2) {
          ((GooSprite) sprite).pumpAttack();
        } else // normal attack
          sprite.attack(enemy.pos);
      } else {
        attack(enemy);
      }

      spend(attackDelay());

      return !visible;

    } else {
      // increase pump
      pumpedUp++;

      ((GooSprite) sprite).pumpUp();

      for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
        int j = pos + PathFinder.NEIGHBOURS9[i];
        if (!Level.Companion.getSolid()[j]) {
          GameScene.add(Blob.seed(j, 2, GooWarn.class));
        }
      }

      if (Dungeon.visible[pos]) {
        sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "!!!"));
        GLog.n(Messages.get(this, "pumpup"));
      }

      spend(attackDelay());

      return true;
    }
  }

  @Override
  public boolean attack(Char enemy) {
    boolean result = super.attack(enemy);
    pumpedUp = 0;
    return result;
  }

  @Override
  protected boolean getCloser(int target) {
    pumpedUp = 0;
    return super.getCloser(target);
  }

  @Override
  public void move(int step) {
    Dungeon.level.seal();
    super.move(step);
  }

  @Override
  public int takeDamage(Damage dmg) {
    boolean bleeding = (HP * 2 <= HT);

    int val = super.takeDamage(dmg);
    if ((HP * 2 <= HT) && !bleeding) {
      BossHealthBar.bleed(true);
      GLog.w(Messages.get(this, "enraged_text"));
      sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "enraged"));
      ((GooSprite) sprite).spray(true);
      yell(Messages.get(this, "gluuurp"));
    }
    LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
    if (lock != null) lock.addTime(dmg.value * 2);

    return val;
  }

  @Override
  public void die(Object cause) {

    super.die(cause);

    Dungeon.level.unseal();

    GameScene.bossSlain();
    Dungeon.level.drop(new SkeletonKey(Dungeon.depth), pos).sprite.drop();

    Badges.validateBossSlain();

    yell(Messages.get(this, "defeated"));
  }

  @Override
  public void notice() {
    super.notice();
    BossHealthBar.assignBoss(this);
    yell(Messages.get(this, "notice"));
  }

  private final String PUMPEDUP = "pumpedup";

  @Override
  public void storeInBundle(Bundle bundle) {

    super.storeInBundle(bundle);

    bundle.put(PUMPEDUP, pumpedUp);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {

    super.restoreFromBundle(bundle);

    pumpedUp = bundle.getInt(PUMPEDUP);
    if (state != SLEEPING) BossHealthBar.assignBoss(this);
    if ((HP * 2 <= HT)) BossHealthBar.bleed(true);

  }

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {
    IMMUNITIES.add(Terror.class);
    IMMUNITIES.add(Corruption.class);
    IMMUNITIES.add(Charm.class);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    return IMMUNITIES;
  }
}
