package com.kelompok4.lokalmart.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kelompok4.lokalmart.feature.chat.data.ChatMessage
import com.kelompok4.lokalmart.feature.chat.viewmodel.ChatViewModel

private val GreenPrimary   = Color(0xFF2D9B4F)
private val GreenLight     = Color(0xFFE8F5ED)
private val TextPrimary    = Color(0xFF1E293B)
private val TextSecondary  = Color(0xFF64748B)
private val CardBg         = Color(0xFFFFFFFF)
private val BorderColor    = Color(0xFFE2E8F0)
private val GrayBg         = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    sellerId: String,
    sellerName: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val activeConversation by viewModel.activeConversation.collectAsState()
    var textMessage by remember { mutableStateOf("") }
    
    val listState = rememberLazyListState()

    LaunchedEffect(sellerId) {
        viewModel.selectConversation(sellerId, sellerName)
    }

    // Auto scroll to bottom when a new message arrives
    LaunchedEffect(activeConversation?.messages?.size) {
        activeConversation?.messages?.size?.let { size ->
            if (size > 0) {
                listState.animateScrollToItem(size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = sellerName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Online",
                                fontSize = 11.sp,
                                color = Color(0xFF22C55E),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.background(Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textMessage,
                        onValueChange = { textMessage = it },
                        placeholder = { Text("Tulis pesan...", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 100.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        maxLines = 4
                    )
                    
                    Spacer(Modifier.width(10.dp))
                    
                    IconButton(
                        onClick = {
                            if (textMessage.isNotBlank()) {
                                viewModel.sendMessage(sellerId, textMessage)
                                textMessage = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        containerColor = GrayBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val messages = activeConversation?.messages ?: emptyList()

            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "💬",
                        fontSize = 40.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Mulai obrolan baru dengan $sellerName",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(message = message)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.senderId == "buyer"
    val bubbleColor = if (isUser) GreenPrimary else Color.White
    val textColor = if (isUser) Color.White else TextPrimary
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(bubbleShape)
                    .background(bubbleColor)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = textColor,
                    lineHeight = 18.sp
                )
            }
            
            Spacer(Modifier.height(2.dp))
            
            Text(
                text = message.timestamp,
                fontSize = 9.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
