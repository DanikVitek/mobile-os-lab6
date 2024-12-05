package me.danikvitek.lab6.screen

import android.text.format.DateFormat
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.map
import me.danikvitek.lab6.R
import me.danikvitek.lab6.data.entity.Reminder
import me.danikvitek.lab6.ui.theme.Lab6Theme
import me.danikvitek.lab6.viewmodel.ReminderViewModel
import java.util.Date

@Composable
fun ReminderScreen(
    id: Long,
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val reminder by viewModel.getReminder(id)
        .map { it?.let(ReminderLoadState.Displayable::Success) ?: ReminderLoadState.Absent }
        .collectAsState(initial = ReminderLoadState.Displayable.Loading)
    when (reminder) {
        is ReminderLoadState.Absent -> {}
        is ReminderLoadState.Displayable ->
            ReminderScreen(
                reminder as ReminderLoadState.Displayable,
                onNavigateBack,
                onDeleteReminder = {
                    onNavigateBack()
                    viewModel.deleteReminder(id).invokeOnCompletion { th ->
                        th?.let {
                            Log.e("ReminderScreen", "Deletion Job for Reminder(id=$id) failed", it)
                        } ?: Log.d("ReminderScreen", "Deletion Job for Reminder(id=$id) completed")
                    }
                },
                modifier,
            )
    }
}

private sealed interface ReminderLoadState {
    sealed interface Displayable : ReminderLoadState {
        data object Loading : Displayable

        @JvmInline
        value class Success(val reminder: Reminder) : Displayable
    }

    data object Absent : ReminderLoadState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderScreen(
    reminderLoadState: ReminderLoadState.Displayable,
    onNavigateBack: () -> Unit,
    onDeleteReminder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.reminder))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onDeleteReminder,
                        enabled = reminderLoadState is ReminderLoadState.Displayable.Success,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (reminderLoadState) {
            ReminderLoadState.Displayable.Loading -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ReminderLoadState.Displayable.Success -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = reminderLoadState.reminder.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                    )
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val context = LocalContext.current
                        val datetime = reminderLoadState.reminder.datetime
                        Text(
                            text = DateFormat.getDateFormat(context).format(datetime),
                            style = MaterialTheme.typography.displaySmall,
                        )
                        VerticalDivider()
                        Text(
                            text = DateFormat.getTimeFormat(context).format(datetime),
                            style = MaterialTheme.typography.displaySmall,
                        )
                    }
                    HorizontalDivider()
                    Text(
                        text = reminderLoadState.reminder.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ReminderScreenPreview(
    @PreviewParameter(ReminderScreenPreviewProvider::class) reminderLoadState: ReminderLoadState.Displayable,
) {
    Lab6Theme {
        ReminderScreen(
            reminderLoadState,
            onNavigateBack = {},
            onDeleteReminder = {},
        )
    }
}

private class ReminderScreenPreviewProvider :
    PreviewParameterProvider<ReminderLoadState.Displayable> {
    override val values = sequenceOf(
        ReminderLoadState.Displayable.Loading,
        ReminderLoadState.Displayable.Success(
            Reminder(
                id = 1,
                title = "Title",
                text = "Text",
                datetime = Date(),
            )
        ),
    )
    override val count: Int = 2
}
