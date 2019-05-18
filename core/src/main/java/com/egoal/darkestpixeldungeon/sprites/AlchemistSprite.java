package com.egoal.darkestpixeldungeon.sprites;

import com.egoal.darkestpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class AlchemistSprite extends MobSprite {

  public AlchemistSprite() {
    super();

    texture(Assets.ALCHEMIST);

    TextureFilm frames = new TextureFilm(texture, 12, 14);   // width & height

    idle = new Animation(5, true);
    idle.frames(frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4,
            1, 2, 3, 4, 4, 4);

    run = new Animation(20, true);
    run.frames(frames, 0);

    die = new Animation(20, false);
    die.frames(frames, 0);

    play(idle);
  }
}
