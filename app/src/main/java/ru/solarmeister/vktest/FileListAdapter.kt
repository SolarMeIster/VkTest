package ru.solarmeister.vktest

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.solarmeister.vktest.databinding.ItemFileListBinding
import java.io.File
import android.text.format.DateFormat
import android.util.Log
import android.widget.AdapterView.OnItemLongClickListener

class FileListAdapter(private val clickable: Clickable) :
    RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    private var filesInFolders: MutableList<File> = mutableListOf()
    private lateinit var binding: ItemFileListBinding

    fun setNotifyData(newFilesInFolders: MutableList<File>) {
        val diffCallback = DiffUtilCallBack(filesInFolders, newFilesInFolders)
        val listBleDeviceDiffResult = DiffUtil.calculateDiff(diffCallback)
        filesInFolders.clear()
        filesInFolders.addAll(newFilesInFolders)
        listBleDeviceDiffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(
        private val binding: ItemFileListBinding,
        //private val onItemClickListener: ((file: File) -> Unit),
        private val context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File, clickable: Clickable) {
            val a = file.hashCode()
            Log.i("HashCode", "HashCode: $a")
            binding.itemView.setOnClickListener {
                clickable.onItemClickListener(file)
            }
            with(binding) {
                fileName.text = file.name
                val lastModifiedDate = DateFormat.format("dd.MM.yyyy k:mm", file.lastModified())
                fileDate.text = lastModifiedDate.toString()
                if (file.isDirectory) {
                    val sizeOfDirectory = file.list()?.size
                    fileSize.text = context.getString(R.string.directorySize, sizeOfDirectory)
                    filePicture.setImageResource(R.drawable.folder)
                } else {
                    binding.itemView.setOnLongClickListener {
                        clickable.onLongItemClickListener(file)
                    }
                    val pair = lengthFile(file.length())
                    fileSize.text = context.getString(R.string.fileSize, pair.first, pair.second)
                    if (file.name.contains("png"))
                        filePicture.setImageResource(R.drawable.png)
                    else if (file.name.contains("jpg"))
                        filePicture.setImageResource(R.drawable.jpg)
                    else if (file.name.contains("jpeg"))
                        filePicture.setImageResource(R.drawable.jpeg)
                    else if (file.name.contains("txt"))
                        filePicture.setImageResource(R.drawable.txt)
                    else if (file.name.contains("pdf"))
                        filePicture.setImageResource(R.drawable.pdf)
                    else if (file.name.contains("doc")) {
                        filePicture.setImageResource(R.drawable.doc)
                    } else if (file.name.contains("mp3")) {
                        filePicture.setImageResource(R.drawable.mp3)
                    } else if (file.name.contains("mp4")) {
                        filePicture.setImageResource(R.drawable.mp4)
                    } else if (file.name.contains("avi")) {
                        filePicture.setImageResource(R.drawable.avi)
                    } else {
                        filePicture.setImageResource(R.drawable.file)
                    }
                }
            }
        }

        private fun lengthFile(size: Long): Pair<String, String> {
            var newSize = size.toDouble()
            var i = 0
            while (newSize > 1024){
                newSize /= 1024.0
                i++
            }
            val stringSize = String.format("%.2f", newSize)
            return when (i) {
                0 -> Pair("$size", "B")
                1 -> Pair(stringSize, "kB")
                2 -> Pair(stringSize, "mB")
                else -> Pair(stringSize, "gB")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemFileListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, /*onItemClickListener,*/ parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = filesInFolders[holder.adapterPosition]
        holder.bind(file, clickable)
    }

    override fun getItemCount(): Int = filesInFolders.size
}