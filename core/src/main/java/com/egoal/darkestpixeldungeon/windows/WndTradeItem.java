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

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.hero.HeroPerk;
import com.egoal.darkestpixeldungeon.items.unclassified.Gold;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.egoal.darkestpixeldungeon.items.EquipableItem;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.artifacts.MasterThievesArmband;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.ui.ItemSlot;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.Window;

public class WndTradeItem extends Window {

  private static final float GAP = 2;
  private static final int WIDTH = 120;
  private static final int BTN_HEIGHT = 16;

  private WndBag owner;

  public WndTradeItem(final Item item, WndBag owner) {

    super();

    this.owner = owner;

    float pos = createDescription(item, false);

    if (item.quantity() == 1) {

      RedButton btnSell = new RedButton(Messages.get(this, "sell", item.price
              ())) {
        @Override
        protected void onClick() {
          sell(item);
          hide();
        }
      };
      btnSell.setRect(0, pos + GAP, WIDTH, BTN_HEIGHT);
      add(btnSell);

      pos = btnSell.bottom();

    } else {

      int priceAll = item.price();
      RedButton btnSell1 = new RedButton(Messages.get(this, "sell_1", 
              priceAll / item.quantity())) {
        @Override
        protected void onClick() {
          sellOne(item);
          hide();
        }
      };
      btnSell1.setRect(0, pos + GAP, WIDTH, BTN_HEIGHT);
      add(btnSell1);
      RedButton btnSellAll = new RedButton(Messages.get(this, "sell_all", 
              priceAll)) {
        @Override
        protected void onClick() {
          sell(item);
          hide();
        }
      };
      btnSellAll.setRect(0, btnSell1.bottom() + GAP, WIDTH, BTN_HEIGHT);
      add(btnSellAll);

      pos = btnSellAll.bottom();

    }

    RedButton btnCancel = new RedButton(Messages.get(this, "cancel")) {
      @Override
      protected void onClick() {
        hide();
      }
    };
    btnCancel.setRect(0, pos + GAP, WIDTH, BTN_HEIGHT);
    add(btnCancel);

    resize(WIDTH, (int) btnCancel.bottom());
  }

  public WndTradeItem(final Heap heap, boolean canBuy) {

    super();

    Item item = heap.peek();

    float pos = createDescription(item, true);

    // sorceress perk1
    final int price = (int) (item.sellPrice() *
            (Dungeon.hero.heroPerk.contain(HeroPerk.Perk.SHREWD) ? .75 : 1));

    if (canBuy) {

      RedButton btnBuy = new RedButton(Messages.get(this, "buy", price)) {
        @Override
        protected void onClick() {
          hide();
          buy(heap, price);
        }
      };
      btnBuy.setRect(0, pos + GAP, WIDTH, BTN_HEIGHT);
      btnBuy.enable(price <= Dungeon.gold);
      add(btnBuy);

      RedButton btnCancel = new RedButton(Messages.get(this, "cancel")) {
        @Override
        protected void onClick() {
          hide();
        }
      };

      final MasterThievesArmband.Thievery thievery = Dungeon.hero.buff
              (MasterThievesArmband.Thievery.class);
      if (thievery != null) {
        final float chance = thievery.stealChance(price);
        RedButton btnSteal = new RedButton(Messages.get(this, "steal", Math
                .min(100, (int) (chance * 100)))) {
          @Override
          protected void onClick() {
            if (thievery.steal(price)) {
              Hero hero = Dungeon.hero;
              Item item = heap.pickUp();
              hide();

              if (!item.doPickUp(hero)) {
                Dungeon.level.drop(item, heap.pos).sprite.drop();
              }
            } else {
              for (Mob mob : Dungeon.level.mobs) {
                if (mob instanceof Shopkeeper) {
                  mob.yell(Messages.get(mob, "thief"));
                  ((Shopkeeper) mob).flee();
                  break;
                }
              }
              hide();
            }
          }
        };
        btnSteal.setRect(0, btnBuy.bottom() + GAP, WIDTH, BTN_HEIGHT);
        add(btnSteal);

        btnCancel.setRect(0, btnSteal.bottom() + GAP, WIDTH, BTN_HEIGHT);
      } else
        btnCancel.setRect(0, btnBuy.bottom() + GAP, WIDTH, BTN_HEIGHT);

      add(btnCancel);

      resize(WIDTH, (int) btnCancel.bottom());

    } else {

      resize(WIDTH, (int) pos);

    }
  }

  @Override
  public void hide() {

    super.hide();

    if (owner != null) {
      owner.hide();
      Shopkeeper.sell();
    }
  }

  private float createDescription(Item item, boolean forSale) {

    // Title
    IconTitle titlebar = new IconTitle();
    titlebar.icon(new ItemSprite(item));
    titlebar.label(forSale ?
            Messages.get(this, "sale", item.toString(), item.sellPrice()) :
            Messages.titleCase(item.toString()));
    titlebar.setRect(0, 0, WIDTH, 0);
    add(titlebar);

    // Upgraded / degraded
    if (item.levelKnown) {
      if (item.level() < 0) {
        titlebar.color(ItemSlot.DEGRADED);
      } else if (item.level() > 0) {
        titlebar.color(ItemSlot.UPGRADED);
      }
    }

    // Description
    RenderedTextMultiline info = PixelScene.renderMultiline(item.info(), 6);
    info.maxWidth(WIDTH);
    info.setPos(titlebar.left(), titlebar.bottom() + GAP);
    add(info);

    return info.bottom();
  }

  // called when sold
  protected void sell(Item item) {

    Hero hero = Dungeon.hero;

    if (item.isEquipped(hero) && !((EquipableItem) item).doUnequip(hero, 
            false)) {
      return;
    }
    item.detachAll(hero.belongings.backpack);

    int price = item.price();

    new Gold(price).doPickUp(hero);
  }

  protected void sellOne(Item item) {

    if (item.quantity() <= 1) {
      sell(item);
    } else {

      Hero hero = Dungeon.hero;

      item = item.detach(hero.belongings.backpack);
      int price = item.price();

      new Gold(price).doPickUp(hero);
    }
  }

  private void buy(Heap heap, int price) {

    Hero hero = Dungeon.hero;
    Item item = heap.pickUp();

    // int price = price( item );
    Dungeon.gold -= price;

    if (!item.doPickUp(hero)) {
      Dungeon.level.drop(item, heap.pos).sprite.drop();
    }
  }
}
