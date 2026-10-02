package habitiq.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import habitiq.app.flats.CreateFlatViewModel
import habitiq.app.flats.FlatUiState
import habitiq.app.flats.launchShareInviteCode
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqInlineError
import habitiq.app.ui.figma.CreateFlatBasicsBrilliantScreen
import habitiq.app.ui.figma.CreateFlatBasicsPremiumScreen
import habitiq.app.ui.figma.CreateFlatLocationScreen
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

private enum class CreateFlatWizardStep { BASICS, LOCATION, SUCCESS }

@Composable
fun CreateFlatScreen(
    viewModel: CreateFlatViewModel,
    onDone: (flatId: String) -> Unit,
    onBack: () -> Unit = {},
    brilliantFlow: Boolean = false
) {
    val c = LocalHqColors.current
    val form by viewModel.form.collectAsStateWithLifecycleCompat()
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val createdFlatId by viewModel.createdFlatId.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current
    var step by rememberSaveable { mutableStateOf(CreateFlatWizardStep.BASICS.name) }
    val wizardStep = CreateFlatWizardStep.valueOf(step)

    // Step back through the wizard. Once the flat exists, back finishes the flow rather than
    // returning to a form that could create a second flat.
    BackHandler(enabled = !brilliantFlow && (wizardStep == CreateFlatWizardStep.LOCATION || createdFlatId != null)) {
        val created = createdFlatId
        if (created != null) onDone(created) else step = CreateFlatWizardStep.BASICS.name
    }

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
                    .padding(HqSpacing.xxl)
            ) {
                Text("Your flat is ready", color = c.textPrimary, style = HqType.headlineMedium)
                Text(
                    "Now bring your flatmates in — or skip and set up later.",
                    color = c.textSecondary,
                    style = HqType.bodyMedium,
                    modifier = Modifier.padding(top = HqSpacing.sm, bottom = HqSpacing.lg)
                )
                Text("Invite code: $createdFlatId", color = c.textBrand, style = HqType.titleMedium)
                Spacer(Modifier.height(HqSpacing.xl))
                HqButton(
                    text = "Share invite code",
                    onClick = { launchShareInviteCode(context, form.name, createdFlatId!!) }
                )
                Spacer(Modifier.height(HqSpacing.md))
                HqButton(
                    text = "I'll do this later",
                    variant = HqButtonVariant.Secondary,
                    onClick = { createdFlatId?.let { onDone(it) } }
                )
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
                HqInlineError(
                    (state as FlatUiState.Error).message,
                    modifier = Modifier.padding(HqSpacing.xl)
                )
            }
        }
    }
}
