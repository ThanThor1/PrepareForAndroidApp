package com.example.testdatabase.presentation.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.testdatabase.domain.model.User
import com.example.testdatabase.presentation.utils.copyUriToCache
import com.example.testdatabase.presentation.utils.rememberImagePicker
import com.example.testdatabase.presentation.viewmodel.UserViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    viewModel: UserViewModel,
    userId: String?,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = userId != null
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Nam") }
    var birthYear by remember { mutableStateOf("2000") }
    var bio by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var expandedGenderMenu by remember { mutableStateOf(false) }
    var oldImageUrl by remember { mutableStateOf("") }

    val genderOptions = listOf("Nam", "Nữ", "Khác")

    // Load user data if editing
    LaunchedEffect(userId) {
        if (userId != null) {
            val user = uiState.users.find { it.id == userId }
            user?.let {
                name = it.name
                gender = it.gender
                birthYear = it.birthYear.toString()
                bio = it.bio
                imageUrl = it.imageUrl
                oldImageUrl = it.imageUrl
            }
        }
    }

    // Image picker
    val imagePicker = rememberImagePicker { uri ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Chỉnh sửa người dùng" else "Thêm người dùng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Section
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                // Display image
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Preview",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Avatar placeholder",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Camera button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset((-10).dp, (-10).dp)
                ) {
                    FloatingActionButton(
                        onClick = imagePicker,
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Chọn ảnh",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (uiState.isUploadingImage) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text(
                    "Đang tải ảnh lên...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Gender Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedGenderMenu,
                onExpandedChange = { expandedGenderMenu = it }
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Giới tính *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expandedGenderMenu
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedGenderMenu,
                    onDismissRequest = { expandedGenderMenu = false }
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                gender = option
                                expandedGenderMenu = false
                            }
                        )
                    }
                }
            }

            // Birth Year Field
            OutlinedTextField(
                value = birthYear,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        birthYear = it
                    }
                },
                label = { Text("Năm sinh *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Bio Field
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Tiểu sử") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    val finalGender = gender.ifBlank { "Nam" }
                    val year = birthYear.toIntOrNull() ?: 2000
                    val finalBio = bio.ifBlank { "" }

                    // Nếu người dùng chọn ảnh mới, upload trước
                    if (selectedImageUri != null) {
                        val file = context.copyUriToCache(selectedImageUri!!)
                        if (file != null) {
                            val tempUserId = userId ?: UUID.randomUUID().toString()
                            viewModel.uploadImage(file, tempUserId) { uploadedUrl ->
                                // Sau khi upload xong, lưu user với URL mới
                                val userToSave = if (isEditMode && userId != null) {
                                    User(
                                        id = userId,
                                        name = name.trim(),
                                        gender = finalGender,
                                        birthYear = year,
                                        bio = finalBio,
                                        imageUrl = uploadedUrl
                                    )
                                } else {
                                    User(
                                        name = name.trim(),
                                        gender = finalGender,
                                        birthYear = year,
                                        bio = finalBio,
                                        imageUrl = uploadedUrl
                                    )
                                }

                                if (isEditMode && userId != null) {
                                    viewModel.updateUser(userToSave, oldImageUrl)
                                } else {
                                    viewModel.addUser(userToSave)
                                }
                                onNavigateBack()
                            }
                        }
                    } else {
                        // Không có ảnh mới, lưu với URL cũ
                        val userToSave = if (isEditMode && userId != null) {
                            User(
                                id = userId,
                                name = name.trim(),
                                gender = finalGender,
                                birthYear = year,
                                bio = finalBio,
                                imageUrl = imageUrl
                            )
                        } else {
                            User(
                                name = name.trim(),
                                gender = finalGender,
                                birthYear = year,
                                bio = finalBio,
                                imageUrl = ""
                            )
                        }

                        if (isEditMode && userId != null) {
                            viewModel.updateUser(userToSave, oldImageUrl)
                        } else {
                            viewModel.addUser(userToSave)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && birthYear.isNotBlank() &&
                        !uiState.isLoading && !uiState.isUploadingImage
            ) {
                if (uiState.isLoading || uiState.isUploadingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditMode) "Cập nhật" else "Thêm")
                }
            }

            // Error Message
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}