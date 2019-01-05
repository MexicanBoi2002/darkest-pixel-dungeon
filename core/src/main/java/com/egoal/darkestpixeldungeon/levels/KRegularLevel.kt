package com.egoal.darkestpixeldungeon.levels

import android.util.Log
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Bones
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.PotionSeller
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.ScrollSeller
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.rings.RingOfWealth
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.LevelDigger
import com.egoal.darkestpixeldungeon.levels.diggers.Space
import com.egoal.darkestpixeldungeon.levels.diggers.normal.*
import com.egoal.darkestpixeldungeon.levels.diggers.secret.*
import com.egoal.darkestpixeldungeon.levels.diggers.specials.*
import com.egoal.darkestpixeldungeon.levels.traps.FireTrap
import com.egoal.darkestpixeldungeon.levels.traps.Trap
import com.egoal.darkestpixeldungeon.levels.traps.WornTrap
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

open abstract class KRegularLevel : Level() {
    init {
        color1 = 0x48763c
        color2 = 0x59994a
        viewDistance = 8
        seeDistance = 8
    }

    protected var spaces: ArrayList<Space> = ArrayList()

    override fun setupSize() {
        if (width == 0 && height == 0) {
            height = 36
            width = height
        }

        length = width * height
    }

    override fun build(iteration: Int): Boolean {
        if (iteration == 0 || chosenDiggers.isEmpty()) {
            chosenDiggers = chooseDiggers()
            Log.d("dpd", "${chosenDiggers.size} diggers chosen.")
        }

        val ld = LevelDigger(this)
        if (!ld.dig(chosenDiggers))
            return false

        Log.d("dpd", "$iteration: level dag.")
        spaces = ld.spaces

        if (!setStairs())
            return false

        // do some painting...
        Log.d("dpd", "$iteration: terrains okay, now paint...")

        paintLuminary()
        paintWater()
        paintGrass()
        placeTraps()

        return true
    }

    private var chosenDiggers = ArrayList<Digger>()
    protected open fun chooseDiggers(): ArrayList<Digger> {
        val diggers = selectDiggers(Random.NormalIntRange(1, 4), 15)
        if (Dungeon.shopOnLevel())
            diggers.add(ShopDigger())

        return diggers
    }

    protected fun selectDiggers(specials: Int, total: Int): ArrayList<Digger> {
        val diggers = ArrayList<Digger>()

        // as most 1 secret per level
        if (Random.IntRange(2, 5) <= specials) {
            Log.d("dpd", "a secret digger chosen.")
            diggers.add(Random.chances(SecretDiggers).newInstance())
        }

        // specials
        val probs = HashMap<Class<out Digger>, Float>(SpecialDiggers)
        if (pitRoomNeeded) {
            // a pit room is need, remove all locked diggers
            // todo: this is truely fragile
            diggers.add(PitDigger())

            probs.remove(ArmoryDigger::class.java)
            probs.remove(LibraryDigger::class.java)
            probs.remove(TreasuryDigger::class.java)
            probs.remove(VaultDigger::class.java)
            probs.remove(WeakFloorDigger::class.java)
        }

        // never fall to boss
        if (Dungeon.bossLevel(Dungeon.depth + 1))
            probs.remove(WeakFloorDigger::class.java)

        while (diggers.size < specials) {
            val cls = Random.chances(probs)
            cls ?: break

            probs[cls] = 0f // unique.
            diggers.add(cls.newInstance())
        }

        // weak floor check
        weakFloorCreated = diggers.any { it is WeakFloorDigger }

        Log.d("dpd", "${diggers.size} special diggers chosen, weak floor: $weakFloorCreated")

        // random draft normal diggers
        while (diggers.size < total)
            diggers.add(Random.chances(NormalDiggers).newInstance())

        return diggers
    }

    private fun setStairs(): Boolean {
        val normalSpaces = spaces.filter { it.type == DigResult.Type.Normal }

        for (_i in 1..10) {
            var spaceEntrance: Space
            do {
                spaceEntrance = Random.element(normalSpaces)
                entrance = pointToCell(spaceEntrance.rect.random(1))
            } while (map[entrance] != Terrain.EMPTY)

            for (_j in 1..30) {
                var spaceExit = Random.element(normalSpaces)
                if (spaceExit == spaceEntrance) continue

                exit = pointToCell(spaceExit.rect.random(1))

                if (map[exit] == Terrain.EMPTY && distance(entrance, exit) >= 12) {
                    // gotcha
                    spaceEntrance.type = DigResult.Type.Entrance
                    spaceExit.type = DigResult.Type.Exit
                    map[entrance] = Terrain.ENTRANCE
                    map[exit] = Terrain.EXIT

                    return true
                }
            }
        }

        return false
    }

