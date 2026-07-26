package dev.mkao.weaver.features.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.net.toUri
import androidx.room.Room
import dev.mkao.weaver.R
import dev.mkao.weaver.data.database.NewsDao
import dev.mkao.weaver.data.database.NewsDatabase
import dev.mkao.weaver.data.database.asDomainModel
import dev.mkao.weaver.domain.model.Article
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NewsRemoteViewsFactory(applicationContext, intent)
    }

    class NewsRemoteViewsFactory(
        private val context: Context,
        intent: Intent
    ) : RemoteViewsFactory {

        private var articles: List<Article> = emptyList()

        private val newsDao: NewsDao by lazy {
            Room.databaseBuilder(
                context.applicationContext,
                NewsDatabase::class.java,
                "NewsDB"
            ).fallbackToDestructiveMigration().build().articleDao()
        }

        override fun onCreate() {}

        override fun onDataSetChanged() {
            runBlocking {
                articles = newsDao.getArticles().first().take(5).asDomainModel()
            }
        }

        override fun onDestroy() {
            articles = emptyList()
        }

        override fun getCount(): Int = articles.size

        override fun getViewAt(position: Int): RemoteViews {
            if (position < 0 || position >= articles.size) {
                return RemoteViews(context.packageName, R.layout.widget_list_item)
            }

            val article = articles[position]
            val rv = RemoteViews(context.packageName, R.layout.widget_list_item)

            rv.setTextViewText(R.id.widget_item_title, article.title)
            rv.setTextViewText(R.id.widget_item_source, article.source.name)

            rv.setImageViewResource(
                R.id.widget_item_image,
                R.drawable.placeholder_image
            )

            val fillInIntent = Intent().apply {
                action = WidgetProvider.ACTION_OPEN_ARTICLE
                putExtra(WidgetProvider.EXTRA_ARTICLE_URL, article.url)
                putExtra(WidgetProvider.EXTRA_ARTICLE_TITLE, article.title)
            }
            rv.setOnClickFillInIntent(R.id.widget_item_title, fillInIntent)

            return rv
        }

        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount(): Int = 1
        override fun getItemId(position: Int): Long = position.toLong()
        override fun hasStableIds(): Boolean = true
    }
}
