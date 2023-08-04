package com.example.storyappdicoding.ui.view.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyappdicoding.R
import com.example.storyappdicoding.data.db.StoryDatabase
import com.example.storyappdicoding.data.remote.ApiConfig
import com.example.storyappdicoding.databinding.ActivityMainBinding
import com.example.storyappdicoding.ui.view.adapter.LoadingStateAdapter
import com.example.storyappdicoding.ui.view.adapter.StoriesAdapter
import com.example.storyappdicoding.ui.view.detail.DetailActivity
import com.example.storyappdicoding.ui.view.detail.DetailActivity.Companion.KEY_DETAIL
import com.example.storyappdicoding.ui.view.login.LoginActivity
import com.example.storyappdicoding.ui.view.map.StoryMapsActivity
import com.example.storyappdicoding.ui.view.upload.UploadStoryActivity
import com.example.storyappdicoding.ui.viewmodel.AccountSettingsViewModel
import com.example.storyappdicoding.ui.viewmodel.MainViewModel
import com.example.storyappdicoding.ui.viewmodel.factory.AccountSettingsViewModelFactory
import com.example.storyappdicoding.ui.viewmodel.factory.MainViewModelFactory
import com.example.storyappdicoding.utils.AccountPreferences
import com.example.storyappdicoding.utils.AccountPreferences.Companion.TOKEN_ID
import com.example.storyappdicoding.utils.dataStore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val storyAdapter = StoriesAdapter()

    private val tokenId by lazy { intent.getStringExtra(TOKEN_ID) }

    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModelFactory(
            tokenId!!,
            StoryDatabase.getDatabaseInstance(this),
            ApiConfig.getApiService()
        )
    }

    private val accountSettingsViewModel by viewModels<AccountSettingsViewModel> {
        AccountSettingsViewModelFactory(
            AccountPreferences.getPrefInstance(dataStore)
        )
    }

    private val addStoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshAdapter()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvStories.apply {
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    storyAdapter.retry()
                }
            )
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        observeFabsMenu()
        observeStories()

        setListeners()
    }

    private fun observeFabsMenu() {
        binding.apply {
            mainViewModel.isAllFabsVisible.observe(this@MainActivity) { isFabVisible ->
                if (isFabVisible) {
                    fabMenu.extend()
                    fabLogout.show()
                    fabMap.show()
                    fabAdd.show()
                } else {
                    fabMenu.shrink()
                    fabLogout.hide()
                    fabMap.hide()
                    fabAdd.hide()
                }
            }
        }
    }

    private fun observeStories() {
        mainViewModel.storyItems.observe(this) { storyItems ->
            storyAdapter.submitData(lifecycle, storyItems)
        }
    }

    private fun setListeners() {
        binding.apply {
            storyAdapter.onStoryClick = { view, story ->
                val ivStory = view.findViewById<ImageView>(R.id.iv_story)
                val layoutTime = view.findViewById<LinearLayout>(R.id.layout_time)
                val tvStoryName =
                    view.findViewById<TextView>(R.id.tv_story_name)
                val tvStoryDesc =
                    view.findViewById<TextView>(R.id.tv_story_description)

                val iDetail = Intent(this@MainActivity, DetailActivity::class.java)
                iDetail.putExtra(KEY_DETAIL, story)

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@MainActivity,
                        androidx.core.util.Pair(ivStory, "image"),
                        androidx.core.util.Pair(layoutTime, "time"),
                        androidx.core.util.Pair(tvStoryName, "username"),
                        androidx.core.util.Pair(tvStoryDesc, "desc")
                    )

                startActivity(iDetail, optionsCompat.toBundle())
            }

            fabMenu.setOnClickListener {
                mainViewModel.isAllFabsVisible.postValue(!mainViewModel.isAllFabsVisible.value!!)
            }

            fabLogout.setOnClickListener {
                accountSettingsViewModel.clearToken()
                val iLogin = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(iLogin)
                finishAffinity()
            }

            fabMap.setOnClickListener {
                val iMap = Intent(this@MainActivity, StoryMapsActivity::class.java)
                iMap.putExtra(TOKEN_ID, tokenId)
                startActivity(iMap)
            }

            fabAdd.setOnClickListener {
                val iUpload = Intent(this@MainActivity, UploadStoryActivity::class.java)
                iUpload.putExtra(TOKEN_ID, tokenId)
                addStoryLauncher.launch(iUpload)
            }
        }
    }

    private fun refreshAdapter() {
        storyAdapter.refresh()
        binding.rvStories.smoothScrollToPosition(0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_refresh) {
            refreshAdapter()
        }
        return super.onOptionsItemSelected(item)
    }


}