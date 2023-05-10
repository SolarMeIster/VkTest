package ru.solarmeister.vktest

import androidx.recyclerview.widget.DiffUtil
import java.io.File

class DiffUtilCallBack(private val oldArray: MutableList<File>, private val newArray: MutableList<File>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldArray.size

    override fun getNewListSize(): Int = newArray.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldArray[oldItemPosition] === newArray[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldArray[oldItemPosition].name == newArray[newItemPosition].name &&
                oldArray[oldItemPosition].absolutePath == newArray[newItemPosition].absolutePath &&
                oldArray[oldItemPosition].length() == newArray[newItemPosition].length() &&
                oldArray[oldItemPosition].lastModified() == newArray[newItemPosition].lastModified()
    }
}