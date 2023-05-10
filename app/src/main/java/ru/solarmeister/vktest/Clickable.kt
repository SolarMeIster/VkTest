package ru.solarmeister.vktest

import java.io.File

interface Clickable {

    fun onItemClickListener(file: File)

    fun onLongItemClickListener(file: File): Boolean

}