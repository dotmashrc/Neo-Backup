/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.utils

import android.content.pm.PackageManager
import android.text.format.Formatter
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import com.machiav3lli.backup.CHIP_SIZE_APP
import com.machiav3lli.backup.CHIP_SIZE_CACHE
import com.machiav3lli.backup.CHIP_SIZE_DATA
import com.machiav3lli.backup.CHIP_SPLIT
import com.machiav3lli.backup.CHIP_TYPE
import com.machiav3lli.backup.CHIP_VERSION
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.ui.compose.navigation.NavItem
import com.machiav3lli.backup.ui.compose.theme.ColorDisabled
import com.machiav3lli.backup.ui.compose.theme.ColorNotInstalled
import com.machiav3lli.backup.ui.compose.theme.ColorSpecial
import com.machiav3lli.backup.ui.compose.theme.ColorSystem
import com.machiav3lli.backup.ui.compose.theme.ColorUpdated
import com.machiav3lli.backup.ui.compose.theme.ColorUser
import com.machiav3lli.backup.ui.item.InfoChipItem

fun getStats(appsList: MutableList<Package>): Triple<Int, Int, Int> {
    var backupsNumber = 0
    var updatedNumber = 0
    appsList.forEach {
        if (it.hasBackups) {
            backupsNumber += it.numberOfBackups
            if (it.isUpdated) updatedNumber += 1
        }
    }
    return Triple(appsList.size, backupsNumber, updatedNumber)
}

fun PackageManager.getInstalledPackagesWithPermissions() =
    getInstalledPackages(0).map { getPackageInfo(it.packageName, PackageManager.GET_PERMISSIONS) }

fun List<AppExtras>.get(packageName: String) =
    find { it.packageName == packageName } ?: AppExtras(packageName)

@Composable
fun Package.infoChips(): List<InfoChipItem> = listOfNotNull(
    InfoChipItem(
        flag = CHIP_TYPE,
        text = stringResource(if (isSpecial) R.string.apptype_special else if (isSystem) R.string.apptype_system else R.string.apptype_user),
        iconId = when {
            isSpecial -> R.drawable.ic_special
            isSystem -> R.drawable.ic_system
            else -> R.drawable.ic_user
        },
        color = when {
            !isInstalled -> ColorNotInstalled
            isDisabled -> ColorDisabled
            isSpecial -> ColorSpecial
            isSystem -> ColorSystem
            else -> ColorUser
        }
    ),
    InfoChipItem(
        flag = CHIP_VERSION,
        text = versionName ?: versionCode.toString(),
        iconId = if (this.isUpdated) R.drawable.ic_updated else -1,
        color = if (this.isUpdated) ColorUpdated else null,
    ),
    InfoChipItem(
        flag = CHIP_SIZE_APP,
        text = stringResource(id = R.string.app_size) + Formatter.formatFileSize(
            LocalContext.current,
            storageStats?.appBytes ?: 0
        ),
    ),
    InfoChipItem(
        flag = CHIP_SIZE_DATA,
        text = stringResource(id = R.string.data_size) + Formatter.formatFileSize(
            LocalContext.current,
            storageStats?.dataBytes ?: 0
        ),
    ),
    InfoChipItem(
        flag = CHIP_SIZE_CACHE,
        text = stringResource(id = R.string.cache_size) + Formatter.formatFileSize(
            LocalContext.current,
            storageStats?.cacheBytes ?: 0
        ),
    ),
    if (this.apkSplits.isNotEmpty()) InfoChipItem(
        flag = CHIP_SPLIT,
        text = stringResource(id = R.string.split_apks),
    ) else null
)

fun NavDestination.destinationToItem(): NavItem? = listOf(
    NavItem.UserPrefs,
    NavItem.ServicePrefs,
    NavItem.AdvancedPrefs,
    NavItem.ToolsPrefs,
    NavItem.Home,
    NavItem.Backup,
    NavItem.Restore,
    NavItem.Scheduler,
    NavItem.Settings
).find { this.route == it.destination }