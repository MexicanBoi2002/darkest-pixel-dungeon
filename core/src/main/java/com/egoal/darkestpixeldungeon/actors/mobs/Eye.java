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

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.PropertyConfiger;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.TempPathLight;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.PurpleParticle;
import com.egoal.darkestpixeldungeon.items.unclassified.Dewdrop;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.EyeSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.HashSet;

public class Eye extends Mob {

  {
    PropertyConfiger.INSTANCE.set(this, "Eye");

    spriteClass = EyeSprite.class;

    setFlying(true);

    HUNTING = new Hunting();

    loot = new Dewdrop();
  }

  @Override
  public int viewDistance() {
    return 6;
  }

  @Override
  public Damage giveDamage(Char target) {
    return super.giveDamage(target).addElement(Damage.Element.SHADOW);
  }

  private Ballistica beam;
  private int beamTarget = -1;
  private int beamCooldown;
  public boolean beamCharged;

  @Override
  protected boolean canAttack(Char enemy) {

    if (beamCooldown == 0) {
      Ballistica aim = new Ballistica(getPos(), enemy.getPos(), Ballistica.STOP_TERRAIN);

      if (enemy.getInvisible() == 0 && Level.Companion.getFieldOfView()[enemy.getPos()] && aim.subPath
              (1, aim.dist).contains(enemy.getPos())) {
        beam = aim;
        beamTarget = aim.collisionPos;
        return true;
      } else
        //if the beam is charged, it has to attack, will aim at previous 
        // location of hero.
        return beamCharged;
    } else
      return super.canAttack(enemy);
  }

  @Override
  protected boolean act() {
    if (beam == null && beamTarget != -1) {
      beam = new Ballistica(getPos(), beamTarget, Ballistica.STOP_TERRAIN);
      getSprite().turnTo(getPos(), beamTarget);
    }
    if (beamCooldown > 0)
      beamCooldown--;
    return super.act();
  }

  @Override
  protected Char chooseEnemy() {
    if (beamCharged && enemy != null) return enemy;
    return super.chooseEnemy();
  }

  @Override
  protected boolean doAttack(Char enemy) {

    if (beamCooldown > 0) {
      return super.doAttack(enemy);
    } else if (!beamCharged) {
      ((EyeSprite) getSprite()).charge(enemy.getPos());
      spend(attackDelay() * 2f);
      beamCharged = true;
      return true;
    } else {

      spend(attackDelay());

      if (Dungeon.visible[getPos()]) {
        getSprite().zap(beam.collisionPos);
        return false;
      } else {
        deathGaze();
        return true;
      }
    }

  }

  @Override
  public int takeDamage(Damage dmg) {
    if (beamCharged) dmg.value /= 4;

    return super.takeDamage(dmg);
  }

  public void deathGaze() {
    if (!beamCharged || beamCooldown > 0 || beam == null)
      return;

    beamCharged = false;
    beamCooldown = Random.IntRange(3, 6);

    for (int pos : beam.subPath(1, beam.dist)) {

      if (Level.Companion.getFlamable()[pos]) {

        Dungeon.level.destroy(pos);
        GameScene.updateMap(pos);
      }

      Char ch = Actor.Companion.findChar(pos);
      if (ch == null) {
        continue;
      }

      Damage dmg = new Damage(Random.NormalIntRange(30, 50),
              this, ch).type(Damage.Type.MAGICAL);
      if (ch.checkHit(dmg)) {
        ch.takeDamage(dmg);

        if (Dungeon.visible[pos]) {
          ch.getSprite().flash();
          CellEmitter.center(pos).burst(PurpleParticle.BURST, Random.IntRange
                  (1, 2));
        }

        if (!ch.isAlive() && ch == Dungeon.hero) {
          Dungeon.fail(getClass());
          GLog.n(Messages.get(this, "deathgaze_kill"));
        }
      } else {
        ch.getSprite().showStatus(CharSprite.NEUTRAL, ch.defenseVerb());
      }
    }

    TempPathLight.Companion.Light(beam.path, 3f);

    beam = null;
    beamTarget = -1;
    getSprite().idle();
  }

  private static final String BEAM_TARGET = "beamTarget";
  private static final String BEAM_COOLDOWN = "beamCooldown";
  private static final String BEAM_CHARGED = "beamCharged";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(BEAM_TARGET, beamTarget);
    bundle.put(BEAM_COOLDOWN, beamCooldown);
    bundle.put(BEAM_CHARGED, beamCharged);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    if (bundle.contains(BEAM_TARGET))
      beamTarget = bundle.getInt(BEAM_TARGET);
    beamCooldown = bundle.getInt(BEAM_COOLDOWN);
    beamCharged = bundle.getBoolean(BEAM_CHARGED);
  }

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {
    IMMUNITIES.add(Terror.class);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    return IMMUNITIES;
  }

  private class Hunting extends Mob.Hunting {
    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
      //always attack if the beam is charged, no exceptions
      if (beamCharged && enemy != null)
        enemyInFOV = true;
      return super.act(enemyInFOV, justAlerted);
    }
  }
}
