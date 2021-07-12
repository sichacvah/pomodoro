package com.sergey_kurochkin.core

import oolong.Dispatch
import oolong.effect
import oolong.effect.none
import oolong.Effect


object PomodoroTimer {
    sealed class Model {
        abstract val sections: List<Section.Model>
        data class Running(override val sections : List<Section.Model>, val currentSection : Int) : Model()
        data class Paused(override val sections : List<Section.Model>, val currentSection: Int) : Model()
        data class Pending(override val sections: List<Section.Model>) : Model()
        data class Finished(override val sections: List<Section.Model>) : Model()
    }

    sealed class Msg {
        object Start : Msg()
        object Tick : Msg()
        object Pause : Msg()
    }

    private val convertMsgToChildMsg : (Msg) -> Section.Msg = { msg ->
        when (msg) {
            Msg.Start -> Section.Msg.Start
            Msg.Tick -> Section.Msg.Tick
            Msg.Pause -> Section.Msg.Pause
        }
    }

    data class Props(
        val model : Model,
        val timeLeft: Int,
        val duration: Int,
        val start: (Dispatch<Msg>) -> Unit,
        val pause: (Dispatch<Msg>) -> Unit,
        val kind : Section.Model.Kind,
        val currentSection: Int
    )

    private val extractTimeLeft : (Model) -> Int = { model ->
        when (model) {
            is Model.Running -> extractTimeFromSection(extractSection(model))
            is Model.Paused -> extractTimeFromSection(extractSection(model))
            is Model.Finished -> extractSection(model).duration
            is Model.Pending -> extractSection(model).meta.duration
        }
    }

    private val extractDuration : (Model) -> Int = { model ->
        when (model) {
            is Model.Running -> extractSection(model).duration
            is Model.Paused -> extractSection(model).duration
            is Model.Finished -> extractSection(model).duration
            is Model.Pending -> extractSection(model).duration
        }
    }

    private val extractTimeFromSection : (Section.Model) -> Int = { model ->
        when (model) {
            is Section.Model.Idle -> model.duration
            is Section.Model.Finished -> 0
            is Section.Model.Running -> model.duration - model.currentTime
            is Section.Model.Paused -> model.duration - model.currentTime
        }
    }

    private val extractSectionIndex : (Model) -> Int = { model ->
        when (model) {
            is Model.Running -> model.currentSection
            is Model.Paused -> model.currentSection
            is Model.Finished -> 0
            is Model.Pending -> 0
        }
    }

    private val extractSection : (Model) -> Section.Model = { model ->
        when (model) {
            is Model.Running -> model.sections[model.currentSection]
            is Model.Paused -> model.sections[model.currentSection]
            is Model.Finished -> model.sections.first()
            is Model.Pending -> model.sections.first()
        }
    }

    private val extractKind : (Model) -> Section.Model.Kind = {model ->
        extractSection(model).meta.kind
    }


    val view : (Model) -> Props = { model ->
        Props(
            model = model,
            kind = extractKind(model),
            timeLeft = extractTimeLeft(model),
            duration = extractDuration(model),
            start = { dispatch -> dispatch(Msg.Start) },
            pause = { dispatch -> dispatch(Msg.Pause) },
            currentSection = extractSectionIndex(model)
        )
    }

    private val defaultSections = listOf<Section.Model>(
        Section.Model.Idle(
            meta = Section.Model.Meta(duration = 20, kind = Section.Model.Kind.WORK)
        ),
        Section.Model.Idle(
            meta = Section.Model.Meta(duration = 10, kind = Section.Model.Kind.REST)
        ),
        Section.Model.Idle(
            meta = Section.Model.Meta(duration = 20, kind = Section.Model.Kind.WORK)
        ),
    )

    val init : () -> Pair<Model, Effect<Msg>> = { ->
        Model.Pending(defaultSections) to none()
    }

    val update : (Msg, Model) -> Pair<Model, Effect<Msg>> = { msg, model ->

        val childMsg = convertMsgToChildMsg(msg)
        when (msg) {
            is Msg.Start -> {
                when (model) {
                    is Model.Finished -> {
                        val sections = Section.reset(model.sections)
                        val currentSection = 0
                        val section = Section.update(childMsg, sections[currentSection])
                        Model.Running(sections = sections.updatedAt(currentSection, section), currentSection = currentSection) to effect<Msg> { dispatch ->
                            Ticker.start {
                                dispatch(Msg.Tick)
                            }
                        }
                    }
                    is Model.Pending -> {
                        val currentSection = 0
                        val section = Section.update(childMsg, model.sections[currentSection])
                        Model.Running(sections = model.sections.updatedAt(currentSection, section), currentSection = currentSection) to effect<Msg> { dispatch ->
                            Ticker.start {
                                dispatch(Msg.Tick)
                            }
                        }
                    }
                    is Model.Paused -> {
                        val currentSection = model.currentSection
                        val section = Section.update(childMsg, model.sections[currentSection])
                        Model.Running(sections = model.sections.updatedAt(currentSection, section), currentSection = model.currentSection) to effect { dispatch ->
                            Ticker.start { dispatch(Msg.Tick) }
                        }
                    }
                    else -> model to none()
                }
            }
            is Msg.Pause -> {
                when (model) {
                    is Model.Running -> {
                        val currentSection = model.currentSection
                        val section = Section.update(childMsg, model.sections[currentSection])
                        Model.Paused(model.sections.updatedAt(currentSection, section), model.currentSection) to effect {
                            Ticker.stop()
                        }
                    }
                    else -> model to none()
                }
            }
            is Msg.Tick -> {
                when (model) {
                    is Model.Running -> {
                        val currentSectionIndex = model.currentSection
                        val currentSection = model.sections.get(currentSectionIndex)
                        val section = Section.update(childMsg, currentSection)
                        val sections = model.sections.updatedAt(currentSectionIndex, section)
                        if (section is Section.Model.Finished) {
                            if (currentSectionIndex == model.sections.size - 1) {
                                Model.Finished(sections) to effect<Msg> {
                                    Ticker.stop()
                                }
                            } else {
                                Model.Paused(sections = sections, currentSection = currentSectionIndex + 1) to effect {
                                    Ticker.stop()
                                }
                            }
                        } else {
                            model.copy(sections = sections) to none()
                        }
                    }
                    else -> model to none()
                }
            }
        }
    }
}