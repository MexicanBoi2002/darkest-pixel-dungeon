package com.egoal.darkestpixeldungeon.scenes

import android.graphics.drawable.Icon
import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.effects.BannerSprites
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.*
import com.egoal.darkestpixeldungeon.windows.WndChallenges
import com.egoal.darkestpixeldungeon.windows.WndClass
import com.egoal.darkestpixeldungeon.windows.WndMessage
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.*
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.particles.BitmaskEmitter
import com.watabou.noosa.particles.Emitter
import com.watabou.noosa.ui.Button
import com.watabou.utils.Callback
import kotlin.math.min

class NewStartScene : PixelScene() {
    private lateinit var btnLoadGame: GameButton
    private lateinit var btnNewGame: GameButton
    private val shields = hashMapOf<HeroClass, ClassShield>()

    private lateinit var slider: ClassSlideBar
    private lateinit var unlock: Group
    private var unlockText: RenderedTextMultiline? = null

    private var buttonX: Float = 0f
    private var buttonY: Float = 0f

    override fun create() {
        super.create()

        Badges.loadGlobal()

        uiCamera.visible = false

        val w = Camera.main.width
        val h = Camera.main.height

        val width = WIDTH_P
        val height = HEIGHT_P

        val left = (w - width) / 2f
        val top = (h - height) / 2f
        val bottom = h - top

        val archs = Archs().apply { setSize(w.toFloat(), h.toFloat()) }
        add(archs)

        val title = BannerSprites.get(BannerSprites.Type.SELECT_YOUR_HERO)
        title.x = (w - title.width()) / 2f
        title.y = top
        align(title)
        add(title)

        buttonX = left
        buttonY = bottom - BUTTON_HEIGHT

        btnNewGame = object : GameButton(M.L(this, "new")) {
            override fun onClick() {
                if (GamesInProgress.check(CurrentClass) != null) {
                    this@NewStartScene.add(object : WndOptions(
                            M.L(NewStartScene::class.java, "really"),
                            M.L(NewStartScene::class.java, "warning"),
                            M.L(NewStartScene::class.java, "yes"),
                            M.L(NewStartScene::class.java, "no")) {
                        override fun onSelect(index: Int) {
                            if (index == 0) startNewGame()
                        }
                    })
                } else
                    startNewGame()
            }
        }
        add(btnNewGame)

        btnLoadGame = object : GameButton(M.L(this, "load")) {
            override fun onClick() {
                InterlevelScene.mode = InterlevelScene.Mode.CONTINUE
                Game.switchScene(InterlevelScene::class.java)
            }

            override fun onLongClick(): Boolean {
                InterlevelScene.mode = InterlevelScene.Mode.REFLUX
                Game.switchScene(InterlevelScene::class.java)
                return false
            }
        }
        add(btnLoadGame)

        slider = ClassSlideBar().apply { centered(w / 2f, buttonY - 20f) }
        add(slider)

        val challenge = ChallengeButton()
        challenge.setPos((w - challenge.width()) / 2f, slider.btnClassName.top() - challenge.height() - 5f)
        add(challenge)

        val centralHeight = challenge.top() - title.y - title.height()
        val shieldW = width / 4
        val shieldH = min(centralHeight, shieldW)
        val shieldTop = title.y + title.height + (centralHeight - shieldH) / 2f
        val shieldLeft = left + (width - shieldW) / 2f
        for (cl in enumValues<HeroClass>()) {
            val shield = object : ClassShield(cl) {
                override fun onTouchDown() {
                    Sample.INSTANCE.play(Assets.SND_CLICK, 1f, 1f, 1.2f)
                    this@NewStartScene.add(WndClass(heroClass))
                }
            }
            shield.setRect(shieldLeft, shieldTop, shieldW, shieldH)
            shield.visible = false
            add(shield)
            shields[cl] = shield
        }

        unlock = Group()
        add(unlock)
        if (!isHuntressUnlocked() || !IsSorceressUnlocked()) {
            unlockText = renderMultiline(9).apply {
                maxWidth(width.toInt())
                hardlight(0xffff00)
            }
            unlock.add(unlockText)
        }

        val btnExit = ExitButton()
        btnExit.setPos(Camera.main.width - btnExit.width(), 0f)
        add(btnExit)

        updateClass(HeroClass.values()[DarkestPixelDungeon.lastClass()])
        fadeIn()

        Badges.loadingListener = Callback {
            if (Game.scene() === this@NewStartScene)
                DarkestPixelDungeon.switchNoFade(NewStartScene::class.java)
        }
    }

