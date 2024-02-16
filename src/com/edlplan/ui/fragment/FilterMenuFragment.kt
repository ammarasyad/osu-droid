package com.edlplan.ui.fragment

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.edlplan.framework.easing.Easing
import com.edlplan.framework.support.util.Updater
import com.edlplan.ui.BaseAnimationListener
import com.edlplan.ui.EasingHelper
import com.reco1l.framework.lang.uiThread
import org.andengine.engine.handler.IUpdateHandler
import org.andengine.entity.scene.Scene
import ru.nsu.ccfit.zuev.osu.helper.InputManager
import ru.nsu.ccfit.zuev.osu.helper.StringTable
import ru.nsu.ccfit.zuev.osu.menu.IFilterMenu
import ru.nsu.ccfit.zuev.osu.menu.SongMenu
import ru.nsu.ccfit.zuev.osu.GlobalManager.getInstance as getGlobal
import ru.nsu.ccfit.zuev.osuplus.R

class FilterMenuFragment : BaseFragment(),
    IUpdateHandler, IFilterMenu {
    private var configContext: Context? = null
    private var savedFolder: String? = null
    private var savedFavOnly = false
    private var savedFilter: String? = null
    private var scene: Scene? = null
    private lateinit var filter: EditText
    private var menu: SongMenu? = null
    private lateinit var favoritesOnly: CheckBox
    private lateinit var favoriteFolder: TextView
    private lateinit var sortButton: Button
    private var updater: Updater? = null

    init {
        isDismissOnBackgroundClick = true
    }

    override val layoutID: Int
        get() = R.layout.fragment_filtermenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val (folder, favOnly, filter) = restoreState()
        savedFolder = folder
        savedFavOnly = favOnly
        savedFilter = filter
    }

    override fun onLoadView() {
        reloadViewData()
        playOnLoadAnim()
    }

    override fun getFilter(): String {
        return filter.text.toString()
    }

    override fun getOrder(): SongMenu.SortOrder {
        val prefs =
            PreferenceManager.getDefaultSharedPreferences(configContext!!)
        val order = prefs.getInt("sortorder", 0)
        return SongMenu.SortOrder.entries[order % SongMenu.SortOrder.entries.size]
    }

    override fun isFavoritesOnly(): Boolean = favoritesOnly.isChecked

    override fun getFavoriteFolder(): String =
        if (StringTable.get(R.string.favorite_default) == favoriteFolder.text) "" else favoriteFolder.text.toString()

    override fun loadConfig(context: Context?) {
        configContext = context
        uiThread(this::reloadViewData)
    }

    override fun getScene(): Scene = scene!!

    override fun hideMenu() {
        updateUpdater()
        scene = null
        dismiss()
    }

    override fun showMenu(parent: SongMenu?) {
        this.menu = parent!!
        scene = Scene()
        scene!!.isBackgroundEnabled = false
        updater = object : Updater() {
            override fun createEventRunnable(): Runnable =
                Runnable { parent.loadFilter(this@FilterMenuFragment) }

            override fun postEvent(r: Runnable?) = parent.scene.postRunnable(r)
        }
        show()
    }

    override fun onDetach() {
        super.onDetach()
        menu?.scene?.postRunnable { menu?.loadFilter(this) }
        menu = null
        scene = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        reloadViewData()
    }

    override fun dismiss() {
        playEndAnim { super.dismiss() }
        getGlobal().songMenu.unloadFilterFragment()
        saveState(savedFolder, savedFavOnly, savedFilter)
    }

    private fun reloadViewData() {
        if (isCreated) {
            filter = findViewById(R.id.searchEditText)!!
            favoritesOnly = findViewById(R.id.showFav)!!
            sortButton = findViewById(R.id.sortButton)!!
            favoriteFolder = findViewById(R.id.favFolder)!!

            favoritesOnly.setOnCheckedChangeListener { _, isChecked ->
                updateFavChecked()
                updateUpdater()
                savedFavOnly = isChecked
            }

            sortButton.setOnClickListener {
                nextOrder()
                updateOrderButton()
                updateUpdater()
            }

            findViewById<View>(R.id.favFolderLayout)!!.setOnClickListener {
                val favoriteManagerFragment = FavoriteManagerFragment()
                favoriteManagerFragment.showToSelectFolder {
                    savedFolder = it
                    favoriteFolder.text = it ?: StringTable.get(R.string.favorite_default)
                    updateUpdater()
                }
            }

            filter.setOnEditorActionListener { _, _, event ->
                if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    hideMenu()
                    return@setOnEditorActionListener true
                }
                false
            }

            filter.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable) {
                        savedFilter = s.toString()
                        updateUpdater()
                    }
                }
            )

            favoritesOnly.isChecked = savedFavOnly

            if (savedFilter?.isNotEmpty() == true) {
                filter.setText(savedFilter)
            }

            updateOrderButton()
            updateFavChecked()
            updateFavFolderText()
        }
    }

    private fun playOnLoadAnim() {
        val body = findViewById<View>(R.id.frg_body)!!
        body.alpha = 0f
        body.translationX = 400f
        body.animate().cancel()
        body.animate()
            .alpha(1f)
            .translationX(0f)
            .setInterpolator(EasingHelper.asInterpolator(Easing.InOutQuad))
            .setDuration(300)
            .start()
        playBackgroundHideInAnim(150)
    }

    private fun playEndAnim(action: () -> Unit) {
        val body = findViewById<View>(R.id.frg_body)!!
        body.animate().cancel()
        body.animate()
            .alpha(0f)
            .translationX(400f)
            .setInterpolator(EasingHelper.asInterpolator(Easing.InOutQuad))
            .setDuration(300)
            .setListener(
                object : BaseAnimationListener() {
                    override fun onAnimationEnd(animation: Animator) {
                        action()
                    }
                }
            )
            .start()
        playBackgroundHideOutAnim(150)
    }

    private fun updateFavChecked() {
        favoritesOnly.text =
            if (favoritesOnly.isChecked) getString(R.string.menu_search_favsenabled) else getString(
                R.string.menu_search_favsdisabled
            )
    }

    private fun updateOrderButton() {
        val s = when (order) {
            SongMenu.SortOrder.Title -> StringTable.get(R.string.menu_search_sort_title)
            SongMenu.SortOrder.Artist -> StringTable.get(R.string.menu_search_sort_artist)
            SongMenu.SortOrder.Creator -> StringTable.get(R.string.menu_search_sort_creator)
            SongMenu.SortOrder.Date -> StringTable.get(R.string.menu_search_sort_date)
            SongMenu.SortOrder.Bpm -> StringTable.get(R.string.menu_search_sort_bpm)
            SongMenu.SortOrder.Stars -> StringTable.get(R.string.menu_search_sort_stars)
            SongMenu.SortOrder.Length -> StringTable.get(R.string.menu_search_sort_length)
        }

        sortButton.text = s
    }

    private fun updateFavFolderText() {
        favoriteFolder.text =
            savedFolder.orEmpty().ifEmpty { StringTable.get(R.string.favorite_default) }
    }

    private fun nextOrder() {
        var order = order
        order = SongMenu.SortOrder.entries.toTypedArray()[(order.ordinal + 1) % SongMenu.SortOrder.entries.size]
        saveOrder(order)
    }

    private fun saveOrder(order: SongMenu.SortOrder) {
        PreferenceManager
            .getDefaultSharedPreferences(configContext!!)
            .edit()
            .putInt("sortorder", order.ordinal)
            .commit()
    }

    private fun updateUpdater() {
        updater?.update()
    }

    override fun onUpdate(pSecondsElapsed: Float) {
        if (InputManager.getInstance().isChanged) {
            filter.setText(InputManager.getInstance().text)
        }
    }

    override fun reset() {
        TODO("Not yet implemented")
    }

    // Due to how the fragment lifecycle works in this context, the state needs to be saved manually
    private companion object {
        var folder: String? = null
        var favOnly = false
        var filter: String? = null

        fun saveState(savedFolder: String?, savedFavOnly: Boolean, savedFilter: String?) {
            folder = savedFolder
            favOnly = savedFavOnly
            filter = savedFilter
        }

        fun restoreState() = Triple(folder, favOnly, filter)
    }
}