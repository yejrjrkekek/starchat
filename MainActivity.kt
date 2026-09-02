package com.starchat.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// ============================================================================
// 1. النشاط الرئيسي وتحديد اللغة العربية كافتراضية
// ============================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // إجبار اللغة العربية للتطبيق
        val locale = Locale("ar")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        setContent {
            StarChatTheme {
                StarChatMainScreen()
            }
        }
    }
}

// ============================================================================
// 2. الهوية والتصميم والتأثيرات البصرية (StarChat Theme)
// ============================================================================
@Composable
fun StarChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF0088CC),
            secondary = Color(0xFFFFD700),
            background = Color(0xFF17212B),
            surface = Color(0xFF242F3D),
            onPrimary = Color.White
        ),
        content = content
    )
}

// ============================================================================
// 3. نماذج البيانات (Data Models)
// ============================================================================
data class ChatItemData(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val isVerified: Boolean = false
)

// ============================================================================
// 4. الشاشة الرئيسية للتطبيق وهيكل التنقل
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarChatMainScreen() {
    // حالة المالك (تمنح صلاحية ⭐ ∞ وحظر المستخدمين)
    var isOwnerAccount by remember { mutableStateOf(true) }
    var showAdminPanel by remember { mutableStateOf(false) }
    var currentTab by remember { mutableIntStateOf(0) }

    // عرض النجوم للمالك أو المستخدم العادي
    val displayStars = if (isOwnerAccount) "⭐ ∞" else "⭐ 150"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (showAdminPanel) "لوحة تحكم المالك 🛡️" else "ستار شات (StarChat)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    // عرض رصيد النجوم
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(Color(0xFF2B5278), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = displayStars,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // زر اختبار تبديل الحساب بين المالك والمستخدم
                    IconButton(onClick = { 
                        isOwnerAccount = !isOwnerAccount 
                        showAdminPanel = false
                    }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "تبديل وضع الحساب",
                            tint = if (isOwnerAccount) Color(0xFFFFD700) else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF242F3D))
            )
        },
        bottomBar = {
            if (!showAdminPanel) {
                NavigationBar(containerColor = Color(0xFF242F3D)) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(Icons.Default.Email, contentDescription = "المحادثات") },
                        label = { Text("المحادثات") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Default.Star, contentDescription = "النجوم") },
                        label = { Text("النجوم") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "الإعدادات") },
                        label = { Text("الإعدادات") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0E1621))
        ) {
            if (showAdminPanel && isOwnerAccount) {
                OwnerDashboardView(onBack = { showAdminPanel = false })
            } else {
                when (currentTab) {
                    0 -> ChatsListView(
                        isOwner = isOwnerAccount, 
                        onOpenDashboard = { showAdminPanel = true }
                    )
                    1 -> StarsWalletView(isOwner = isOwnerAccount)
                    2 -> SettingsView(isOwner = isOwnerAccount)
                }
            }
        }
    }
}

// ============================================================================
// 5. قائمة المحادثات الرئيسية
// ============================================================================
@Composable
fun ChatsListView(isOwner: Boolean, onOpenDashboard: () -> Unit) {
    val chats = remember {
        listOf(
            ChatItemData("1", "الرسائل المحفوظة", "مرحباً بك في تطبيق StarChat!", "12:00 م"),
            ChatItemData("2", "قناة التحديثات الرسمية", "تم تفعيل نظام النجوم⭐", "10:30 ص", isVerified = true),
            ChatItemData("3", "مجموعة النقاش العامة", "علي: متى يبدأ البث المباشر؟", "أمس", unreadCount = 5)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // زر خاص يظهر فقط للمالك للدخول للوحة التحكم
        if (isOwner) {
            Button(
                onClick = onOpenDashboard,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("فتح لوحة تحكم المالك (Owner Dashboard)", color = Color.White)
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(chats) { chat ->
                ChatItemRow(chat)
                Divider(color = Color(0xFF17212B), thickness = 1.dp)
            }
        }
    }
}

@Composable
fun ChatItemRow(chat: ChatItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFF0088CC)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.name.take(1),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (chat.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "موثق",
                        tint = Color(0xFF0088CC),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.lastMessage,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = chat.time, color = Color.Gray, fontSize = 12.sp)
            if (chat.unreadCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF0088CC))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = chat.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ============================================================================
// 6. لوحة تحكم المالك الحصرية (Owner Dashboard)
// ============================================================================
@Composable
fun OwnerDashboardView(onBack: () -> Unit) {
    var searchUser by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
            }
            Text(
                text = "إدارة المستخدمين والحظر السيرفري",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchUser,
            onValueChange = { searchUser = it },
            label = { Text("اسم المستخدم أو المعرف (ID)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0088CC),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF0088CC),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("سبب الحظر") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0088CC),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF0088CC),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (searchUser.isNotEmpty()) {
                        Toast.makeText(context, "تم حظر $searchUser من السيرفر بنجاح", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier.weight(1f)
            ) {
                Text("حظر دائم 🚫")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    if (searchUser.isNotEmpty()) {
                        Toast.makeText(context, "تم إلغاء حظر $searchUser", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.weight(1f)
            ) {
                Text("إلغاء الحظر ✅")
            }
        }
    }
}

// ============================================================================
// 7. شاشة النجوم (Stars System)
// ============================================================================
@Composable
fun StarsWalletView(isOwner: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("رصيدك الحالي من النجوم", color = Color.Gray, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isOwner) "⭐ ∞" else "⭐ 150",
            color = Color(0xFFFFD700),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isOwner) "حساب المالك (نجوم غير محدودة)" else "حساب مستخدم عادي",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

// ============================================================================
// 8. شاشة الإعدادات
// ============================================================================
@Composable
fun SettingsView(isOwner: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("الإعدادات العامة", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("نوع الحساب: ${if (isOwner) "مالك التطبيق (Owner)" else "مستخدم عادي"}", color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Text("اللغة الافتراضية: العربية", color = Color.Gray)
    }
}
