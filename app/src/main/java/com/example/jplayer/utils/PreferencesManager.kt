package com.example.jplayer.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.jplayer.model.SortOrder
import com.example.jplayer.model.ViewMode

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREF_NAME,
        Context.MODE_PRIVATE
    )

    var viewMode: ViewMode
        get() = ViewMode.valueOf(
            prefs.getString(Constants.PREF_VIEW_MODE, ViewMode.LIST.name) 
                ?: ViewMode.LIST.name
        )
        set(value) = prefs.edit()
            .putString(Constants.PREF_VIEW_MODE, value.name)
            .apply()

    var sortOrder: SortOrder
        get() = SortOrder.valueOf(
            prefs.getString(Constants.PREF_SORT_ORDER, SortOrder.NAME_ASC.name)
                ?: SortOrder.NAME_ASC.name
        )
        set(value) = prefs.edit()
            .putString(Constants.PREF_SORT_ORDER, value.name)
            .apply()

    var playbackSpeed: Float
        get() = prefs.getFloat(Constants.PREF_PLAYBACK_SPEED, 1.0f)
        set(value) = prefs.edit()
            .putFloat(Constants.PREF_PLAYBACK_SPEED, value)
            .apply()

    var repeatMode: Int
        get() = prefs.getInt(Constants.PREF_REPEAT_MODE, 0)
        set(value) = prefs.edit()
            .putInt(Constants.PREF_REPEAT_MODE, value)
            .apply()

    var lockOrientation: Boolean
        get() = prefs.getBoolean(Constants.PREF_LOCK_ORIENTATION, false)
        set(value) = prefs.edit()
            .putBoolean(Constants.PREF_LOCK_ORIENTATION, value)
            .apply()
}
