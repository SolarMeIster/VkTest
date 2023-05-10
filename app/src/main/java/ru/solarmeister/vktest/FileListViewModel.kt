package ru.solarmeister.vktest

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File

class FileListViewModel : ViewModel() {

    private val typesOfFiles = mapOf(
        "image" to listOf("jpeg", "jpg", "png"),
        "video" to listOf("mp4", "avi"),
        "text" to listOf("plain"),
        "audio" to listOf("mp3"),
        "application" to listOf("doc", "pdf", "docx")
    )

    private val _files = MutableLiveData<MutableList<File>>()
    val files: LiveData<MutableList<File>> = _files

    fun setPathToGetFiles(path: String?) {
        val file = path?.let { File(it) }
        viewModelScope.run {
            _files.value = file?.listFiles()?.toMutableList()
            _files.value?.sortBy {it.name}
        }
    }

    fun sortFiles(id: Int) {
        viewModelScope.run {
            val sortedFiles = _files.value!!
            when (id) {
                R.id.sortByDate -> {
                    sortedFiles.sortBy { it.lastModified() }
                    for (element in sortedFiles) {
                        Log.i("Sorted", "File $element and ${element.lastModified()}")
                    }
                }
                R.id.sortBySize -> {
                    sortedFiles.sortBy { it.length() }
                    for (element in sortedFiles) {
                        Log.i("Sorted", "File $element and ${element.length()}")
                    }
                }
                R.id.sortByExtension -> {
                    sortedFiles.sortBy { it.extension }
                    for (element in sortedFiles) {
                        Log.i("Sorted", "File $element and ${if (element.extension == "" || element.isDirectory) "Directory" else element.extension}")
                    }
                }
                else -> {
                    Log.e("SortedError", "Not find necessary id")
                }
            }
            _files.value = sortedFiles
        }
    }

    fun chooseTypeForOpenFile(extension: String): String {
        for ((key, list) in typesOfFiles) {
            if (list.contains(extension)) {
                return when (key) {
                    "image" -> {
                        "$key/jpeg"
                    }
                    "video" -> {
                        "$key/*"
                    }
                    "text" -> {
                        "$key/plain"
                    }
                    "audio" -> {
                        "$key/x-wav"
                    }
                    "application" -> {
                        if (extension == "doc" || extension == "docx")
                            "$key/msword"
                        else
                            "$key/$extension"
                    }
                    else -> {
                        UNKNOWN
                    }
                }
            }
        }
        return UNKNOWN
    }
    companion object {
        const val UNKNOWN = "UNKNOWN"
    }

}