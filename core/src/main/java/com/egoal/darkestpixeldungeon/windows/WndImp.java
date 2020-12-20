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
package com.egoal.darkestpixeldungeon.windows;

import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Imp;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.quest.DwarfToken;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.utils.GLog;

public class WndImp extends Window {

  private static final int WIDTH = 120;
  private static final int BTN_HEIGHT = 20;
  private static final int GAP = 2;

  public WndImp(final Imp imp, final DwarfToken tokens) {

    super();

    IconTitle titlebar = new IconTitle();
    titlebar.icon(new ItemSprite(tokens.image(), null));
    titlebar.label(Messages.titleCase(tokens.name()));
    titlebar.setRect(0, 0, WIDTH, 0);
    add(titlebar);

    RenderedTextMultiline message = PixelScene.renderMultiline(Messages.get
            (this, "message"), 6);
    message.maxWidth(WIDTH);
    message.setPos(0, titlebar.bottom() + GAP);
    add(message);

    RedButton btnReward = new RedButton(Messages.get(this, "reward")) {
      @Override
      protected void onClick() {
        takeReward(imp, tokens, Imp.Quest.INSTANCE.getReward());
      }
    };
    btnReward.setRect(0, message.top() + message.height() + GAP, WIDTH, 
            BTN_HEIGHT);
    add(btnReward);

    resize(WIDTH, (int) btnReward.bottom());
  }

  private void takeReward(Imp imp, DwarfToken tokens, Item reward) {

    hide();

    tokens.detachAll(Dungeon.INSTANCE.getHero().getBelongings().getBackpack());

    reward.identify();
    if (reward.doPickUp(Dungeon.INSTANCE.getHero())) {
      GLog.i(Messages.get(Dungeon.INSTANCE.getHero(), "you_now_have", reward.name()));
    } else {
      Dungeon.INSTANCE.getLevel().drop(reward, imp.getPos()).getSprite().drop();
    }

    imp.flee();

    Imp.Quest.INSTANCE.complete();
  }
}
