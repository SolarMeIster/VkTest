package ru.solarmeister.vktest

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ru.solarmeister.vktest.databinding.FragmentFileListBinding
import java.io.File


class FileListFragment : Fragment(), Clickable {

    private lateinit var binding: FragmentFileListBinding

    private val fileListViewModel: FileListViewModel by viewModels()

    private var flagBack: Boolean? = false

    private val fileListAdapter: FileListAdapter by lazy {
        FileListAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileListViewModel.setPathToGetFiles(arguments?.getString(ARG_VALUE))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        flagBack = arguments?.getBoolean(FLAG_BACK)
        fileListViewModel.files.observe(viewLifecycleOwner) { files ->
            if (flagBack == true) {
                binding.toolbar.title = arguments?.getString(FILENAME)
            }
            if (files == null || files.isEmpty()) {
                binding.emptyFolder.visibility = View.VISIBLE
            } else {
                binding.emptyFolder.visibility = View.GONE
                fileListAdapter.setNotifyData(files)
            }
        }
        binding = FragmentFileListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (flagBack == true) {
            with(binding) {
                toolbar.setNavigationIcon(R.drawable.arrow_back)
                toolbar.setNavigationOnClickListener {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        binding.toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                fileListViewModel.sortFiles(menuItem.itemId)
                return true
            }

        })
        setupRecyclerView()
    }

    //открыть файл или папку
    override fun onItemClickListener(file: File) {
        if (file.isDirectory) {
            openDirectory(file)
        } else {
            openFile(file)
        }
    }

    //передача файла другому приложению
    override fun onLongItemClickListener(file: File): Boolean {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.name)
        val type = mimeTypeMap.getExtensionFromMimeType(extension) ?: "*/*"
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, "Передача файла из менеджера файлов")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val content = FileProvider.getUriForFile(
                    requireContext(),
                    context?.packageName + ".provider",
                    file
                )
                intent.putExtra(Intent.EXTRA_STREAM, content)
                intent.setDataAndTypeAndNormalize(content, type)
            } else {
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                intent.setDataAndTypeAndNormalize(Uri.fromFile(file), type)
            }
            startActivity(intent)
        } catch (e: java.lang.Exception) {
            Toast.makeText(requireContext(), "Нельзя передать этот файл", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    //инициализация RecyclerView
    private fun setupRecyclerView() {
        with(binding) {
            fileListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            fileListRecyclerView.adapter = fileListAdapter
        }
    }

    private fun openDirectory(directory: File) {
        val newFileListFragment = newInstance(directory.absolutePath, directory.name)
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            addToBackStack(null)
            replace(R.id.fragment_container, newFileListFragment)
        }
    }

    private fun openFile(file: File) {
        try {
            val index = file.name.lastIndexOf('.')
            if (index <= 0) {
                return
            }
            val extension = file.name.substring(index + 1)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                context?.packageName + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndTypeAndNormalize(uri, fileListViewModel.chooseTypeForOpenFile(extension))
            }
            startActivity(intent)
        } catch (e: java.lang.Exception) {
            Toast.makeText(requireContext(), "Невозможно открыть файл", Toast.LENGTH_SHORT)
                .show()
        }
    }

    companion object {

        const val ARG_VALUE = "ARG_VALUE"
        private const val FLAG_BACK = "BACK_PRESS"
        private const val FILENAME = "FILENAME"

        fun newInstance(path: String, filename: String): FileListFragment {
            val args = Bundle().apply {
                putString(ARG_VALUE, path)
                putBoolean(FLAG_BACK, true)
                putString(FILENAME, filename)
            }
            val fragment = FileListFragment()
            fragment.arguments = args
            return fragment
        }
    }
}