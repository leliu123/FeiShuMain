package com.feishu.mainfeature.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.WavingHand
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.feishu.tabinterface.TabDescriptor
import com.feishu.tabinterface.TabRegister

/**
 * "我的" Tab 的占位符实现。
 *
 * 这是一个最简单的 Tab 示例，它不包含任何复杂的业务逻辑，
 * 仅用于在主导航栏中占据一个位置，并显示一个简单的文本界面。
 * 这种方式非常适合在应用开发的早期阶段，快速搭建起整体的UI框架。
 */
class ProfileTab : TabRegister {
    override val descriptor: TabDescriptor = TabDescriptor(
        id = "profile",
        title = "我的",
        icon = Icons.Outlined.Person,
        route = "tab/profile"
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        // No top bar for this placeholder tab
    }

    @Composable
    override fun Content(navController: NavHostController) {
        var profile by remember {
            mutableStateOf(
                ProfileInfo(
                    name = "张三",
                    department = "研发部",
                    title = "Android 开发"
                )
            )
        }
        var status by remember { mutableStateOf(UserStatus.Online) }
        var showEditDialog by remember { mutableStateOf(false) }
        var showStatusDialog by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- 用户信息展示区 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "用户头像",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${profile.department} | ${profile.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = status)
            }

            HorizontalDivider() // 分隔线

            // --- 功能选项列表 ---
            LazyColumn {
                item {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Edit,
                        text = "编辑个人资料",
                        onClick = { showEditDialog = true }
                    )
                }
                item {
                    ProfileMenuItem(
                        icon = Icons.Outlined.WavingHand,
                        text = "修改状态",
                        onClick = { showStatusDialog = true }
                    )
                }
            }
        }

        if (showEditDialog) {
            EditProfileDialog(
                profile = profile,
                onDismiss = { showEditDialog = false },
                onSave = { updatedProfile ->
                    profile = updatedProfile
                    showEditDialog = false
                }
            )
        }

        if (showStatusDialog) {
            StatusSelectionDialog(
                currentStatus = status,
                onStatusSelected = { selected ->
                    status = selected
                    showStatusDialog = false
                },
                onDismiss = { showStatusDialog = false }
            )
        }
    }
}

/**
 * "我的" 页面中的一个可点击的功能菜单项
 * @param icon 菜单项的图标
 * @param text 菜单项的文本
 * @param onClick 点击事件
 */
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "进入",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 简单的用户信息模型
private data class ProfileInfo(
    val name: String,
    val department: String,
    val title: String
)

// 可选的在线状态
private enum class UserStatus(val label: String, val description: String, val color: Color) {
    Online("在线", "同事可以立即联系你", Color(0xFF4CAF50)),
    Busy("忙碌", "集中处理事务，稍后回复", Color(0xFFFF9800)),
    DoNotDisturb("请勿打扰", "静音提醒，紧急电话除外", Color(0xFFF44336)),
    Away("离开", "临时不在工位", Color(0xFF9E9E9E))
}

@Composable
private fun StatusBadge(status: UserStatus) {
    Surface(
        color = status.color.copy(alpha = 0.12f),
        contentColor = status.color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EditProfileDialog(
    profile: ProfileInfo,
    onDismiss: () -> Unit,
    onSave: (ProfileInfo) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var department by remember { mutableStateOf(profile.department) }
    var title by remember { mutableStateOf(profile.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "编辑个人资料") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("部门") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("职位") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        ProfileInfo(
                            name = name.ifBlank { profile.name },
                            department = department.ifBlank { profile.department },
                            title = title.ifBlank { profile.title }
                        )
                    )
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun StatusSelectionDialog(
    currentStatus: UserStatus,
    onStatusSelected: (UserStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "修改状态") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                UserStatus.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStatusSelected(status) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (status == currentStatus) {
                                Icons.Filled.CheckCircle
                            } else {
                                Icons.Outlined.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = status.color
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = status.label,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = status.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}
