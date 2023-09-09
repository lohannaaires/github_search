package com.lohanna.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.lohanna.githubsearch.R
import com.lohanna.githubsearch.databinding.RepositoryItemBinding
import com.lohanna.githubsearch.domain.Repository

class RepositoryAdapter(private val repositories: List<Repository>) : RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    private lateinit var binding: RepositoryItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = RepositoryItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(repositories[position])

        holder.itemView.setOnClickListener {
            setOptionClickListener?.let {
                it(0, repositories[position].htmlUrl)
            }
        }

        val ivShare = holder.itemView.findViewById<ImageView>(R.id.iv_share)

        ivShare.setOnClickListener {
            setOptionClickListener?.let {
                it(1, repositories[position].htmlUrl)
            }
        }
    }

    override fun getItemCount(): Int = repositories.size

    private var setOptionClickListener : ((option: Int, htmlUrl: String) -> Unit)? = null

    fun onOptionClicked(listener:(option: Int, htmlUrl: String) -> Unit) {
        setOptionClickListener = listener
    }

    class ViewHolder(private val binding: RepositoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(selectedRepository: Repository) {
            binding.apply {
                selectedRepository.apply { tvRepositoryTitle.text = name }
            }
        }
    }
}