    override fun nMobs(): Int = when (Dungeon.depth) {
        0, 1 -> 0
        in 2..4 -> 3 + Dungeon.depth % 5 + Random.Int(5)
        else -> 3 + Dungeon.depth % 5 + Random.Int(8)
    }

    protected fun createSellers() {
        if (Dungeon.depth in 1 until 20) {
            val psProb = if (Dungeon.shopOnLevel()) .1f else .2f
            if (Random.Float() < psProb) {
                val ps = PotionSeller().initSellItems()
                val s = randomSpace(DigResult.Type.Normal)
                do {
                    ps.pos = pointToCell(s!!.rect.random())
                } while (findMob(ps.pos) != null || !Level.passable[ps.pos])
                mobs.add(ps)
            }

            val ssProb = if (Dungeon.shopOnLevel()) .08f else .18f
            if (Random.Float() < ssProb) {
                val ps = ScrollSeller().initSellItems()
                val s = randomSpace(DigResult.Type.Normal)
                do {
                    ps.pos = pointToCell(s!!.rect.random())
                } while (findMob(ps.pos) != null || !Level.passable[ps.pos])
                mobs.add(ps)
            }
        }
    }

    override fun createMobs() {
        createSellers()

        val trySpawn = { space: Space ->
            val mob = Bestiary.mob(Dungeon.depth).apply {
                pos = pointToCell(space.rect.random())
            }
            if (passable[mob.pos] && findMob(mob.pos) == null) {
                mobs.add(mob)
                1
            } else 0
        }

        var mobsToSpawn = if (Dungeon.depth == 1) 10 else nMobs()

        // well distributed in each space
        val normalSpaces = spaces.filter { it.type == DigResult.Type.Normal }
        var index = 0
        while (mobsToSpawn > 0) {
            mobsToSpawn -= trySpawn(normalSpaces[index])

            // extra one in the same space
            if (mobsToSpawn > 0 && Random.Int(4) == 0)
                mobsToSpawn -= trySpawn(normalSpaces[index])

            if (++index >= normalSpaces.size)
                index = 0
        }
    }

    override fun createItems() {
        var nItems = 3

        // bonus from wealth
        val bonus = Math.min(10, RingOfWealth.getBonus(Dungeon.hero, RingOfWealth.Wealth::class.java))
        while (Random.Float() < .25f + bonus * 0.05f)
            ++nItems

        for (i in 1..nItems) {
            val heap = when (Random.Int(20)) {
                0 -> Heap.Type.SKELETON
                in 1..4 -> Heap.Type.CHEST
                4 -> if (Dungeon.depth > 1) Heap.Type.MIMIC else Heap.Type.CHEST
                else -> Heap.Type.HEAP
            }

            drop(Generator.random(), randomDropCell()).type = heap
        }

        // inherent items
        for (item in itemsToSpawn) {
            var c = randomDropCell()
            // never drop scroll on fire trap
            if (item is Scroll)
                while ((map[c] == Terrain.TRAP || map[c] == Terrain.SECRET_TRAP) && traps.get(c) is FireTrap)
                    c = randomDropCell()

            drop(item, c).type = Heap.Type.HEAP
        }

        // hero remains
        val item = Bones.get()
        if (item != null)
            drop(item, randomDropCell()).type = Heap.Type.REMAINS
    }

    // paintings
    protected fun paintLuminary() {
        val availableWalls = HashSet<Int>()
        for (i in width until length - width)
            if (map[i] == Terrain.WALL && PathFinder.NEIGHBOURS4.any {
                        map[i + it] in listOf(Terrain.EMPTY, Terrain.EMPTY_SP, Terrain.EMPTY_DECO)
                    })
                availableWalls.add(i)

        val ratioLight = if (Dungeon.depth < 10) 0.15f else 0.1f
        val ratioLightOn = if (feeling == Level.Feeling.DARK) 0.5f else 0.7f
        for (i in availableWalls) {
            if (Random.Float() < ratioLight)
                map[i] = if (Random.Float() < ratioLightOn) Terrain.WALL_LIGHT_ON
                else Terrain.WALL_LIGHT_OFF
        }
    }

    protected abstract fun water(): BooleanArray
    protected abstract fun grass(): BooleanArray

    protected fun paintWater() {
        val water = water()
        for (i in 0 until length)
            if (map[i] == Terrain.EMPTY && water[i])
                map[i] = Terrain.WATER

    }

    protected fun paintGrass() {
        val grass = grass()

        if (feeling == Feeling.GRASS)
            for (space in spaces) {
                val rect = space.rect
                grass[xy2cell(rect.x1, rect.y1)] = Random.Int(2) == 0
                grass[xy2cell(rect.x2, rect.y1)] = Random.Int(2) == 0
                grass[xy2cell(rect.x1, rect.y2)] = Random.Int(2) == 0
                grass[xy2cell(rect.x2, rect.y2)] = Random.Int(2) == 0
            }

        for (i in width + 1 until length - width - 1) {
            if (map[i] == Terrain.EMPTY && grass[i]) {
                val count = PathFinder.NEIGHBOURS8.count { grass[i + it] }

                map[i] = if (Random.Float() < count / 12f) Terrain.HIGH_GRASS else Terrain.GRASS
            }
        }
    }

