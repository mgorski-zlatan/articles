package com.mgorski.articles.viewmodel

import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.view.View
import android.widget.Toast
import com.mgorski.articles.Logger
import com.mgorski.articles.R
import com.mgorski.articles.di.AppComponent
import com.mgorski.articles.model.ArticleCategory
import com.mgorski.articles.model.toArticleCategory
import com.mgorski.articles.network.ArticleCategoriesService
import com.mgorski.articles.network.model.WebArticleCategoryRequest
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class ArticleCategoriesViewModel {

    @Inject lateinit var articleService: ArticleCategoriesService

    val articleCategories = ObservableArrayList<ArticleCategory>()
    val isLoading = ObservableBoolean()
    val categoryToAdd = ObservableField<String>()

    init {
        AppComponent.instance.inject(this)

        downloadArticleCategories()
    }

    private fun downloadArticleCategories() {
        articleService.getArticleCategories()
                .doOnSubscribe { isLoading.set(true) }
                .doAfterTerminate { isLoading.set(false) }
                .map { webArticleCategories -> webArticleCategories.map { it.toArticleCategory() } }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ newArticleCategories ->
                    articleCategories.clear()
                    articleCategories.addAll(newArticleCategories)
                }, { error ->
                    Logger.e(error)
                })
    }

    fun onAddClick(view: View) {
        articleService.sendArticleCategory(WebArticleCategoryRequest(categoryToAdd.get()))
                .doOnSubscribe { isLoading.set(true) }
                .doAfterTerminate { isLoading.set(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    articleCategories.add(ArticleCategory(categoryToAdd.get()))
                    categoryToAdd.set("")
                    Toast.makeText(view.context, R.string.success_add_new_category, Toast.LENGTH_LONG).show()
                }, { error ->
                    Toast.makeText(view.context, R.string.fail_add_new_category, Toast.LENGTH_LONG).show()
                    Logger.e(error)
                })
    }
}