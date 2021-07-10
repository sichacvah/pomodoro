package com.sergey_kurochkin.core



object Section {


    sealed class Model {
        enum class Kind {
            WORK, REST
        }

        data class Meta(val duration: Int, val kind: Kind)
        abstract val meta : Meta

        val duration: Int
            get() = this.meta.duration


        data class Running(override val meta: Meta, val currentTime: Int): Model()
        data class Idle(override val meta: Meta): Model()
        data class Paused(override val meta: Meta, val currentTime: Int): Model()
        data class Finished(override val meta: Meta): Model()
    }

    sealed class Msg {
        object Start : Msg()
        object Pause : Msg()
        object Tick : Msg()
    }

    val reset : (models: List<Model>) -> List<Model> = { models ->
        models.map { model ->
            Model.Idle(model.meta)
        }
    }

    val update : (Msg, Model) -> Model = { msg, model ->
        when (msg) {
            is Msg.Start ->
                when (model) {
                    is Model.Idle -> Model.Running(model.meta, currentTime = 0)

                    is Model.Paused -> Model.Running(model.meta, currentTime = model.currentTime)

                    else -> model
                }

            is Msg.Pause ->
                when (model) {
                    is Model.Running -> Model.Paused(model.meta, model.currentTime)
                    else -> model
                }

            is Msg.Tick ->
                when (model) {
                    is Model.Running -> {
                        val currentTime = model.currentTime + 1
                        if (currentTime >= model.meta.duration) {
                            Model.Finished(model.meta)
                        } else {
                            model.copy(currentTime = currentTime)
                        }
                    }
                    else -> model
                }


        }
    }
}

