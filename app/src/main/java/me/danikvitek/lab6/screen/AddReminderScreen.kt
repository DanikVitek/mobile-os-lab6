package me.danikvitek.lab6.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import me.danikvitek.lab6.R
import me.danikvitek.lab6.ui.theme.Lab6Theme
import me.danikvitek.lab6.util.serializer.calendarUTC
import me.danikvitek.lab6.util.serializer.hourOfDay
import me.danikvitek.lab6.util.serializer.minute
import me.danikvitek.lab6.util.serializer.year
import me.danikvitek.lab6.viewmodel.AddReminderViewModel
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Date

@Composable
fun AddReminderScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReminder: (id: Long) -> Unit,
    viewModel: AddReminderViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    var coroutineScope = rememberCoroutineScope()
    AddReminderScreen(
        modifier = modifier,
        callbacks = Callbacks(
            onNavigateBack = onNavigateBack,
            onAddReminder = { title, description, datetime ->
                val id = viewModel.addReminder(title, description, datetime)
                coroutineScope.launch {
                    val id = id.single()
                    onNavigateToReminder(id)
                }
            },
        ),
    )
}

private class Callbacks(
    val onNavigateBack: () -> Unit = {},
    val onAddReminder: (title: String, description: String, datetime: Date) -> Unit = { _, _, _ -> },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderScreen(
    callbacks: Callbacks = Callbacks(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendarUTC().timeInMillis,
        yearRange = calendarUTC().year..DatePickerDefaults.YearRange.endInclusive,
        selectableDates = FutureDates,
    )
    val timePickerState = Calendar.getInstance().let {
        rememberTimePickerState(
            initialHour = it.hourOfDay.also { Log.d("AddReminderScreen", "hour: $it") },
            initialMinute = it.minute,
        )
    }
    val datetime by remember(datePickerState, timePickerState) {
        derivedStateOf {
            datePickerState.selectedDateMillis?.let {
                val localDate = Instant.ofEpochMilli(it)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()
                val localDateTimeAtStartOfDay = localDate.atStartOfDay()
                val dateInstant = localDateTimeAtStartOfDay.toInstant(ZoneOffset.UTC)
                val dateMillis = localDateTimeAtStartOfDay.toEpochSecond(ZoneOffset.UTC) * 1000

                val zoneOffset = ZoneId.systemDefault().rules.getOffset(dateInstant)
                val timeMillis =
                    (timePickerState.hour * 3600 + timePickerState.minute * 60 - zoneOffset.totalSeconds) * 1000

                Date(dateMillis + timeMillis).also { Log.d("AddReminderScreen", "datetime: $it") }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.add_reminder))
                },
                navigationIcon = {
                    IconButton(onClick = callbacks.onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        enabled = title.isNotBlank() && datetime?.let {
                            Instant.now().isBefore(it.toInstant())
                        } == true,
                        onClick = {
                            callbacks.onAddReminder(title, description, datetime!!)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = stringResource(R.string.done),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = { title = it },
                label = { Text(text = stringResource(R.string.title)) },
                singleLine = true,
            )

            var showDateTimePicker by remember { mutableStateOf(ShowDateTimePicker.No) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val dateString by remember(datePickerState) {
                    derivedStateOf {
                        datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate().format(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                                    .withLocale(context.resources.configuration.locales[0])
                            )
                        }
                    }
                }
                TextButton(
                    onClick = { showDateTimePicker = ShowDateTimePicker.Date },
                ) {
                    Text(
                        style = MaterialTheme.typography.displaySmall,
                        text = dateString ?: stringResource(R.string.set_date),
                    )
                }

                VerticalDivider()

                val timeString by remember(
                    datePickerState.selectedDateMillis == null,
                    timePickerState
                ) {
                    derivedStateOf {
                        datePickerState.selectedDateMillis?.let {
                            LocalTime.of(timePickerState.hour, timePickerState.minute).format(
                                DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                                    .withLocale(context.resources.configuration.locales[0])
                            )
                        }
                    }
                }
                TextButton(
                    onClick = { showDateTimePicker = ShowDateTimePicker.Time },
                ) {
                    Text(
                        style = MaterialTheme.typography.displaySmall,
                        text = timeString ?: stringResource(R.string.set_time),
                    )
                }
            }

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(text = stringResource(R.string.description)) },
                modifier = Modifier.fillMaxSize(),
                singleLine = false,
                colors = textFieldColors(),
            )

            when (showDateTimePicker) {
                ShowDateTimePicker.No -> {}

                ShowDateTimePicker.Date -> {
                    DatePickerDialog(
                        onDismissRequest = { showDateTimePicker = ShowDateTimePicker.No },
                        confirmButton = {
                            TextButton(onClick = { showDateTimePicker = ShowDateTimePicker.No }) {
                                Text(text = stringResource(R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDateTimePicker = ShowDateTimePicker.No }) {
                                Text(text = stringResource(R.string.cancel))
                            }
                        },
                    ) {
                        DatePicker(
                            state = datePickerState,
                        )
                    }
                }

                ShowDateTimePicker.Time -> {
                    TimePickerDialog(
                        onDismiss = { showDateTimePicker = ShowDateTimePicker.No },
                        onConfirm = { showDateTimePicker = ShowDateTimePicker.No },
                    ) {
                        TimePicker(
                            state = timePickerState,
                        )
                    }
                }
            }
        }
    }
}

private enum class ShowDateTimePicker {
    No,
    Date,
    Time,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String = "Select Time",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier =
            Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private object FutureDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        Instant.ofEpochMilli(utcTimeMillis)
            .atZone(ZoneId.of("UTC"))
            .isAfter(ZonedDateTime.now())

    override fun isSelectableYear(year: Int): Boolean =
        Instant.now().atZone(ZoneId.of("UTC")).year <= year
}

@Preview
@Composable
private fun AddReminderScreenPreview() {
    Lab6Theme {
        AddReminderScreen(
            callbacks = Callbacks(),
        )
    }
}