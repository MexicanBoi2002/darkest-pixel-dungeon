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
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.blobs.Web;
import com.egoal.darkestpixeldungeon.actors.buffs.Poison;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Roots;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat;
import com.egoal.darkestpixeldungeon.items.unclassified.SpiderGland;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.SpinnerSprite;
import com.watabou.utils.Random;

import java.util.HashSet;

public class Spinner extends Mob {

    {
        PropertyConfiger.INSTANCE.set(this, "Spinner");

        spriteClass = SpinnerSprite.class;
        loot = new MysteryMeat();

        FLEEING = new Fleeing();
    }

    @Override
    public Damage giveDamage(Char target) {
        return super.giveDamage(target).addElement(Damage.Element.POISON);
    }

    @Override
    protected boolean act() {
        boolean result = super.act();

        if (state == FLEEING && buff(Terror.class) == null &&
                enemy != null && enemySeen && enemy.buff(Poison.class) == null) {
            state = HUNTING;
        }
        return result;
    }

    @Override
    public Damage attackProc(Damage damage) {
        Char enemy = (Char) damage.to;
        if (Random.Int(2) == 0) {
            Buff.affect(enemy, Poison.class).set(Random.Int(7, 9) * Poison
                    .durationFactor(enemy));
            state = FLEEING;
        }

        return damage;
    }

    @Override
    public void move(int step) {
        if (state == FLEEING) {
            GameScene.add(Blob.seed(pos, Random.Int(5, 7), Web.class));
        }
        super.move(step);
    }

    @Override
    protected Item createLoot() {
        if (Random.Float() < 0.3f) {
            int q = Random.Float() < 0.3f ? 2 : 1;
            return new SpiderGland().quantity(q).identify();
        }
        return super.createLoot();
    }

    private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

    static {
        IMMUNITIES.add(Roots.class);
    }

    @Override
    public HashSet<Class<?>> immunizedBuffs() {
        return IMMUNITIES;
    }

    private class Fleeing extends Mob.Fleeing {
        @Override
        protected void nowhereToRun() {
            if (buff(Terror.class) == null) {
                state = HUNTING;
            } else {
                super.nowhereToRun();
            }
        }
    }
}
