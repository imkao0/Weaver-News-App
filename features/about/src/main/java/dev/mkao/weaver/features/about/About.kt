package dev.mkao.weaver.features.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import dev.mkao.weaver.R
import dev.mkao.weaver.util.Dimens

@Composable
fun AboutMe() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = Dimens.PaddingStandard,
                end = Dimens.PaddingStandard,
                top = Dimens.PaddingTripleExtraLarge,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        bottomStart = Dimens.PaddingStandard,
                        bottomEnd = Dimens.PaddingStandard,
                    ),
                )
                .height(Dimens.AboutImageSize),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_splash),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(Dimens.AboutImageSize),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(Dimens.PaddingExtraLarge)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                        ),
                    ),
            )
        }
        Text(
            text = stringResource(R.string.AboutTitle),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = Dimens.PaddingStandard,
                    horizontal = Dimens.PaddingStandard,
                ),
        )
        Spacer(modifier = Modifier.height(Dimens.PaddingMediumSmall))
        Text(
            text = stringResource(R.string.About),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Dimens.PaddingStandard),
        )
    }
}
