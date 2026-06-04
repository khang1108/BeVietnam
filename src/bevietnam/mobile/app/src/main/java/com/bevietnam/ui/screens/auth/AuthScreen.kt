package com.bevietnam.ui.screens.auth

import android.app.DatePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import java.util.Calendar

@Composable
fun AuthScreen(
    onNavigateToProfile: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.NavigateToProfile -> onNavigateToProfile(event.userId)
                is AuthUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))

            Text(
                text = "bevietnam",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    AuthTabSwitcher(
                        selectedTab = uiState.selectedTab,
                        onTabChange = viewModel::selectTab
                    )

                    Spacer(Modifier.height(24.dp))

                    if (uiState.selectedTab == AuthTab.LOGIN) {
                        LoginForm(
                            uiState = uiState,
                            onEmailChange = viewModel::onEmailChange,
                            onPasswordChange = viewModel::onPasswordChange,
                            onLoginClick = viewModel::login,
                            onForgotPasswordClick = viewModel::onForgotPassword,
                            onSwitchToRegister = { viewModel.selectTab(AuthTab.REGISTER) }
                        )
                    } else {
                        RegisterForm(
                            uiState = uiState,
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

@Composable
private fun AuthTabSwitcher(
    selectedTab: AuthTab,
    onTabChange: (AuthTab) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
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
                    .clickable { onTabChange(tab) },
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

@Composable
private fun LoginForm(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSwitchToRegister: () -> Unit
) {
    BeVietnamTextField(
        label = stringResource(R.string.email),
        value = uiState.email,
        onValueChange = onEmailChange,
        placeholder = stringResource(R.string.hint_email),
        leadingIcon = Icons.Default.Email
    )

    Spacer(Modifier.height(14.dp))

    BeVietnamTextField(
        label = stringResource(R.string.password),
        value = uiState.password,
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

    uiState.errorMessage?.let { message ->
        if (uiState.selectedTab == AuthTab.LOGIN) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }

    PrimaryLoadingButton(
        text = stringResource(R.string.login),
        isLoading = uiState.isLoading,
        onClick = onLoginClick
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

@Composable
private fun RegisterForm(
    uiState: AuthUiState,
    onNameChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onDateOfBirthChange: (LocalDate) -> Unit,
    onRegisterEmailChange: (String) -> Unit,
    onRegisterPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onSwitchToLogin: () -> Unit
) {
    BeVietnamTextField(
        label = stringResource(R.string.full_name),
        value = uiState.name,
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
    GenderSelector(selected = uiState.gender, onSelect = onGenderChange)

    Spacer(Modifier.height(14.dp))

    DatePickerField(
        label = stringResource(R.string.date_of_birth),
        displayValue = uiState.dateOfBirthDisplay,
        onDateSelected = onDateOfBirthChange
    )

    Spacer(Modifier.height(14.dp))

    BeVietnamTextField(
        label = stringResource(R.string.email),
        value = uiState.registerEmail,
        onValueChange = onRegisterEmailChange,
        placeholder = stringResource(R.string.hint_email_register),
        leadingIcon = Icons.Default.Email
    )

    Spacer(Modifier.height(14.dp))

    BeVietnamTextField(
        label = stringResource(R.string.password),
        value = uiState.registerPassword,
        onValueChange = onRegisterPasswordChange,
        placeholder = stringResource(R.string.hint_password_register),
        leadingIcon = Icons.Default.Lock,
        visualTransformation = PasswordVisualTransformation()
    )

    uiState.errorMessage?.let { message ->
        if (uiState.selectedTab == AuthTab.REGISTER) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }

    Spacer(Modifier.height(20.dp))

    PrimaryLoadingButton(
        text = stringResource(R.string.register),
        isLoading = uiState.isLoading,
        onClick = onRegisterClick
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


@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
