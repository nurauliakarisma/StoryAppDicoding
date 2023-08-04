package com.example.storyappdicoding.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.storyappdicoding.databinding.LayoutLoaderBinding

class LoadingStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoadingStateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        return ViewHolder(LayoutLoaderBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false), retry)
    }

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class ViewHolder(private val binding: LayoutLoaderBinding, retry: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnReconnect.setOnClickListener { retry.invoke() }
        }

        fun bind(loadState: LoadState) {
            binding.apply {
                progressbar.isVisible = loadState is LoadState.Loading
                btnReconnect.isVisible = loadState is LoadState.Error
                tvError.isVisible = loadState is LoadState.Error

                if (loadState is LoadState.Error) {
                    tvError.text = loadState.error.localizedMessage
                }
            }
        }
    }
}