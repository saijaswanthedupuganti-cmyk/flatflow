package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import habitiq.app.flats.CreateFlatViewModel
import habitiq.app.flats.FlatUiState
import habitiq.app.flats.launchShareInviteCode
import habitiq.app.ui.figma.CreateFlatBasicsBrilliantScreen
import habitiq.app.ui.figma.CreateFlatBasicsPremiumScreen
import habitiq.app.ui.figma.CreateFlatLocationScreen
import habitiq.app.ui.theme.FigmaColors

private enum class CreateFlatWizardStep { BASICS, LOCATION, SUCCESS }

@Composable
fun CreateFlatScreen(
    viewModel: CreateFlatViewModel,
    onDone: (flatId: String) -> Unit,
    onBack: () -> Unit = {},
    brilliantFlow: Boolean = false
) {
    val form by viewModel.form.collectAsStateWithLifecycleCompat()
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val createdFlatId by viewModel.createdFlatId.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current
    var step by rememberSaveable { mutableStateOf(CreateFlatWizardStep.BASICS.name) }
    val wizardStep = CreateFlatWizardStep.valueOf(step)

    LaunchedEffect(createdFlatId) {
        if (createdFlatId != null && wizardStep == CreateFlatWizardStep.LOCATION) {
            step = CreateFlatWizardStep.SUCCESS.name
        }
    }

    if (brilliantFlow) {
        LaunchedEffect(createdFlatId) {
            createdFlatId?.let { onDone(it) }
        }
        CreateFlatBasicsBrilliantScreen(
            flatName = form.name,
            onFlatNameChange = viewModel::updateName,
            city = form.city,
            onCityChange = viewModel::updateCity,
            state = state,
            onBack = onBack,
            onCreateFlat = { viewModel.createFlat() },
            onSkip = onBack
        )
        return
    }

    when {
        createdFlatId != null || wizardStep == CreateFlatWizardStep.SUCCESS -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FigmaColors.Background)
                    .padding(24.dp)
            ) {
                Text("Your flat is ready!", color = FigmaColors.Ink)
                Text("Invite code: $createdFlatId", color = FigmaColors.Primary)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { launchShareInviteCode(context, form.name, createdFlatId!!) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Primary)
                ) { Text("Share invite code") }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { createdFlatId?.let { onDone(it) } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Done") }
            }
        }
        wizardStep == CreateFlatWizardStep.BASICS -> {
            CreateFlatBasicsPremiumScreen(
                flatName = form.name,
                onFlatNameChange = viewModel::updateName,
                flatType = form.flatType,
                onFlatTypeChange = viewModel::updateFlatType,
                onBack = onBack,
                onContinue = { step = CreateFlatWizardStep.LOCATION.name },
                onSkip = { step = CreateFlatWizardStep.LOCATION.name }
            )
        }
        wizardStep == CreateFlatWizardStep.LOCATION -> {
            CreateFlatLocationScreen(
                area = form.area,
                onAreaChange = viewModel::updateArea,
                city = form.city,
                onCityChange = viewModel::updateCity,
                pincode = form.pincode,
                onPincodeChange = viewModel::updatePincode,
                landmark = form.landmark,
                onLandmarkChange = viewModel::updateLandmark,
                onBack = { step = CreateFlatWizardStep.BASICS.name },
                onContinue = { viewModel.createFlat() },
                onSkip = { viewModel.createFlat() },
                showBottomSkip = true,
                showHeaderSkip = true
            )
            if (state is FlatUiState.Error) {
                Text(
                    (state as FlatUiState.Error).message,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}
