package me.danikvitek.lab6.screen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults.textButtonColors
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Job
import me.danikvitek.lab6.R
import me.danikvitek.lab6.data.entity.Reminder
import me.danikvitek.lab6.ui.theme.Lab6Theme
import me.danikvitek.lab6.viewmodel.MainScreenViewModel
import java.util.Date
import kotlin.random.Random

@Composable
fun MainScreen(
    onNavigateToAddReminder: () -> Unit,
    onNavigateToReminder: (id: Long) -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val reminders by viewModel.reminders.collectAsState()
    MainScreen(
        reminders = reminders,
        onNavigateToAddReminder = onNavigateToAddReminder,
        onNavigateToReminder = onNavigateToReminder,
        onDeleteReminder = viewModel::deleteReminder,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    reminders: List<Reminder>,
    onNavigateToAddReminder: () -> Unit,
    onNavigateToReminder: (id: Long) -> Unit,
    onDeleteReminder: (id: Long) -> Job,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.reminders))
                },
                actions = {
                    IconButton(onClick = { onNavigateToAddReminder() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_reminder),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        var contextMenuReminderId: Long? by remember { mutableStateOf(null) }
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(all = 8.dp),
        ) {
            items(reminders, { it.id }) {
                ReminderItem(
                    it,
                    onNavigateToReminder = { onNavigateToReminder(it.id) },
                    onOpenContextMenu = { contextMenuReminderId = it.id },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        contextMenuReminderId?.let { id ->
            ModalBottomSheet(
                onDismissRequest = { contextMenuReminderId = null },
            ) {
                TextButton(
                    onClick = {
                        onDeleteReminder(id).invokeOnCompletion { th ->
                            th?.let {
                                Log.e("MainScreen", "Deletion Job for Reminder(id=$id) failed", th)
                            } ?: run {
                                Log.d("MainScreen", "Deletion Job for Reminder(id=$id) completed")
                                contextMenuReminderId = null
                            }
                        }
                    },
                    colors = textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                    )
                    Text(text = stringResource(R.string.delete))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReminderItem(
    reminder: Reminder,
    onNavigateToReminder: () -> Unit,
    onOpenContextMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current

    ElevatedCard(
        modifier = modifier
            .combinedClickable(
                onClick = onNavigateToReminder,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenContextMenu()
                },
                onLongClickLabel = stringResource(R.string.open_context_menu),
            ),
        colors = cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = reminder.text,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview(
    @PreviewParameter(MainScreenPreviewProvider::class) list: List<Reminder>,
) {
    Lab6Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainScreen(
                list,
                onNavigateToAddReminder = {},
                onDeleteReminder = { Job() },
                onNavigateToReminder = {},
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

private class MainScreenPreviewProvider : PreviewParameterProvider<List<Reminder>> {
    override val values = sequenceOf(
        listOf(
            Reminder(
                id = 1,
                title = "Title 1",
                text = "Text 1",
                datetime = Date(),
            ),
            Reminder(
                id = 2,
                title = "Title 2",
                text = "Text 2",
                datetime = Date(),
            ),
        ),
        List(15) { idx ->
            val id = idx.toLong() + 1
            Reminder(
                id = id,
                title = randomString(Random.nextInt(10, 100)),
                text = randomString(Random.nextInt(10, 50)),
                datetime = Date(),
            )
        }
    )
    override val count: Int = 2

    companion object {
        private val chars = buildSet(capacity = 26 + 26 + 10 + 1) {
            addAll('a'..'z')
            addAll('A'..'Z')
            addAll('0'..'9')
            add(' ')
        }

        private fun randomString(length: Int): String =
            generateSequence { chars.random() }.take(length).joinToString("")
    }
}