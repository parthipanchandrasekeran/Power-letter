package com.powerletter.ui.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.powerletter.domain.model.LetterType
import com.powerletter.ui.common.AppStrings
import com.powerletter.ui.components.InputField
import com.powerletter.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterFormScreen(
    letterType: LetterType,
    viewModel: FormViewModel,
    onNavigateBack: () -> Unit,
    onGenerateClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(letterType) {
        viewModel.setLetterType(letterType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.FORM_TITLE,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Letter type indicator
            Text(
                text = letterType.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = AppStrings.FORM_SUBTITLE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            InputField(
                value = state.companyName,
                onValueChange = viewModel::updateCompanyName,
                label = AppStrings.FormFields.COMPANY_LABEL,
                placeholder = AppStrings.FormFields.COMPANY_HELPER,
                error = state.errors.companyName
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                value = state.issueDescription,
                onValueChange = viewModel::updateIssueDescription,
                label = AppStrings.FormFields.ISSUE_LABEL,
                placeholder = AppStrings.FormFields.ISSUE_HELPER,
                error = state.errors.issueDescription,
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                value = state.amount,
                onValueChange = viewModel::updateAmount,
                label = AppStrings.FormFields.AMOUNT_LABEL,
                placeholder = AppStrings.FormFields.AMOUNT_HELPER,
                error = state.errors.amount,
                keyboardType = KeyboardType.Decimal
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                value = state.incidentDate,
                onValueChange = viewModel::updateIncidentDate,
                label = AppStrings.FormFields.DATE_LABEL,
                placeholder = AppStrings.FormFields.DATE_HELPER,
                error = state.errors.incidentDate
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                value = state.referenceNumber,
                onValueChange = viewModel::updateReferenceNumber,
                label = AppStrings.FormFields.REFERENCE_LABEL,
                placeholder = AppStrings.FormFields.REFERENCE_HELPER,
                error = state.errors.referenceNumber
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reassurance text
            Text(
                text = AppStrings.GENERATE_REASSURANCE,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = AppStrings.GENERATE_BUTTON,
                onClick = {
                    if (viewModel.validate()) {
                        onGenerateClick()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