    override fun destroy() {
        Badges.saveGlobal()
        Badges.loadingListener = null

        super.destroy()
    }

    override fun onBackPressed() {
        DarkestPixelDungeon.switchNoFade(TitleScene::class.java)
    }

    private fun startNewGame() {
        Dungeon.hero = null
        InterlevelScene.mode = InterlevelScene.Mode.DESCEND
        Generator.reset()

        if (DarkestPixelDungeon.intro()) {
            DarkestPixelDungeon.intro(false)
            Game.switchScene(IntroScene::class.java)
        } else Game.switchScene(InterlevelScene::class.java)
    }

    private fun updateClass(cl: HeroClass) {
        shields[CurrentClass]!!.visible = false
        CurrentClass = cl
        shields[cl]!!.visible = true
        shields[cl]!!.showSpeckEffects()

        slider.btnClassName.text(CurrentClass.title().toUpperCase())
        slider.btnClassName.textColor(if (Badges.isUnlocked(CurrentClass.masteryBadge()))
            MASTERY_HIGHLIGHTED else BASIC_HIGHLIGHTED)

        if (!IsLocked(CurrentClass)) {
            unlock.visible = false

            val info = GamesInProgress.check(CurrentClass)
            if (info != null) {
                btnLoadGame.visible = true
                btnLoadGame.secondary(M.L(this, "depth_level", info.depth, info.level), info.challenges)

                btnNewGame.visible = true
                btnNewGame.secondary(M.L(this, "erase"), false)

                val w = (Camera.main.width - GAP) / 2 - buttonX
                btnLoadGame.setRect(buttonX, buttonY, w, BUTTON_HEIGHT)
                btnNewGame.setRect(btnLoadGame.right() + GAP, buttonY, w, BUTTON_HEIGHT)
            } else {
                btnLoadGame.visible = false

                btnNewGame.visible = true
                btnNewGame.secondary(null, false)
                btnNewGame.setRect(buttonX, buttonY, Camera.main.width - buttonX * 2, BUTTON_HEIGHT)
            }
        } else {
            val text = when (CurrentClass) {
                HeroClass.HUNTRESS -> M.L(this, "unlock_huntress")
                HeroClass.SORCERESS -> M.L(this, "unlock_sorceress")
                else -> ""
            }
            // unlock
            val height = HEIGHT_P
            val bottom = Camera.main.height - (Camera.main.height - height) / 2
            unlockText!!.text(text)
            unlockText!!.setPos(Camera.main.width / 2f - unlockText!!.width() / 2f,
                    (bottom - BUTTON_HEIGHT) + (BUTTON_HEIGHT - unlockText!!.height()) / 2f)
            align(unlockText)

            unlock.visible = true
            btnLoadGame.visible = false
            btnNewGame.visible = false
        }
    }

    companion object {
        private const val BUTTON_HEIGHT = 24f
        private const val GAP = 2f

        private const val WIDTH_P = 116f
        private const val HEIGHT_P = 220f

        var CurrentClass = HeroClass.WARRIOR

        //todo:
        private fun isHuntressUnlocked(): Boolean = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_2)

