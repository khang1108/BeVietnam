package com.bevietnam.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bevietnam.R
import com.bevietnam.core.model.Gender
import com.bevietnam.ui.components.BeVietnamTextField
import com.bevietnam.ui.components.DatePickerField
import com.bevietnam.ui.components.GenderSelector
import com.bevietnam.ui.components.PrimaryLoadingButton
import java.time.LocalDate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.bevietnam.ui.theme.BeVietnamTheme

/**
 * Màn hình Xác thực tài khoản (Auth Screen) của ứng dụng BeVietnam.
 *
 * Tích hợp cả hai luồng nghiệp vụ Đăng nhập và Đăng ký trên một giao diện thống nhất qua thanh trượt Tab Switcher.
 * Kết nối dữ liệu dạng luồng quan sát từ [AuthViewModel] và gom nhóm các LaunchedEffect để xử lý các sự kiện một lần mượt mà.
 *
 * @param onNavigateToProfile Callback điều hướng người dùng sang màn hình Hồ sơ cá nhân sau khi xác thực thành công, nhận tham số là ID người dùng.
 * @param viewModel ViewModel quản lý nghiệp vụ đăng nhập/đăng ký ([AuthViewModel]). Mặc định là [hiltViewModel].
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 */
@Composable
fun AuthScreen(
    onNavigateToProfile: (Int) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Lắng nghe và điều phối các sự kiện một lần (One-off UI Events)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.NavigateToProfile -> onNavigateToProfile(event.userId)
                is AuthUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(32.dp))
            // Logo thương hiệu
            Image(
                painter = painterResource(id = R.drawable.smarttravellogo),
                contentDescription = "Logo BeVietnam",
                modifier = Modifier.size(100.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.auth_brand_name),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.auth_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            // Thẻ chứa form nhập liệu nổi bật (Surface Card)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // Bộ chuyển đổi Tab Đăng nhập / Đăng ký
                    AuthTabSwitcher(
                        selectedTab = uiState.selectedTab,
                        onTabChange = viewModel::selectTab
                    )

                    Spacer(Modifier.height(24.dp))

                    if (uiState.selectedTab == AuthTab.LOGIN) {
                        LoginForm(
                            email = uiState.email,
                            password = uiState.password,
                            errorMessage = uiState.errorMessage,
                            isLoading = uiState.isLoading,
                            onEmailChange = viewModel::onEmailChange,
                            onPasswordChange = viewModel::onPasswordChange,
                            onLoginClick = viewModel::login,
                            onForgotPasswordClick = viewModel::onForgotPassword,
                            onSwitchToRegister = { viewModel.selectTab(AuthTab.REGISTER) }
                        )
                    } else {
                        RegisterForm(
                            name = uiState.name,
                            gender = uiState.gender,
                            dateOfBirthDisplay = uiState.dateOfBirthDisplay,
                            registerEmail = uiState.registerEmail,
                            registerPassword = uiState.registerPassword,
                            errorMessage = uiState.errorMessage,
                            isLoading = uiState.isLoading,
                            onNameChange = viewModel::onNameChange,
                            onGenderChange = viewModel::onGenderChange,
                            onDateOfBirthChange = viewModel::onDateOfBirthChange,
                            onRegisterEmailChange = viewModel::onRegisterEmailChange,
                            onRegisterPasswordChange = viewModel::onRegisterPasswordChange,
                            onRegisterClick = viewModel::register,
                            onSwitchToLogin = { viewModel.selectTab(AuthTab.LOGIN) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

/**
 * Thanh chuyển đổi Tab chọn Đăng nhập hoặc Đăng ký (Auth Tab Switcher).
 *
 * Thiết kế mượt mà dạng con nhộng (pill shape), hỗ trợ hiệu ứng động chuyển sắc.
 */
@Composable
private fun AuthTabSwitcher(
    selectedTab: AuthTab,
    onTabChange: (AuthTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(secondaryContainer)
            .padding(4.dp)
    ) {
        for (tab in AuthTab.entries) {
            val isSelected = tab == selectedTab
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) primary else Color.Transparent,
                label = "tab_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) onPrimary else onSurfaceVariant,
                label = "tab_text"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50.dp))
                    .background(bgColor)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onTabChange(tab) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tab == AuthTab.LOGIN) stringResource(R.string.tab_login)
                    else stringResource(R.string.tab_register),
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Form điền thông tin đăng nhập tài khoản (Login Form).
 */
@Composable
private fun LoginForm(
    email: String,
    password: String,
    errorMessage: String?,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSwitchToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        BeVietnamTextField(
            label = stringResource(R.string.email),
            value = email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.hint_email),
            leadingIcon = Icons.Default.Email
        )

        Spacer(Modifier.height(14.dp))

        BeVietnamTextField(
            label = stringResource(R.string.password),
            value = password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.hint_password),
            leadingIcon = Icons.Default.Lock,
            visualTransformation = PasswordVisualTransformation()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onForgotPasswordClick) {
                Text(
                    text = stringResource(R.string.forgot_password),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        PrimaryLoadingButton(
            text = stringResource(R.string.login),
            isLoading = isLoading,
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        OrDivider()
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSwitchToRegister,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.create_account),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Form điền thông tin đăng ký tài khoản mới (Register Form).
 */
@Composable
private fun RegisterForm(
    name: String,
    gender: Gender?,
    dateOfBirthDisplay: String,
    registerEmail: String,
    registerPassword: String,
    errorMessage: String?,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onDateOfBirthChange: (LocalDate) -> Unit,
    onRegisterEmailChange: (String) -> Unit,
    onRegisterPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onSwitchToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        BeVietnamTextField(
            label = stringResource(R.string.full_name),
            value = name,
            onValueChange = onNameChange,
            placeholder = stringResource(R.string.hint_full_name),
            leadingIcon = Icons.Default.Person
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.gender),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        GenderSelector(selected = gender, onSelect = onGenderChange)

        Spacer(Modifier.height(14.dp))

        DatePickerField(
            label = stringResource(R.string.date_of_birth),
            displayValue = dateOfBirthDisplay,
            onDateSelected = onDateOfBirthChange
        )

        Spacer(Modifier.height(14.dp))

        BeVietnamTextField(
            label = stringResource(R.string.email),
            value = registerEmail,
            onValueChange = onRegisterEmailChange,
            placeholder = stringResource(R.string.hint_email_register),
            leadingIcon = Icons.Default.Email
        )

        Spacer(Modifier.height(14.dp))

        BeVietnamTextField(
            label = stringResource(R.string.password),
            value = registerPassword,
            onValueChange = onRegisterPasswordChange,
            placeholder = stringResource(R.string.hint_password_register),
            leadingIcon = Icons.Default.Lock,
            visualTransformation = PasswordVisualTransformation()
        )

        errorMessage?.let { message ->
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(Modifier.height(20.dp))

        PrimaryLoadingButton(
            text = stringResource(R.string.register),
            isLoading = isLoading,
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        OrDivider()
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSwitchToLogin,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.already_have_account),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Đường chia ngang "Hoặc" (Or Divider) trang nhã.
 */
@Composable
private fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = "  ${stringResource(R.string.or_divider)}  ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// region Previews

@Preview(showBackground = true, name = "Login Tab - Light")
@Composable
private fun LoginFormPreview() {
    BeVietnamTheme {
        Column(Modifier.padding(24.dp)) {
            LoginForm(
                email = "",
                password = "",
                errorMessage = null,
                isLoading = false,
                onEmailChange = {},
                onPasswordChange = {},
                onLoginClick = {},
                onForgotPasswordClick = {},
                onSwitchToRegister = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Register Tab - Light")
@Composable
private fun RegisterFormPreview() {
    BeVietnamTheme {
        Column(Modifier.padding(24.dp)) {
            RegisterForm(
                name = "",
                gender = null,
                dateOfBirthDisplay = "",
                registerEmail = "",
                registerPassword = "",
                errorMessage = null,
                isLoading = false,
                onNameChange = {},
                onGenderChange = {},
                onDateOfBirthChange = {},
                onRegisterEmailChange = {},
                onRegisterPasswordChange = {},
                onRegisterClick = {},
                onSwitchToLogin = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Login Tab - With Error")
@Composable
private fun LoginFormErrorPreview() {
    BeVietnamTheme {
        Column(Modifier.padding(24.dp)) {
            LoginForm(
                email = "user@example.com",
                password = "123",
                errorMessage = "Email hoặc mật khẩu không đúng",
                isLoading = false,
                onEmailChange = {},
                onPasswordChange = {},
                onLoginClick = {},
                onForgotPasswordClick = {},
                onSwitchToRegister = {}
            )
        }
    }
}

// endregion
