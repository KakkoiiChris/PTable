package kakkoiichris.ptable

import kakkoiichris.hypergame.input.Button
import kakkoiichris.hypergame.input.Input
import kakkoiichris.hypergame.media.Renderer
import kakkoiichris.hypergame.media.Sprite
import kakkoiichris.hypergame.state.State
import kakkoiichris.hypergame.state.StateManager
import kakkoiichris.hypergame.util.Time
import kakkoiichris.hypergame.view.View
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints

object IntroState : State {
    private enum class SubState {
        FadeInAtom {
            override val next get() = FadeInTitle
        },

        FadeInTitle {
            override val next get() = Idle
        },

        Idle {
            override val next get() = FadeOutTitle
        },

        FadeOutTitle {
            override val next get() = FadeOutTitle
        };

        companion object {
            var current = FadeInAtom

            fun next() {
                current = current.next
            }
        }

        abstract val next: SubState
    }

    private val atom = Sprite.load("/resources/img/icon.png")
    private val titleFont = Font("Boogie Boys", Font.BOLD, 150)
    private val versionFont = Font("Chemical Reaction B BRK", Font.PLAIN, 50)

    private const val ATOM_THETA_DELTA = 0.01
    private var atomTheta = 0.0

    private const val ATOM_ALPHA_DELTA = 0.01
    private var atomAlpha = 0.0

    private const val TITLE_ALPHA_DELTA = 0.01
    private var titleAlpha = 0.0

    private const val IDLE_TIME = 3.0
    private var idleTimer = 0.0

    override fun swapTo(view: View) {
        view.renderer.addRenderingHints(
            mapOf(
                RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
                RenderingHints.KEY_TEXT_ANTIALIASING to RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB,
            )
        )
    }

    override fun swapFrom(view: View) {
    }

    override fun update(view: View, manager: StateManager, time: Time, input: Input) {
        atomTheta += ATOM_THETA_DELTA

        if (input.buttonDown(Button.LEFT)) {
            manager.goto(MainState)
        }

        when (SubState.current) {
            SubState.FadeInAtom   -> {
                atomAlpha += time.delta * ATOM_ALPHA_DELTA

                if (atomAlpha >= 1.0) {
                    atomAlpha = 1.0

                    SubState.next()
                }
            }

            SubState.FadeInTitle  -> {
                titleAlpha += time.delta * TITLE_ALPHA_DELTA

                if (titleAlpha >= 1.0) {
                    titleAlpha = 1.0

                    SubState.next()
                }
            }

            SubState.Idle         -> {
                idleTimer += time.seconds

                if (idleTimer >= IDLE_TIME) {
                    SubState.next()
                }
            }

            SubState.FadeOutTitle -> {
                titleAlpha -= time.delta * TITLE_ALPHA_DELTA

                if (titleAlpha <= 0.0) {
                    titleAlpha = 0.0

                    manager.goto(MainState)
                }
            }
        }
    }

    override fun render(view: View, renderer: Renderer) {
        with(renderer) {
            color = bgColor

            fillRect(0, 0, view.width, view.height)

            push()

            translate(view.width / 2, view.height / 2)
            rotate(atomTheta)

            composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, atomAlpha.toFloat())

            drawImage(atom, -view.height / 2, -view.height / 2, view.height, view.height)

            pop()

            when (SubState.current) {
                SubState.FadeInTitle, SubState.Idle, SubState.FadeOutTitle -> {
                    font = titleFont

                    val titleWidth = fontMetrics.stringWidth(TITLE)

                    color = Color(fgDark.red, fgDark.green, fgDark.blue, (titleAlpha * 255).toInt())

                    drawString(TITLE, (view.width - titleWidth) / 2, (view.height - fontMetrics.height) / 2)
                }

                else                                                       -> Unit
            }
        }
    }

    override fun halt(view: View) {
    }
}