    // traps
    protected fun nTraps() = Random.NormalIntRange(1, 5 + Dungeon.depth / 2)

    protected open fun trapClasses(): Array<Class<out Trap>> = arrayOf(WornTrap::class.java)

    protected open fun trapChances(): FloatArray = floatArrayOf(1f)

    protected fun placeTraps() {
        val trapChances = trapChances()
        val trapClasses = trapClasses()

        val validCells = (1 until length).filter { map[it] == Terrain.EMPTY && findMob(it) == null }.shuffled()
        var traps = Math.min(nTraps(), (validCells.size * 0.175).toInt())

        Log.d("dpd", "would add $traps traps.")

        for (i in validCells) {
            val trap = trapClasses[Random.chances(trapChances)].newInstance().hide()
            setTrap(trap, i)

            map[i] = if (trap.visible) Terrain.TRAP else Terrain.SECRET_TRAP

            if (--traps <= 0) break
        }
    }

    protected fun randomSpace(type: DigResult.Type, tries: Int = Int.MAX_VALUE): Space? {
        for (i in 1..tries) {
            val s = Random.element(spaces)
            if (s.type == type)
                return s
        }

        return null
    }

    //!!! this function's behaviour can be undefined!!!
    fun spaceAt(cell: Int): Space? = spaces.find {
        it.rect.inside(cellToPoint(cell))
    }

    override fun randomRespawnCell(): Int {
        for (i in 1..30) {
            val s = randomSpace(DigResult.Type.Normal, 10)
            if (s != null) {
                val cell = pointToCell(s.rect.random())
                if (!Dungeon.visible[cell] && passable[cell] && Actor.findChar(cell) == null)
                    return cell
            }
        }
        return -1
    }

    override fun randomDestination(): Int {
        while (true) {
            val cell = Random.Int(length())
            if (passable[cell])
                return cell
        }
    }

    protected fun randomDropCell(): Int {
        while (true) {
            val s = randomSpace(DigResult.Type.Normal, 1)
            if (s != null) {
                val cell = pointToCell(s.rect.random())
                if (passable[cell])
                    return cell
            }
        }
    }

    override fun pitCell(): Int {
        val s = spaces.find { it.type == DigResult.Type.Pit }
        return if (s == null) super.pitCell() else pointToCell(s.rect.random(1))
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(SPACES, spaces)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        spaces = bundle.getCollection(SPACES) as ArrayList<Space>
        weakFloorCreated = spaces.any { it.type == DigResult.Type.WeakFloor }

        Log.d("dpd", String.format("%d spaces restored.", spaces.size))
    }

    companion object {
        private val SPACES = "spaces"

        val SpecialDiggers: Map<Class<out Digger>, Float> = mapOf(
                ArmoryDigger::class.java to 1f,
                GardenDigger::class.java to 1f,
                LaboratoryDigger::class.java to 1f,
                LibraryDigger::class.java to 1f,
                MagicWellDigger::class.java to 1f,
                PitDigger::class.java to 0f,
                PoolDigger::class.java to 1f,
                QuestionerDigger::class.java to 1f,
                ShopDigger::class.java to 0f,
                StatuaryDigger::class.java to 1f,
                StatueDigger::class.java to 1f,
                StorageDigger::class.java to 1f,
                TrapsDigger::class.java to 1f,
                TreasuryDigger::class.java to 1f,
                VaultDigger::class.java to 1f,
                WeakFloorDigger::class.java to 0.75f
        )

        val SecretDiggers: HashMap<Class<out Digger>, Float> = hashMapOf(
                SecretGuardianDigger::class.java to 1f,
                SecretLibraryDigger::class.java to 1f,
                SecretSummoningDigger::class.java to 1f,
                SecretTreasuryDigger::class.java to 1f,
                SecretGardenDigger::class.java to 1f
        )

        val NormalDiggers: HashMap<Class<out Digger>, Float> = hashMapOf(
                BrightDigger::class.java to .1f,
                CellDigger::class.java to .1f,
                CircleDigger::class.java to .075f,
                DiamondDigger::class.java to .05f,
                LatticeDigger::class.java to .1f,
                RectDigger::class.java to 1f,
                RoundDigger::class.java to .05f,
                StripDigger::class.java to .1f,
                CrossDigger::class.java to .05f,
                PatchDigger::class.java to .075f, 
                GraveyardDigger::class.java to .5f
        )
    }
}