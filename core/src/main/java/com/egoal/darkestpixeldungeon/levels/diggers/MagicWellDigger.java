package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfAwareness;
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfHealth;
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfTransmutation;
import com.egoal.darkestpixeldungeon.actors.blobs.WellWater;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/12.
 */

public class MagicWellDigger extends Digger {
  @Override
  public XRect chooseDigArea(XWall wall) {
    return chooseCenteredBox(wall, Random.IntRange(3, 5));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Point cen = rect.cen();
    int hs = rect.w() / 2;
    int hs2 = hs * hs;
    for (Point p : rect.getAllPoints())
      if (Point.DistanceL22(cen, p) <= hs2)
        Set(level, p, Terrain.EMPTY);

    int ccen = level.pointToCell(cen);
    for (int i : PathFinder.NEIGHBOURS9)
      if (Random.Int(2) == 0)
        Set(level, ccen + i, Terrain.GRASS);

    Set(level, ccen, Terrain.WELL);

    // well
    Class<? extends WellWater> cls = (Class<? extends WellWater>) Random
            .element(WATERS);

    WellWater ww = (WellWater) level.blobs.get(cls);
    if (ww == null) {
      try {
        ww = cls.newInstance();
      } catch (Exception e) {
        DarkestPixelDungeon.reportException(e);
        return null;
      }
    }
    ww.seed(level, ccen, 1);
    level.blobs.put(cls, ww);

    //
    Point door = rect.cen();
    if (wall.direction == LEFT || wall.direction == RIGHT)
      door.x = wall.x1;
    else
      door.y = wall.y1;
    Set(level, door, Terrain.DOOR);

    DigResult dr = new DigResult(DigResult.Type.SPECIAL);

    if (-wall.direction != LEFT)
      dr.walls.add(new XWall(rect.x1 - 1, cen.y, LEFT));
    if (-wall.direction != RIGHT)
      dr.walls.add(new XWall(rect.x2 + 1, cen.y, RIGHT));
    if (-wall.direction != UP)
      dr.walls.add(new XWall(cen.x, rect.y1 - 1, UP));
    if (-wall.direction != DOWN)
      dr.walls.add(new XWall(cen.x, rect.y2 + 1, DOWN));

    return dr;
  }

  private static final Class<?>[] WATERS = {WaterOfAwareness.class,
          WaterOfHealth.class, WaterOfTransmutation.class};

}