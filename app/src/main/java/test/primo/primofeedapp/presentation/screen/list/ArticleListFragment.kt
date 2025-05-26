package test.primo.primofeedapp.presentation.screen.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import test.primo.primofeedapp.R
import test.primo.primofeedapp.databinding.FragmentArticleListBinding

@AndroidEntryPoint
class ArticleListFragment : Fragment() {

    private val viewModel: ArticleListViewModel by viewModels()
    private var _binding: FragmentArticleListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentArticleListBinding.inflate(LayoutInflater.from(context), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        getAllArticle()
        observeUiState()
    }

    private fun setUpView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this@ArticleListFragment.adapter = ArticleAdapter(onArticleClick)
            adapter = this@ArticleListFragment.adapter
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            isNestedScrollingEnabled = false
        }
    }

    private fun getAllArticle() {
        viewModel.getAllArticle("@primoapp")
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.loadingProgress.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                    if (it.hasData) {
                        adapter.submitList(it.data)
                    } else if (it.hasError) {
                        Toast.makeText(context, it.error, Toast.LENGTH_SHORT).show()
                    } else if(it.isEmpty) {
                        Toast.makeText(context, getString(R.string.no_data_available), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val onArticleClick: (content: String) -> Unit = { content ->
        findNavController().navigate(ArticleListFragmentDirections.actionArticleListFragmentToArticleDetailFragment(content))
    }

}