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
package com.egoal.darkestpixeldungeon.items.scrolls;

import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.hero.HeroPerk;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.CellSelector;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.HeroSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;

public class ScrollOfTeleportation extends Scroll {

  {
    initials = 9;
  }

  @Override
  protected void doRead() {

    Sample.INSTANCE.play(Assets.SND_READ);
    Invisibility.dispel();

    setKnown();

    if (Dungeon.bossLevel()) {
      GLog.w(Messages.get(this, "no_tele"));
      return;
    }

    if (curUser.getHeroPerk().contain(HeroPerk.Perk.INTENDED_TRANSPORTATION))
      IntendTeleportHero(curUser);
    else {
      teleportHero(curUser);
      readAnimation();
    }
  }

  public static void teleportHero(Hero hero) {

    int count = 10;
    int pos;
    do {
      pos = Dungeon.level.randomRespawnCell();
      if (count-- <= 0) {
        break;
      }
    } while (pos == -1);

    if (pos == -1 || Dungeon.bossLevel()) {
      GLog.w(Messages.get(ScrollOfTeleportation.class, "no_tele"));
    } else {

      appear(hero, pos);
      Dungeon.level.press(pos, hero);
      Dungeon.observe();
      GameScene.updateFog();

      GLog.i(Messages.get(ScrollOfTeleportation.class, "tele"));

    }
  }

  public static void IntendTeleportHero(Hero hero) {
    GameScene.selectCell(selectorDst);
  }

  private static final CellSelector.Listener selectorDst = new CellSelector
          .Listener() {
    @Override
    public void onSelect(Integer cell) {
      if (cell == null)
        teleportHero(curUser);
      else if (Dungeon.level.visited[cell] || Dungeon.level.mapped[cell]) {
        if (Level.solid[cell] || Actor.findChar(cell) != null)
          return;

        appear(curUser, cell);
        Dungeon.level.press(cell, curUser);
        Dungeon.observe();
        GameScene.updateFog();

        GLog.i(Messages.get(ScrollOfTeleportation.class, "tele"));

        // read animation...
        curUser.spend(TIME_TO_READ);
        curUser.busy();
        ((HeroSprite) curUser.sprite).read();
      }
    }

    @Override
    public String prompt() {
      return Messages.get(ScrollOfTeleportation.class, "select-destination");
    }
  };

  public static void appear(Char ch, int pos) {

    ch.sprite.interruptMotion();

    ch.move(pos);
    ch.sprite.place(pos);

    if (ch.invisible == 0) {
      ch.sprite.alpha(0);
      ch.sprite.parent.add(new AlphaTweener(ch.sprite, 1, 0.4f));
    }

    ch.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.2f, 3);
    Sample.INSTANCE.play(Assets.SND_TELEPORT);
  }

  @Override
  public int price() {
    return isKnown() ? 30 * quantity : super.price();
  }
}
