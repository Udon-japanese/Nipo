package com.example.nipo.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nipo.data.Post
import com.example.nipo.data.PostRepository
import com.example.nipo.data.PostTag
import com.example.nipo.data.SosPost
import com.example.nipo.data.SosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PostRepository(application)
    private val sosRepository = SosRepository(application)

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _sosPosts = MutableStateFlow<List<SosPost>>(emptyList())
    val sosPosts: StateFlow<List<SosPost>> = _sosPosts

    private val _filterTips = MutableStateFlow(true)
    val filterTips: StateFlow<Boolean> = _filterTips

    private val _filterSos = MutableStateFlow(true)
    val filterSos: StateFlow<Boolean> = _filterSos

    private val _filterTags = MutableStateFlow<Set<PostTag>>(PostTag.entries.toSet())
    val filterTags: StateFlow<Set<PostTag>> = _filterTags

    init {
        viewModelScope.launch {
            repository.observePosts().collectLatest { _posts.value = it }
        }
        viewModelScope.launch {
            sosRepository.observeOpenSos().collectLatest { _sosPosts.value = it }
        }
    }

    fun setFilterTips(value: Boolean) {
        _filterTips.value = value
    }

    fun setFilterSos(value: Boolean) {
        _filterSos.value = value
    }

    fun toggleFilterTag(tag: PostTag) {
        _filterTags.value = if (tag in _filterTags.value) {
            _filterTags.value - tag
        } else {
            _filterTags.value + tag
        }
    }
}
