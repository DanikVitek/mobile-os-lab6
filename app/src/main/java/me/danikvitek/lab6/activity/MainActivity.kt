package me.danikvitek.lab6.activity

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import me.danikvitek.lab6.screen.AddReminderScreen
import me.danikvitek.lab6.screen.MainScreen
import me.danikvitek.lab6.screen.ReminderScreen
import me.danikvitek.lab6.ui.theme.Lab6Theme

@Serializable
private data object Main

@Serializable
private data object AddReminder

@Serializable
private data class Reminder(val id: Long)

const val BASE_URI = "mobile-os://lab6"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS,
            )
            Log.d(MainActivity::class.simpleName, "permission status=$status")
            if (status != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.shouldShowRequestPermissionRationale(
//                    this,
//                    android.Manifest.permission.POST_NOTIFICATIONS,
//                )
//                ActivityResultContracts.RequestPermission().
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    0,
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            Lab6Theme {
                NavHost(
                    navController = navController,
                    startDestination = Main,
                ) {
                    composable<Main> {
                        MainScreen(
                            onNavigateToReminder = { navController.navigate(route = Reminder(it)) },
                            onNavigateToAddReminder = { navController.navigate(route = AddReminder) },
                        )
                    }
                    composable<AddReminder> {
                        AddReminderScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToReminder = {
                                navController.navigate(Reminder(it)) { popUpTo(Main) }
                            },
                        )
                    }
                    composable<Reminder>(
                        deepLinks = listOf(navDeepLink<Reminder>(basePath = "$BASE_URI/reminder"))
                    ) {
                        ReminderScreen(
                            id = it.toRoute<Reminder>().id,
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
