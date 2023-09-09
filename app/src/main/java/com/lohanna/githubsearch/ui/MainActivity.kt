package com.lohanna.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lohanna.githubsearch.R
import com.lohanna.githubsearch.data.GitHubService
import com.lohanna.githubsearch.databinding.ActivityMainBinding
import com.lohanna.githubsearch.domain.Repository
import com.lohanna.githubsearch.ui.adapter.LastItemMarginItemDecoration
import com.lohanna.githubsearch.ui.adapter.RepositoryAdapter
import com.lohanna.githubsearch.util.dismissLoading
import com.lohanna.githubsearch.util.hideKeyboard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.lohanna.githubsearch.util.loading


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var gitHubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupRetrofit()
        showUsername()
    }

    private fun setupView() {
        setupListeners()

        binding.tiUsername.setOnFocusChangeListener { view, isFocused ->
            if(!isFocused) { view.hideKeyboard(this) }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    private fun setupListeners() {
        binding.apply {
            btnConfirm.setOnClickListener {
                tiUsername.clearFocus()
                pbLoader.loading(this@MainActivity)
                getAllReposByUserName(tiUsername.text.toString())
            }

            btnConfirm.setOnLongClickListener {
                it.isPressed = false
                true
            }
        }
    }

    private fun saveUserLocal(user: String) {
        val sharedPreference =  getSharedPreferences("PREFERENCE_USERNAME", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("username", user)
        editor.apply()
    }

    private fun showUsername() {
        val sharedPreferences =
            getSharedPreferences("PREFERENCE_USERNAME", Context.MODE_PRIVATE)

        sharedPreferences.getString("username", null)?.let {
            binding.apply {
                tiUsername.setText(it)
                btnConfirm.performClick()
            }
        }
    }

    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        gitHubApi = retrofit.create(GitHubService::class.java)
    }

    private fun getAllReposByUserName(user: String) {
        gitHubApi.getAllRepositoriesByUser(user).enqueue(object : Callback<List<Repository>> {
            override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(user, it)
                    }
                } else {
                    onError(response.code())
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                onError(null)
            }
        })
    }

    private fun setupAdapter(list: List<Repository>) {
        val repositoryAdapter = RepositoryAdapter(list)
        val lastItemMargin = resources.getDimensionPixelOffset(R.dimen.item_margin)

        binding.apply {
            ivEmptyList.visibility = View.GONE
            rvRepositories.adapter = repositoryAdapter
            rvRepositories.addItemDecoration(LastItemMarginItemDecoration(lastItemMargin))
        }

        repositoryAdapter.onOptionClicked { option, htmlUrl  ->
            when(option) {
                0 -> openBrowser(htmlUrl)
                1 -> shareRepositoryLink(htmlUrl)
            }
        }

    }

    private fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }

    fun onSuccess(user: String, list: List<Repository>) {
        saveUserLocal(user)
        setupAdapter(list)
        binding.pbLoader.dismissLoading(this@MainActivity)
    }

    fun onError(code: Int?) {
        val message = when(code) {
            404 -> getString(R.string.response_error)
            else -> getString(R.string.response_on_failure)
        }

        setupAdapter(listOf())

        binding.apply {
            pbLoader.dismissLoading(this@MainActivity)
            ivEmptyList.visibility = View.VISIBLE
        }

        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
    }
}