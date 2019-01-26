package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.artifacts.MaskOfMadness;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * Created by 93942 on 6/18/2018.
 */

public class DemonicSkull extends Item {

  private static final String AC_SMEAR = "SMEAR";

  {
    image = ItemSpriteSheet.DEMONIC_SKULL;

    cursedKnown = levelKnown = true;
    unique = true;
  }

  @Override
  public boolean isUpgradable() {
    return false;
  }

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    UnholyBlood ub = hero.belongings.getItem(UnholyBlood.class);
    if (ub != null) {
      actions.add(AC_SMEAR);
    }

    return actions;
  }

  @Override
  public void execute(final Hero hero, String action) {
    super.execute(hero, action);

    if (action == AC_SMEAR) {
      detach(hero.belongings.backpack);
      hero.belongings.getItem(UnholyBlood.class).detach(hero.belongings
              .backpack);

      MaskOfMadness mom = new MaskOfMadness();
      mom.identify().collect();

      GLog.w(Messages.get(Dungeon.hero, "you_now_have", mom.name()));
    }
  }
}
