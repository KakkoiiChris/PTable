package kakkoiichris.ptable

import kakkoiichris.hypergame.input.Input
import kakkoiichris.hypergame.input.Key
import kakkoiichris.hypergame.media.Renderer
import kakkoiichris.hypergame.state.State
import kakkoiichris.hypergame.state.StateManager
import kakkoiichris.hypergame.util.Time
import kakkoiichris.hypergame.view.View
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.image.BufferedImage

object TableState : State {
    private enum class SubState {
        FadeIn {
            override val next get() = Expanding
        },
        
        Expanding {
            override val next get() = SlideDown
        },
        
        SlideDown {
            override val next get() = SlideOut
        },
        
        SlideOut {
            override val next get() = Main
        },
        
        Main {
            override val next get() = Main
        };
        
        companion object {
            var current = FadeIn
            
            fun next() {
                current = current.next
            }
        }
        
        abstract val next: SubState
    }
    
    private const val FADE_ALPHA_DELTA = 0.01
    private var fadeAlpha = 1.0
    
    private lateinit var screenshot: BufferedImage
    
    override val name get() = STATE_TABLE

    override fun swapTo(view: View, passed: List<Any>) {
        val (ss) = passed

        screenshot = ss as BufferedImage
    }

    override fun swapFrom(view: View) {
    }

    override fun update(view: View, manager: StateManager, time: Time, input: Input) {
        when (SubState.current) {
            SubState.FadeIn    -> {
                println(time.delta)
                
                fadeAlpha -= time.delta * FADE_ALPHA_DELTA
                
                if (fadeAlpha <= 0.0) {
                    fadeAlpha = 0.0
                    
                    Table.expand()
                    
                    SubState.next()
                }
            }
            
            SubState.Expanding -> {
                Table.update(view, manager, time, input)
                
                if (!Table.expanding) {
                    Table.slideDown()
                    
                    SubState.next()
                }
            }
            
            SubState.SlideDown -> {
                Table.update(view, manager, time, input)
                
                if (!Table.expanding) {
                    Table.slideOut()
                    
                    SubState.next()
                }
            }
            
            SubState.SlideOut  -> {
                Table.update(view, manager, time, input)
                
                if (!Table.expanding) {
                    SubState.next()
                }
            }
            
            SubState.Main      -> {
                if (input.keyDown(Key.SPACE)) {
                    Labels.nextMode = Labels.Mode.Numerals
                }
                
                Table.update(view, manager, time, input)
                
                Labels.update(view, manager, time, input)
            }
        }
    }
    
    override fun render(view: View, renderer: Renderer) {
        with(renderer) {
            color = Color.BLACK
            
            fillRect(0, 0, view.width, view.height)
            
            when (SubState.current) {
                SubState.FadeIn                                           -> {
                    Table.render(view, renderer)
                    
                    val compositeLast = renderer.composite
                    
                    renderer.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha.toFloat())
                    
                    renderer.drawImage(screenshot, 0, 0)
                    
                    renderer.composite = compositeLast
                }
                
                SubState.Expanding, SubState.SlideDown, SubState.SlideOut -> Table.render(view, renderer)
                
                SubState.Main                                             -> {
                    Labels.render(view, renderer)
                    
                    Table.render(view, renderer)
                }
            }
        }
    }
    
    override fun halt(view: View) {
    }
}