        private fun IsSorceressUnlocked(): Boolean = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_3)

        private fun IsLocked(cls: HeroClass): Boolean = (cls == HeroClass.HUNTRESS && !isHuntressUnlocked()) ||
                (cls == HeroClass.SORCERESS && !IsSorceressUnlocked())

        // GameButton
        private const val SECONDARY_COLOR_N = 0xCACFC2
        private const val SECONDARY_COLOR_H = 0xFFFF88

        // ClassShield
        private const val MIN_BRIGHTNESS = 0.6f

        private const val BASIC_HIGHLIGHTED = 0xCACFC2
        private const val MASTERY_HIGHLIGHTED = 0xFFFF88

        private const val WIDTH = 24
        private const val HEIGHT = 32
        private const val SCALE = 2f
    }

    private inner class ClassSlideBar : Group() {
        val btnLeft: RedButton
        val btnRight: RedButton
        val btnClassName: RedButton

        init {
            btnLeft = object : RedButton("<-") {
                override fun onClick() {
                    super.onClick()
                    val values = enumValues<HeroClass>()
                    val pre = if (CurrentClass.ordinal == 0) values.size - 1 else CurrentClass.ordinal - 1

                    updateClass(values[pre])
                }
            }
            add(btnLeft)
            btnRight = object : RedButton("->") {
                override fun onClick() {
                    super.onClick()
                    val values = enumValues<HeroClass>()
                    val next = (CurrentClass.ordinal + 1) % values.size

                    updateClass(values[next])
                }
            }
            add(btnRight)
            btnClassName = object : RedButton("Class") {
                init {
                    bg.visible = false
                }
            }
            add(btnClassName)
        }

        fun centered(x: Float, y: Float) {
            val btnWidth = 40f
            val arrowWidth = 30f
            val btnHeight = 20f
            val GAP = 5f

            btnLeft.setRect(x - btnWidth / 2f - GAP - arrowWidth, y - btnHeight / 2f, arrowWidth, btnHeight)
            btnClassName.setRect(btnLeft.right() + GAP, btnLeft.top(), btnWidth, btnHeight)
            btnRight.setRect(btnClassName.right() + GAP, btnLeft.top(), arrowWidth, btnHeight)
        }
    }

    private open class ClassShield(val heroClass: HeroClass) : Button() {
        private lateinit var avatar: Image
        private lateinit var emitter: Emitter
        private var brightness: Float = 0f

        init {
            avatar.frame(heroClass.ordinal * WIDTH, 0, WIDTH, HEIGHT)
            avatar.scale.set(SCALE)

            brightness = if (IsLocked(heroClass)) MIN_BRIGHTNESS else 1f
            updateBrightness()
        }

        override fun createChildren() {
            super.createChildren()

            avatar = Image(Assets.DPD_AVATARS)
            add(avatar)

            emitter = BitmaskEmitter(avatar)
            add(emitter)
        }

        override fun layout() {
            super.layout()

            avatar.x = x + (width - avatar.width()) / 2f
            avatar.y = y + (height - avatar.height()) / 2f
            align(avatar)
        }

        fun showSpeckEffects() {
            emitter.revive()
            emitter.start(Speck.factory(Speck.LIGHT), 0.05f, 7)
        }

        private fun updateBrightness() {
            avatar.am = brightness
            avatar.rm = avatar.am
            avatar.bm = avatar.rm
            avatar.gm = avatar.bm
        }
    }

    private class WndClasses : Window() {
        init {
            var h = GAP
            for (cls in enumValues<HeroClass>()) {
                val btn = RedButton(cls.title())
                btn.setRect(0f, h, WIDTH_P, 20f)
                add(btn)

                h += 20f + GAP
            }

            resize(WIDTH_P.toInt(), h.toInt())
        }
    }

    private open class GameButton(primary: String) : RedButton(primary) {
        private lateinit var secondary: RenderedText

        init {
            this.secondary.text(null)
        }

        override fun createChildren() {
            super.createChildren()

            secondary = renderText(6)
            add(secondary)
        }

        override fun layout() {
            super.layout()

            if (secondary.text().isNotEmpty()) {
                text.y = y + (height - text.height() - secondary.baseLine()) / 2

                secondary.x = x + (width - secondary.width()) / 2
                secondary.y = text.y + text.height()
            } else {
                text.y = y + (height - text.baseLine()) / 2
            }
            align(text)
            align(secondary)
        }

        fun secondary(text: String?, highlighted: Boolean) {
            secondary.text(text)

            secondary.hardlight(if (highlighted) SECONDARY_COLOR_H else SECONDARY_COLOR_N)
        }
    }

    private inner class ChallengeButton : Button() {
        private lateinit var image: Image

        init {
            width = image.width
            height = image.height

            image.am = if (Badges.isUnlocked(Badges.Badge.VICTORY)) 1f else 0.5f
        }

        override fun createChildren() {
            super.createChildren()

            image = Icons.get(if (DarkestPixelDungeon.challenges() > 0) Icons.CHALLENGE_ON else Icons.CHALLENGE_OFF)
            add(image)
        }

        override fun layout() {
            super.layout()

            image.x = x
            image.y = y
        }

        override fun onClick() {
            if (Badges.isUnlocked(Badges.Badge.VICTORY)) {
                this@NewStartScene.add(object : WndChallenges(DarkestPixelDungeon.challenges(), true) {
                    override fun onBackPressed() {
                        super.onBackPressed()
                        image.copy(Icons.get(if (DarkestPixelDungeon.challenges() > 0) Icons.CHALLENGE_ON else Icons.CHALLENGE_OFF))
                    }
                })
            } else
                this@NewStartScene.add(WndMessage(M.L(NewStartScene::class.java, "need_to_win")))
        }
    }
}