package com.example.testdatabase.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testdatabase.domain.model.User
import com.example.testdatabase.domain.repository.ImageUploadRepository
import com.example.testdatabase.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class UserUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedGender: String = "Tất cả",
    val isUploadingImage: Boolean = false
)

class UserViewModel(
    private val repository: UserRepository,
    private val imageRepository: ImageUploadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getUsers().fold(
                onSuccess = { users ->
                    _uiState.value = _uiState.value.copy(
                        users = users,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun uploadImage(file: File, userId: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingImage = true, error = null)

            imageRepository.uploadImage(file, userId).fold(
                onSuccess = { imageUrl ->
                    _uiState.value = _uiState.value.copy(isUploadingImage = false)
                    onSuccess(imageUrl)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        error = "Lỗi upload ảnh: ${error.message}"
                    )
                }
            )
        }
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.insertUser(user).fold(
                onSuccess = {
                    loadUsers()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun updateUser(user: User, oldImageUrl: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.updateUser(user).fold(
                onSuccess = {
                    // Xóa ảnh cũ nếu có và khác với ảnh mới
                    if (!oldImageUrl.isNullOrEmpty() &&
                        oldImageUrl != user.imageUrl &&
                        oldImageUrl.contains("avatars")) {
                        imageRepository.deleteImage(oldImageUrl)
                    }
                    loadUsers()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Lấy user để xóa ảnh trước
            val user = _uiState.value.users.find { it.id == id }

            repository.deleteUser(id).fold(
                onSuccess = {
                    // Xóa ảnh nếu có
                    user?.let {
                        if (it.imageUrl.isNotEmpty() && it.imageUrl.contains("avatars")) {
                            imageRepository.deleteImage(it.imageUrl)
                        }
                    }
                    loadUsers()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun searchUsers(query: String, gender: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                searchQuery = query,
                selectedGender = gender
            )

            repository.searchUsers(query, gender).fold(
                onSuccess = { users ->
                    _uiState.value = _uiState.value.copy(
                        users = users,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        searchUsers(query, _uiState.value.selectedGender)
    }

    fun updateSelectedGender(gender: String) {
        searchUsers(_uiState.value.searchQuery, gender)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}