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
package com.egoal.darkestpixeldungeon.scenes;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.Statistics;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.KGenerator;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.GameLog;
import com.egoal.darkestpixeldungeon.windows.WndError;
import com.egoal.darkestpixeldungeon.levels.RegularLevel;
import com.egoal.darkestpixeldungeon.windows.WndStory;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.io.FileNotFoundException;
import java.io.IOException;

public class InterlevelScene extends PixelScene {

  private static final float TIME_TO_FADE = 0.3f;

  public enum Mode {
    DESCEND, ASCEND, CONTINUE, RESURRECT, RETURN, FALL, RESET, NONE, REFLUX,
  }

  public static Mode mode;

  public static int returnDepth;
  public static int returnPos;

  public static boolean noStory = false;

  public static boolean fallIntoPit;

  private enum Phase {
    FADE_IN, STATIC, FADE_OUT
  }

  private Phase phase;
  private float timeLeft;

  private RenderedText message;

  private Thread thread;
  private Exception error = null;

  @Override
  public void create() {
    super.create();

    String text = Messages.get(Mode.class, mode.name());

    message = renderText(text, 9);
    message.x = (Camera.main.width - message.width()) / 2;
    message.y = (Camera.main.height - message.height()) / 2;
    align(message);
    add(message);

    phase = Phase.FADE_IN;
    timeLeft = TIME_TO_FADE;

    thread = new Thread() {
      @Override
      public void run() {

        try {

          Generator.reset();
          
          switch (mode) {
            case DESCEND:
              descend();
              break;
            case ASCEND:
              ascend();
              break;
            case CONTINUE:
              restore();
              break;
            case REFLUX:
              reflux();
              break;
            case RESURRECT:
              resurrect();
              break;
            case RETURN:
              returnTo();
              break;
            case FALL:
              fall();
              break;
            case RESET:
              reset();
              break;
          }

          if (Dungeon.bossLevel())
            Sample.INSTANCE.load(Assets.SND_BOSS);

        } catch (Exception e) {

          error = e;

        }

        if (phase == Phase.STATIC && error == null) {
          phase = Phase.FADE_OUT;
          timeLeft = TIME_TO_FADE;
        }
      }
    };
    thread.start();
  }

  @Override
  public void update() {
    super.update();

    float p = timeLeft / TIME_TO_FADE;

    switch (phase) {

      case FADE_IN:
        message.alpha(1 - p);
        if ((timeLeft -= Game.elapsed) <= 0) {
          if (!thread.isAlive() && error == null) {
            phase = Phase.FADE_OUT;
            timeLeft = TIME_TO_FADE;
          } else {
            phase = Phase.STATIC;
          }
        }
        break;

      case FADE_OUT:
        message.alpha(p);

        if (mode == Mode.CONTINUE || (mode == Mode.DESCEND && Dungeon.depth
                == 1)) {
          Music.INSTANCE.volume(p * (DarkestPixelDungeon.musicVol() / 10f));
        }
        if ((timeLeft -= Game.elapsed) <= 0) {
          Game.switchScene(GameScene.class);
        }
        break;

      case STATIC:
        if (error != null) {
          String errorMsg;
          if (error instanceof FileNotFoundException)
            errorMsg = Messages.get(this, "file_not_found");
          else if (error instanceof IOException)
            errorMsg = Messages.get(this, "io_error");
          else
            throw new RuntimeException("fatal error occured while moving " +
                    "between floors", error);

          if (InterlevelScene.mode == Mode.REFLUX)
            errorMsg = "no backup file found, return.";

          add(new WndError(errorMsg) {
            public void onBackPressed() {
              super.onBackPressed();
              if (InterlevelScene.mode == Mode.REFLUX)
                Game.switchScene(StartScene.class);
              else {
                InterlevelScene.mode = Mode.REFLUX;
                Game.switchScene(InterlevelScene.class);
              }
            }
          });
          error = null;
        }
        break;
    }
  }

  private void descend() throws IOException {

    Actor.fixTime();
    if (Dungeon.hero == null) {
      Dungeon.init();
      if (noStory) {
        Dungeon.chapters.add(WndStory.ID_SEWERS);
        noStory = false;
      }
      GameLog.wipe();
    } else {
      Dungeon.hero.holdFollowers(Dungeon.level);
      Dungeon.saveAll(true);
    }

    Level level;
    if (Dungeon.depth >= Statistics.INSTANCE.getDeepestFloor()) {
      level = Dungeon.newLevel();
    } else {
      Dungeon.depth++;
      level = Dungeon.loadLevel(Dungeon.hero.heroClass);
    }
    Dungeon.switchLevel(level, level.entrance);
  }

  private void fall() throws IOException {

    Actor.fixTime();
    Dungeon.hero.holdFollowers(Dungeon.level);
    Dungeon.saveAll(true);

    Level level;
    if (Dungeon.depth >= Statistics.INSTANCE.getDeepestFloor()) {
      level = Dungeon.newLevel();
    } else {
      Dungeon.depth++;
      level = Dungeon.loadLevel(Dungeon.hero.heroClass);
    }
    Dungeon.switchLevel(level, fallIntoPit ? level.pitCell() : level
            .randomRespawnCell());
  }

  private void ascend() throws IOException {
    Actor.fixTime();
    Dungeon.hero.holdFollowers(Dungeon.level);

    Dungeon.saveAll();
    Dungeon.depth--;
    Level level = Dungeon.loadLevel(Dungeon.hero.heroClass);
    Dungeon.switchLevel(level, level.exit);
  }

  private void returnTo() throws IOException {

    Actor.fixTime();
    Dungeon.hero.holdFollowers(Dungeon.level);

    Dungeon.saveAll();
    Dungeon.depth = returnDepth;
    Level level = Dungeon.loadLevel(Dungeon.hero.heroClass);
    Dungeon.switchLevel(level, returnPos);
  }

  private void restore() throws IOException {

    Actor.fixTime();
    // Dungeon.hero.clearFollowers();

    GameLog.wipe();

    Dungeon.loadGame(StartScene.curClass);
    if (Dungeon.depth == -1) {
      Dungeon.depth = Statistics.INSTANCE.getDeepestFloor();
      Dungeon.switchLevel(Dungeon.loadLevel(StartScene.curClass), -1);
    } else {
      Level level = Dungeon.loadLevel(StartScene.curClass);
      Dungeon.switchLevel(level, Dungeon.hero.pos);
    }
  }

  private void reflux() throws IOException {
    Actor.fixTime();

    GameLog.wipe();

    Dungeon.loadBackupGame(StartScene.curClass);
    Level level = Dungeon.loadBackupLevel(StartScene.curClass);
    Dungeon.switchLevel(level, Dungeon.hero.pos);
  }

  private void resurrect() throws IOException {

    Actor.fixTime();
    Dungeon.hero.holdFollowers(Dungeon.level);

    if (Dungeon.level.locked) {
      Dungeon.hero.resurrect(Dungeon.depth);
      Dungeon.depth--;
      Level level = Dungeon.newLevel();
      Dungeon.switchLevel(level, level.entrance);
    } else {
      Dungeon.hero.resurrect(-1);
      Dungeon.resetLevel();
    }
  }

  private void reset() throws IOException {

    Actor.fixTime();
    Dungeon.hero.holdFollowers(Dungeon.level);

    Dungeon.depth--;
    Level level = Dungeon.newLevel();
    //FIXME this only partially addresses issues regarding weak floors.
    RegularLevel.weakFloorCreated = false;
    Dungeon.switchLevel(level, level.entrance);
  }

  @Override
  protected void onBackPressed() {
    //Do nothing
  }
}
