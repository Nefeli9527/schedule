package top.nefeli.schedule.view.screens

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import top.nefeli.schedule.R
import top.nefeli.schedule.util.ImportResult
import top.nefeli.schedule.util.ScheduleImporter
import top.nefeli.schedule.viewmodel.ScheduleViewModel
import top.nefeli.schedule.viewmodel.ScheduleViewModelFactory
import top.nefeli.schedule.viewmodel.SettingsViewModel
import top.nefeli.schedule.viewmodel.SettingsViewModelFactory
import java.io.File
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScheduleScreen(
    scheduleViewModelFactory: ScheduleViewModelFactory,
    settingsViewModelFactory: SettingsViewModelFactory,
    onBack: () -> Unit,
    onImportComplete: () -> Unit
) {
    val scheduleViewModel: ScheduleViewModel = viewModel(factory = scheduleViewModelFactory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isImporting by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf("") }
    val importingText = stringResource(R.string.importing)
    val importSuccessText = stringResource(R.string.import_success)
    val importFailedText = stringResource(R.string.import_failed, "")
    val importInstructionsText = stringResource(R.string.import_instructions)
    val cancelText = stringResource(R.string.cancel)
    val chooseFileText = stringResource(R.string.choose_file)
    val menuImportText = stringResource(R.string.menu_import)
    val importScheduleTitleText = stringResource(R.string.import_schedule_title)
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            isImporting = true
            importMessage = importingText
            
            scope.launch {
                try {
                    // 将文件复制到应用的缓存目录
                    val cacheFile = File(context.cacheDir, "import_schedule.wakeup_schedule")
                    context.contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                        FileOutputStream(cacheFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    
                    // 导入课程表
                    val result = ScheduleImporter.importScheduleFromFile(cacheFile)
                    
                    when (result) {
                        is ImportResult.Success -> {
                            // 更新设置
                            settingsViewModel.updateSettings(result.settings)
                            
                            // 清空现有课程表数据
                            // 这里我们简单地重新添加所有导入的课程
                            result.scheduleData.forEach { course ->
                                // TODO: 需要更新以适配新的数据结构
                                // scheduleViewModel.addCourse(period, day, course)
                            }
                            
                            importMessage = importSuccessText
                            scope.launch {
                                snackbarHostState.showSnackbar(importSuccessText)
                            }
                            
                            // 延迟一段时间后返回主界面
                            scope.launch {
                                kotlinx.coroutines.delay(1500)
                                onImportComplete()
                            }
                        }
                        is ImportResult.Error -> {
                            val errorMessage = importFailedText.replace("%s", result.message)
                            importMessage = errorMessage
                            scope.launch {
                                snackbarHostState.showSnackbar(errorMessage)
                            }
                        }
                    }
                } catch (e: Exception) {
                    val errorMessage = importFailedText.replace("%s", e.message ?: "")
                    importMessage = errorMessage
                    scope.launch {
                        snackbarHostState.showSnackbar(errorMessage)
                    }
                } finally {
                    isImporting = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = importScheduleTitleText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = cancelText,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = importMessage,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = menuImportText,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = importScheduleTitleText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Support .wakeup_schedule file format",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        filePickerLauncher.launch("*/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(chooseFileText)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = importInstructionsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}