package test.primo.primofeedapp.presentation.screen.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import test.primo.primofeedapp.R
import test.primo.primofeedapp.core.extention.extractParagraphs
import test.primo.primofeedapp.databinding.ItemArticleBinding

class ArticleAdapter(
    private val onArticleClick: ((String) -> Unit)? = null
) : ListAdapter<ArticleUiState, ArticleViewHolder>(TimelineDiffUtils()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding, onArticleClick)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class ArticleViewHolder(
    private val binding: ItemArticleBinding,
    private val onArticleClick: ((String) -> Unit)? = null
) : ViewHolder(binding.root) {
    fun bind(item: ArticleUiState) {
        binding.root.setOnClickListener {
            onArticleClick?.invoke(item.content)
        }

        binding.title.text = item.title
        val htmlContent = item.content.extractParagraphs()
        binding.subTitle.text = htmlContent
        binding.dateTime.text = item.dateTime

        Glide
            .with(binding.root)
            .load(item.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_background)
            .error(android.R.drawable.stat_notify_error)
            .into(binding.imageCover)
    }
}

class TimelineDiffUtils : DiffUtil.ItemCallback<ArticleUiState>() {

    override fun areItemsTheSame(oldItem: ArticleUiState, newItem: ArticleUiState): Boolean {
        return when {
            oldItem == newItem -> true
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: ArticleUiState, newItem: ArticleUiState): Boolean {
        return when {
            oldItem == newItem -> true
            else -> false
        }
    }
}