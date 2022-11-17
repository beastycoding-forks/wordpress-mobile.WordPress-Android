package org.wordpress.android.ui.main.jetpack.migration.compose.state

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wordpress.android.R
import org.wordpress.android.R.dimen
import org.wordpress.android.ui.compose.theme.AppTheme
import org.wordpress.android.ui.compose.utils.htmlToAnnotatedString
import org.wordpress.android.ui.compose.utils.uiStringText
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.ActionButton.DonePrimaryButton
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.UiState
import org.wordpress.android.ui.main.jetpack.migration.compose.components.PrimaryButton
import org.wordpress.android.ui.main.jetpack.migration.compose.components.ScreenIcon
import org.wordpress.android.ui.main.jetpack.migration.compose.components.Subtitle
import org.wordpress.android.ui.main.jetpack.migration.compose.components.Title

@Composable
fun DoneStep(uiState: UiState.Content.Done) = with(uiState) {
    Column {
        Column(
                modifier = Modifier.padding(horizontal = dimensionResource(dimen.jp_migration_padding_horizontal))
        ) {
            ScreenIcon(iconRes = screenIconRes)
            Title(text = uiStringText(title))
            Subtitle(text = uiStringText(subtitle))
        }
        Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
        ) {
            Image(
                    painter = painterResource(deleteWpIcon),
                    contentDescription = stringResource(R.string.jp_migration_remove_wp_app_icon_content_description),
                    modifier = Modifier
                            .padding(bottom = 10.dp)
                            .size(70.dp)
            )
            Text(
                    text = htmlToAnnotatedString(uiStringText(message)),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = colorResource(R.color.gray_50),
                    modifier = Modifier.fillMaxWidth(0.75f)
            )
        }
        PrimaryButton(
                text = uiStringText(primaryActionButton.text),
                onClick = primaryActionButton.onClick,
                modifier = Modifier.padding(bottom = 50.dp),
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4_XL)
@Preview(showBackground = true, device = Devices.PIXEL_4_XL, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDoneStep() {
    AppTheme {
        val uiState = UiState.Content.Done(DonePrimaryButton {})
        DoneStep(uiState)
    }
}